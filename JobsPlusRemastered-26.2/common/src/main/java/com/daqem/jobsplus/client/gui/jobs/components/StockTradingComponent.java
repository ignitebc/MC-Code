package com.daqem.jobsplus.client.gui.jobs.components;

import com.daqem.jobsplus.client.gui.theme.JobsTheme;
import com.daqem.jobsplus.client.gui.theme.StockIcons;
import com.daqem.jobsplus.client.gui.theme.JobsEditBox;
import com.daqem.jobsplus.client.gui.confimation.ConfirmationScreen;
import com.daqem.jobsplus.client.gui.confimation.ConfirmationScreenState;
import com.daqem.jobsplus.client.gui.jobs.JobsScreenState;
import com.daqem.jobsplus.client.gui.jobs.stock.StockPanelMode;
import com.daqem.jobsplus.client.stock.ClientStockMarket;
import com.daqem.jobsplus.stock.SnapshotStatus;
import com.daqem.jobsplus.stock.StockCatalog;
import com.daqem.jobsplus.stock.StockMarketSnapshot;
import com.daqem.jobsplus.stock.StockQuote;
import com.daqem.jobsplus.client.gui.jobs.widgets.StockHistoryScrollWidget;
import com.daqem.jobsplus.client.gui.jobs.widgets.StockHoldingsScrollWidget;
import com.daqem.jobsplus.networking.c2s.ServerboundStockActionPacket;
import com.daqem.jobsplus.player.stock.StockAccount;
import com.daqem.jobsplus.player.stock.StockDecimal;
import com.daqem.jobsplus.player.stock.StockPosition;
import com.daqem.jobsplus.player.stock.StockPositionSide;
import com.daqem.uilib.gui.component.EmptyComponent;
import com.daqem.uilib.gui.widget.CustomButtonWidget;
import com.daqem.uilib.gui.widget.EditBoxWidget;
import dev.architectury.networking.NetworkManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;

import java.text.NumberFormat;
import java.util.Locale;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

public class StockTradingComponent extends EmptyComponent
{
    private final int contentWidth;
    private final boolean expanded;
    private static final int TEXT_COLOR = JobsTheme.TEXT;
    private static final int BORDER_COLOR = JobsTheme.DIVIDER;
    private static final NumberFormat NUMBER_FORMAT = NumberFormat.getNumberInstance(Locale.KOREA);
    private final JobsScreenState state;
    private final EditBoxWidget buyAmountInput;
    private final EditBoxWidget sellAmountInput;
    private final EditBoxWidget transferAmountInput;
    private final StockHoldingsScrollWidget buyHoldingsScrollWidget;
    private final StockHoldingsScrollWidget sellHoldingsScrollWidget;
    private final StockHistoryScrollWidget historyScrollWidget;
    private final StyledButton sellButton;
    private final StyledButton leverageButton;
    private final List<StyledButton> styledButtons = new ArrayList<>();
    private boolean leverageDropdownOpen;

