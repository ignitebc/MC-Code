package com.daqem.jobsplus.client.gui.jobs.components;

import com.daqem.jobsplus.JobsPlus;
import com.daqem.jobsplus.client.gui.confimation.ConfirmationScreen;
import com.daqem.jobsplus.client.gui.confimation.ConfirmationScreenState;
import com.daqem.jobsplus.client.gui.jobs.JobsScreenState;
import com.daqem.jobsplus.client.gui.jobs.stock.StockMarketService;
import com.daqem.jobsplus.client.gui.jobs.stock.StockPanelMode;
import com.daqem.jobsplus.client.gui.jobs.stock.StockQuote;
import com.daqem.jobsplus.client.gui.jobs.widgets.StockHistoryScrollWidget;
import com.daqem.jobsplus.client.gui.jobs.widgets.StockHoldingsScrollWidget;
import com.daqem.jobsplus.networking.c2s.ServerboundStockActionPacket;
import com.daqem.jobsplus.player.stock.StockAccount;
import com.daqem.jobsplus.player.stock.StockDecimal;
import com.daqem.jobsplus.player.stock.StockPosition;
import com.daqem.uilib.gui.component.EmptyComponent;
import com.daqem.uilib.gui.widget.CustomButtonWidget;
import com.daqem.uilib.gui.widget.EditBoxWidget;
import dev.architectury.networking.NetworkManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ARGB;

import java.text.NumberFormat;
import java.util.Locale;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;

public class StockTradingComponent extends EmptyComponent
{
    private static final int CONTENT_WIDTH = 146;
    private static final int TEXT_COLOR = 0xFF1E1410;
    private static final int BORDER_COLOR = 0xFFD8BF96;
    private static final NumberFormat NUMBER_FORMAT = NumberFormat.getNumberInstance(Locale.KOREA);
    private final JobsScreenState state;
    private final StockMarketService stockMarketService;
    private final EditBoxWidget buyAmountInput;
    private final EditBoxWidget sellAmountInput;
    private final EditBoxWidget transferAmountInput;
    private final StockHoldingsScrollWidget buyHoldingsScrollWidget;
    private final StockHoldingsScrollWidget sellHoldingsScrollWidget;
    private final StockHistoryScrollWidget historyScrollWidget;
    private final StyledButton sellButton;
    private final List<StyledButton> styledButtons = new ArrayList<>();

