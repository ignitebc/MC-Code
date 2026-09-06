package com.daqem.jobsplus.client.gui.powerups;

import com.daqem.jobsplus.player.job.Job;
import com.daqem.jobsplus.client.gui.powerups.widgets.PowerupItemWidget;

public class PowerupsScreenState
{

    private final Job job;
    private int coins;
    private PowerupItemWidget previewWidget;
    private boolean detailsPanelVisible;

    public PowerupsScreenState(Job job, int coins)
    {
        this.job = job;
        this.coins = coins;
    }

    public PowerupItemWidget getPreviewWidget() {
        return previewWidget;
    }

    public void setPreviewWidget(PowerupItemWidget widget) {
        this.previewWidget = widget;
    }

    public boolean isDetailsPanelVisible() {
        return detailsPanelVisible;
    }

    public void setDetailsPanelVisible(boolean visible) {
        this.detailsPanelVisible = visible;
    }

    public Job getJob()
    {
        return job;
    }

    public int getCoins()
    {
        return coins;
    }

    public void setCoins(int coins)
    {
        this.coins = coins;
    }
}
