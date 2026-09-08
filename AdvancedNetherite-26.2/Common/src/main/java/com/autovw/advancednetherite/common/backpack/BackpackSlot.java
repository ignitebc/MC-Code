package com.autovw.advancednetherite.common.backpack;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public final class BackpackSlot extends Slot
{
    private final BackpackInventory backpack;
    private final boolean equipment;

    public BackpackSlot(Inventory inventory, int slot, int x, int y)
    {
        super(inventory, slot, x, y);
        this.backpack = BackpackInventory.get(inventory);
        this.equipment = slot == BackpackInventory.EQUIPMENT_SLOT;
    }

    @Override
    public boolean mayPlace(ItemStack stack)
    {
        return this.backpack.canPlace(getContainerSlot(), stack);
    }

    @Override
    public boolean mayPickup(Player player)
    {
        // 숫자키 교환·드래그·Shift 클릭도 같은 서버 측 해제 규칙을 적용한다.
        return !this.equipment || this.backpack.canEquip(ItemStack.EMPTY);
    }

    @Override
    public int getMaxStackSize()
    {
        return this.equipment ? 1 : super.getMaxStackSize();
    }

    @Override
    public boolean isActive()
    {
        // 명령어나 사망 규칙으로 가방만 제거되어도 남은 물건을 꺼낼 수 있다.
        return this.equipment || getContainerSlot() < BackpackInventory.STORAGE_START + this.backpack.capacity() || hasItem();
    }
}
