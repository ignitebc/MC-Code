package com.daqem.jobsplus.client.gui.jobs.components;

import com.daqem.jobsplus.JobsPlus;
import com.daqem.jobsplus.client.gui.theme.JobsCloseButton;
import com.daqem.jobsplus.client.gui.theme.JobsLayout;
import com.daqem.jobsplus.client.gui.theme.JobsTheme;
import com.daqem.jobsplus.client.gui.theme.StockIcons;
import com.daqem.jobsplus.client.gui.jobs.JobsScreenState;
import com.daqem.jobsplus.client.gui.jobs.tab.RightTab;
import com.daqem.jobsplus.client.gui.jobs.widgets.PowerupsButtonWidget;
import com.daqem.jobsplus.client.gui.jobs.widgets.ShopSellButtonWidget;
import com.daqem.jobsplus.client.stock.ClientStockMarket;
import com.daqem.jobsplus.networking.c2s.ServerboundStockViewStatePacket;
import dev.architectury.networking.NetworkManager;
import com.daqem.uilib.gui.component.AbstractComponent;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

public class JobsComponent extends AbstractComponent
{
    private final JobsScreenState state;
    private final JobSelectionComponent jobSelectionComponent;
    private final SelectedJobComponent selectedJobComponent;
    private final CoinsComponent coinsComponent;
    private final StockTableComponent stockTableComponent;
    private final StockTradingComponent stockTradingComponent;
    private final PowerupsButtonWidget powerupsButtonWidget;
    private final ShopSellButtonWidget shopSellButtonWidget;
    private RightTab cachedRightTab;
    private final JobsLayout layout;

    public JobsComponent(JobsScreenState state)
    {
        this(state, JobsLayout.forScreen(Minecraft.getInstance().getWindow().getGuiScaledWidth(),
                Minecraft.getInstance().getWindow().getGuiScaledHeight()));
    }

