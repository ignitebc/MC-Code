package com.daqem.jobsplus.mixin.achievement;

import com.daqem.jobsplus.achievement.ProductionContainer;
import com.daqem.jobsplus.achievement.ProductionTracker;
import com.daqem.jobsplus.achievement.ProductionSnapshots;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayDeque;
import java.util.Deque;

/** 화로·용광로·훈연기 공통. 실제 조리가 완료된 수량에 투입 작업자의 소유권을 계승한다. */
@Mixin(AbstractFurnaceBlockEntity.class)
public abstract class MixinAchievementFurnace implements ProductionContainer
{
    @Unique private ProductionTracker jobsplus$production;
    @Unique private static final ThreadLocal<Deque<ProductionSnapshots.Furnace>> jobsplus$cooks = ThreadLocal.withInitial(ArrayDeque::new);

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

    @Inject(method = "serverTick", at = @At("HEAD"))
    private static void jobsplus$beforeCook(ServerLevel level, BlockPos pos, BlockState state,
                                            AbstractFurnaceBlockEntity furnace, CallbackInfo ci)
    {
        ProductionTracker tracker = ((ProductionContainer) furnace).jobsplus$getProductionTracker();
        tracker.reconcile(0, furnace.getItem(0));
        tracker.reconcile(2, furnace.getItem(2));
        jobsplus$cooks.get().push(new ProductionSnapshots.Furnace(furnace.getItem(0).getCount(), furnace.getItem(2).copy()));
    }

    @Inject(method = "serverTick", at = @At("RETURN"))
    private static void jobsplus$afterCook(ServerLevel level, BlockPos pos, BlockState state,
                                           AbstractFurnaceBlockEntity furnace, CallbackInfo ci)
    {
        Deque<ProductionSnapshots.Furnace> snapshots = jobsplus$cooks.get();
        ProductionSnapshots.Furnace before = snapshots.pop();
        if (snapshots.isEmpty())
        {
            jobsplus$cooks.remove();
        }
        ProductionTracker tracker = ((ProductionContainer) furnace).jobsplus$getProductionTracker();
        ItemStack output = furnace.getItem(2);
        int produced = output.getCount() - before.previousOutput().getCount();
        int consumed = before.inputCount() - furnace.getItem(0).getCount();
        if (consumed == 1 && produced > 0)
        {
            String owner = tracker.firstOwner(0);
            tracker.take(0, 1, "");
            tracker.append(2, output, owner, produced);
        }
        tracker.reconcile(0, furnace.getItem(0));
        tracker.reconcile(2, output);
        if (tracker.consumeDirty())
        {
            furnace.setChanged();
        }
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
