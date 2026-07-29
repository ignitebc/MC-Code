package com.daqem.jobsplus.client.gui.jobs.components;

import com.daqem.jobsplus.client.gui.jobs.JobsScreenState;
import com.daqem.jobsplus.client.gui.jobs.widgets.StockTableScrollWidget;
import com.daqem.uilib.gui.component.EmptyComponent;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public class StockTableComponent extends EmptyComponent
{
    static final int TEXT_COLOR = 0xFF1E1410;
    static final int GRID_COLOR = 0xFFD8BF96;
    static final int HEADER_HEIGHT = 10;
    static final int ROW_HEIGHT = 8;
    static final int TABLE_WIDTH = 118;
    static final int NAME_COLUMN_WIDTH = 42;
    static final int PRICE_COLUMN_WIDTH = 86;

    public StockTableComponent(JobsScreenState state)
    {
        super(31, 31, 132, 178);

        EmptyComponent rowsComponent = new EmptyComponent(
                0, HEADER_HEIGHT, getWidth(), getHeight() - HEADER_HEIGHT);
        rowsComponent.addWidget(new StockTableScrollWidget(
                getWidth(), getHeight() - HEADER_HEIGHT, state));
        this.addComponent(rowsComponent);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick, int parentWidth,
                       int parentHeight)
    {
        int x = getTotalX();
        int y = getTotalY();
        int right = x + TABLE_WIDTH;

        guiGraphics.fill(x, y, right, y + 1, GRID_COLOR);
        guiGraphics.fill(x, y + HEADER_HEIGHT - 1, right, y + HEADER_HEIGHT, GRID_COLOR);
        guiGraphics.fill(x, y, x + 1, y + HEADER_HEIGHT, GRID_COLOR);
        guiGraphics.fill(x + NAME_COLUMN_WIDTH, y, x + NAME_COLUMN_WIDTH + 1, y + HEADER_HEIGHT, GRID_COLOR);
        guiGraphics.fill(x + PRICE_COLUMN_WIDTH, y, x + PRICE_COLUMN_WIDTH + 1, y + HEADER_HEIGHT, GRID_COLOR);
        guiGraphics.fill(right - 1, y, right, y + HEADER_HEIGHT, GRID_COLOR);

        StockTableRowsContentComponent.drawScaledStringCentered(
                guiGraphics, "종목명", x + NAME_COLUMN_WIDTH / 2, y + 3, TEXT_COLOR);
        StockTableRowsContentComponent.drawScaledStringCentered(
                guiGraphics, "가격(원)", x + (NAME_COLUMN_WIDTH + PRICE_COLUMN_WIDTH) / 2, y + 3, TEXT_COLOR);
        StockTableRowsContentComponent.drawScaledStringCentered(
                guiGraphics, "등락률(%)", x + (PRICE_COLUMN_WIDTH + TABLE_WIDTH) / 2, y + 3, TEXT_COLOR);
    }
}
