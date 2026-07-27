package com.daqem.jobsplus.client.gui.jobs.components;

import com.daqem.jobsplus.client.gui.jobs.JobsScreenState;
import com.daqem.jobsplus.client.stock.ClientStockMarket;
import com.daqem.jobsplus.stock.SnapshotStatus;
import com.daqem.jobsplus.stock.StockCatalog;
import com.daqem.jobsplus.stock.StockMarketSnapshot;
import com.daqem.jobsplus.stock.StockQuote;
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

    private final JobsScreenState state;

    public StockTableRowsContentComponent(JobsScreenState state)
    {
        // 행 구성은 서버 스냅샷이 아니라 고정된 종목 목록을 따른다.
        // 스냅샷이 아직 도착하지 않아도 표 높이와 종목명이 흔들리지 않도록 하기 위함이다.
        super(0, 0, StockTableComponent.TABLE_WIDTH,
                StockTableComponent.ROW_HEIGHT * StockCatalog.getStocks().size());
        this.state = state;

        List<StockCatalog.StockDefinition> stocks = StockCatalog.getStocks();
        for (int index = 0; index < stocks.size(); index++)
        {
            String stockId = stocks.get(index).id();
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

        List<StockCatalog.StockDefinition> stocks = StockCatalog.getStocks();
        // 플레이어 PC 시간이 아니라 서버가 알려 준 상태로만 판단한다.
        StockMarketSnapshot snapshot = ClientStockMarket.getSnapshot();
        boolean refreshing = snapshot.status() == SnapshotStatus.REFRESHING;
        for (int index = 0; index < stocks.size(); index++)
        {
            StockCatalog.StockDefinition stock = stocks.get(index);
            StockQuote quote = snapshot.getQuote(stock.id());
            int rowY = y + index * StockTableComponent.ROW_HEIGHT;
            boolean selected = stock.id().equals(this.state.getSelectedStockId());
            if (selected)
            {
                guiGraphics.fill(x + 1, rowY, right - 1,
                        rowY + StockTableComponent.ROW_HEIGHT - 1, 0x55F2C94C);
            }
            guiGraphics.fill(x, rowY + StockTableComponent.ROW_HEIGHT - 1, right,
                    rowY + StockTableComponent.ROW_HEIGHT, StockTableComponent.GRID_COLOR);

            drawScaledString(guiGraphics, selected ? "▶" : "",
                    x + 1, rowY + 2, StockTableComponent.TEXT_COLOR);
            drawScaledString(guiGraphics, stock.name(), x + 5, rowY + 2, StockTableComponent.TEXT_COLOR);
            if (quote == null || !quote.hasValidPrice())
            {
                // 갱신 중과 조회 실패는 모두 거래가 막히지만, 원인이 다르므로 구분해서 알린다.
                String statusText = refreshing ? "갱신 중" : "조회 실패";
                drawScaledString(guiGraphics, statusText,
                        x + StockTableComponent.NAME_COLUMN_WIDTH + 2, rowY + 2,
                        StockTableComponent.TEXT_COLOR);
                drawScaledStringRight(guiGraphics, "-", right - 3, rowY + 2,
                        StockTableComponent.TEXT_COLOR);
                continue;
            }

            String priceText = PRICE_FORMAT.format(Math.round(quote.priceKrw()));
            drawScaledStringRight(guiGraphics, priceText,
                    x + StockTableComponent.PRICE_COLUMN_WIDTH - 2, rowY + 2,
                    StockTableComponent.TEXT_COLOR);

            String changeText = String.format(Locale.ROOT, "%+.2f%%", quote.percentChange());
            int changeColor = quote.percentChange() > 0
                    ? POSITIVE_COLOR
                    : quote.percentChange() < 0 ? NEGATIVE_COLOR : StockTableComponent.TEXT_COLOR;
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
