package com.daqem.jobsplus.client.gui.jobs.widgets;

import com.daqem.jobsplus.client.gui.theme.JobsTheme;
import com.daqem.jobsplus.JobsPlus;
import com.daqem.jobsplus.client.gui.jobs.JobsScreenState;
import com.daqem.jobsplus.client.gui.powerups.PowerupsScreen;
import com.daqem.jobsplus.client.gui.powerups.PowerupsScreenState;
import com.daqem.jobsplus.player.job.Job;
import com.daqem.jobsplus.player.job.powerup.PowerupAvailability;
import com.daqem.uilib.gui.widget.CustomButtonWidget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;

public class PowerupsButtonWidget extends CustomButtonWidget
{

    private final static Component MESSAGE = JobsPlus.translatable("gui.jobs.powerups");

    private final JobsScreenState state;
    private Job cachedJob;
    private int cachedAvailablePowerupCount;

    public PowerupsButtonWidget(JobsScreenState state)
    {
        super(211, 212, Minecraft.getInstance().font.width(MESSAGE) + 14, JobsTheme.BUTTON_HEIGHT,
                MESSAGE, null, button -> Minecraft.getInstance().gui.setScreen(new PowerupsScreen(
                        new PowerupsScreenState(state.getJobs(), state.getSelectedJob(), state.getCoins()),
                        Minecraft.getInstance().gui.screen())));
        this.state = state;
    }

    @Override
    protected void extractContents(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick)
    {
        int availablePowerupCount = this.getAvailablePowerupCount();
        if (availablePowerupCount > 0)
        {
            Component notification = JobsPlus.translatable(
                    "gui.jobs.new_powerups", availablePowerupCount);
            JobsTheme.label(guiGraphics, notification, getX(), getY() - 11,
                    getWidth(), 10, JobsTheme.WARNING);
        }

        JobsTheme.button(guiGraphics, getX(), getY(), getWidth(), getHeight(),
                this.active, isHoveredOrFocused(), false, true);
        JobsTheme.label(guiGraphics, getMessage(), getX(), getY(), getWidth(), getHeight(),
                this.active ? JobsTheme.TEXT : JobsTheme.DISABLED);
    }

    private int getAvailablePowerupCount()
    {
        Job selectedJob = this.state.getSelectedJob();
        if (this.cachedJob != selectedJob)
        {
            this.cachedJob = selectedJob;
            this.cachedAvailablePowerupCount = PowerupAvailability.countAvailablePowerups(selectedJob);
        }
        return this.cachedAvailablePowerupCount;
    }
}