    private JobsComponent(JobsScreenState state, JobsLayout layout)
    {
        super(0, 0, layout.width(), layout.height());
        this.layout = layout;
        this.state = state;
        this.cachedRightTab = state.getSelectedRightTab();

        this.jobSelectionComponent = new JobSelectionComponent(state, 14, layout.jobsY(),
                layout.leftWidth() - 12, layout.jobsHeight());
        this.selectedJobComponent = new SelectedJobComponent(state, layout);
        this.coinsComponent = new CoinsComponent(state);
        this.coinsComponent.setX(getWidth() - this.coinsComponent.getWidth() - 8);
        this.coinsComponent.setY(getHeight() - 17);
        this.stockTableComponent = new StockTableComponent(state, 14, layout.bodyY() + 7,
                layout.stockTradingX() - 28, layout.bodyHeight() - 14);
        this.stockTradingComponent = new StockTradingComponent(state, layout.stockTradingWidth(), layout.bodyHeight());
        this.stockTradingComponent.setX(layout.stockTradingX());
        this.stockTradingComponent.setY(layout.bodyY());
        this.stockTradingComponent.setHeight(layout.bodyHeight());
        TabSwitcherComponent tabSwitcherComponent = new TabSwitcherComponent(state, layout);

        // 스킬 버튼(기존 유지)
        this.powerupsButtonWidget = new PowerupsButtonWidget(state);
        this.powerupsButtonWidget.setWidth(Math.min(this.powerupsButtonWidget.getWidth(),
                (layout.detailWidth() - 22) / 2));
        this.powerupsButtonWidget.setX(layout.detailX() + 8);
        this.powerupsButtonWidget.setY(layout.actionY());

        // 판매 버튼: 스킬 버튼과 동일한 크기(Width/Height)로 맞춤 + 스킬 버튼 오른쪽 배치
        int gap = 6;
        int sellX = this.powerupsButtonWidget.getX() + this.powerupsButtonWidget.getWidth() + gap;
        int sellY = this.powerupsButtonWidget.getY();

        this.shopSellButtonWidget = new ShopSellButtonWidget(
                sellX,
                sellY,
                this.powerupsButtonWidget.getWidth(),
                this.powerupsButtonWidget.getHeight(),
                state
        );

        this.addComponent(this.jobSelectionComponent);
        this.addComponent(this.selectedJobComponent);
        this.addComponent(this.coinsComponent);
        this.addComponent(tabSwitcherComponent);
        if (layout.wide()) {
            this.addComponent(new ShopDetailsComponent(state, getWidth() - 158, layout.bodyY(), 150, layout.bodyHeight()));
        }

        this.addWidget(this.powerupsButtonWidget);
        this.addWidget(this.shopSellButtonWidget);
        this.addWidget(new JobsCloseButton(getWidth() - 22, 7));

        this.updateStockPageVisibility();

        // 거래 후 화면이 다시 만들어질 때도 주식 탭이면 시청 상태를 다시 알린다.
        // 서버는 중복 등록을 무시하고 현재 시세만 보내 주므로 API를 다시 호출하지 않는다.
        if (this.cachedRightTab == RightTab.UP_AND_DOWN)
        {
            NetworkManager.sendToServer(new ServerboundStockViewStatePacket(true));
        }
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick, int parentWidth, int parentHeight)
    {
        if (this.cachedRightTab != this.state.getSelectedRightTab())
        {
            RightTab previousTab = this.cachedRightTab;
            this.cachedRightTab = this.state.getSelectedRightTab();
            this.updateStockViewState(previousTab, this.cachedRightTab);
            this.updateStockPageVisibility();
            this.updateParentPosition(getParentX(), getParentY(), parentWidth, parentHeight);
        }

        int x = getTotalX();
        int y = getTotalY();
        JobsTheme.panel(guiGraphics, x, y, getWidth(), getHeight());
        JobsTheme.texture(guiGraphics, JobsTheme.Skin.HEADER, x + 2, y + 2, getWidth() - 4, 25);
        guiGraphics.fill(x + 8, y + 27, x + getWidth() - 8, y + 28, JobsTheme.DIVIDER);
        if (layout.wide()) {
            int badgeX = x + getWidth() - 169;
            JobsTheme.texture(guiGraphics, JobsTheme.Skin.INSET, badgeX, y + 6, 72, 16);
            JobsTheme.sprite(guiGraphics, JobsPlus.getId("jobs/coins"), badgeX + 4, y + 10, 7, 8);
            JobsTheme.text(guiGraphics, Component.literal("직업코인 " + state.getCoins()),
                    badgeX + 14, y + 10, 54, JobsTheme.TEXT);
            JobsTheme.texture(guiGraphics, JobsTheme.Skin.INSET, badgeX + 76, y + 6, 67, 16);
            StockIcons.draw(guiGraphics, "BTC", badgeX + 79, y + 9, 10);
            JobsTheme.text(guiGraphics, Component.literal("계좌 " + java.math.BigDecimal.valueOf(state.getStockAccount().balance()).stripTrailingZeros().toPlainString()),
                    badgeX + 91, y + 10, 48, JobsTheme.TEXT);
        }
        boolean stock = this.cachedRightTab == RightTab.UP_AND_DOWN;
        if (stock)
        {
            JobsTheme.panel(guiGraphics, x + 8, y + layout.bodyY(),
                    layout.stockTradingX() - 16, layout.bodyHeight());
        }
        else if (this.cachedRightTab == RightTab.SHOP || this.cachedRightTab == RightTab.GUN_GUIDE)
        {
            JobsTheme.panel(guiGraphics, x + layout.pageX(this.cachedRightTab), y + layout.bodyY(),
                    layout.pageWidth(this.cachedRightTab), layout.bodyHeight());
            JobsTheme.texture(guiGraphics, JobsTheme.Skin.HEADER, x + 9, y + layout.bodyY() + 1,
                    layout.pageWidth(this.cachedRightTab) - 2, 18);
            JobsTheme.text(guiGraphics, this.cachedRightTab.getName(), x + 16, y + layout.bodyY() + 7,
                    layout.pageWidth(this.cachedRightTab) - 16, JobsTheme.CYAN);
        }
        else
        {
            JobsTheme.panel(guiGraphics, x + 8, y + layout.bodyY(), layout.leftWidth(), layout.bodyHeight());
            JobsTheme.texture(guiGraphics, JobsTheme.Skin.HEADER, x + 9, y + layout.bodyY() + 1,
                    layout.leftWidth() - 2, 18);
            if (layout.wide() && !layout.expandedPage(this.cachedRightTab))
            {
                JobsTheme.panel(guiGraphics, x + layout.detailX(), y + layout.bodyY(),
                        layout.detailWidth(), layout.bodyHeight());
                JobsTheme.texture(guiGraphics, JobsTheme.Skin.HEADER, x + layout.detailX() + 1,
                        y + layout.bodyY() + 1, layout.detailWidth() - 2, 18);
                JobsTheme.text(guiGraphics, Component.literal("보유 " + state.getActiveJobCount() + " / " + state.getMaxJobs()),
                        x + 8 + layout.leftWidth() - 59, y + layout.bodyY() + 7, 53, JobsTheme.MUTED);
            }
            JobsTheme.text(guiGraphics, Component.literal("직업"),
                    x + 16, y + layout.jobsY() - 12, 42, JobsTheme.MUTED);
            JobsTheme.panel(guiGraphics, x + layout.pageX(this.cachedRightTab), y + layout.bodyY(),
                    layout.pageWidth(this.cachedRightTab), layout.bodyHeight());
            JobsTheme.texture(guiGraphics, JobsTheme.Skin.HEADER, x + layout.pageX(this.cachedRightTab) + 1,
                    y + layout.bodyY() + 1, layout.pageWidth(this.cachedRightTab) - 2, 18);
            JobsTheme.text(guiGraphics, this.cachedRightTab.getName(), x + layout.pageX(this.cachedRightTab) + 8,
                    y + layout.bodyY() + 7, layout.pageWidth(this.cachedRightTab) - 16, JobsTheme.CYAN);
        }
        if (this.cachedRightTab == RightTab.RECIPES && layout.wide() && state.getSelectedJob() != null) {
            JobsTheme.text(guiGraphics, state.getSelectedJob().getJobInstance().getName().copy()
                            .append(" · Lv. " + state.getSelectedJob().getLevel()),
                    x + 16, y + layout.bodyY() + layout.bodyHeight() - 35, layout.leftWidth() - 16, JobsTheme.TEXT);
        }
        guiGraphics.fill(x + 8, y + getHeight() - 20, x + getWidth() - 8, y + getHeight() - 19, JobsTheme.DIVIDER);
        JobsTheme.text(guiGraphics, Component.literal("ESC  닫기  ·  휠 스크롤"), x + 10,
                y + getHeight() - 12, 110, JobsTheme.MUTED);
    }

