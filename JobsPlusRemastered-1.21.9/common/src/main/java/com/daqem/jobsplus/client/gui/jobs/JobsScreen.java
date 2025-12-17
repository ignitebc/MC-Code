package com.daqem.jobsplus.client.gui.jobs;

import com.daqem.jobsplus.JobsPlus;
import com.daqem.jobsplus.client.gui.jobs.components.JobsComponent;
import com.daqem.uilib.gui.AbstractScreen;
import net.minecraft.client.gui.screens.Screen;

public class JobsScreen extends AbstractScreen
{

    private final JobsScreenState state;
    private final Screen previousScreen;

    public JobsScreen(JobsScreenState state, Screen previousScreen)
    {
        super(JobsPlus.translatable("gui.title.jobs"));
        this.state = state;
        this.previousScreen = previousScreen;
    }

    @Override
    protected void init()
    {
        JobsComponent jobsComponent = new JobsComponent(this.state);
        jobsComponent.center();

        this.addComponent(jobsComponent);

        super.init();
    }

    @Override
    public void onClose()
    {
        assert this.minecraft != null;
        this.minecraft.setScreen(previousScreen);
    }

    public Screen getPreviousScreen()
    {
        return this.previousScreen;
    }

    /** 기존 상태 보존(Shop 탭 유지)용 Getter */
    public JobsScreenState getState()
    {
        return this.state;
    }

    @Override
    public boolean isPauseScreen()
    {
        return false;
    }
}
