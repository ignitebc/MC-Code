package com.daqem.jobsplus.client.gui.jobs.components;

import com.daqem.jobsplus.client.gui.jobs.JobsScreenState;
import com.daqem.jobsplus.client.stock.ClientStockMarket;
import com.daqem.jobsplus.stock.SnapshotStatus;
import com.daqem.jobsplus.stock.StockCatalog;
import com.daqem.jobsplus.stock.StockMarketSnapshot;
import com.daqem.jobsplus.stock.StockQuote;
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
    private static final int TABLE_WIDTH = 146;
    private static final int NAME_COLUMN_END = 37;
    private static final int POSITION_COLUMN_END = 58;
    private static final int AVERAGE_PRICE_COLUMN_END = 92;
    private static final int QUANTITY_COLUMN_END = 113;
    private static final int TEXT_COLOR = 0xFF1E1410;
    private static final int GRID_COLOR = 0xFFD8BF96;
    private static final NumberFormat PRICE_FORMAT = NumberFormat.getIntegerInstance(Locale.KOREA);

    private final JobsScreenState state;

    public StockHoldingsContentComponent(JobsScreenState state)
    {
        super(0, 0, TABLE_WIDTH, Math.max(20, (state.getStockAccount().positions().size() + 1) * ROW_HEIGHT));
        this.state = state;
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
                        this.state.setSelectedStockPositionSide(position.side());
                        this.state.setSelectedStockLeverage(position.leverage());
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
        int bottom = y + getHeight();
        drawScaledCentered(guiGraphics, "주식명", x + NAME_COLUMN_END / 2, y + 2, TEXT_COLOR);
        drawScaledCentered(guiGraphics, "포지션", x + (NAME_COLUMN_END + POSITION_COLUMN_END) / 2, y + 2,
                TEXT_COLOR);
        drawScaledCentered(guiGraphics, "평단가", x + (POSITION_COLUMN_END + AVERAGE_PRICE_COLUMN_END) / 2,
                y + 2, TEXT_COLOR);
        drawScaledCentered(guiGraphics, "투자금", x + (AVERAGE_PRICE_COLUMN_END + QUANTITY_COLUMN_END) / 2, y + 2, TEXT_COLOR);
        // 전일 대비가 아니라 평단가 대비 손익이므로 "수익률"이 맞다.
        drawScaledCentered(guiGraphics, "수익률(%)", x + (QUANTITY_COLUMN_END + TABLE_WIDTH) / 2, y + 2, TEXT_COLOR);
        guiGraphics.fill(x, y + ROW_HEIGHT - 1, right, y + ROW_HEIGHT, GRID_COLOR);

        StockMarketSnapshot snapshot = ClientStockMarket.getSnapshot();
        int index = 0;
        for (StockPosition position : this.state.getStockAccount().positions())
        {
            int rowY = y + ROW_HEIGHT + index * ROW_HEIGHT;
            boolean selected = position.stockId().equals(this.state.getSelectedHoldingStockId());
            if (selected)
            {
                guiGraphics.fill(x, rowY, right, rowY + ROW_HEIGHT - 1, 0x55F2C94C);
            }
            StockQuote quote = snapshot.getQuote(position.stockId());
            String name = StockCatalog.getStockName(position.stockId());
            String positionName = position.side().getDisplayName() + " "
                    + StockPosition.getLeverageDisplayName(position.leverage());
            String averagePrice = PRICE_FORMAT.format(Math.round(position.getAverageEntryPrice()));
            String investedAmount = formatAmount(position.investedAmount());
            // 시세를 못 받은 상태와 해당 종목만 실패한 상태를 구분해서 알린다.
            String returnRate = snapshot.status() == SnapshotStatus.REFRESHING ? "갱신중" : "실패";
            int returnColor = TEXT_COLOR;
            if (quote != null && quote.hasValidPrice() && position.getAverageEntryPrice() > 0)
            {
                double rate = position.getReturnRate(quote.priceKrw());
                rate = Math.max(-100, rate);
                returnRate = String.format(Locale.ROOT, "%+.2f%%", rate);
                if (rate > 0)
                {
                    returnColor = 0xFFE53935;
                }
                else if (rate < 0)
                {
                    returnColor = 0xFF1976D2;
                }
            }

            drawScaled(guiGraphics, selected ? "▶" : "", x + 1, rowY + 2, TEXT_COLOR);
            drawScaled(guiGraphics, name, x + 6, rowY + 2, TEXT_COLOR);
            drawScaledCentered(guiGraphics, positionName, x + (NAME_COLUMN_END + POSITION_COLUMN_END) / 2,
                    rowY + 2, TEXT_COLOR);
            drawScaledRight(guiGraphics, averagePrice, x + AVERAGE_PRICE_COLUMN_END - 2, rowY + 2, TEXT_COLOR);
            drawScaledRight(guiGraphics, investedAmount, x + QUANTITY_COLUMN_END - 2, rowY + 2, TEXT_COLOR);
            drawScaledRight(guiGraphics, returnRate, right - 2, rowY + 2, returnColor);
            guiGraphics.fill(x, rowY + ROW_HEIGHT - 1, right, rowY + ROW_HEIGHT, GRID_COLOR);
            index++;
        }

        guiGraphics.fill(x + NAME_COLUMN_END, y, x + NAME_COLUMN_END + 1, bottom, GRID_COLOR);
        guiGraphics.fill(x + POSITION_COLUMN_END, y, x + POSITION_COLUMN_END + 1, bottom, GRID_COLOR);
        guiGraphics.fill(x + AVERAGE_PRICE_COLUMN_END, y, x + AVERAGE_PRICE_COLUMN_END + 1, bottom, GRID_COLOR);
        guiGraphics.fill(x + QUANTITY_COLUMN_END, y, x + QUANTITY_COLUMN_END + 1, bottom, GRID_COLOR);
    }

    private static String formatAmount(double amount)
    {
        return BigDecimal.valueOf(amount).stripTrailingZeros().toPlainString();
    }

    private static void drawScaled(GuiGraphics guiGraphics, String text, int x, int y, int color)
    {
        guiGraphics.pose().pushMatrix();
        guiGraphics.pose().translate(x, y);
        guiGraphics.pose().scale(0.50f, 0.50f);
        guiGraphics.drawString(Minecraft.getInstance().font, text, 0, 0, color, false);
        guiGraphics.pose().popMatrix();
    }

    private static void drawScaledCentered(GuiGraphics guiGraphics, String text, int center, int y, int color)
    {
        int width = (int) Math.ceil(Minecraft.getInstance().font.width(text) * 0.50f);
        drawScaled(guiGraphics, text, center - width / 2, y, color);
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
