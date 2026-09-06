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

        int uniformWidth = (layout.width() - 250) / 4;
        int experienceTabWidth = layout.wide() ? uniformWidth : RightTabComponent.getTabWidth(RightTab.EXPERIENCE);
        int recipesTabWidth = layout.wide() ? uniformWidth : RightTabComponent.getTabWidth(RightTab.RECIPES);
        int upAndDownTabWidth = layout.wide() ? uniformWidth : RightTabComponent.getTabWidth(RightTab.UP_AND_DOWN);
        int tabY = 6;
        int tabGap = 2;
        int tabStartX = 70;

        RightTabComponent experienceTab = new RightTabComponent(tabStartX, tabY, state, RightTab.EXPERIENCE, experienceTabWidth);
        RightTabComponent recipesTab = new RightTabComponent(tabStartX + experienceTabWidth + tabGap, tabY, state, RightTab.RECIPES, recipesTabWidth);
        RightTabComponent upAndDownTab = new RightTabComponent(tabStartX + experienceTabWidth + recipesTabWidth + tabGap * 2, tabY, state, RightTab.UP_AND_DOWN, upAndDownTabWidth);
        RightTabComponent shopTab = new RightTabComponent(tabStartX + experienceTabWidth + recipesTabWidth + upAndDownTabWidth + tabGap * 3, tabY, state, RightTab.SHOP, layout.wide() ? uniformWidth : RightTabComponent.getTabWidth(RightTab.SHOP));
        RightPageContentComponent rightPageContentComponent = new RightPageContentComponent(state, layout);

        this.addComponent(experienceTab);
        this.addComponent(recipesTab);
        this.addComponent(upAndDownTab);
        this.addComponent(shopTab);
        this.addComponent(rightPageContentComponent);
    }
}