    public StockTradingComponent(JobsScreenState state, int width, int height)
    {
        super(0, 0, width, height);
        this.contentWidth = width - 10;
        this.expanded = width >= 156 && height >= 190;
        this.state = state;

        int modeButtonX = 0;
        for (StockPanelMode mode : StockPanelMode.values())
        {
            int modeButtonWidth = (getWidth() - 6) / StockPanelMode.values().length;
            this.addStyledButton(new StyledButton(
                    modeButtonX,
                    0,
                    modeButtonWidth,
                    16,
                    Component.literal(mode.getName()),
                    () -> true,
                    () -> {
                        this.state.setStockPanelMode(mode);
                        this.leverageDropdownOpen = false;
                        if (mode == StockPanelMode.SELL)
                        {
                            this.state.setSelectedHoldingStockId(null);
                        }
                    },
                    () -> this.state.getStockPanelMode() == mode
            ));
            modeButtonX += modeButtonWidth + 2;
        }

        this.buyAmountInput = createAmountInput(column(12), expanded ? 93 : 112, 1);
        this.sellAmountInput = createAmountInput(column(12), 149, 1);
        this.transferAmountInput = createAmountInput(column(12), 96, 10);
        this.addWidget(this.buyAmountInput);
        this.addWidget(this.sellAmountInput);
        this.addWidget(this.transferAmountInput);

        this.addStyledButton(new StyledButton(column(6), expanded ? 66 : 79, column(30), JobsTheme.BUTTON_HEIGHT, Component.literal("롱"),
                () -> this.state.getStockPanelMode() == StockPanelMode.BUY,
                () -> this.selectPositionSide(StockPositionSide.LONG),
                () -> this.state.getSelectedStockPositionSide() == StockPositionSide.LONG));
        this.addStyledButton(new StyledButton(column(39), expanded ? 66 : 79, column(30), JobsTheme.BUTTON_HEIGHT, Component.literal("숏"),
                () -> this.state.getStockPanelMode() == StockPanelMode.BUY,
                () -> this.selectPositionSide(StockPositionSide.SHORT),
                () -> this.state.getSelectedStockPositionSide() == StockPositionSide.SHORT));
        this.leverageButton = new StyledButton(column(72), expanded ? 66 : 79, column(79), 16, Component.empty(),
                () -> this.state.getStockPanelMode() == StockPanelMode.BUY,
                () -> this.leverageDropdownOpen = !this.leverageDropdownOpen,
                () -> this.leverageDropdownOpen);
        this.addStyledButton(this.leverageButton);

        this.addStyledButton(new StyledButton(column(76), expanded ? 93 : 112, column(17), JobsTheme.BUTTON_HEIGHT, Component.literal("-"),
                () -> this.state.getStockPanelMode() == StockPanelMode.BUY && !this.leverageDropdownOpen,
                () -> changeAmount(this.buyAmountInput, -1), () -> false));
        this.addStyledButton(new StyledButton(column(98), expanded ? 93 : 112, column(17), JobsTheme.BUTTON_HEIGHT, Component.literal("+"),
                () -> this.state.getStockPanelMode() == StockPanelMode.BUY && !this.leverageDropdownOpen,
                () -> changeAmount(this.buyAmountInput, 1), () -> false));

        this.addStyledButton(new StyledButton(expanded ? 6 : column(124), expanded ? 120 : 112, expanded ? getWidth() - 12 : column(27), JobsTheme.BUTTON_HEIGHT, Component.literal("예약"),
                () -> this.state.getStockPanelMode() == StockPanelMode.BUY && !this.leverageDropdownOpen,
                this::sendBuyAction, () -> false));

        for (int optionIndex = 0; optionIndex < StockPosition.ALLOWED_LEVERAGES.size(); optionIndex++)
        {
            int selectedLeverage = StockPosition.ALLOWED_LEVERAGES.get(optionIndex);
            int optionColumn = optionIndex % 2;
            int optionRow = optionIndex / 2;
            int optionX = column(72 + optionColumn * 40);
            int optionY = (expanded ? 83 : 96) + optionRow * 17;
            this.addStyledButton(new StyledButton(
                    optionX,
                    optionY,
                    column(39),
                    16,
                    Component.literal(getLeverageOptionName(selectedLeverage)),
                    () -> this.state.getStockPanelMode() == StockPanelMode.BUY && this.leverageDropdownOpen,
                    () -> this.selectLeverage(selectedLeverage),
                    () -> this.state.getSelectedStockLeverage() == selectedLeverage
            ));
        }

        this.addStyledButton(new StyledButton(column(76), 149, column(17), JobsTheme.BUTTON_HEIGHT, Component.literal("-"),
                () -> this.state.getStockPanelMode() == StockPanelMode.SELL,
                () -> changeAmount(this.sellAmountInput, -1), () -> false));
        this.addStyledButton(new StyledButton(column(98), 149, column(17), JobsTheme.BUTTON_HEIGHT, Component.literal("+"),
                () -> this.state.getStockPanelMode() == StockPanelMode.SELL,
                () -> changeAmount(this.sellAmountInput, 1), () -> false));
        this.sellButton = new StyledButton(column(124), 149, column(27), JobsTheme.BUTTON_HEIGHT, Component.literal("판매"),
                () -> this.state.getStockPanelMode() == StockPanelMode.SELL,
                this::sendSelectedHoldingSellAction, () -> false);
        this.addStyledButton(this.sellButton);

        this.addStyledButton(new StyledButton(column(76), 96, column(17), JobsTheme.BUTTON_HEIGHT, Component.literal("-"),
                () -> this.state.getStockPanelMode() == StockPanelMode.TRANSFER,
                () -> changeAmount(this.transferAmountInput, -10), () -> false));
        this.addStyledButton(new StyledButton(column(98), 96, column(17), JobsTheme.BUTTON_HEIGHT, Component.literal("+"),
                () -> this.state.getStockPanelMode() == StockPanelMode.TRANSFER,
                () -> changeAmount(this.transferAmountInput, 10), () -> false));
        this.addStyledButton(new StyledButton(column(10), 122, column(65), JobsTheme.BUTTON_HEIGHT, Component.literal("입금"),
                () -> this.state.getStockPanelMode() == StockPanelMode.TRANSFER,
                () -> showTransferConfirmation(ServerboundStockActionPacket.Action.DEPOSIT),
                () -> false));
        this.addStyledButton(new StyledButton(column(81), 122, column(65), JobsTheme.BUTTON_HEIGHT, Component.literal("출금"),
                () -> this.state.getStockPanelMode() == StockPanelMode.TRANSFER,
                () -> showTransferConfirmation(ServerboundStockActionPacket.Action.WITHDRAW),
                () -> false));

        EmptyComponent buyHoldingsComponent = new EmptyComponent(5, expanded ? 158 : 135, contentWidth, expanded ? getHeight() - 163 : 33);
        this.buyHoldingsScrollWidget = createHoldingsScrollWidget(expanded ? getHeight() - 163 : 33);
        buyHoldingsComponent.addWidget(this.buyHoldingsScrollWidget);
        this.addComponent(buyHoldingsComponent);

        EmptyComponent sellHoldingsComponent = new EmptyComponent(5, 52, contentWidth, 49);
        this.sellHoldingsScrollWidget = createHoldingsScrollWidget(49);
        sellHoldingsComponent.addWidget(this.sellHoldingsScrollWidget);
        this.addComponent(sellHoldingsComponent);

        EmptyComponent historyComponent = new EmptyComponent(5, 22, contentWidth, getHeight() - 27);
        this.historyScrollWidget = new StockHistoryScrollWidget(contentWidth, getHeight() - 27, this.state);
        historyComponent.addWidget(this.historyScrollWidget);
        this.addComponent(historyComponent);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick, int parentWidth,
                       int parentHeight)
    {
        StockPanelMode panelMode = this.state.getStockPanelMode();
        if (panelMode != StockPanelMode.BUY)
        {
            this.leverageDropdownOpen = false;
        }
        this.styledButtons.forEach(StyledButton::updateVisibility);
        this.leverageButton.setMessage(Component.literal(
                "배율 " + getLeverageOptionName(this.state.getSelectedStockLeverage()) + " ▼"
        ));
        this.buyAmountInput.visible = panelMode == StockPanelMode.BUY && !this.leverageDropdownOpen;
        this.sellAmountInput.visible = panelMode == StockPanelMode.SELL;
        this.transferAmountInput.visible = panelMode == StockPanelMode.TRANSFER;
        this.buyHoldingsScrollWidget.visible =
                panelMode == StockPanelMode.BUY && !this.leverageDropdownOpen;
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
        JobsTheme.panel(guiGraphics, x, y + 19, getWidth(), getHeight() - 19);

        if (panelMode == StockPanelMode.HISTORY)
        {
            drawBox(guiGraphics, x + 5, y + 22, contentWidth, getHeight() - 27);
            return;
        }

        StockMarketSnapshot snapshot = ClientStockMarket.getSnapshot();
        String displayedStockId = panelMode == StockPanelMode.SELL
                ? selectedHoldingStockId
                : this.state.getSelectedStockId();
        StockQuote selectedQuote = displayedStockId == null
                ? null
                : snapshot.getQuote(displayedStockId);
        String selectedName = displayedStockId == null
                ? "선택 필요"
                : StockCatalog.getStockName(displayedStockId);
        if (expanded && panelMode == StockPanelMode.BUY) {
            drawExpandedBuy(guiGraphics, x, y, displayedStockId, selectedName, snapshot, selectedQuote);
            return;
        }
        drawBox(guiGraphics, x + 5, y + 22, contentWidth, 26);
        drawCenteredScaled(guiGraphics, "보유 자산: " + formatAmount(this.state.getStockAccount().balance()),
                x + getWidth() / 2, y + 26);

        drawCenteredScaled(guiGraphics, "내 포지션 평가: " + formatTotalStockValue(snapshot),
                x + getWidth() / 2, y + 37);

        if (panelMode == StockPanelMode.TRANSFER)
        {
            drawBox(guiGraphics, x + 5, y + 82, contentWidth, 34);
            drawCenteredScaled(guiGraphics, "입출금 수량 (10개 단위·최대 1,000)", x + getWidth() / 2, y + 85);
            return;
        }

        String priceLabel = "현재가격";
        if (panelMode == StockPanelMode.BUY)
        {
            priceLabel = "예약 확인가";
        }
        String currentPrice = formatPrice(snapshot, selectedQuote, priceLabel);

        if (panelMode == StockPanelMode.SELL)
        {
            drawBox(guiGraphics, x + 5, y + 52, contentWidth, 49);
            drawBox(guiGraphics, x + 5, y + 104, contentWidth, 27);
            drawCenteredScaled(guiGraphics, "판매 종목: " + selectedName, x + getWidth() / 2, y + 108);
            drawCenteredScaled(guiGraphics, currentPrice, x + getWidth() / 2, y + 119);
            drawBox(guiGraphics, x + 5, y + 134, contentWidth, 34);
            // 입력값은 평가금액이 아니라 처분할 투자원금이다.
            drawCenteredScaled(guiGraphics, "판매할 투자원금 (1개 단위·최대 1,000)", x + getWidth() / 2, y + 137);
            return;
        }

        drawBox(guiGraphics, x + 5, y + 52, contentWidth, 24);
        drawCenteredScaled(guiGraphics, "선택 종목: " + selectedName, x + getWidth() / 2, y + 56);
        drawCenteredScaled(guiGraphics, currentPrice, x + getWidth() / 2, y + 66);
        drawBox(guiGraphics, x + 5, y + 98, contentWidth, 34);
        drawCentered(guiGraphics, "예약 투자금 (1개 단위·최대 1,000)", x + getWidth() / 2, y + 101);
        drawBox(guiGraphics, x + 5, y + 135, contentWidth, 33);
    }