    public StockTradingComponent(JobsScreenState state)
    {
        super(208, 38, 156, 173);
        this.state = state;
        this.stockMarketService = StockMarketService.getInstance();

        int modeButtonX = 0;
        for (StockPanelMode mode : StockPanelMode.values())
        {
            int modeButtonWidth = Minecraft.getInstance().font.width(mode.getName()) + 6;
            this.addStyledButton(new StyledButton(
                    modeButtonX,
                    0,
                    modeButtonWidth,
                    16,
                    Component.literal(mode.getName()),
                    () -> true,
                    () -> {
                        this.state.setStockPanelMode(mode);
                        if (mode == StockPanelMode.SELL)
                        {
                            this.state.setSelectedHoldingStockId(null);
                        }
                    },
                    () -> this.state.getStockPanelMode() == mode
            ));
            modeButtonX += modeButtonWidth + 2;
        }

        this.buyAmountInput = createAmountInput(12, 96, 1);
        this.sellAmountInput = createAmountInput(12, 149, 1);
        this.transferAmountInput = createAmountInput(12, 96, 10);
        this.addWidget(this.buyAmountInput);
        this.addWidget(this.sellAmountInput);
        this.addWidget(this.transferAmountInput);

        this.addStyledButton(new StyledButton(76, 96, 17, 16, Component.literal("-"),
                () -> this.state.getStockPanelMode() == StockPanelMode.BUY,
                () -> changeAmount(this.buyAmountInput, -1), () -> false));
        this.addStyledButton(new StyledButton(98, 96, 17, 16, Component.literal("+"),
                () -> this.state.getStockPanelMode() == StockPanelMode.BUY,
                () -> changeAmount(this.buyAmountInput, 1), () -> false));

        this.addStyledButton(new StyledButton(124, 96, 27, 16, Component.literal("구매"),
                () -> this.state.getStockPanelMode() == StockPanelMode.BUY,
                this::sendBuyAction, () -> false));

        this.addStyledButton(new StyledButton(76, 149, 17, 16, Component.literal("-"),
                () -> this.state.getStockPanelMode() == StockPanelMode.SELL,
                () -> changeAmount(this.sellAmountInput, -1), () -> false));
        this.addStyledButton(new StyledButton(98, 149, 17, 16, Component.literal("+"),
                () -> this.state.getStockPanelMode() == StockPanelMode.SELL,
                () -> changeAmount(this.sellAmountInput, 1), () -> false));
        this.sellButton = new StyledButton(124, 149, 27, 16, Component.literal("판매"),
                () -> this.state.getStockPanelMode() == StockPanelMode.SELL,
                this::sendSelectedHoldingSellAction, () -> false);
        this.addStyledButton(this.sellButton);

        this.addStyledButton(new StyledButton(76, 96, 17, 16, Component.literal("-"),
                () -> this.state.getStockPanelMode() == StockPanelMode.TRANSFER,
                () -> changeAmount(this.transferAmountInput, -10), () -> false));
        this.addStyledButton(new StyledButton(98, 96, 17, 16, Component.literal("+"),
                () -> this.state.getStockPanelMode() == StockPanelMode.TRANSFER,
                () -> changeAmount(this.transferAmountInput, 10), () -> false));
        this.addStyledButton(new StyledButton(10, 122, 65, 18, Component.literal("입금"),
                () -> this.state.getStockPanelMode() == StockPanelMode.TRANSFER,
                () -> showTransferConfirmation(ServerboundStockActionPacket.Action.DEPOSIT),
                () -> false));
        this.addStyledButton(new StyledButton(81, 122, 65, 18, Component.literal("출금"),
                () -> this.state.getStockPanelMode() == StockPanelMode.TRANSFER,
                () -> showTransferConfirmation(ServerboundStockActionPacket.Action.WITHDRAW),
                () -> false));

        EmptyComponent buyHoldingsComponent = new EmptyComponent(5, 119, CONTENT_WIDTH, 49);
        this.buyHoldingsScrollWidget = createHoldingsScrollWidget();
        buyHoldingsComponent.addWidget(this.buyHoldingsScrollWidget);
        this.addComponent(buyHoldingsComponent);

        EmptyComponent sellHoldingsComponent = new EmptyComponent(5, 52, CONTENT_WIDTH, 49);
        this.sellHoldingsScrollWidget = createHoldingsScrollWidget();
        sellHoldingsComponent.addWidget(this.sellHoldingsScrollWidget);
        this.addComponent(sellHoldingsComponent);

        EmptyComponent historyComponent = new EmptyComponent(5, 22, CONTENT_WIDTH, 146);
        this.historyScrollWidget = new StockHistoryScrollWidget(CONTENT_WIDTH, 146, this.state);
        historyComponent.addWidget(this.historyScrollWidget);
        this.addComponent(historyComponent);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick, int parentWidth,
                       int parentHeight)
    {
        this.stockMarketService.refreshIfNeeded();
        this.styledButtons.forEach(StyledButton::updateVisibility);
        StockPanelMode panelMode = this.state.getStockPanelMode();
        this.buyAmountInput.visible = panelMode == StockPanelMode.BUY;
        this.sellAmountInput.visible = panelMode == StockPanelMode.SELL;
        this.transferAmountInput.visible = panelMode == StockPanelMode.TRANSFER;
        this.buyHoldingsScrollWidget.visible = panelMode == StockPanelMode.BUY;
        this.sellHoldingsScrollWidget.visible = panelMode == StockPanelMode.SELL;
        this.historyScrollWidget.visible = panelMode == StockPanelMode.HISTORY;

        String selectedHoldingStockId = this.state.getSelectedHoldingStockId();
        if (selectedHoldingStockId != null
                && this.state.getStockAccount().getPosition(selectedHoldingStockId) == null)
        {
            this.state.setSelectedHoldingStockId(null);
            selectedHoldingStockId = null;
        }
        this.sellButton.active = selectedHoldingStockId != null;

        int x = getTotalX();
        int y = getTotalY();
        guiGraphics.fill(x, y + 19, x + getWidth(), y + 20, BORDER_COLOR);
        guiGraphics.fill(x, y + 19, x + 1, y + getHeight(), BORDER_COLOR);
        guiGraphics.fill(x + getWidth() - 1, y + 19, x + getWidth(), y + getHeight(), BORDER_COLOR);
        guiGraphics.fill(x, y + getHeight() - 1, x + getWidth(), y + getHeight(), BORDER_COLOR);

        if (panelMode == StockPanelMode.HISTORY)
        {
            drawBox(guiGraphics, x + 5, y + 22, CONTENT_WIDTH, 146);
            return;
        }

        String displayedStockId = panelMode == StockPanelMode.SELL
                ? selectedHoldingStockId
                : this.state.getSelectedStockId();
        StockQuote selectedQuote = displayedStockId == null
                ? null
                : this.stockMarketService.getQuote(displayedStockId);
        String selectedName = displayedStockId == null
                ? "선택 필요"
                : selectedQuote == null ? displayedStockId : selectedQuote.name();
        drawBox(guiGraphics, x + 5, y + 22, CONTENT_WIDTH, 26);
        drawCenteredScaled(guiGraphics, "보유 자산: " + formatAmount(this.state.getStockAccount().balance()),
                x + getWidth() / 2, y + 26);

        drawCenteredScaled(guiGraphics, "내 주식 평가: " + formatAmount(getTotalStockValue()),
                x + getWidth() / 2, y + 37);

        if (panelMode == StockPanelMode.TRANSFER)
        {
            drawBox(guiGraphics, x + 5, y + 82, CONTENT_WIDTH, 34);
            drawCentered(guiGraphics, "입출금 수량 (10개 단위)", x + getWidth() / 2, y + 85);
            return;
        }

        String currentPrice = selectedQuote != null && selectedQuote.available()
                ? "현재가격: " + NUMBER_FORMAT.format(Math.round(selectedQuote.priceKrw()))
                : "현재가격: 조회 중";

        if (panelMode == StockPanelMode.SELL)
        {
            drawBox(guiGraphics, x + 5, y + 52, CONTENT_WIDTH, 49);
            drawBox(guiGraphics, x + 5, y + 104, CONTENT_WIDTH, 27);
            drawCenteredScaled(guiGraphics, "판매 종목: " + selectedName, x + getWidth() / 2, y + 108);
            drawCenteredScaled(guiGraphics, currentPrice, x + getWidth() / 2, y + 119);
            drawBox(guiGraphics, x + 5, y + 134, CONTENT_WIDTH, 34);
            drawCentered(guiGraphics, "판매할 수량 (1개 단위)", x + getWidth() / 2, y + 137);
            return;
        }

        drawBox(guiGraphics, x + 5, y + 52, CONTENT_WIDTH, 27);
        drawCenteredScaled(guiGraphics, "선택 종목: " + selectedName, x + getWidth() / 2, y + 56);
        drawCenteredScaled(guiGraphics, currentPrice, x + getWidth() / 2, y + 67);
        drawBox(guiGraphics, x + 5, y + 82, CONTENT_WIDTH, 34);
        drawCentered(guiGraphics, "구매할 수량 (1개 단위)", x + getWidth() / 2, y + 85);
        drawBox(guiGraphics, x + 5, y + 119, CONTENT_WIDTH, 49);
    }

