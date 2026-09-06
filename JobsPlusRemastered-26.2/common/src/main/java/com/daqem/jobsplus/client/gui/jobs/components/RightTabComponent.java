package com.daqem.jobsplus.client.gui.jobs.components;

import com.daqem.jobsplus.client.gui.jobs.JobsScreenState;
import com.daqem.jobsplus.client.gui.jobs.tab.RightTab;
import com.daqem.jobsplus.client.gui.jobs.widgets.RightTabWidget;
import com.daqem.uilib.gui.component.EmptyComponent;
import net.minecraft.client.Minecraft;
import com.daqem.jobsplus.client.gui.theme.JobsTheme;

public class RightTabComponent extends EmptyComponent
{
    public static final int HEIGHT = JobsTheme.TAB_HEIGHT;

    public RightTabComponent(int x, int y, JobsScreenState state, RightTab tab)
    {
        super(x, y, getTabWidth(tab), HEIGHT);

        RightTabWidget rightTabWidget = new RightTabWidget(state, tab, getWidth(), getHeight());

        this.addWidget(rightTabWidget);
    }

    public static int getTabWidth(RightTab tab)
    {
        return (int) Math.ceil(Minecraft.getInstance().font.width(tab.getName()) * JobsTheme.LABEL_SCALE) + 14;
    }
}
