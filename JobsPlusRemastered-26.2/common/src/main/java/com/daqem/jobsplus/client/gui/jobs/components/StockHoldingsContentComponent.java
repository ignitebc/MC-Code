package com.daqem.jobsplus.client.gui.jobs.components;

import com.daqem.jobsplus.client.gui.theme.JobsTheme;
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
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

public class StockHoldingsContentComponent extends EmptyComponent
{
    private static final int ROW_HEIGHT = 16;
    private static final int TABLE_WIDTH = 136;
    private static final int NAME_COLUMN_END = 34;
    private static final int POSITION_COLUMN_END = 54;
    private static final int AVERAGE_PRICE_COLUMN_END = 86;
    private static final int QUANTITY_COLUMN_END = 108;
    private static final int TEXT_COLOR = JobsTheme.TEXT;
    private static final int GRID_COLOR = JobsTheme.DIVIDER;
    private static final NumberFormat PRICE_FORMAT = NumberFormat.getIntegerInstance(Locale.KOREA);

    private final JobsScreenState state;

    public StockHoldingsContentComponent(JobsScreenState state, int width)
    {
        super(0, 0, width, Math.max(36, (state.getStockAccount().positions().size() + 1) * ROW_HEIGHT));
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
    public void extractRenderState(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick, int parentWidth,
                       int parentHeight)
    {
        int nameColumn = getWidth() * 25 / 100;
        int positionColumn = getWidth() * 40 / 100;
        int priceColumn = getWidth() * 63 / 100;
        int amountColumn = getWidth() * 80 / 100;
        int x = getTotalX();
        int y = getTotalY();
        int right = x + getWidth();
        JobsTheme.texture(guiGraphics, JobsTheme.Skin.HEADER, x, y, getWidth(), ROW_HEIGHT);
        drawScaledCentered(guiGraphics, "종목명", x + nameColumn / 2, y + 2, TEXT_COLOR);
        drawScaledCentered(guiGraphics, "포지션", x + (nameColumn + positionColumn) / 2, y + 2,
                TEXT_COLOR);
        drawScaledCentered(guiGraphics, "평단가", x + (positionColumn + priceColumn) / 2,
                y + 2, TEXT_COLOR);
        drawScaledCentered(guiGraphics, "투자금", x + (priceColumn + amountColumn) / 2, y + 2, TEXT_COLOR);
        // 전일 대비가 아니라 평단가 대비 손익이므로 "수익률"이 맞다.
        drawScaled(guiGraphics, "수익률(%)", x + amountColumn + 2, y + 2, TEXT_COLOR);
        guiGraphics.fill(x, y + ROW_HEIGHT - 1, right, y + ROW_HEIGHT, GRID_COLOR);

        StockMarketSnapshot snapshot = ClientStockMarket.getSnapshot();
        int index = 0;
        for (StockPosition position : this.state.getStockAccount().positions())
        {
            int rowY = y + ROW_HEIGHT + index * ROW_HEIGHT;
            boolean selected = position.stockId().equals(this.state.getSelectedHoldingStockId());
            guiGraphics.fill(x, rowY, right, rowY + ROW_HEIGHT - 1,
                    selected ? JobsTheme.SELECTED : (index % 2 == 0 ? JobsTheme.INSET : JobsTheme.PANEL));
            if (selected) {
                guiGraphics.fill(x, rowY + 5, x + 2, rowY + ROW_HEIGHT - 2, JobsTheme.CYAN);
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
                    returnColor = JobsTheme.SUCCESS;
                }
                else if (rate < 0)
                {
                    returnColor = JobsTheme.ERROR;
                }
            }

            drawScaled(guiGraphics, selected ? "▶" : "", x + 1, rowY + 5, TEXT_COLOR);
            JobsTheme.text(guiGraphics, Component.literal(name), x + 6, rowY + 5,
                    nameColumn - 9, TEXT_COLOR);
            drawScaledCentered(guiGraphics, positionName, x + (nameColumn + positionColumn) / 2,
                    rowY + 5, TEXT_COLOR);
            drawScaledRight(guiGraphics, averagePrice, x + priceColumn - 2, rowY + 5, TEXT_COLOR);
            drawScaledRight(guiGraphics, investedAmount, x + amountColumn - 2, rowY + 5, TEXT_COLOR);
            drawScaled(guiGraphics, returnRate, x + amountColumn + 2, rowY + 5, returnColor);
            guiGraphics.fill(x, rowY + ROW_HEIGHT - 1, right, rowY + ROW_HEIGHT, GRID_COLOR);
            index++;
        }

        if (this.state.getStockAccount().positions().isEmpty()) {
            JobsTheme.label(guiGraphics, Component.literal("보유 포지션이 없습니다."), x, y + ROW_HEIGHT + 4,
                    getWidth(), 12, JobsTheme.MUTED);
        }

    }

    private static String formatAmount(double amount)
    {
        return BigDecimal.valueOf(amount).stripTrailingZeros().toPlainString();
    }

    private static void drawScaled(GuiGraphicsExtractor guiGraphics, String text, int x, int y, int color)
    {
        guiGraphics.pose().pushMatrix();
        guiGraphics.pose().translate(x, y);
        guiGraphics.pose().scale(0.50f, 0.50f);
        guiGraphics.text(Minecraft.getInstance().font, text, 0, 0, color, false);
        guiGraphics.pose().popMatrix();
    }

    private static void drawScaledCentered(GuiGraphicsExtractor guiGraphics, String text, int center, int y, int color)
    {
        int width = (int) Math.ceil(Minecraft.getInstance().font.width(text) * 0.50f);
        drawScaled(guiGraphics, text, center - width / 2, y, color);
    }

    private static void drawScaledRight(GuiGraphicsExtractor guiGraphics, String text, int right, int y, int color)
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
        protected void extractContents(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick)
        {
        }
    }
}