    private double getTotalStockValue()
    {
        StockAccount account = this.state.getStockAccount();
        double total = 0;
        for (StockPosition position : account.positions())
        {
            StockQuote quote = this.stockMarketService.getQuote(position.stockId());
            if (quote != null && quote.available())
            {
                total += position.getCurrentValue(quote.priceKrw());
            }
        }
        return total;
    }

    private String getStockName(String stockId)
    {
        StockQuote quote = this.stockMarketService.getQuote(stockId);
        return quote == null ? stockId : quote.name();
    }

    private EditBoxWidget createAmountInput(int x, int y, int defaultValue)
    {
        EditBoxWidget amountInput = new EditBoxWidget(
                Minecraft.getInstance().font, x, y, 54, 16, Component.literal("수량"));
        amountInput.setValue(Integer.toString(defaultValue));
        amountInput.setMaxLength(8);
        amountInput.setFilter(value -> value.isEmpty() || value.chars().allMatch(Character::isDigit));
        return amountInput;
    }

    private StockHoldingsScrollWidget createHoldingsScrollWidget()
    {
        StockHoldingsScrollWidget holdingsScrollWidget =
                new StockHoldingsScrollWidget(CONTENT_WIDTH, 49, this.state);
        return holdingsScrollWidget;
    }

    private void addStyledButton(StyledButton button)
    {
        this.styledButtons.add(button);
        this.addWidget(button);
    }

