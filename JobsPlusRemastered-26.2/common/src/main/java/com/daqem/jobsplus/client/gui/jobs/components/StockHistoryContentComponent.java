package com.daqem.jobsplus.client.gui.jobs.components;

import com.daqem.jobsplus.client.gui.theme.JobsTheme;
import com.daqem.jobsplus.client.gui.theme.StockIcons;
import net.minecraft.network.chat.Component;
import com.daqem.jobsplus.client.gui.jobs.JobsScreenState;
import com.daqem.jobsplus.stock.StockCatalog;
import com.daqem.jobsplus.player.stock.StockTransaction;
import com.daqem.uilib.gui.component.EmptyComponent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;

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
    private static final int ROW_HEIGHT = 28;
    private static final int TEXT_COLOR = JobsTheme.TEXT;
    private static final int GRID_COLOR = JobsTheme.DIVIDER;
    private static final DateTimeFormatter TIME_FORMAT =
            DateTimeFormatter.ofPattern("MM-dd HH:mm").withZone(ZoneId.systemDefault());

    private final JobsScreenState state;

    public StockHistoryContentComponent(JobsScreenState state, int width)
    {
        super(0, 0, width, HEADER_HEIGHT
                + Math.max(1, Math.min(MAX_TRANSACTIONS, state.getStockAccount().transactions().size()))
                * ROW_HEIGHT);
        this.state = state;
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick, int parentWidth,
                       int parentHeight)
    {
        int x = getTotalX();
        int y = getTotalY();
        int right = x + getWidth();

        JobsTheme.texture(guiGraphics, JobsTheme.Skin.HEADER, x, y, getWidth(), HEADER_HEIGHT - 3);
        JobsTheme.text(guiGraphics, Component.literal("최근 거래내역"), x + 6, y + 5, getWidth() - 12, JobsTheme.CYAN);

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
            JobsTheme.texture(guiGraphics, JobsTheme.Skin.INSET, x, rowY, getWidth(), ROW_HEIGHT - 3);
            String iconId = transaction.stockId().isEmpty() ? "BTC" : transaction.stockId();
            StockIcons.draw(guiGraphics, iconId, x + 4, rowY + 4, 16);
            String title = getTransactionName(transaction.type()) + " · " + getStockName(transaction.stockId());
            JobsTheme.text(guiGraphics, Component.literal(title), x + 24, rowY + 4,
                    getWidth() * 60 / 100 - 26, TEXT_COLOR);
            drawScaled(guiGraphics, TIME_FORMAT.format(Instant.ofEpochMilli(transaction.timestamp())),
                    x + 24, rowY + 16, JobsTheme.MUTED);
            drawScaledRight(guiGraphics, String.format(Locale.ROOT, "%.2f개", transaction.amount()),
                    right - 5, rowY + 4, TEXT_COLOR);
            drawScaledRight(guiGraphics, formatReturnRate(transaction), right - 5, rowY + 16,
                    getReturnRateColor(transaction));
            guiGraphics.fill(x, rowY + ROW_HEIGHT - 1, right, rowY + ROW_HEIGHT, GRID_COLOR);
        }
    }

    private static String formatReturnRate(StockTransaction transaction)
    {
        if (!transaction.returnRateRecorded())
        {
            return "-";
        }
        return String.format(Locale.ROOT, "%+.2f%%", transaction.returnRate());
    }

    private static int getReturnRateColor(StockTransaction transaction)
    {
        if (!transaction.returnRateRecorded())
        {
            return TEXT_COLOR;
        }
        if (transaction.returnRate() > 0)
        {
            return JobsTheme.SUCCESS;
        }
        if (transaction.returnRate() < 0)
        {
            return JobsTheme.ERROR;
        }
        return TEXT_COLOR;
    }

    private static String getStockName(String stockId)
    {
        if (stockId.isEmpty())
        {
            return "-";
        }
        return StockCatalog.getStockName(stockId);
    }

    private static String getTransactionName(String type)
    {
        return switch (type)
        {
            case "DEPOSIT" -> "입금";
            case "WITHDRAW" -> "출금";
            case "BUY" -> "체결";
            case "SELL" -> "판매";
            case "LIQUIDATION" -> "청산";
            default -> type;
        };
    }

    private static void drawCentered(GuiGraphicsExtractor guiGraphics, String text, int centerX, int y)
    {
        int width = Minecraft.getInstance().font.width(text);
        guiGraphics.text(Minecraft.getInstance().font, text, centerX - width / 2, y, TEXT_COLOR, false);
    }

    private static void drawScaled(GuiGraphicsExtractor guiGraphics, String text, int x, int y, int color)
    {
        guiGraphics.pose().pushMatrix();
        guiGraphics.pose().translate(x, y);
        guiGraphics.pose().scale(0.50f, 0.50f);
        guiGraphics.text(Minecraft.getInstance().font, text, 0, 0, color, false);
        guiGraphics.pose().popMatrix();
    }

    private static void drawScaledRight(GuiGraphicsExtractor guiGraphics, String text, int right, int y, int color)
    {
        int width = (int) Math.ceil(Minecraft.getInstance().font.width(text) * 0.50f);
        drawScaled(guiGraphics, text, right - width, y, color);
    }
}
