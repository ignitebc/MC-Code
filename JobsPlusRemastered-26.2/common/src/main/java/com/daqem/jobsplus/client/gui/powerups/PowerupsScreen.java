package com.daqem.jobsplus.client.gui.powerups;

import com.daqem.jobsplus.JobsPlus;
import com.daqem.jobsplus.client.gui.powerups.components.PowerupsComponent;
import com.daqem.jobsplus.player.job.Job;
import com.daqem.uilib.gui.AbstractScreen;
import net.minecraft.client.gui.screens.Screen;

public class PowerupsScreen extends AbstractScreen
{

    private final PowerupsScreenState state;
    private final Screen previousScreen;
    private PowerupsComponent powerupsComponent;
    private int layoutWidth;
    private int layoutHeight;

    public PowerupsScreen(PowerupsScreenState state, Screen previousScreen)
    {
        super(JobsPlus.translatable("gui.title.powerups"));
        this.state = state;
        this.previousScreen = previousScreen;
    }

    @Override
    protected void init()
    {
        this.layoutWidth = this.width;
        this.layoutHeight = this.height;
        this.state.setPreviewWidget(null);
        this.powerupsComponent = new PowerupsComponent(this.state);
        powerupsComponent.center();

        this.addComponent(powerupsComponent);

        super.init();
    }

    @Override
    protected void repositionElements()
    {
        // 확인창에서 같은 화면으로 복귀할 때 바닐라의 위젯 재생성을 막는다.
        if (this.powerupsComponent != null && this.layoutWidth == this.width && this.layoutHeight == this.height) return;
        super.repositionElements();
    }

    public void update(Job job, int coins)
    {
        this.state.update(job, coins);
        if (this.powerupsComponent != null) this.powerupsComponent.refreshPowerups();
    }

    public Screen getPreviousScreen()
    {
        return previousScreen;
    }

    public PowerupsScreenState getState()
    {
        return state;
    }

    @Override
    public void onClose()
    {
        assert this.minecraft != null;
        this.minecraft.gui.setScreen(previousScreen);
    }

    @Override
    public boolean isPauseScreen()
    {
        return false;
    }
}
