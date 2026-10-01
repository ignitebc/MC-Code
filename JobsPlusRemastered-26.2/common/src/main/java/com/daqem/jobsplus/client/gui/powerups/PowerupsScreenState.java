package com.daqem.jobsplus.client.gui.powerups;

import com.daqem.jobsplus.player.job.Job;
import com.daqem.jobsplus.client.gui.powerups.tab.PowerupTab;
import com.daqem.jobsplus.client.gui.powerups.widgets.PowerupItemWidget;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class PowerupsScreenState
{

    private Job job;
    private List<Job> jobs;
    private Identifier selectedHyperJobLocation;
    private int coins;
    private PowerupItemWidget previewWidget;
    private boolean detailsPanelVisible;
    private boolean hyperRequestPending;
    private PowerupTab selectedTab = PowerupTab.NORMAL;

    public PowerupsScreenState(List<Job> jobs, Job job, int coins)
    {
        this.jobs = List.copyOf(jobs);
        this.job = job;
        this.selectedHyperJobLocation = job.getJobInstance().getLocation();
        this.coins = coins;
    }

    public PowerupItemWidget getPreviewWidget() {
        return previewWidget;
    }

    public void setPreviewWidget(PowerupItemWidget widget) {
        this.previewWidget = widget;
    }

    public PowerupTab getSelectedTab() {
        return selectedTab;
    }

    public void setSelectedTab(PowerupTab selectedTab) {
        this.selectedTab = selectedTab;
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

    public @Nullable Job getJob(Identifier jobLocation)
    {
        for (Job currentJob : this.jobs)
        {
            if (currentJob.getJobInstance().getLocation().equals(jobLocation))
            {
                return currentJob;
            }
        }
        return null;
    }

    public Identifier getSelectedHyperJobLocation()
    {
        return this.selectedHyperJobLocation;
    }

    public void setSelectedHyperJobLocation(Identifier jobLocation)
    {
        if (!this.hyperRequestPending)
        {
            this.selectedHyperJobLocation = jobLocation;
        }
    }

    public @Nullable Job getSelectedHyperJob()
    {
        return getJob(this.selectedHyperJobLocation);
    }

    public void update(List<Job> jobs, int coins)
    {
        this.jobs = List.copyOf(jobs);
        Job updatedJob = getJob(this.job.getJobInstance().getLocation());
        if (updatedJob != null)
        {
            this.job = updatedJob;
        }
        this.coins = coins;
        this.hyperRequestPending = false;
    }

    public boolean isHyperRequestPending()
    {
        return this.hyperRequestPending;
    }

    public void setHyperRequestPending(boolean pending)
    {
        this.hyperRequestPending = pending;
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
