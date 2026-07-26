package com.daqem.jobsplus.client.gui.jobs.components;

import com.daqem.jobsplus.client.gui.jobs.JobsScreenState;
import com.daqem.jobsplus.client.gui.jobs.stock.StockMarketService;
import com.daqem.jobsplus.client.gui.jobs.stock.StockQuote;
import com.daqem.uilib.gui.component.EmptyComponent;
import com.daqem.uilib.gui.widget.CustomButtonWidget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class StockTableRowsContentComponent extends EmptyComponent
{
    private static final float TEXT_SCALE = 0.50f;
    private static final int POSITIVE_COLOR = 0xFFE53935;
    private static final int NEGATIVE_COLOR = 0xFF1976D2;
    private static final NumberFormat PRICE_FORMAT = NumberFormat.getIntegerInstance(Locale.KOREA);

    private final StockMarketService stockMarketService;
    private final JobsScreenState state;

    public StockTableRowsContentComponent(JobsScreenState state)
    {
        super(0, 0, StockTableComponent.TABLE_WIDTH,
                StockTableComponent.ROW_HEIGHT * StockMarketService.getInstance().getQuotes().size());
        this.state = state;
        this.stockMarketService = StockMarketService.getInstance();

        for (int index = 0; index < this.stockMarketService.getQuotes().size(); index++)
        {
            String stockId = this.stockMarketService.getQuotes().get(index).id();
            this.addWidget(new StockRowButtonWidget(
                    0,
                    index * StockTableComponent.ROW_HEIGHT,
                    getWidth(),
                    StockTableComponent.ROW_HEIGHT,
                    () -> this.state.setSelectedStockId(stockId)
            ));
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick, int parentWidth,
                       int parentHeight)
    {
        this.stockMarketService.refreshIfNeeded();

        int x = getTotalX();
        int y = getTotalY();
        int right = x + getWidth();
        int bottom = y + getHeight();
        guiGraphics.fill(x, y, x + 1, bottom, StockTableComponent.GRID_COLOR);
        guiGraphics.fill(x + StockTableComponent.NAME_COLUMN_WIDTH, y,
                x + StockTableComponent.NAME_COLUMN_WIDTH + 1, bottom, StockTableComponent.GRID_COLOR);
        guiGraphics.fill(x + StockTableComponent.PRICE_COLUMN_WIDTH, y,
                x + StockTableComponent.PRICE_COLUMN_WIDTH + 1, bottom, StockTableComponent.GRID_COLOR);
        guiGraphics.fill(right - 1, y, right, bottom, StockTableComponent.GRID_COLOR);

        List<StockQuote> quotes = this.stockMarketService.getQuotes();
        for (int index = 0; index < quotes.size(); index++)
        {
            StockQuote quote = quotes.get(index);
            int rowY = y + index * StockTableComponent.ROW_HEIGHT;
            if (quote.id().equals(this.state.getSelectedStockId()))
            {
                guiGraphics.fill(x + 1, rowY, right - 1,
                        rowY + StockTableComponent.ROW_HEIGHT - 1, 0x55F2C94C);
            }
            guiGraphics.fill(x, rowY + StockTableComponent.ROW_HEIGHT - 1, right,
                    rowY + StockTableComponent.ROW_HEIGHT, StockTableComponent.GRID_COLOR);

            drawScaledString(guiGraphics, quote.id().equals(this.state.getSelectedStockId()) ? "▶" : "",
                    x + 1, rowY + 2, StockTableComponent.TEXT_COLOR);
            drawScaledString(guiGraphics, quote.name(), x + 5, rowY + 2, StockTableComponent.TEXT_COLOR);
            if (!quote.available())
            {
                String unavailableText = this.stockMarketService.isRefreshing() ? "불러오는 중" : "조회 불가";
                drawScaledString(guiGraphics, unavailableText,
                        x + StockTableComponent.NAME_COLUMN_WIDTH + 2, rowY + 2,
                        StockTableComponent.TEXT_COLOR);
                drawScaledStringRight(guiGraphics, "-", right - 3, rowY + 2,
                        StockTableComponent.TEXT_COLOR);
                continue;
            }

            String priceText = PRICE_FORMAT.format(Math.round(quote.priceKrw()));
            String changeText = String.format(Locale.ROOT, "%+.2f%%", quote.percentChange());
            int changeColor = quote.percentChange() > 0
                    ? POSITIVE_COLOR
                    : quote.percentChange() < 0 ? NEGATIVE_COLOR : StockTableComponent.TEXT_COLOR;

            drawScaledStringRight(guiGraphics, priceText,
                    x + StockTableComponent.PRICE_COLUMN_WIDTH - 2, rowY + 2,
                    StockTableComponent.TEXT_COLOR);
            drawScaledStringRight(guiGraphics, changeText, right - 3, rowY + 2, changeColor);
        }
    }

    static void drawScaledString(GuiGraphics guiGraphics, String text, int x, int y, int color)
    {
        guiGraphics.pose().pushMatrix();
        guiGraphics.pose().translate(x, y);
        guiGraphics.pose().scale(TEXT_SCALE, TEXT_SCALE);
        guiGraphics.drawString(Minecraft.getInstance().font, text, 0, 0, color, false);
        guiGraphics.pose().popMatrix();
    }

    static void drawScaledStringRight(GuiGraphics guiGraphics, String text, int right, int y, int color)
    {
        int width = (int) Math.ceil(Minecraft.getInstance().font.width(text) * TEXT_SCALE);
        drawScaledString(guiGraphics, text, right - width, y, color);
    }

    static void drawScaledStringCentered(GuiGraphics guiGraphics, String text, int center, int y, int color)
    {
        int width = (int) Math.ceil(Minecraft.getInstance().font.width(text) * TEXT_SCALE);
        drawScaledString(guiGraphics, text, center - width / 2, y, color);
    }

    private static class StockRowButtonWidget extends CustomButtonWidget
    {
        public StockRowButtonWidget(int x, int y, int width, int height, Runnable onPress)
        {
            super(x, y, width, height, Component.empty(), null, button -> onPress.run());
        }

        @Override
        protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick)
        {
        }
    }
}
