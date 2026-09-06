package com.daqem.jobsplus.client.gui.jobs.components;

import com.daqem.jobsplus.client.gui.theme.JobsTheme;
import com.daqem.jobsplus.client.gui.jobs.JobsScreenState;
import com.daqem.jobsplus.client.gui.jobs.widgets.StockTableScrollWidget;
import com.daqem.uilib.gui.component.EmptyComponent;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public class StockTableComponent extends EmptyComponent
{
    static final int TEXT_COLOR = JobsTheme.TEXT;
    static final int GRID_COLOR = JobsTheme.DIVIDER;
    static final int HEADER_HEIGHT = 13;
    static final int ROW_HEIGHT = 12;

    public StockTableComponent(JobsScreenState state, int x, int y, int width, int height)
    {
        super(x, y, width, height);

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
        int tableWidth = getWidth() - 10;
        int nameWidth = nameColumn(tableWidth);
        int priceWidth = priceColumn(tableWidth);
        int right = x + tableWidth;

        guiGraphics.fill(x, y, right, y + 1, GRID_COLOR);
        guiGraphics.fill(x, y + HEADER_HEIGHT - 1, right, y + HEADER_HEIGHT, GRID_COLOR);
        guiGraphics.fill(x, y, x + 1, y + HEADER_HEIGHT, GRID_COLOR);
        guiGraphics.fill(x + nameWidth, y, x + nameWidth + 1, y + HEADER_HEIGHT, GRID_COLOR);
        guiGraphics.fill(x + priceWidth, y, x + priceWidth + 1, y + HEADER_HEIGHT, GRID_COLOR);
        guiGraphics.fill(right - 1, y, right, y + HEADER_HEIGHT, GRID_COLOR);

        StockTableRowsContentComponent.drawScaledStringCentered(
                guiGraphics, "종목명", x + nameWidth / 2, y + 3, TEXT_COLOR);
        StockTableRowsContentComponent.drawScaledStringCentered(
                guiGraphics, "가격(원)", x + (nameWidth + priceWidth) / 2, y + 3, TEXT_COLOR);
        StockTableRowsContentComponent.drawScaledStringCentered(
                guiGraphics, "등락률(%)", x + (priceWidth + tableWidth) / 2, y + 3, TEXT_COLOR);
    }
    static int nameColumn(int width) {
        return width * 36 / 100;
    }

    static int priceColumn(int width) {
        return width * 72 / 100;
    }
}
