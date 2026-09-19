package com.autovw.advancednetherite.client.gui;

import com.autovw.advancednetherite.client.ClientPetData;
import com.autovw.advancednetherite.common.backpack.BackpackInventory;

/**
 * 인벤토리 화면 아래에 붙는 펫 ON/OFF 줄의 자리 계산.
 *
 * <p>줄의 높이만큼 화면의 imageHeight를 늘려 두면 바닐라가 인벤토리를 그만큼 위로 올려
 * 가운데를 다시 맞춘다. 펫이 없으면 늘릴 높이가 0이라 화면은 바닐라와 똑같다.
 *
 * <p>펫이 아무리 많아도 판은 두 줄까지만 커지고 나머지는 굴려서 본다. 그래야 화면이 낮아도
 * 인벤토리가 위아래로 잘리지 않는다.
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
    /** 한 번에 보여 주는 줄 수. 이보다 많으면 굴려서 본다. */
    public static final int VISIBLE_ROWS = 2;

    private PetPanelLayout() { }

    public static int petCount()
    {
        return ClientPetData.getPets().size();
    }

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

    /** 펫을 모두 늘어놓는 데 필요한 줄 수 */
    public static int totalRows()
    {
        int count = petCount();
        return count <= 0 ? 0 : (count + columns() - 1) / columns();
    }

    /** 실제로 판에 그려지는 줄 수 */
    public static int visibleRows()
    {
        return Math.min(VISIBLE_ROWS, totalRows());
    }

    /** 굴릴 수 있는 최대 줄 수. 0이면 스크롤이 필요 없다. */
    public static int maxScrollRow()
    {
        return Math.max(0, totalRows() - VISIBLE_ROWS);
    }

    /**
     * 인벤토리 아래에 더 필요한 높이.
     *
     * <p>항상 짝수라서 imageHeight에 더해도 바닐라의 가운데 맞춤이 1px 어긋나지 않는다.
     */
    public static int extraHeight()
    {
        int rows = visibleRows();
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

    /** 단추 격자의 왼쪽 끝. 남는 폭을 좌우로 나눠 가운데에 둔다. */
    public static int gridLeft()
    {
        int columns = columns();
        int rowWidth = columns * BUTTON_WIDTH + (columns - 1) * BUTTON_GAP;
        return (width() - rowWidth) / 2;
    }

    /** 판 안에서의 단추 x. index는 전체 목록 기준이다. */
    public static int buttonX(int index)
    {
        return gridLeft() + (index % columns()) * (BUTTON_WIDTH + BUTTON_GAP);
    }

    /**
     * 판 안에서의 단추 y.
     *
     * @param scrollRow 지금 맨 위에 보이는 줄 번호
     */
    public static int buttonY(int index, int scrollRow)
    {
        int row = index / columns() - scrollRow;
        return PADDING + row * (BUTTON_HEIGHT + BUTTON_GAP);
    }
}