    /**
     * 표에 보이는 가격 문구. 거래가 막힌 상태는 원인을 구분해서 알린다.
     */
    private static String formatPrice(StockMarketSnapshot snapshot, StockQuote quote, String priceLabel)
    {
        if (snapshot.status() == SnapshotStatus.REFRESHING)
        {
            return priceLabel + ": 갱신 중";
        }
        if (quote == null || !quote.hasValidPrice())
        {
            return priceLabel + ": 조회 실패";
        }
        return priceLabel + ": " + NUMBER_FORMAT.format(Math.round(quote.priceKrw()));
    }

    /**
     * 보유 포지션 평가금액 문구.
     * <p>
     * 시세를 못 받은 상태에서 0으로 표시하면 자산이 사라진 것처럼 보이므로 상태를 그대로 알린다.
     */
    private String formatTotalStockValue(StockMarketSnapshot snapshot)
    {
        StockAccount account = this.state.getStockAccount();
        if (account.positions().isEmpty())
        {
            return formatAmount(0);
        }
        if (snapshot.status() == SnapshotStatus.REFRESHING)
        {
            return "갱신 중";
        }
        if (snapshot.status() == SnapshotStatus.FAILED)
        {
            return "조회 실패";
        }

        double total = 0;
        boolean missingQuote = false;
        for (StockPosition position : account.positions())
        {
            StockQuote quote = snapshot.getQuote(position.stockId());
            if (quote != null && quote.hasValidPrice())
            {
                total += position.getCurrentValue(quote.priceKrw());
                continue;
            }
            missingQuote = true;
        }

        if (missingQuote)
        {
            return formatAmount(total) + " (일부 조회 실패)";
        }
        return formatAmount(total);
    }

