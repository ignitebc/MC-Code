package com.autovw.advancednetherite.mixin;

import com.autovw.advancednetherite.common.backpack.BackpackInventory;
import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractContainerMenu.class)
public abstract class BackpackTransferMixin
{
    @Shadow @Final public NonNullList<Slot> slots;

    @Inject(method = "moveItemStackTo", at = @At("RETURN"), cancellable = true)
    private void advancednetherite$overflow(ItemStack stack, int start, int end, boolean reverse,
                                           CallbackInfoReturnable<Boolean> cir)
    {
        if (stack.isEmpty() || start < 0 || start >= end || end > this.slots.size()) return;
        if (!(this.slots.get(start).container instanceof Inventory inventory)) return;
        // 상자·제작 결과에서 플레이어 일반 인벤토리로 옮기는 경우에만 확장 칸을 사용한다.
        for (int i = start; i < end; i++)
        {
            Slot target = this.slots.get(i);
            if (target.container != inventory || target.getContainerSlot() >= Inventory.INVENTORY_SIZE) return;
        }
        if (BackpackInventory.get(inventory).insert(stack))
        {
            inventory.setChanged();
            cir.setReturnValue(true);
        }
    }
}
