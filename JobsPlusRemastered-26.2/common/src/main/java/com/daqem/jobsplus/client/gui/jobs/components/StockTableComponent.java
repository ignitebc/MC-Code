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
    static final int HEADER_HEIGHT = 28;
    static final int ROW_HEIGHT = 24;

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

        JobsTheme.text(guiGraphics, net.minecraft.network.chat.Component.literal("주식 시세"),
                x + 2, y + 2, tableWidth - 4, JobsTheme.TEXT);
        JobsTheme.texture(guiGraphics, JobsTheme.Skin.HEADER, x, y + 13, tableWidth, 13);
        StockTableRowsContentComponent.drawScaledStringCentered(
                guiGraphics, "종목", x + nameWidth / 2, y + 16, TEXT_COLOR);
        StockTableRowsContentComponent.drawScaledStringCentered(
                guiGraphics, "현재가(원)", x + (nameWidth + priceWidth) / 2, y + 16, TEXT_COLOR);
        StockTableRowsContentComponent.drawScaledStringCentered(
                guiGraphics, "등락률", x + (priceWidth + tableWidth) / 2, y + 16, TEXT_COLOR);

    }
    static int nameColumn(int width) {
        return width * 47 / 100;
    }

    static int priceColumn(int width) {
        return width * 76 / 100;
    }
}
