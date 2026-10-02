package com.daqem.jobsplus.mixin.achievement;

import com.daqem.jobsplus.achievement.ProductionContainer;
import com.daqem.jobsplus.achievement.ProductionTracker;
import com.daqem.jobsplus.achievement.ProductionSnapshots;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BrewingStandBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** 클릭·시프트 클릭·숫자키·드래그로 실제 증가한 투입량에만 작업자를 붙인다. */
@Mixin(AbstractContainerMenu.class)
public abstract class MixinProductionMenu
{
    @Unique private final Deque<List<ProductionSnapshots.Menu>> jobsplus$productionClicks = new ArrayDeque<>();

    @Inject(method = "clicked", at = @At("HEAD"))
    private void jobsplus$captureProduction(int slotId, int button, ContainerInput input, Player player, CallbackInfo ci)
    {
        List<ProductionSnapshots.Menu> snapshots = new ArrayList<>();
        if (player instanceof ServerPlayer)
        {
            Set<Container> captured = new HashSet<>();
            for (Slot slot : ((AbstractContainerMenu) (Object) this).slots)
            {
                Container container = slot.container;
                if (!(container instanceof ProductionContainer production) || !captured.add(container))
                {
                    continue;
                }
                List<ItemStack> before = new ArrayList<>();
                List<ItemStack> references = new ArrayList<>();
                for (int index = 0; index < container.getContainerSize(); index++)
                {
                    ItemStack stack = container.getItem(index);
                    production.jobsplus$getProductionTracker().reconcile(index, stack);
                    before.add(stack.copy());
                    references.add(stack);
                }
                snapshots.add(new ProductionSnapshots.Menu(container, before, references));
            }
        }
        jobsplus$productionClicks.push(snapshots);
    }

    @Inject(method = "clicked", at = @At("RETURN"))
    private void jobsplus$recordProduction(int slotId, int button, ContainerInput input, Player player, CallbackInfo ci)
    {
        List<ProductionSnapshots.Menu> snapshots = jobsplus$productionClicks.pop();
        if (!(player instanceof ServerPlayer serverPlayer))
        {
            return;
        }
        for (ProductionSnapshots.Menu snapshot : snapshots)
        {
            Container container = snapshot.container();
            ProductionTracker tracker = ((ProductionContainer) container).jobsplus$getProductionTracker();
            for (int slot = 0; slot < snapshot.before().size(); slot++)
            {
                boolean tracksInput = false;
                if (container instanceof AbstractFurnaceBlockEntity && slot == 0)
                {
                    tracksInput = true;
                }
                if (container instanceof BrewingStandBlockEntity && slot <= 3)
                {
                    tracksInput = true;
                }
                ItemStack before = snapshot.before().get(slot);
                ItemStack after = container.getItem(slot);
                if (tracksInput && !serverPlayer.isCreative() && !serverPlayer.isSpectator())
                {
                    if (input == ContainerInput.SWAP && snapshot.references().get(slot) != after)
                    {
                        // 동일 아이템/수량의 숫자키 교체도 다른 사람이 넣은 실제 새 스택이다.
                        tracker.replaceManually(slot, after, player.getUUID());
                    }
                    else
                    {
                        tracker.manualChange(slot, before, after, player.getUUID());
                    }
                }
                else
                {
                    tracker.reconcile(slot, after);
                }
            }
            if (tracker.consumeDirty() && container instanceof BlockEntity blockEntity)
            {
                blockEntity.setChanged();
            }
        }
    }

}