    /**
     * 주식 탭에 들어오고 나가는 것을 서버에 알린다.
     * 시청자가 없어도 미결제 포지션이나 예약 주문이 있으면 서버는 시세 감시를 계속한다.
     */
    private void updateStockViewState(RightTab previousTab, RightTab currentTab)
    {
        if (currentTab == RightTab.UP_AND_DOWN)
        {
            // 지난 세션 가격이 잠깐 보이지 않도록 먼저 지운다.
            ClientStockMarket.clear();
            NetworkManager.sendToServer(new ServerboundStockViewStatePacket(true));
            return;
        }

        if (previousTab == RightTab.UP_AND_DOWN)
        {
            NetworkManager.sendToServer(new ServerboundStockViewStatePacket(false));
            ClientStockMarket.clear();
        }
    }

    private void updateStockPageVisibility()
    {
        boolean isStockPage = this.state.getSelectedRightTab() == RightTab.UP_AND_DOWN;

        if (isStockPage)
        {
            this.removeComponent(this.jobSelectionComponent);
            this.removeComponent(this.selectedJobComponent);
            this.removeComponent(this.coinsComponent);
            this.removeWidget(this.powerupsButtonWidget);
            this.removeWidget(this.shopSellButtonWidget);
            if (!this.getComponents().contains(this.stockTableComponent))
            {
                this.addComponent(this.stockTableComponent);
            }
            if (!this.getComponents().contains(this.stockTradingComponent))
            {
                this.addComponent(this.stockTradingComponent);
            }
            return;
        }

        this.removeComponent(this.stockTableComponent);
        this.removeComponent(this.stockTradingComponent);
        if (!this.getComponents().contains(this.jobSelectionComponent))
        {
            this.addComponent(this.jobSelectionComponent);
        }
        if (layout.expandedPage(this.cachedRightTab)) {
            this.removeComponent(this.selectedJobComponent);
        } else if (!this.getComponents().contains(this.selectedJobComponent)) {
            this.addComponent(this.selectedJobComponent);
        }
        boolean expandedPage = layout.expandedPage(this.cachedRightTab);
        this.jobSelectionComponent.resizeHeight(expandedPage ? layout.bodyHeight() - 59 : layout.jobsHeight());
        int actionWidth = Math.min(90, layout.detailWidth() - 16);
        this.powerupsButtonWidget.setX(expandedPage ? 8 + (layout.leftWidth() - actionWidth) / 2
                : layout.detailX() + (layout.detailWidth() - actionWidth) / 2);
        this.powerupsButtonWidget.setY(expandedPage ? layout.bodyY() + layout.bodyHeight() - 22 : layout.actionY());
        this.powerupsButtonWidget.setWidth(actionWidth);
        boolean shopDetails = layout.wide() && this.cachedRightTab == RightTab.SHOP;
        this.shopSellButtonWidget.setX(shopDetails ? getWidth() - 150 : this.powerupsButtonWidget.getX() + this.powerupsButtonWidget.getWidth() + 6);
        this.shopSellButtonWidget.setY(shopDetails ? layout.bodyY() + layout.bodyHeight() - 26 : layout.actionY());
        this.shopSellButtonWidget.setWidth(shopDetails ? 134 : this.powerupsButtonWidget.getWidth());
        if (!this.getComponents().contains(this.coinsComponent))
        {
            this.addComponent(this.coinsComponent);
        }
        if (!this.getWidgets().contains(this.powerupsButtonWidget))
        {
            this.addWidget(this.powerupsButtonWidget);
        }
        if (!this.getWidgets().contains(this.shopSellButtonWidget))
        {
            this.addWidget(this.shopSellButtonWidget);
        }
        if (this.cachedRightTab == RightTab.SHOP || this.cachedRightTab == RightTab.GUN_GUIDE) {
            this.removeComponent(this.jobSelectionComponent);
            this.removeComponent(this.selectedJobComponent);
            this.removeWidget(this.powerupsButtonWidget);
            if (this.cachedRightTab == RightTab.GUN_GUIDE) {
                this.removeWidget(this.shopSellButtonWidget);
            } else if (!layout.wide()) {
                this.shopSellButtonWidget.setWidth(Math.min(110, layout.pageWidth(this.cachedRightTab) - 16));
                this.shopSellButtonWidget.setX(8 + (layout.pageWidth(this.cachedRightTab) - this.shopSellButtonWidget.getWidth()) / 2);
                this.shopSellButtonWidget.setY(layout.bodyY() + layout.bodyHeight() - 22);
            }
        }
    }
}
