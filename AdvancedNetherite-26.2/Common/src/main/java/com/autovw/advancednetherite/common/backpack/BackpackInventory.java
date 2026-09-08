package com.autovw.advancednetherite.common.backpack;

import com.autovw.advancednetherite.common.item.BackpackItem;
import net.minecraft.core.NonNullList;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

/**
 * 바닐라의 0~35 일반 슬롯과 36~42 장비 인덱스를 보존한다.
 * 가방과 추가 칸은 플레이어 Inventory의 저장·동기화 경로에 함께 참여한다.
 */
public final class BackpackInventory
{
    public static final int MAX_CAPACITY = 12;
    public static final int EQUIPMENT_SLOT = Inventory.INVENTORY_SIZE + Inventory.EQUIPMENT_SLOT_MAPPING.size();
    public static final int STORAGE_START = EQUIPMENT_SLOT + 1;
    public static final int SLOT_COUNT = MAX_CAPACITY + 1;
    public static final int MENU_EQUIPMENT_SLOT = 46;
    public static final int PANEL_LEFT = 176;
    public static final int PANEL_WIDTH = 84;
    public static final int EQUIPMENT_X = 210;
    public static final int EQUIPMENT_Y = 24;
    public static final int STORAGE_X = 184;
    public static final int STORAGE_Y = 84;

    private final NonNullList<ItemStack> items = NonNullList.withSize(SLOT_COUNT, ItemStack.EMPTY);

    public static BackpackInventory get(Inventory inventory)
    {
        return ((BackpackInventoryAccess) inventory).advancednetherite$getBackpackInventory();
    }

    public static boolean isBackpackSlot(int slot)
    {
        return slot >= EQUIPMENT_SLOT && slot < EQUIPMENT_SLOT + SLOT_COUNT;
    }

    public static int capacityOf(ItemStack stack)
    {
        return stack.getItem() instanceof BackpackItem backpack ? backpack.getCapacity() : 0;
    }

    public int capacity()
    {
        return capacityOf(this.items.getFirst());
    }

    public boolean canEquip(ItemStack replacement)
    {
        if (!replacement.isEmpty() && (!(replacement.getItem() instanceof BackpackItem) || replacement.getCount() != 1))
        {
            return false;
        }
        int newCapacity = capacityOf(replacement);
        for (int i = newCapacity; i < MAX_CAPACITY; i++)
        {
            if (!this.items.get(i + 1).isEmpty())
            {
                return false;
            }
        }
        return true;
    }

    public boolean canPlace(int slot, ItemStack stack)
    {
        if (slot == EQUIPMENT_SLOT)
        {
            return !stack.isEmpty() && canEquip(stack);
        }
        return slot >= STORAGE_START && slot < STORAGE_START + capacity();
    }

    public ItemStack getItem(int slot)
    {
        return this.items.get(slot - EQUIPMENT_SLOT);
    }

    // 저장 복원·사망·서버 패킷은 슬롯 사용 권한과 분리한다. 강제 제거 후에도 내용물을 복구할 수 있어야 한다.
    public void setItem(int slot, ItemStack stack)
    {
        this.items.set(slot - EQUIPMENT_SLOT, stack);
    }

    public ItemStack removeItem(int slot, int count)
    {
        return ContainerHelper.removeItem(this.items, slot - EQUIPMENT_SLOT, count);
    }

    public ItemStack removeItemNoUpdate(int slot)
    {
        return ContainerHelper.takeItem(this.items, slot - EQUIPMENT_SLOT);
    }

    public boolean owns(ItemStack stack)
    {
        if (stack.isEmpty())
        {
            return false;
        }
        return this.items.stream().anyMatch(item -> item == stack);
    }

    public boolean isEmpty()
    {
        return this.items.stream().allMatch(ItemStack::isEmpty);
    }

    public void clear()
    {
        this.items.clear();
    }

    public boolean insert(ItemStack stack)
    {
        if (stack.isEmpty() || owns(stack))
        {
            return false;
        }
        int originalCount = stack.getCount();
        // 기존 스택을 먼저 채워 불필요하게 빈 칸을 소비하지 않는다.
        for (int pass = 0; pass < 2 && !stack.isEmpty(); pass++)
        {
            for (int i = 0; i < capacity() && !stack.isEmpty(); i++)
            {
                ItemStack target = this.items.get(i + 1);
                if ((pass == 0 && target.isEmpty()) || (pass == 1 && !target.isEmpty()))
                {
                    continue;
                }
                insertAt(STORAGE_START + i, stack);
            }
        }
        return stack.getCount() < originalCount;
    }

    public boolean insertAt(int slot, ItemStack stack)
    {
        if (stack.isEmpty() || !canPlace(slot, stack))
        {
            return false;
        }
        ItemStack target = getItem(slot);
        if (target == stack || (!target.isEmpty() && !ItemStack.isSameItemSameComponents(target, stack)))
        {
            return false;
        }
        int limit = slot == EQUIPMENT_SLOT ? 1 : stack.getMaxStackSize();
        int transferred = Math.min(stack.getCount(), limit - target.getCount());
        if (transferred <= 0)
        {
            return false;
        }
        if (target.isEmpty())
        {
            setItem(slot, stack.split(transferred));
        }
        else
        {
            target.grow(transferred);
            stack.shrink(transferred);
        }
        getItem(slot).setPopTime(5);
        return true;
    }
}
