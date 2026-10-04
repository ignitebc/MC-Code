package com.daqem.jobsplus.mixin.achievement;

import com.daqem.jobsplus.achievement.AchievementManager;
import com.daqem.jobsplus.achievement.ProductionContainer;
import com.daqem.jobsplus.achievement.ProductionTracker;
import com.daqem.jobsplus.achievement.ProductionSnapshots;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BrewingStandBlockEntity;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.UUID;

@Mixin(BrewingStandBlockEntity.class)
public abstract class MixinAchievementBrewingStand implements ProductionContainer
{
    @Unique private ProductionTracker jobsplus$production;
    @Unique private static final ThreadLocal<Deque<ProductionSnapshots.Brew>> jobsplus$brews = ThreadLocal.withInitial(ArrayDeque::new);

    @Override
    public ProductionTracker jobsplus$getProductionTracker()
    {
        // 생성자 필드 초기화에 의존하지 않고 첫 사용 시 추적기를 보장한다.
        if (jobsplus$production == null)
        {
            jobsplus$production = new ProductionTracker();
        }
        return jobsplus$production;
    }

    @Inject(method = "doBrew", at = @At("HEAD"))
    private static void jobsplus$beforeBrew(Level level, BlockPos pos, NonNullList<ItemStack> items, CallbackInfo ci)
    {
        BrewingStandBlockEntity stand = (BrewingStandBlockEntity) level.getBlockEntity(pos);
        ProductionTracker tracker = ((ProductionContainer) stand).jobsplus$getProductionTracker();
        List<ItemStack> bottles = new ArrayList<>();
        for (int slot = 0; slot < 4; slot++)
        {
            tracker.reconcile(slot, items.get(slot));
            if (slot < 3)
            {
                bottles.add(items.get(slot).copy());
            }
        }
        jobsplus$brews.get().push(new ProductionSnapshots.Brew(stand, bottles, items.get(3).getCount()));
    }

    @Inject(method = "doBrew", at = @At("RETURN"))
    private static void jobsplus$afterBrew(Level level, BlockPos pos, NonNullList<ItemStack> items, CallbackInfo ci)
    {
        Deque<ProductionSnapshots.Brew> snapshots = jobsplus$brews.get();
        ProductionSnapshots.Brew before = snapshots.pop();
        if (snapshots.isEmpty())
        {
            jobsplus$brews.remove();
        }
        ProductionTracker tracker = ((ProductionContainer) before.stand()).jobsplus$getProductionTracker();
        String ingredientOwner = tracker.firstOwner(3);
        boolean consumedIngredient = before.ingredientCount() > items.get(3).getCount();
        if (consumedIngredient)
        {
            tracker.take(3, before.ingredientCount() - items.get(3).getCount(), "");
        }
        for (int slot = 0; slot < 3; slot++)
        {
            String owner = tracker.firstOwner(slot);
            ItemStack original = before.bottles().get(slot);
            ItemStack result = items.get(slot);
            if (consumedIngredient && !owner.isEmpty() && owner.equals(ingredientOwner)
                    && !original.isEmpty() && !hasEffects(original) && hasEffects(result))
            {
                UUID playerId = UUID.fromString(owner);
                ServerPlayer online = level.getServer().getPlayerList().getPlayer(playerId);
                if (online == null || AchievementManager.isEligible(online))
                {
                    AchievementManager.add(level.getServer(), playerId, "potions", result.getCount());
                }
            }
            tracker.transform(slot, result);
        }
        tracker.reconcile(3, items.get(3));
        if (tracker.consumeDirty())
        {
            before.stand().setChanged();
        }
    }

    @Unique
    private static boolean hasEffects(ItemStack stack)
    {
        var contents = stack.get(DataComponents.POTION_CONTENTS);
        if (contents == null)
        {
            return false;
        }
        return contents.getAllEffects().iterator().hasNext();
    }

    @Inject(method = "loadAdditional", at = @At("TAIL"))
    private void jobsplus$loadProduction(ValueInput input, CallbackInfo ci)
    {
        jobsplus$production = input.read("JobsPlusProduction", ProductionTracker.CODEC).orElseGet(ProductionTracker::new);
    }

    @Inject(method = "saveAdditional", at = @At("TAIL"))
    private void jobsplus$saveProduction(ValueOutput output, CallbackInfo ci)
    {
        output.store("JobsPlusProduction", ProductionTracker.CODEC, jobsplus$getProductionTracker());
    }

}