    private EditBoxWidget createAmountInput(int x, int y, int defaultValue)
    {
        EditBoxWidget amountInput = new JobsEditBox(
                Minecraft.getInstance().font, x, y, column(54), 16, Component.literal("금액"));
        amountInput.setValue(Integer.toString(defaultValue));
        amountInput.setMaxLength(10);
        amountInput.setResponder(new AmountInputFilter(amountInput));
        return amountInput;
    }

    private static boolean isValidAmountInput(String value)
    {
        if (value.isEmpty())
        {
            return true;
        }
        if (!value.chars().allMatch(Character::isDigit))
        {
            return false;
        }

        try
        {
            return Long.parseLong(value) <= ServerboundStockActionPacket.MAX_TRADE_AMOUNT;
        }
        catch (NumberFormatException ignored)
        {
            return false;
        }
    }

    private StockHoldingsScrollWidget createHoldingsScrollWidget(int height)
    {
        StockHoldingsScrollWidget holdingsScrollWidget =
                new StockHoldingsScrollWidget(contentWidth, height, this.state);
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
        long changedAmount = (long) amount + delta;
        changedAmount = Math.max(0, Math.min(ServerboundStockActionPacket.MAX_TRADE_AMOUNT, changedAmount));
        amountInput.setValue(Long.toString(changedAmount));
    }

