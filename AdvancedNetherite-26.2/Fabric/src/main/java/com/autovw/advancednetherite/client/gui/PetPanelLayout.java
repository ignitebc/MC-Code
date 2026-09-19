package com.autovw.advancednetherite.client.gui;

import com.autovw.advancednetherite.client.ClientPetData;
import com.autovw.advancednetherite.common.backpack.BackpackInventory;

/**
 * 인벤토리 화면 아래에 붙는 펫 ON/OFF 줄의 자리 계산.
 *
 * <p>줄의 높이만큼 화면의 imageHeight를 늘려 두면 바닐라가 인벤토리를 그만큼 위로 올려
 * 가운데를 다시 맞춘다. 그래서 펫이 몇 마리든 줄이 화면 밖으로 나가지 않는다.
 * 펫이 없으면 늘릴 높이가 0이라 화면은 바닐라와 똑같다.
 *
 * <p>좌표는 모두 화면 왼쪽 위(leftPos, topPos) 기준의 상대값이다.
 */
public final class PetPanelLayout
{
    /** 바닐라 인벤토리 본체의 높이 */
    public static final int INVENTORY_HEIGHT = 166;
    /** 인벤토리 바닥과 펫 줄 사이의 간격 */
    public static final int TOP_GAP = 4;
    /** 펫 줄 테두리와 단추 사이의 여백 */
    public static final int PADDING = 5;
    public static final int BUTTON_WIDTH = 56;
    public static final int BUTTON_HEIGHT = 16;
    public static final int BUTTON_GAP = 2;

    private PetPanelLayout() { }

    /** 펫 줄의 너비. 인벤토리와 가방 패널을 합친 폭에 맞춘다. */
    public static int width()
    {
        return BackpackInventory.PANEL_LEFT + BackpackInventory.PANEL_WIDTH;
    }

    /** 한 줄에 들어가는 단추 수 */
    public static int columns()
    {
        int inner = width() - PADDING * 2;
        return Math.max(1, (inner + BUTTON_GAP) / (BUTTON_WIDTH + BUTTON_GAP));
    }

    public static int rows()
    {
        int count = ClientPetData.getPets().size();
        return count <= 0 ? 0 : (count + columns() - 1) / columns();
    }

    /**
     * 인벤토리 아래에 더 필요한 높이.
     *
     * <p>항상 짝수라서 imageHeight에 더해도 바닐라의 가운데 맞춤이 1px 어긋나지 않는다.
     */
    public static int extraHeight()
    {
        int rows = rows();
        return rows <= 0 ? 0 : TOP_GAP + PADDING * 2 + rows * BUTTON_HEIGHT + (rows - 1) * BUTTON_GAP;
    }

    public static int panelTop()
    {
        return INVENTORY_HEIGHT + TOP_GAP;
    }

    public static int panelHeight()
    {
        int extra = extraHeight();
        return extra == 0 ? 0 : extra - TOP_GAP;
    }

    public static int buttonX(int index)
    {
        int columns = columns();
        int rowWidth = columns * BUTTON_WIDTH + (columns - 1) * BUTTON_GAP;
        int gridLeft = (width() - rowWidth) / 2;
        return gridLeft + (index % columns) * (BUTTON_WIDTH + BUTTON_GAP);
    }

    public static int buttonY(int index)
    {
        return panelTop() + PADDING + (index / columns()) * (BUTTON_HEIGHT + BUTTON_GAP);
    }
}