    private void changeAmount(EditBoxWidget amountInput, int delta)
    {
        int amount = getAmount(amountInput);
        amountInput.setValue(Integer.toString(Math.max(0, amount + delta)));
    }

    private int getAmount(EditBoxWidget amountInput)
    {
        try
        {
            return Math.max(0, Integer.parseInt(amountInput.getValue()));
        }
        catch (NumberFormatException ignored)
        {
            return 10;
        }
    }

    private void sendSelectedHoldingSellAction()
    {
        String selectedHoldingStockId = this.state.getSelectedHoldingStockId();
        if (selectedHoldingStockId == null
                || this.state.getStockAccount().getPosition(selectedHoldingStockId) == null)
        {
            return;
        }

        int amount = getAmount(this.sellAmountInput);
        if (amount <= 0)
        {
            showAlert("판매할 수량을 1개 이상 입력해 주세요.");
            return;
        }
        String stockName = getStockName(selectedHoldingStockId);
        Minecraft minecraft = Minecraft.getInstance();
        minecraft.setScreen(new ConfirmationScreen(
                minecraft.screen,
                new ConfirmationScreenState(
                        Component.literal(stockName + "를 " + amount + "개 판매 하시겠습니까?"),
                        Component.literal("판매"),
                        Component.literal("취소"),
                        () -> {
                            if (minecraft.screen instanceof ConfirmationScreen confirmationScreen)
                            {
                                minecraft.setScreen(confirmationScreen.getPreviousScreen());
                            }
                            NetworkManager.sendToServer(new ServerboundStockActionPacket(
                                    ServerboundStockActionPacket.Action.SELL, selectedHoldingStockId, amount));
                        }
                )
        ));
    }

    private void sendBuyAction()
    {
        int amount = getAmount(this.buyAmountInput);
        if (amount <= 0)
        {
            showAlert("구매할 수량을 1개 이상 입력해 주세요.");
            return;
        }
        if (this.state.getStockAccount().balance() + 0.00000001 < amount)
        {
            showAlert("충전된 비트코인이 부족하여 구매할 수 없습니다.\n입출금 탭에서 비트코인을 충전해 주세요.");
            return;
        }

        String selectedStockId = this.state.getSelectedStockId();
        String stockName = getStockName(selectedStockId);
        Minecraft minecraft = Minecraft.getInstance();
        minecraft.setScreen(new ConfirmationScreen(
                minecraft.screen,
                new ConfirmationScreenState(
                        Component.literal(stockName + "를 " + amount + "개 구매 하시겠습니까?"),
                        Component.literal("구매"),
                        Component.literal("취소"),
                        () -> {
                            if (minecraft.screen instanceof ConfirmationScreen confirmationScreen)
                            {
                                minecraft.setScreen(confirmationScreen.getPreviousScreen());
                            }
                            NetworkManager.sendToServer(new ServerboundStockActionPacket(
                                    ServerboundStockActionPacket.Action.BUY, selectedStockId, amount));
                        }
                )
        ));
    }

