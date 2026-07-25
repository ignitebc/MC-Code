package com.daqem.jobsplus.client.gui.jobs.components;

import com.daqem.jobsplus.client.gui.jobs.JobsScreenState;
import com.daqem.jobsplus.client.gui.jobs.stock.StockMarketService;
import com.daqem.jobsplus.client.gui.jobs.stock.StockQuote;
import com.daqem.jobsplus.player.stock.StockTransaction;
import com.daqem.uilib.gui.component.EmptyComponent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class StockHistoryContentComponent extends EmptyComponent
{
    private static final int MAX_TRANSACTIONS = 50;
    private static final int HEADER_HEIGHT = 22;
    private static final int ROW_HEIGHT = 10;
    private static final int TEXT_COLOR = 0xFF1E1410;
    private static final int GRID_COLOR = 0xFFD8BF96;
    private static final DateTimeFormatter TIME_FORMAT =
            DateTimeFormatter.ofPattern("MM-dd HH:mm").withZone(ZoneId.systemDefault());

    private final JobsScreenState state;
    private final StockMarketService stockMarketService;

    public StockHistoryContentComponent(JobsScreenState state)
    {
        super(0, 0, 132, HEADER_HEIGHT
                + Math.max(1, Math.min(MAX_TRANSACTIONS, state.getStockAccount().transactions().size()))
                * ROW_HEIGHT);
        this.state = state;
        this.stockMarketService = StockMarketService.getInstance();
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick, int parentWidth,
                       int parentHeight)
    {
        int x = getTotalX();
        int y = getTotalY();
        int right = x + getWidth();

        drawCentered(guiGraphics, "최근 거래내역", x + getWidth() / 2, y + 2);
        drawScaled(guiGraphics, "일시", x + 2, y + 13, TEXT_COLOR);
        drawScaled(guiGraphics, "구분", x + 35, y + 13, TEXT_COLOR);
        drawScaled(guiGraphics, "종목", x + 52, y + 13, TEXT_COLOR);
        drawScaledRight(guiGraphics, "수량", x + 103, y + 13, TEXT_COLOR);
        drawScaledRight(guiGraphics, "수익률(%)", right - 2, y + 13, TEXT_COLOR);
        guiGraphics.fill(x, y + HEADER_HEIGHT - 1, right, y + HEADER_HEIGHT, GRID_COLOR);

        List<StockTransaction> transactions = this.state.getStockAccount().transactions().stream()
                .sorted(Comparator.comparingLong(StockTransaction::timestamp).reversed())
                .limit(MAX_TRANSACTIONS)
                .toList();
        int count = transactions.size();
        if (count == 0)
        {
            drawScaled(guiGraphics, "거래내역이 없습니다.", x + 2, y + HEADER_HEIGHT + 2, TEXT_COLOR);
            return;
        }

        for (int index = 0; index < count; index++)
        {
            StockTransaction transaction = transactions.get(index);
            int rowY = y + HEADER_HEIGHT + index * ROW_HEIGHT;
            drawScaled(guiGraphics, TIME_FORMAT.format(Instant.ofEpochMilli(transaction.timestamp())),
                    x + 2, rowY + 2, TEXT_COLOR);
            drawScaled(guiGraphics, getTransactionName(transaction.type()), x + 35, rowY + 2, TEXT_COLOR);
            drawScaled(guiGraphics, getStockName(transaction.stockId()), x + 52, rowY + 2, TEXT_COLOR);
            drawScaledRight(guiGraphics, String.format(Locale.ROOT, "%.2f개", transaction.amount()),
                    x + 103, rowY + 2, TEXT_COLOR);
            drawScaledRight(guiGraphics, formatReturnRate(transaction), right - 2, rowY + 2,
                    getReturnRateColor(transaction));
            guiGraphics.fill(x, rowY + ROW_HEIGHT - 1, right, rowY + ROW_HEIGHT, GRID_COLOR);
        }
    }

    private static String formatReturnRate(StockTransaction transaction)
    {
        if (!transaction.type().equals("SELL") || !transaction.returnRateRecorded())
        {
            return "-";
        }
        return String.format(Locale.ROOT, "%+.2f%%", transaction.returnRate());
    }

    private static int getReturnRateColor(StockTransaction transaction)
    {
        if (!transaction.type().equals("SELL") || !transaction.returnRateRecorded())
        {
            return TEXT_COLOR;
        }
        return transaction.returnRate() > 0
                ? 0xFFE53935
                : transaction.returnRate() < 0 ? 0xFF1976D2 : TEXT_COLOR;
    }

    private String getStockName(String stockId)
    {
        if (stockId.isEmpty())
        {
            return "-";
        }
        StockQuote quote = this.stockMarketService.getQuote(stockId);
        return quote == null ? stockId : quote.name();
    }

    private static String getTransactionName(String type)
    {
        return switch (type)
        {
            case "DEPOSIT" -> "입금";
            case "WITHDRAW" -> "출금";
            case "BUY" -> "구매";
            case "SELL" -> "판매";
            default -> type;
        };
    }

    private static void drawCentered(GuiGraphics guiGraphics, String text, int centerX, int y)
    {
        int width = Minecraft.getInstance().font.width(text);
        guiGraphics.drawString(Minecraft.getInstance().font, text, centerX - width / 2, y, TEXT_COLOR, false);
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
}
