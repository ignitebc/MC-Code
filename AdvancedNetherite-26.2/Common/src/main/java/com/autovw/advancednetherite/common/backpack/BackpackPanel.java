package com.autovw.advancednetherite.common.backpack;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;

import java.util.ArrayList;
import java.util.List;

/**
 * 메뉴 오른쪽에 붙는 가방 패널(장착 칸 1개 + 추가 칸 12개)의 배치 기준.
 *
 * <p>패널 칸은 항상 메뉴 슬롯 목록의 맨 끝에 한 덩어리로 붙인다. 바닐라 슬롯 번호를 밀지 않아야
 * 각 메뉴의 Shift 이동과 다른 모드의 슬롯 번호 판정이 그대로 동작한다.</p>
 */
public final class BackpackPanel
{
    /** 플레이어 인벤토리 화면(InventoryMenu)에서 일반 인벤토리 첫 줄의 y. 패널 배치의 기준선이다. */
    public static final int ANCHOR_Y = 84;
    public static final int PANEL_HEIGHT = 166;

    private BackpackPanel()
    {
    }

    /**
     * @param offsetY 메뉴의 플레이어 인벤토리 첫 줄이 {@link #ANCHOR_Y}에서 벗어난 만큼
     */
    public static List<Slot> createSlots(Inventory inventory, int offsetY)
    {
        List<Slot> slots = new ArrayList<>(BackpackInventory.SLOT_COUNT);
        slots.add(new BackpackSlot(inventory, BackpackInventory.EQUIPMENT_SLOT,
                BackpackInventory.EQUIPMENT_X, BackpackInventory.EQUIPMENT_Y + offsetY));
        for (int i = 0; i < BackpackInventory.MAX_CAPACITY; i++)
        {
            int column = i % BackpackInventory.STORAGE_COLUMNS;
            int row = i / BackpackInventory.STORAGE_COLUMNS;
            int x = BackpackInventory.STORAGE_X + column * BackpackInventory.SLOT_SIZE;
            int y = BackpackInventory.STORAGE_Y + offsetY + row * BackpackInventory.SLOT_SIZE;
            slots.add(new BackpackSlot(inventory, BackpackInventory.STORAGE_START + i, x, y));
        }
        return slots;
    }

    public static boolean hasPanel(AbstractContainerMenu menu)
    {
        int size = menu.slots.size();
        if (size < BackpackInventory.SLOT_COUNT)
        {
            return false;
        }
        return menu.slots.get(size - 1) instanceof BackpackSlot;
    }

    /** 패널의 첫 칸(장착 칸) 메뉴 번호. {@link #hasPanel}이 true일 때만 의미가 있다. */
    public static int firstSlotIndex(AbstractContainerMenu menu)
    {
        return menu.slots.size() - BackpackInventory.SLOT_COUNT;
    }

    /** 패널이 E키 화면 배치보다 위아래로 옮겨진 거리. {@link #hasPanel}이 true일 때만 의미가 있다. */
    public static int offsetY(AbstractContainerMenu menu)
    {
        Slot equipment = menu.slots.get(firstSlotIndex(menu));
        return equipment.y - BackpackInventory.EQUIPMENT_Y;
    }

    /** 화면 왼쪽 위(leftPos, topPos) 기준 좌표가 패널 영역 안인지 판정한다. */
    public static boolean contains(AbstractContainerMenu menu, double x, double y)
    {
        int top = offsetY(menu);
        boolean insideX = x >= BackpackInventory.PANEL_LEFT
                && x < BackpackInventory.PANEL_LEFT + BackpackInventory.PANEL_WIDTH;
        boolean insideY = y >= top && y < top + PANEL_HEIGHT;
        return insideX && insideY;
    }
}
