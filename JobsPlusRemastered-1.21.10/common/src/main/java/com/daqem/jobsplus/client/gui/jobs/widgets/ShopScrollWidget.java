package com.daqem.jobsplus.client.gui.jobs.widgets;

import com.daqem.jobsplus.client.gui.jobs.JobsScreenState;
import com.daqem.jobsplus.client.gui.jobs.components.ShopScrollContentComponent;

public class ShopScrollWidget extends AbstractScrollWidget
{
    public ShopScrollWidget(int width, int height, JobsScreenState state)
    {
        super(width, height, 24);
        this.addComponent(new ShopScrollContentComponent(state));
    }
}
