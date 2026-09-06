package com.daqem.jobsplus.client.gui.jobs.components;

import com.daqem.jobsplus.client.gui.jobs.JobsScreenState;
import com.daqem.jobsplus.client.gui.jobs.widgets.JobSelectionWidget;
import com.daqem.uilib.gui.component.EmptyComponent;

public class JobSelectionComponent extends EmptyComponent
{

    public JobSelectionComponent(JobsScreenState state, int x, int y, int width, int height)
    {
        super(x, y, width, height);

        JobSelectionWidget jobSelectionWidget = new JobSelectionWidget(getWidth(), getHeight(), state);
        this.addWidget(jobSelectionWidget);
    }
}
