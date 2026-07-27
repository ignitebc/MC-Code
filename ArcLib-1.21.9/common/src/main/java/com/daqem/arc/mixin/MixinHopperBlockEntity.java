package com.daqem.arc.mixin;

import com.daqem.arc.api.block.ArcHopperFedContainer;
import net.minecraft.core.Direction;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayDeque;
import java.util.Deque;

@Mixin(HopperBlockEntity.class)
public class MixinHopperBlockEntity
{

    @Unique
    private static final ThreadLocal<Deque<Integer>> arc$incomingStackCounts =
            ThreadLocal.withInitial(ArrayDeque::new);

    /**
     * 호퍼가 보관함의 개별 슬롯에 아이템을 넣을 때 지나가는 경로다.
     * 실제로 이동한 수량만 기록해 자동 투입된 처리 재료가 모두 소진될 때까지 보상에서 제외한다.
     */
    @Inject(
            at = @At("HEAD"),
            method = "tryMoveInItem(Lnet/minecraft/world/Container;Lnet/minecraft/world/Container;Lnet/minecraft/world/item/ItemStack;ILnet/minecraft/core/Direction;)Lnet/minecraft/world/item/ItemStack;"
    )
    private static void arc$captureIncomingStackCount(
            @Nullable Container source,
            Container destination,
            ItemStack stack,
            int slot,
            @Nullable Direction direction,
            CallbackInfoReturnable<ItemStack> cir)
    {
        arc$incomingStackCounts.get().push(stack.getCount());
    }

    @Inject(
            at = @At("RETURN"),
            method = "tryMoveInItem(Lnet/minecraft/world/Container;Lnet/minecraft/world/Container;Lnet/minecraft/world/item/ItemStack;ILnet/minecraft/core/Direction;)Lnet/minecraft/world/item/ItemStack;"
    )
    private static void arc$recordHopperInsertion(
            @Nullable Container source,
            Container destination,
            ItemStack stack,
            int slot,
            @Nullable Direction direction,
            CallbackInfoReturnable<ItemStack> cir)
    {
        Deque<Integer> incomingStackCounts = arc$incomingStackCounts.get();
        int incomingCount = incomingStackCounts.pop();
        if (incomingStackCounts.isEmpty())
        {
            arc$incomingStackCounts.remove();
        }

        int insertedCount = incomingCount - cir.getReturnValue().getCount();
        if (insertedCount > 0 && destination instanceof ArcHopperFedContainer hopperFedContainer)
        {
            hopperFedContainer.arc$recordHopperInsertion(slot, insertedCount);
        }
    }
}
