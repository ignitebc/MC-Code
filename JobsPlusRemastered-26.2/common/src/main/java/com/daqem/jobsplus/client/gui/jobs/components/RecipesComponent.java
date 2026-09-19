package com.daqem.jobsplus.client.gui.jobs.components;

import com.daqem.uilib.gui.component.EmptyComponent;

public class RecipesComponent extends EmptyComponent
{

    public RecipesComponent(int width, int height)
    {
        super(0, 0, width, height);

        this.addComponent(new UserGuideScrollComponent(getWidth(), getHeight()));
    }
}
