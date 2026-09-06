package com.daqem.jobsplus.client.gui.jobs.widgets;

import com.daqem.jobsplus.client.gui.jobs.JobsScreenState;
import com.daqem.jobsplus.client.gui.jobs.components.StockHoldingsContentComponent;

public class StockHoldingsScrollWidget extends AbstractScrollWidget
{
    private static final int SCROLL_HANDLE_WIDTH = 7;
    private static final int SCROLL_TRACK_WIDTH = 4;

    public StockHoldingsScrollWidget(int width, int height, JobsScreenState state)
    {
        super(width, height, 10);
        this.addComponent(new StockHoldingsContentComponent(state, width - 10));
    }

    @Override
    protected int scrollHandleWidth()
    {
        return SCROLL_HANDLE_WIDTH;
    }

    @Override
    protected int scrollTrackWidth()
    {
        return SCROLL_TRACK_WIDTH;
    }
}