    private void showAlert(String message)
    {
        Minecraft minecraft = Minecraft.getInstance();
        minecraft.setScreen(new ConfirmationScreen(
                minecraft.screen,
                ConfirmationScreenState.alert(Component.literal(message))
        ));
    }

    private void showTransferConfirmation(ServerboundStockActionPacket.Action action)
    {
        int amount = getAmount(this.transferAmountInput);
        String actionName = action == ServerboundStockActionPacket.Action.DEPOSIT ? "입금" : "출금";
        String confirmationMessage = action == ServerboundStockActionPacket.Action.WITHDRAW
                ? "출금하시겠습니까?\n(※ 0.2% 소득세 차감)"
                : "입금하시겠습니까?";
        Minecraft minecraft = Minecraft.getInstance();
        minecraft.setScreen(new ConfirmationScreen(
                minecraft.screen,
                new ConfirmationScreenState(
                        Component.literal(confirmationMessage),
                        Component.literal(actionName),
                        Component.literal("취소"),
                        () -> {
                            if (minecraft.screen instanceof ConfirmationScreen confirmationScreen)
                            {
                                minecraft.setScreen(confirmationScreen.getPreviousScreen());
                            }
                            NetworkManager.sendToServer(new ServerboundStockActionPacket(action, "", amount));
                        }
                )
        ));
    }

    private static String formatAmount(double amount)
    {
        return String.format(Locale.ROOT, "%.8f개", StockDecimal.truncate(amount));
    }

    private static void drawCentered(GuiGraphics guiGraphics, String text, int centerX, int y)
    {
        int textWidth = Minecraft.getInstance().font.width(text);
        guiGraphics.drawString(Minecraft.getInstance().font, text, centerX - textWidth / 2, y, TEXT_COLOR, false);
    }

    private static void drawCenteredScaled(GuiGraphics guiGraphics, String text, int centerX, int y)
    {
        float scale = 0.50f;
        int textWidth = Minecraft.getInstance().font.width(text);
        guiGraphics.pose().pushMatrix();
        guiGraphics.pose().translate(centerX - textWidth * scale / 2, y);
        guiGraphics.pose().scale(scale, scale);
        guiGraphics.drawString(Minecraft.getInstance().font, text, 0, 0, TEXT_COLOR, false);
        guiGraphics.pose().popMatrix();
    }

    private static void drawBox(GuiGraphics guiGraphics, int x, int y, int width, int height)
    {
        guiGraphics.fill(x, y, x + width, y + 1, BORDER_COLOR);
        guiGraphics.fill(x, y + height - 1, x + width, y + height, BORDER_COLOR);
        guiGraphics.fill(x, y, x + 1, y + height, BORDER_COLOR);
        guiGraphics.fill(x + width - 1, y, x + width, y + height, BORDER_COLOR);
    }

    private static class StyledButton extends CustomButtonWidget
    {
        private final BooleanSupplier visibleSupplier;
        private final BooleanSupplier selectedSupplier;

        public StyledButton(int x, int y, int width, int height, Component message,
                            BooleanSupplier visibleSupplier, Runnable onPress, BooleanSupplier selectedSupplier)
        {
            super(x, y, width, height, message, null, button -> onPress.run());
            this.visibleSupplier = visibleSupplier;
            this.selectedSupplier = selectedSupplier;
        }

        @Override
        protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick)
        {
            guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, JobsPlus.getId("jobs/tab_bottom"),
                    getX(), getY(), getWidth(), getHeight(), ARGB.white(this.alpha));
            int color = !this.active
                    ? 0xFF8C8178
                    : this.selectedSupplier.getAsBoolean() || isHoveredOrFocused() ? 0xFFC62828 : TEXT_COLOR;
            int textX = getX() + (getWidth() - Minecraft.getInstance().font.width(getMessage())) / 2;
            guiGraphics.drawString(Minecraft.getInstance().font, getMessage(), textX, getY() + 5, color, false);
        }

        public void updateVisibility()
        {
            this.visible = this.visibleSupplier.getAsBoolean();
        }
    }
}
