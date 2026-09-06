package com.daqem.jobsplus.client.gui.jobs.components;

import com.daqem.jobsplus.client.gui.jobs.JobsScreenState;
import com.daqem.jobsplus.client.gui.jobs.tab.RightTab;
import com.daqem.uilib.gui.component.EmptyComponent;
import com.daqem.jobsplus.client.gui.theme.JobsLayout;

public class TabSwitcherComponent extends EmptyComponent
{

    public TabSwitcherComponent(JobsScreenState state, JobsLayout layout)
    {
        super(0, 0, layout.width(), layout.height());

        int experienceTabWidth = RightTabComponent.getTabWidth(RightTab.EXPERIENCE);
        int recipesTabWidth = RightTabComponent.getTabWidth(RightTab.RECIPES);
        int upAndDownTabWidth = RightTabComponent.getTabWidth(RightTab.UP_AND_DOWN);
        int tabY = 6;
        int tabGap = 2;
        int tabStartX = 70;

        RightTabComponent experienceTab = new RightTabComponent(tabStartX, tabY, state, RightTab.EXPERIENCE);
        RightTabComponent recipesTab = new RightTabComponent(tabStartX + experienceTabWidth + tabGap, tabY, state, RightTab.RECIPES);
        RightTabComponent upAndDownTab = new RightTabComponent(tabStartX + experienceTabWidth + recipesTabWidth + tabGap * 2, tabY, state, RightTab.UP_AND_DOWN);
        RightTabComponent shopTab = new RightTabComponent(tabStartX + experienceTabWidth + recipesTabWidth + upAndDownTabWidth + tabGap * 3, tabY, state, RightTab.SHOP);
        RightPageContentComponent rightPageContentComponent = new RightPageContentComponent(state,
                layout.contentWidth() - 14, layout.bodyHeight() - 26);
        rightPageContentComponent.setX(layout.contentX() + 7);
        rightPageContentComponent.setY(layout.bodyY() + 20);

        this.addComponent(experienceTab);
        this.addComponent(recipesTab);
        this.addComponent(upAndDownTab);
        this.addComponent(shopTab);
        this.addComponent(rightPageContentComponent);
    }
}
