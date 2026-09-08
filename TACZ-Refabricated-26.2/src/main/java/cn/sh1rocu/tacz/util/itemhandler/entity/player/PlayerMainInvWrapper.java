package cn.sh1rocu.tacz.util.itemhandler.entity.player;

import cn.sh1rocu.tacz.util.itemhandler.InvWrapper;
import cn.sh1rocu.tacz.util.itemhandler.RangedWrapper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

public class PlayerMainInvWrapper extends RangedWrapper {
    private final Inventory inventoryPlayer;

    public PlayerMainInvWrapper(Inventory inv) {
        super(new MainInventoryView(inv), 0, MainInventoryView.sizeOf(inv));
        inventoryPlayer = inv;
    }

    @Override
    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
        ItemStack rest = super.insertItem(slot, stack, simulate);
        if (rest.getCount() != stack.getCount()) {
            // the stack in the slot changed, animate it
            ItemStack inSlot = getStackInSlot(slot);
            if (!inSlot.isEmpty()) {
                if (getInventoryPlayer().player.level().isClientSide()) {
                    inSlot.setPopTime(5);
                } else if (getInventoryPlayer().player instanceof ServerPlayer) {
                    getInventoryPlayer().player.containerMenu.broadcastChanges();
                }
            }
        }
        return rest;
    }

    public Inventory getInventoryPlayer() {
        return inventoryPlayer;
    }

    /** 일반 슬롯 뒤에 추가된 모드 슬롯을 포함하되 바닐라 장비를 중복 계산하지 않는다. */
    private static final class MainInventoryView extends InvWrapper {
        private static final int EQUIPMENT_COUNT = Inventory.EQUIPMENT_SLOT_MAPPING.size();

        private MainInventoryView(Inventory inventory) {
            super(inventory);
        }

        private static int sizeOf(Inventory inventory) {
            return inventory.getContainerSize() - EQUIPMENT_COUNT;
        }

        @Override
        public int getSlots() {
            return getInv().getContainerSize() - EQUIPMENT_COUNT;
        }

        private boolean isValidSlot(int slot) {
            return slot >= 0 && slot < getSlots();
        }

        private int inventorySlot(int slot) {
            return slot < Inventory.INVENTORY_SIZE ? slot : slot + EQUIPMENT_COUNT;
        }

        @Override
        public ItemStack getStackInSlot(int slot) {
            return isValidSlot(slot) ? super.getStackInSlot(inventorySlot(slot)) : ItemStack.EMPTY;
        }

        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            return isValidSlot(slot) ? super.insertItem(inventorySlot(slot), stack, simulate) : stack;
        }

        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            return isValidSlot(slot) ? super.extractItem(inventorySlot(slot), amount, simulate) : ItemStack.EMPTY;
        }

        @Override
        public void setStackInSlot(int slot, ItemStack stack) {
            if (isValidSlot(slot)) super.setStackInSlot(inventorySlot(slot), stack);
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return isValidSlot(slot) && super.isItemValid(inventorySlot(slot), stack);
        }
    }
}