    /**
     * 입력칸의 수량. 비어 있거나 숫자가 아니면 0으로 본다.
     * 예전에는 10을 돌려줘서 빈칸인데도 10개 거래 확인창이 떴다.
     */
    private int getAmount(EditBoxWidget amountInput)
    {
        try
        {
            return Math.max(0, Integer.parseInt(amountInput.getValue()));
        }
        catch (NumberFormatException ignored)
        {
            return 0;
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
            showAlert("판매할 투자원금을 1개 이상 입력해 주세요.");
            return;
        }

        // 가격 확인과 번호 추출이 서로 다른 스냅샷을 보지 않도록 한 번만 읽는다.
        StockMarketSnapshot snapshot = ClientStockMarket.getSnapshot();
        if (!isTradable(snapshot, selectedHoldingStockId))
        {
            return;
        }

        String stockName = StockCatalog.getStockName(selectedHoldingStockId);
        // 확인창을 띄운 시점의 가격으로만 체결한다. 그 사이 가격이 갱신되면 서버가 거래를 거절한다.
        long snapshotVersion = snapshot.version();
        Minecraft minecraft = Minecraft.getInstance();
        minecraft.gui.setScreen(new ConfirmationScreen(
                minecraft.gui.screen(),
                new ConfirmationScreenState(
                        Component.literal(stockName + " 투자원금 " + amount + "개를 판매하시겠습니까?"),
                        Component.literal("판매"),
                        Component.literal("취소"),
                        () -> {
                            if (minecraft.gui.screen() instanceof ConfirmationScreen confirmationScreen)
                            {
                                minecraft.gui.setScreen(confirmationScreen.getPreviousScreen());
                            }
                            NetworkManager.sendToServer(new ServerboundStockActionPacket(
                                    ServerboundStockActionPacket.Action.SELL, selectedHoldingStockId, amount,
                                    snapshotVersion));
                        }
                )
        ));
    }

    private void sendBuyAction()
    {
        int amount = getAmount(this.buyAmountInput);
        if (amount <= 0)
        {
            showAlert("투자할 금액을 1개 이상 입력해 주세요.");
            return;
        }
        if (this.state.getStockAccount().balance() + 0.00000001 < amount)
        {
            showAlert("주식 계좌의 비트코인이 부족합니다.\n입출금 메뉴에서 먼저 입금해 주세요.");
            return;
        }

        String selectedStockId = this.state.getSelectedStockId();
        StockMarketSnapshot snapshot = ClientStockMarket.getSnapshot();
        if (!isTradable(snapshot, selectedStockId))
        {
            return;
        }

        String stockName = StockCatalog.getStockName(selectedStockId);
        StockPositionSide positionSide = this.state.getSelectedStockPositionSide();
        int leverage = this.state.getSelectedStockLeverage();
        StockPosition existingPosition = this.state.getStockAccount().getPosition(selectedStockId);
        if (existingPosition != null
                && (existingPosition.side() != positionSide || existingPosition.leverage() != leverage))
        {
            showAlert("같은 종목에는 하나의 포지션만 보유할 수 있습니다.\n"
                    + "기존 포지션을 모두 판매한 후 변경해 주세요.");
            return;
        }

        long snapshotVersion = snapshot.version();
        Minecraft minecraft = Minecraft.getInstance();
        minecraft.gui.setScreen(new ConfirmationScreen(
                minecraft.gui.screen(),
                new ConfirmationScreenState(
                        Component.literal(stockName + " " + positionSide.getDisplayName() + " "
                                + getLeverageOptionName(leverage)
                                + " 구매를 예약하시겠습니까?\n"
                                + "투자금 " + amount + "개는 지금 차감됩니다.\n"
                                + "다음 분 시작가로 진입하며, 거래 기록이 없으면 예약 확인가를 사용합니다.\n"
                                + "예약 후에는 취소할 수 없습니다."),
                        Component.literal("예약"),
                        Component.literal("취소"),
                        () -> {
                            if (minecraft.gui.screen() instanceof ConfirmationScreen confirmationScreen)
                            {
                                minecraft.gui.setScreen(confirmationScreen.getPreviousScreen());
                            }
                            NetworkManager.sendToServer(new ServerboundStockActionPacket(
                                    ServerboundStockActionPacket.Action.BUY, selectedStockId, amount,
                                    snapshotVersion, positionSide, leverage));
                        }
                )
        ));
    }

