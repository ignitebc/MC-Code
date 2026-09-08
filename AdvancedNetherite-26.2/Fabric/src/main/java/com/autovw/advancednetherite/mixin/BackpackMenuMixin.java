package com.autovw.advancednetherite.mixin;

import com.autovw.advancednetherite.common.backpack.BackpackInventory;
import com.autovw.advancednetherite.common.backpack.BackpackSlot;
import com.autovw.advancednetherite.common.item.BackpackItem;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(InventoryMenu.class)
public abstract class BackpackMenuMixin extends AbstractContainerMenu
{
    protected BackpackMenuMixin(MenuType<?> type, int containerId)
    {
        super(type, containerId);
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void advancednetherite$slots(Inventory inventory, boolean active, Player owner, CallbackInfo ci)
    {
        addSlot(new BackpackSlot(inventory, BackpackInventory.EQUIPMENT_SLOT,
                BackpackInventory.EQUIPMENT_X, BackpackInventory.EQUIPMENT_Y));
        for (int i = 0; i < BackpackInventory.MAX_CAPACITY; i++)
        {
            addSlot(new BackpackSlot(inventory, BackpackInventory.STORAGE_START + i,
                    BackpackInventory.STORAGE_X + (i % 4) * 18,
                    BackpackInventory.STORAGE_Y + (i / 4) * 18));
        }
    }

    @Inject(method = "quickMoveStack", at = @At("HEAD"), cancellable = true)
    private void advancednetherite$quickMove(Player player, int slotIndex, CallbackInfoReturnable<ItemStack> cir)
    {
        if (slotIndex < 0 || slotIndex >= this.slots.size())
        {
            cir.setReturnValue(ItemStack.EMPTY);
            return;
        }
        Slot source = this.slots.get(slotIndex);
        ItemStack stack = source.getItem();
        boolean extraSlot = source instanceof BackpackSlot;
        boolean equipBackpack = slotIndex >= 9 && stack.getItem() instanceof BackpackItem
                && !this.slots.get(BackpackInventory.MENU_EQUIPMENT_SLOT).hasItem();
        if (!extraSlot && !equipBackpack) return;
        if (stack.isEmpty() || !source.mayPickup(player))
        {
            cir.setReturnValue(ItemStack.EMPTY);
            return;
        }
        ItemStack original = stack.copy();
        boolean moved;
        if (equipBackpack)
        {
            moved = moveItemStackTo(stack, BackpackInventory.MENU_EQUIPMENT_SLOT,
                    BackpackInventory.MENU_EQUIPMENT_SLOT + 1, false);
        }
        else
        {
            moved = moveItemStackTo(stack, 9, 45, false);
        }
        if (!moved)
        {
            cir.setReturnValue(ItemStack.EMPTY);
            return;
        }
        if (stack.isEmpty()) source.setByPlayer(ItemStack.EMPTY, original);
        else source.setChanged();
        source.onTake(player, stack);
        cir.setReturnValue(original);
    }
}
