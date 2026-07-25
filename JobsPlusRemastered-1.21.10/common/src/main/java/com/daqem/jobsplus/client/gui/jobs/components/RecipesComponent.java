package com.daqem.jobsplus.client.gui.jobs.components;

import com.daqem.jobsplus.client.gui.jobs.JobsScreenState;
import com.daqem.uilib.gui.component.EmptyComponent;

public class RecipesComponent extends EmptyComponent
{

    public RecipesComponent(JobsScreenState state, int width, int height)
    {
        super(0, 0, width, height);

        RecipesScrollComponent recipesScrollComponent = new RecipesScrollComponent(state, getWidth() - 10, getHeight());

        this.addComponent(recipesScrollComponent);
    }
}