    private void selectPositionSide(StockPositionSide positionSide)
    {
        this.state.setSelectedStockPositionSide(positionSide);
        this.leverageDropdownOpen = false;
    }

    private void selectLeverage(int leverage)
    {
        this.state.setSelectedStockLeverage(leverage);
        this.leverageDropdownOpen = false;
    }

    private static String getLeverageOptionName(int leverage)
    {
        return StockPosition.getLeverageDisplayName(leverage);
    }

    /**
     * 서버가 거절할 것이 확실한 거래는 확인창을 띄우기 전에 막는다.
     * <p>
     * 최종 판정은 서버가 현재 분을 기준으로 다시 수행한다. 여기서는 플레이어 PC 시간을 쓰지 않고
     * 서버가 보내 준 상태만 본다.
     */
    private boolean isTradable(StockMarketSnapshot snapshot, String stockId)
    {
        if (snapshot.status() == SnapshotStatus.REFRESHING)
        {
            showAlert("최신 시세를 불러오고 있습니다.\n잠시 후 다시 시도해 주세요.");
            return false;
        }
        if (snapshot.status() == SnapshotStatus.FAILED)
        {
            showAlert("시세를 불러오지 못했습니다.\n다음 시세 갱신 후 다시 시도해 주세요.");
            return false;
        }

        StockQuote quote = stockId == null ? null : snapshot.getQuote(stockId);
        if (quote == null || !quote.hasValidPrice())
        {
            String stockName = stockId == null ? "해당 종목" : StockCatalog.getStockName(stockId);
            showAlert(stockName + " 시세를 불러오지 못했습니다.\n다음 시세 갱신 후 다시 시도해 주세요.");
            return false;
        }
        return true;
    }

    private void showAlert(String message)
    {
        Minecraft minecraft = Minecraft.getInstance();
        minecraft.gui.setScreen(new ConfirmationScreen(
                minecraft.gui.screen(),
                ConfirmationScreenState.alert(Component.literal(message))
        ));
    }

    private void showTransferConfirmation(ServerboundStockActionPacket.Action action)
    {
        int amount = getAmount(this.transferAmountInput);
        String actionName = action == ServerboundStockActionPacket.Action.DEPOSIT ? "입금" : "출금";
        // 서버도 같은 조건을 검사하지만, 확인창을 띄우기 전에 알려 주는 편이 덜 헷갈린다.
        if (amount < 10 || amount % 10 != 0)
        {
            showAlert(actionName + "은 10개 단위로만 가능합니다.");
            return;
        }

        String confirmationMessage = action == ServerboundStockActionPacket.Action.WITHDRAW
                ? "출금하시겠습니까?\n(※ 0.2% 소득세 차감)"
                : "입금하시겠습니까?";
        Minecraft minecraft = Minecraft.getInstance();
        minecraft.gui.setScreen(new ConfirmationScreen(
                minecraft.gui.screen(),
                new ConfirmationScreenState(
                        Component.literal(confirmationMessage),
                        Component.literal(actionName),
                        Component.literal("취소"),
                        () -> {
                            if (minecraft.gui.screen() instanceof ConfirmationScreen confirmationScreen)
                            {
                                minecraft.gui.setScreen(confirmationScreen.getPreviousScreen());
                            }
                            // 입출금은 시세와 무관하므로 스냅샷 번호를 사용하지 않는다.
                            NetworkManager.sendToServer(new ServerboundStockActionPacket(action, "", amount, 0));
                        }
                )
        ));
    }

    private static String formatAmount(double amount)
    {
        return String.format(Locale.ROOT, "%.8f개", StockDecimal.truncate(amount));
    }

    private void drawCentered(GuiGraphicsExtractor guiGraphics, String text, int centerX, int y)
    {
        JobsTheme.label(guiGraphics, Component.literal(text), centerX - contentWidth / 2,
                y - 1, contentWidth, 10, TEXT_COLOR);
    }

