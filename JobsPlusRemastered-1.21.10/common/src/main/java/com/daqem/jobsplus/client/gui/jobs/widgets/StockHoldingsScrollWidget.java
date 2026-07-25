package com.daqem.jobsplus.client.gui.jobs.widgets;

import com.daqem.jobsplus.client.gui.jobs.JobsScreenState;
import com.daqem.jobsplus.client.gui.jobs.components.StockHoldingsContentComponent;

public class StockHoldingsScrollWidget extends AbstractScrollWidget
{
    public StockHoldingsScrollWidget(int width, int height, JobsScreenState state)
    {
        super(width, height, 10);
        this.addComponent(new StockHoldingsContentComponent(state));
    }
}
