package com.daqem.jobsplus.client.gui.jobs.components;

import com.daqem.jobsplus.client.gui.theme.JobsTheme;
import com.daqem.jobsplus.client.gui.theme.StockIcons;
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

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class StockTableRowsContentComponent extends EmptyComponent
{
    private static final float TEXT_SCALE = 0.60f;
    private static final int POSITIVE_COLOR = JobsTheme.CYAN;
    private static final int NEGATIVE_COLOR = JobsTheme.ERROR;
    private static final NumberFormat PRICE_FORMAT = NumberFormat.getIntegerInstance(Locale.KOREA);

    private final JobsScreenState state;

    public StockTableRowsContentComponent(JobsScreenState state, int width)
    {
        // 행 구성은 서버 스냅샷이 아니라 고정된 종목 목록을 따른다.
        // 스냅샷이 아직 도착하지 않아도 표 높이와 종목명이 흔들리지 않도록 하기 위함이다.
        super(0, 0, width,
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
                    () -> this.selectStock(stockId)
            ));
        }
    }

    private void selectStock(String stockId)
    {
        this.state.setSelectedStockId(stockId);
        StockPosition position = this.state.getStockAccount().getPosition(stockId);
        if (position == null)
        {
            return;
        }
        this.state.setSelectedStockPositionSide(position.side());
        this.state.setSelectedStockLeverage(position.leverage());
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick, int parentWidth,
                       int parentHeight)
    {
        int x = getTotalX();
        int y = getTotalY();
        int right = x + getWidth();
        int bottom = y + getHeight();
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
            boolean hovered = mouseX >= x && mouseX < right && mouseY >= rowY
                    && mouseY < rowY + StockTableComponent.ROW_HEIGHT - 2;
            JobsTheme.texture(guiGraphics, selected || hovered ? JobsTheme.Skin.SECONDARY_HOVER : JobsTheme.Skin.INSET,
                    x, rowY, getWidth(), StockTableComponent.ROW_HEIGHT - 2);
            if (selected) {
                guiGraphics.fill(x + 1, rowY + 2, x + 3, rowY + StockTableComponent.ROW_HEIGHT - 4, JobsTheme.CYAN);
            }
            StockIcons.draw(guiGraphics, stock.id(), x + 6, rowY + 3, 16);
            int nameWidth = StockTableComponent.nameColumn(getWidth()) - 29;
            JobsTheme.text(guiGraphics, Component.literal(stock.id()), x + 26, rowY + 3,
                    nameWidth, StockTableComponent.TEXT_COLOR);
            JobsTheme.text(guiGraphics, Component.literal(stock.name()), x + 26, rowY + 13,
                    nameWidth, JobsTheme.MUTED);
            if (quote == null || !quote.hasValidPrice())
            {
                // 갱신 중과 조회 실패는 모두 거래가 막히지만, 원인이 다르므로 구분해서 알린다.
                String statusText = refreshing ? "갱신 중" : "조회 실패";
                drawScaledString(guiGraphics, statusText,
                        x + StockTableComponent.nameColumn(getWidth()) + 2, rowY + 8,
                        StockTableComponent.TEXT_COLOR);
                drawScaledStringRight(guiGraphics, "-", right - 3, rowY + 8,
                        StockTableComponent.TEXT_COLOR);
                continue;
            }

            String priceText = PRICE_FORMAT.format(Math.round(quote.priceKrw()));
            JobsTheme.label(guiGraphics, Component.literal(priceText),
                    x + StockTableComponent.nameColumn(getWidth()), rowY,
                    StockTableComponent.priceColumn(getWidth()) - StockTableComponent.nameColumn(getWidth()),
                    StockTableComponent.ROW_HEIGHT - 1, StockTableComponent.TEXT_COLOR);

            String direction = "";
            if (quote.percentChange() > 0) {
                direction = "▲ ";
            } else if (quote.percentChange() < 0) {
                direction = "▼ ";
            }
            String changeText = direction + String.format(Locale.ROOT, "%.2f%%", Math.abs(quote.percentChange()));
            int changeColor = quote.percentChange() > 0
                    ? POSITIVE_COLOR
                    : quote.percentChange() < 0 ? NEGATIVE_COLOR : StockTableComponent.TEXT_COLOR;
            JobsTheme.label(guiGraphics, Component.literal(changeText),
                    x + StockTableComponent.priceColumn(getWidth()), rowY,
                    getWidth() - StockTableComponent.priceColumn(getWidth()),
                    StockTableComponent.ROW_HEIGHT - 1, changeColor);
        }
    }

    static void drawScaledString(GuiGraphicsExtractor guiGraphics, String text, int x, int y, int color)
    {
        guiGraphics.pose().pushMatrix();
        guiGraphics.pose().translate(x, y);
        guiGraphics.pose().scale(TEXT_SCALE, TEXT_SCALE);
        guiGraphics.text(Minecraft.getInstance().font, text, 0, 0, color, false);
        guiGraphics.pose().popMatrix();
    }

    static void drawScaledStringRight(GuiGraphicsExtractor guiGraphics, String text, int right, int y, int color)
    {
        int width = (int) Math.ceil(Minecraft.getInstance().font.width(text) * TEXT_SCALE);
        drawScaledString(guiGraphics, text, right - width, y, color);
    }

    static void drawScaledStringCentered(GuiGraphicsExtractor guiGraphics, String text, int center, int y, int color)
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
        protected void extractContents(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick)
        {
        }
    }
}