    private void drawCenteredScaled(GuiGraphicsExtractor guiGraphics, String text, int centerX, int y)
    {
        int textWidth = Minecraft.getInstance().font.width(text);
        float scale = Math.min(0.65f, (contentWidth - 8) / (float) Math.max(1, textWidth));
        guiGraphics.pose().pushMatrix();
        guiGraphics.pose().translate(centerX - textWidth * scale / 2, y);
        guiGraphics.pose().scale(scale, scale);
        guiGraphics.text(Minecraft.getInstance().font, text, 0, 0, TEXT_COLOR, false);
        guiGraphics.pose().popMatrix();
    }

    private static void drawBox(GuiGraphicsExtractor guiGraphics, int x, int y, int width, int height)
    {
        JobsTheme.cutBox(guiGraphics, x, y, width, height, JobsTheme.INSET, BORDER_COLOR);
    }

    private int column(int coordinate) {
        return Math.round(coordinate * getWidth() / 156.0f);
    }

    private void drawExpandedBuy(GuiGraphicsExtractor graphics, int x, int y, String stockId,
                                 String name, StockMarketSnapshot snapshot, StockQuote quote) {
        StockIcons.draw(graphics, stockId, x + 8, y + 25, 20);
        JobsTheme.text(graphics, Component.literal(stockId + " · " + name), x + 34, y + 25,
                getWidth() - 42, JobsTheme.TEXT);
        JobsTheme.text(graphics, Component.literal(formatPrice(snapshot, quote, "현재가(원)")),
                x + 34, y + 38, getWidth() - 42, JobsTheme.MUTED);
        JobsTheme.text(graphics, Component.literal("포지션 평가  " + formatTotalStockValue(snapshot)),
                x + 8, y + 52, getWidth() - 16, JobsTheme.MUTED);
        JobsTheme.text(graphics, Component.literal("투자 원금 · 1개 단위 · 최대 1,000 BTC"),
                x + 8, y + 83, getWidth() - 16, JobsTheme.TEXT);
        if (!this.leverageDropdownOpen) {
            JobsTheme.text(graphics, Component.literal("BTC"), x + column(120), y + 97,
                    getWidth() - column(120) - 6, JobsTheme.MUTED);
            JobsTheme.text(graphics, Component.literal("다음 시세 반영 시 체결"), x + 8, y + 111,
                    getWidth() - 16, JobsTheme.MUTED);
            JobsTheme.text(graphics, Component.literal("보유 포지션"), x + 8, y + 144,
                    getWidth() - 16, JobsTheme.TEXT);
            drawBox(graphics, x + 5, y + 158, contentWidth, getHeight() - 163);
        }
    }

    private static class AmountInputFilter implements Consumer<String>
    {
        private final EditBoxWidget amountInput;
        private String lastValidValue;
        private boolean restoringValue;

        private AmountInputFilter(EditBoxWidget amountInput)
        {
            this.amountInput = amountInput;
            this.lastValidValue = amountInput.getValue();
        }

        @Override
        public void accept(String value)
        {
            if (this.restoringValue)
            {
                return;
            }
            if (isValidAmountInput(value))
            {
                this.lastValidValue = value;
                return;
            }

            this.restoringValue = true;
            this.amountInput.setValue(this.lastValidValue);
            this.restoringValue = false;
        }
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
        protected void extractContents(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick)
        {
            boolean selected = this.selectedSupplier.getAsBoolean();
            boolean modeTab = false;
            for (StockPanelMode mode : StockPanelMode.values()) {
                if (mode.getName().equals(getMessage().getString())) {
                    modeTab = true;
                    break;
                }
            }
            if (modeTab) {
                JobsTheme.tab(guiGraphics, getX(), getY(), getWidth(), getHeight(), isHoveredOrFocused(), selected);
            } else {
                boolean primary = getMessage().getString().equals("예약") || getMessage().getString().equals("입금");
                JobsTheme.button(guiGraphics, getX(), getY(), getWidth(), getHeight(),
                        this.active, isHoveredOrFocused(), selected, primary);
            }
            JobsTheme.label(guiGraphics, getMessage(), getX(), getY(), getWidth(), getHeight(),
                    this.active ? JobsTheme.TEXT : JobsTheme.DISABLED);
        }

        public void updateVisibility()
        {
            this.visible = this.visibleSupplier.getAsBoolean();
        }
    }
}
