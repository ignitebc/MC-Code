package com.daqem.jobsplus.client.gui.jobs.components;

import com.daqem.jobsplus.client.gui.jobs.JobsScreenState;
import com.daqem.jobsplus.client.gui.jobs.tab.RightTab;
import com.daqem.jobsplus.player.job.Job;
import com.daqem.uilib.gui.component.EmptyComponent;
import net.minecraft.client.gui.GuiGraphics;

public class RightPageContentComponent extends EmptyComponent
{

    private final JobsScreenState state;
    private final int contentWidth;
    private final int contentHeight;
    private RightTab cachedTab;
    private Job cachedJob;

    public RightPageContentComponent(JobsScreenState state, int contentWidth, int contentHeight)
    {
        super(0, 0, contentWidth, contentHeight);
        this.state = state;
        this.contentWidth = contentWidth;
        this.contentHeight = contentHeight;
        this.cachedTab = state.getSelectedRightTab();
        this.cachedJob = state.getSelectedJob();
        this.addTabComponent();
    }

    private void addTabComponent()
    {
        switch (this.cachedTab) {
        case EXPERIENCE -> this.addComponent(new ExperienceComponent(state, contentWidth, contentHeight));
        case RECIPES -> this.addComponent(new RecipesComponent(state, contentWidth, contentHeight));
        case UP_AND_DOWN -> {
        }
        case SHOP -> this.addComponent(new ShopComponent(state, contentWidth, contentHeight));
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick, int parentWidth, int parentHeight)
    {
        if (this.cachedTab != this.state.getSelectedRightTab() || this.cachedJob != this.state.getSelectedJob())
        {
            this.cachedTab = this.state.getSelectedRightTab();
            this.cachedJob = this.state.getSelectedJob();
            this.clearComponents();
            this.addTabComponent();
            this.updateParentPosition(getParentX(), getParentY(), parentWidth, parentHeight);
        }
    }
}
