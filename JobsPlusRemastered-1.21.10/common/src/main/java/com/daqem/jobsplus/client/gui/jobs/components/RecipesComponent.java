package com.daqem.jobsplus.client.gui.jobs.components;

import com.daqem.jobsplus.client.gui.jobs.JobsScreenState;
import com.daqem.uilib.gui.component.EmptyComponent;

public class RecipesComponent extends EmptyComponent
{

    public RecipesComponent(JobsScreenState state, int width, int height)
    {
        super(0, 0, width, height);

        UserGuideScrollComponent userGuideScrollComponent =
                new UserGuideScrollComponent(state, getWidth() - 10, getHeight());

        this.addComponent(userGuideScrollComponent);
    }
}
