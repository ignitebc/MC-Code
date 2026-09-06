package com.daqem.jobsplus.client.gui.jobs.components;

import com.daqem.jobsplus.client.gui.jobs.JobsScreenState;
import com.daqem.uilib.gui.component.EmptyComponent;

public class ExperienceComponent extends EmptyComponent
{

    public ExperienceComponent(JobsScreenState state, int width, int height)
    {
        super(0, 0, width, height);

        ActionScrollComponent actionScrollComponent = new ActionScrollComponent(state, getWidth(), getHeight());

        this.addComponent(actionScrollComponent);
    }
}
