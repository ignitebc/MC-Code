package com.daqem.jobsplus.client.gui.jobs.components;

import com.daqem.jobsplus.client.gui.jobs.JobsScreenState;
import com.daqem.jobsplus.client.gui.theme.JobsLayout;
import com.daqem.jobsplus.client.gui.jobs.tab.RightTab;
import com.daqem.jobsplus.player.job.Job;
import com.daqem.uilib.gui.component.EmptyComponent;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public class RightPageContentComponent extends EmptyComponent
{

    private final JobsScreenState state;
    private int contentWidth;
    private final JobsLayout layout;
    private final int contentHeight;
    private RightTab cachedTab;
    private Job cachedJob;

    public RightPageContentComponent(JobsScreenState state, JobsLayout layout)
    {
        super(0, 0, layout.contentWidth() - 14, layout.bodyHeight() - 26);
        this.layout = layout;
        this.state = state;
        this.contentWidth = getWidth();
        this.contentHeight = getHeight();
        this.cachedTab = state.getSelectedRightTab();
        this.cachedJob = state.getSelectedJob();
        this.addTabComponent();
    }

    private void addTabComponent()
    {
        this.contentWidth = layout.pageWidth(this.cachedTab) - 14;
        this.setWidth(contentWidth);
        this.setX(layout.pageX(this.cachedTab) + 7);
        this.setY(layout.bodyY() + 20);
        switch (this.cachedTab) {
        case EXPERIENCE -> this.addComponent(new ExperienceComponent(state, contentWidth, contentHeight));
        case RECIPES -> this.addComponent(new RecipesComponent(state, contentWidth, contentHeight));
        case UP_AND_DOWN -> {
        }
        case SHOP -> this.addComponent(new ShopComponent(state, contentWidth, contentHeight));
        }
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick, int parentWidth, int parentHeight)
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
