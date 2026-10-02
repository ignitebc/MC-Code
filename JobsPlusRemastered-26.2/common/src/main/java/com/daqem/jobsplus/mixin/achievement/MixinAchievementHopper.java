package com.daqem.jobsplus.mixin.achievement;

import com.daqem.jobsplus.achievement.ProductionContainer;
import com.daqem.jobsplus.achievement.ProductionTracker;
import net.minecraft.core.Direction;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.Hopper;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayDeque;
import java.util.Deque;

/** 호퍼가 같은 종류/수량으로 병을 교체해도 수동 투입 소유권이 남지 않도록 실제 전송을 추적한다. */
@Mixin(HopperBlockEntity.class)
public abstract class MixinAchievementHopper
{
    @Unique private static final ThreadLocal<Deque<Integer>> jobsplus$incoming = ThreadLocal.withInitial(ArrayDeque::new);

    @Inject(method = "tryMoveInItem", at = @At("HEAD"))
    private static void jobsplus$beforeInsertion(@Nullable Container source, Container destination, ItemStack stack,
                                                 int slot, @Nullable Direction direction, CallbackInfoReturnable<ItemStack> cir)
    {
        jobsplus$incoming.get().push(stack.getCount());
        if (destination instanceof ProductionContainer production)
        {
            production.jobsplus$getProductionTracker().reconcile(slot, destination.getItem(slot));
        }
    }

    @Inject(method = "tryMoveInItem", at = @At("RETURN"))
    private static void jobsplus$afterInsertion(@Nullable Container source, Container destination, ItemStack stack,
                                                int slot, @Nullable Direction direction, CallbackInfoReturnable<ItemStack> cir)
    {
        Deque<Integer> counts = jobsplus$incoming.get();
        int incoming = counts.pop();
        if (counts.isEmpty())
        {
            jobsplus$incoming.remove();
        }
        if (destination instanceof ProductionContainer production)
        {
            ProductionTracker tracker = production.jobsplus$getProductionTracker();
            int inserted = incoming - cir.getReturnValue().getCount();
            tracker.append(slot, destination.getItem(slot), "", inserted);
            tracker.reconcile(slot, destination.getItem(slot));
            if (tracker.consumeDirty())
            {
                destination.setChanged();
            }
        }
    }

    @Inject(method = "tryTakeInItemFromSlot", at = @At("HEAD"))
    private static void jobsplus$beforeExtraction(Hopper hopper, Container source, int slot, Direction direction,
                                                  CallbackInfoReturnable<Boolean> cir)
    {
        if (source instanceof ProductionContainer production)
        {
            production.jobsplus$getProductionTracker().reconcile(slot, source.getItem(slot));
        }
    }

    @Inject(method = "tryTakeInItemFromSlot", at = @At("RETURN"))
    private static void jobsplus$afterExtraction(Hopper hopper, Container source, int slot, Direction direction,
                                                 CallbackInfoReturnable<Boolean> cir)
    {
        if (source instanceof ProductionContainer production)
        {
            ProductionTracker tracker = production.jobsplus$getProductionTracker();
            if (cir.getReturnValueZ())
            {
                // 바닐라의 성공한 pull은 정확히 1개를 이동한다. 실패했다면 원래 스택으로 복원된다.
                tracker.take(slot, 1, "");
            }
            tracker.reconcile(slot, source.getItem(slot));
            if (tracker.consumeDirty())
            {
                source.setChanged();
            }
        }
    }
}
