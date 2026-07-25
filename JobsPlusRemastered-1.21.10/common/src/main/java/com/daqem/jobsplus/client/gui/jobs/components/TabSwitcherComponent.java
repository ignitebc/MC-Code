package com.daqem.jobsplus.client.gui.jobs.components;

import com.daqem.jobsplus.client.gui.jobs.JobsScreenState;
import com.daqem.jobsplus.client.gui.jobs.tab.RightTab;
import com.daqem.uilib.gui.component.EmptyComponent;

public class TabSwitcherComponent extends EmptyComponent
{

    public TabSwitcherComponent(JobsScreenState state)
    {
        super(179, 18, 209, 190);

        int experienceTabWidth = RightTabComponent.getTabWidth(RightTab.EXPERIENCE);
        int recipesTabWidth = RightTabComponent.getTabWidth(RightTab.RECIPES);
        int upAndDownTabWidth = RightTabComponent.getTabWidth(RightTab.UP_AND_DOWN);
        int shopTabWidth = RightTabComponent.getTabWidth(RightTab.SHOP);
        int tabY = 0;
        int tabGap = 2;
        int tabsWidth = experienceTabWidth + recipesTabWidth + upAndDownTabWidth + shopTabWidth + tabGap * 3;
        int tabStartX = (getWidth() - tabsWidth) / 2;

        RightTabComponent experienceTab = new RightTabComponent(tabStartX, tabY, state, RightTab.EXPERIENCE);
        RightTabComponent recipesTab = new RightTabComponent(tabStartX + experienceTabWidth + tabGap, tabY, state, RightTab.RECIPES);
        RightTabComponent upAndDownTab = new RightTabComponent(tabStartX + experienceTabWidth + recipesTabWidth + tabGap * 2, tabY, state, RightTab.UP_AND_DOWN);
        RightTabComponent shopTab = new RightTabComponent(tabStartX + experienceTabWidth + recipesTabWidth + upAndDownTabWidth + tabGap * 3, tabY, state, RightTab.SHOP);
        RightPageContentComponent rightPageContentComponent = new RightPageContentComponent(state, getWidth() - 53, getHeight());
        rightPageContentComponent.setX(29);

        this.addComponent(experienceTab);
        this.addComponent(recipesTab);
        this.addComponent(upAndDownTab);
        this.addComponent(shopTab);
        this.addComponent(rightPageContentComponent);
    }
}
