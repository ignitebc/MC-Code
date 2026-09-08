package com.autovw.advancednetherite.common.backpack;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public final class BackpackSlot extends Slot
{
    private final BackpackInventory backpack;
    private final Player owner;
    private final boolean equipment;

    public BackpackSlot(Inventory inventory, int slot, int x, int y)
    {
        super(inventory, slot, x, y);
        this.backpack = BackpackInventory.get(inventory);
        this.owner = inventory.player;
        this.equipment = slot == BackpackInventory.EQUIPMENT_SLOT;
    }

    @Override
    public boolean mayPlace(ItemStack stack)
    {
        return this.backpack.canPlace(getContainerSlot(), stack);
    }

    @Override
    public void setByPlayer(ItemStack stack, ItemStack previous)
    {
        super.setByPlayer(stack, previous);
        if (this.equipment)
        {
            // 클릭 교체·숫자키 교환·Shift 해제·버리기는 최종 장착 상태를 기준으로 정리한다.
            this.backpack.dropOverflow(this.owner);
            setChanged();
        }
    }

    @Override
    public int getMaxStackSize()
    {
        return this.equipment ? 1 : super.getMaxStackSize();
    }

    @Override
    public boolean isActive()
    {
        // 명령어나 이전 저장 데이터에 남은 비활성 칸의 물건은 복구할 수 있다.
        return this.equipment || getContainerSlot() < BackpackInventory.STORAGE_START + this.backpack.capacity() || hasItem();
    }
}
