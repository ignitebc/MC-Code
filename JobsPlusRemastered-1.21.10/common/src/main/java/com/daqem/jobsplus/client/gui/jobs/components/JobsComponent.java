package com.daqem.jobsplus.client.gui.jobs.components;

import com.daqem.jobsplus.JobsPlus;
import com.daqem.jobsplus.client.gui.jobs.JobsScreenState;
import com.daqem.jobsplus.client.gui.jobs.widgets.PowerupsButtonWidget;
import com.daqem.jobsplus.client.gui.jobs.widgets.ShopSellButtonWidget;
import com.daqem.uilib.gui.component.AbstractComponent;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderPipelines;

public class JobsComponent extends AbstractComponent
{
    public JobsComponent(JobsScreenState state)
    {
        super(0, 0, 302, 204 + 2);

        JobSelectionComponent jobSelectionComponent = new JobSelectionComponent(state);
        SelectedJobComponent selectedJobComponent = new SelectedJobComponent(state);
        CoinsComponent coinsComponent = new CoinsComponent(state);
        TabSwitcherComponent tabSwitcherComponent = new TabSwitcherComponent(state);

        // 스킬 버튼(기존 유지)
        PowerupsButtonWidget powerupsButtonWidget = new PowerupsButtonWidget(state);

        // 판매 버튼: 스킬 버튼과 동일한 크기(Width/Height)로 맞춤 + 스킬 버튼 오른쪽 배치
        int gap = 6;
        int sellX = powerupsButtonWidget.getX() + powerupsButtonWidget.getWidth() + gap;
        int sellY = powerupsButtonWidget.getY();

        ShopSellButtonWidget shopSellButtonWidget = new ShopSellButtonWidget(
                sellX,
                sellY,
                powerupsButtonWidget.getWidth(),
                powerupsButtonWidget.getHeight(),
                state
        );

        this.addComponent(jobSelectionComponent);
        this.addComponent(selectedJobComponent);
        this.addComponent(coinsComponent);
        this.addComponent(tabSwitcherComponent);

        this.addWidget(powerupsButtonWidget);
        this.addWidget(shopSellButtonWidget);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick, int parentWidth, int parentHeight)
    {
        guiGraphics.blitSprite(
                RenderPipelines.GUI_TEXTURED,
                JobsPlus.getId("jobs/background"),
                this.getTotalX(), this.getTotalY(),
                this.getWidth(), this.getHeight() - 2
        );
    }
}
