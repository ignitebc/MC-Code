package com.daqem.jobsplus.client.gui.jobs.components;

import com.daqem.jobsplus.JobsPlus;
import com.daqem.jobsplus.client.gui.jobs.JobsScreenState;
import com.daqem.uilib.gui.component.EmptyComponent;
import com.daqem.uilib.gui.component.sprite.SpriteComponent;

public class RecipesComponent extends EmptyComponent
{

    public RecipesComponent(JobsScreenState state, int width, int height)
    {
        super(0, 0, width, height);

        SpriteComponent bannerComponent = new SpriteComponent((getWidth() - 98) / 2, 0, 98, 33, JobsPlus.getId("jobs/recipes_banner"));
        RecipesScrollComponent recipesScrollComponent = new RecipesScrollComponent(state, getWidth(), getHeight());

        this.addComponent(bannerComponent);
        this.addComponent(recipesScrollComponent);
    }
}
