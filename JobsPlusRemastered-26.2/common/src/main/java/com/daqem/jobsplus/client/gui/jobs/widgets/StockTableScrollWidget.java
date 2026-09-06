package com.daqem.jobsplus.client.gui.jobs.widgets;

import com.daqem.jobsplus.client.gui.jobs.JobsScreenState;
import com.daqem.jobsplus.client.gui.jobs.components.StockTableRowsContentComponent;

public class StockTableScrollWidget extends AbstractScrollWidget
{
    public StockTableScrollWidget(int width, int height, JobsScreenState state)
    {
        super(width, height, 24);
        this.addComponent(new StockTableRowsContentComponent(state, width - 10));
    }
}
