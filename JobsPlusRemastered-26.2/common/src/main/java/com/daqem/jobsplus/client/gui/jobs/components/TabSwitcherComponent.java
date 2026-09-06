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

        int tabGap = 1;
        int availableWidth = layout.width() - (layout.wide() ? 180 : 36);
        int tabWidth = (availableWidth - tabGap * (RightTab.values().length - 1)) / RightTab.values().length;
        int tabX = 8;
        for (RightTab tab : RightTab.values()) {
            this.addComponent(new RightTabComponent(tabX, 6, state, tab, tabWidth));
            tabX += tabWidth + tabGap;
        }
        RightPageContentComponent rightPageContentComponent = new RightPageContentComponent(state, layout);
        this.addComponent(rightPageContentComponent);
    }
}
