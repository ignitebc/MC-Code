package com.daqem.jobsplus.client.gui.jobs.components;

import com.daqem.jobsplus.JobsPlus;
import com.daqem.jobsplus.client.gui.jobs.JobsScreenState;
import com.daqem.jobsplus.client.gui.jobs.tab.RightTab;
import com.daqem.jobsplus.client.gui.jobs.widgets.PowerupsButtonWidget;
import com.daqem.jobsplus.client.gui.jobs.widgets.ShopSellButtonWidget;
import com.daqem.jobsplus.client.stock.ClientStockMarket;
import com.daqem.jobsplus.networking.c2s.ServerboundStockViewStatePacket;
import dev.architectury.networking.NetworkManager;
import com.daqem.uilib.gui.component.AbstractComponent;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;

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

    public JobsComponent(JobsScreenState state)
    {
        super(0, 0, 390, 230);
        this.state = state;
        this.cachedRightTab = state.getSelectedRightTab();

        this.jobSelectionComponent = new JobSelectionComponent(state);
        this.selectedJobComponent = new SelectedJobComponent(state);
        this.coinsComponent = new CoinsComponent(state);
        this.stockTableComponent = new StockTableComponent(state);
        this.stockTradingComponent = new StockTradingComponent(state);
        TabSwitcherComponent tabSwitcherComponent = new TabSwitcherComponent(state);

        // 스킬 버튼(기존 유지)
        this.powerupsButtonWidget = new PowerupsButtonWidget(state);

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

        this.addWidget(this.powerupsButtonWidget);
        this.addWidget(this.shopSellButtonWidget);

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

        guiGraphics.blitSprite(
                RenderPipelines.GUI_TEXTURED,
                JobsPlus.getId("jobs/background"),
                this.getTotalX(), this.getTotalY(),
                this.getWidth(), this.getHeight() - 2
        );
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
        if (!this.getComponents().contains(this.selectedJobComponent))
        {
            this.addComponent(this.selectedJobComponent);
        }
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
    }
}
