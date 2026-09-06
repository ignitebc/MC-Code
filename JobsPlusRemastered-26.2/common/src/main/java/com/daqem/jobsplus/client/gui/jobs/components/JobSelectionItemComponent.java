package com.daqem.jobsplus.client.gui.jobs.components;

import com.daqem.jobsplus.client.gui.jobs.JobsScreenState;
import com.daqem.jobsplus.client.gui.jobs.widgets.JobSelectionItemWidget;
import com.daqem.jobsplus.player.job.Job;
import com.daqem.uilib.gui.component.EmptyComponent;

public class JobSelectionItemComponent extends EmptyComponent
{

    public JobSelectionItemComponent(Job job, JobsScreenState state, int width)
    {
        super(0, 0, width, 20);

        JobSelectionItemWidget jobSelectionItemWidget = new JobSelectionItemWidget(job, state, width);

        this.addWidget(jobSelectionItemWidget);
    }
}
