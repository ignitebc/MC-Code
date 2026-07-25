package com.daqem.jobsplus.client.gui.jobs.components;

import com.daqem.jobsplus.client.gui.jobs.JobsScreenState;
import com.daqem.jobsplus.client.gui.jobs.stock.StockMarketService;
import com.daqem.jobsplus.client.gui.jobs.stock.StockQuote;
import com.daqem.jobsplus.player.stock.StockPosition;
import com.daqem.uilib.gui.component.EmptyComponent;
import com.daqem.uilib.gui.widget.CustomButtonWidget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

public class StockHoldingsContentComponent extends EmptyComponent
{
    private static final int ROW_HEIGHT = 10;
    private static final int TEXT_COLOR = 0xFF1E1410;
    private static final int GRID_COLOR = 0xFFD8BF96;
    private static final NumberFormat PRICE_FORMAT = NumberFormat.getIntegerInstance(Locale.KOREA);

    private final JobsScreenState state;
    private final StockMarketService stockMarketService;

    public StockHoldingsContentComponent(JobsScreenState state)
    {
        super(0, 0, 132, Math.max(20, (state.getStockAccount().positions().size() + 1) * ROW_HEIGHT));
        this.state = state;
        this.stockMarketService = StockMarketService.getInstance();
        int index = 0;
        for (StockPosition position : state.getStockAccount().positions())
        {
            String stockId = position.stockId();
            this.addWidget(new StockHoldingRowButtonWidget(
                    0,
                    ROW_HEIGHT + index * ROW_HEIGHT,
                    getWidth(),
                    ROW_HEIGHT,
                    () -> {
                        this.state.setSelectedHoldingStockId(stockId);
                        this.state.setSelectedStockId(stockId);
                    }
            ));
            index++;
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick, int parentWidth,
                       int parentHeight)
    {
        int x = getTotalX();
        int y = getTotalY();
        int right = x + getWidth();
        drawScaled(guiGraphics, "주식명", x + 2, y + 2, TEXT_COLOR);
        drawScaled(guiGraphics, "평단가", x + 40, y + 2, TEXT_COLOR);
        drawScaled(guiGraphics, "갯수", x + 78, y + 2, TEXT_COLOR);
        drawScaled(guiGraphics, "등락률", x + 105, y + 2, TEXT_COLOR);
        guiGraphics.fill(x, y + ROW_HEIGHT - 1, right, y + ROW_HEIGHT, GRID_COLOR);

        int index = 0;
        for (StockPosition position : this.state.getStockAccount().positions())
        {
            int rowY = y + ROW_HEIGHT + index * ROW_HEIGHT;
            boolean selected = position.stockId().equals(this.state.getSelectedHoldingStockId());
            if (selected)
            {
                guiGraphics.fill(x, rowY, right, rowY + ROW_HEIGHT - 1, 0x55F2C94C);
            }
            StockQuote quote = this.stockMarketService.getQuote(position.stockId());
            String name = quote == null ? position.stockId() : quote.name();
            String averagePrice = PRICE_FORMAT.format(Math.round(position.getAverageEntryPrice()));
            String units = formatUnits(position.quantity());
            String returnRate = "-";
            int returnColor = TEXT_COLOR;
            if (quote != null && quote.available() && position.getAverageEntryPrice() > 0)
            {
                double rate = (quote.priceKrw() / position.getAverageEntryPrice() - 1) * 100;
                returnRate = String.format(Locale.ROOT, "%+.2f%%", rate);
                returnColor = rate > 0 ? 0xFFE53935 : rate < 0 ? 0xFF1976D2 : TEXT_COLOR;
            }

            drawScaled(guiGraphics, selected ? "▶" : "", x + 1, rowY + 2, TEXT_COLOR);
            drawScaled(guiGraphics, name, x + 6, rowY + 2, TEXT_COLOR);
            drawScaledRight(guiGraphics, averagePrice, x + 74, rowY + 2, TEXT_COLOR);
            drawScaledRight(guiGraphics, units, x + 101, rowY + 2, TEXT_COLOR);
            drawScaled(guiGraphics, returnRate, x + 105, rowY + 2, returnColor);
            guiGraphics.fill(x, rowY + ROW_HEIGHT - 1, right, rowY + ROW_HEIGHT, GRID_COLOR);
            index++;
        }
    }

    private static String formatUnits(double units)
    {
        return BigDecimal.valueOf(units).stripTrailingZeros().toPlainString();
    }

    private static void drawScaled(GuiGraphics guiGraphics, String text, int x, int y, int color)
    {
        guiGraphics.pose().pushMatrix();
        guiGraphics.pose().translate(x, y);
        guiGraphics.pose().scale(0.50f, 0.50f);
        guiGraphics.drawString(Minecraft.getInstance().font, text, 0, 0, color, false);
        guiGraphics.pose().popMatrix();
    }

    private static void drawScaledRight(GuiGraphics guiGraphics, String text, int right, int y, int color)
    {
        int width = (int) Math.ceil(Minecraft.getInstance().font.width(text) * 0.50f);
        drawScaled(guiGraphics, text, right - width, y, color);
    }

    private static class StockHoldingRowButtonWidget extends CustomButtonWidget
    {
        public StockHoldingRowButtonWidget(int x, int y, int width, int height, Runnable onPress)
        {
            super(x, y, width, height, Component.empty(), null, button -> onPress.run());
        }

        @Override
        protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick)
        {
        }
    }
}
