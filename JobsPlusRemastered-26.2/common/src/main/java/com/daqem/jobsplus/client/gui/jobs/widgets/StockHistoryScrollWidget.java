package com.daqem.jobsplus.client.gui.jobs.widgets;

import com.daqem.jobsplus.client.gui.jobs.JobsScreenState;
import com.daqem.jobsplus.client.gui.jobs.components.StockHistoryContentComponent;

public class StockHistoryScrollWidget extends AbstractScrollWidget
{
    public StockHistoryScrollWidget(int width, int height, JobsScreenState state)
    {
        super(width, height, 10);
        this.addComponent(new StockHistoryContentComponent(state, width - 10));
    }
}
