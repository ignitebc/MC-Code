package com.daqem.jobsplus.client.gui.jobs.widgets;

import com.daqem.jobsplus.client.gui.theme.JobsTheme;
import com.daqem.jobsplus.JobsPlus;
import com.daqem.jobsplus.client.gui.jobs.JobsScreenState;
import com.daqem.jobsplus.player.job.Job;
import com.daqem.jobsplus.player.job.powerup.PowerupAvailability;
import com.daqem.uilib.gui.widget.CustomButtonWidget;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.network.chat.Component;

public class JobSelectionItemWidget extends CustomButtonWidget
{

    private static final WidgetSprites SPRITES = new WidgetSprites(JobsPlus.getId("jobs/job_button"), JobsPlus.getId("jobs/job_button_hovered"));

    private final Job job;
    private final JobsScreenState state;
    private final int availablePowerupCount;

    public JobSelectionItemWidget(Job job, JobsScreenState state, int width)
    {
        super(0, 0, width, 23, job.getJobInstance().getName(), SPRITES, button -> state.setSelectedJob(job));
        this.job = job;
        this.state = state;
        this.availablePowerupCount = PowerupAvailability.countAvailablePowerups(job);
    }

    @Override
    protected void extractContents(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick)
    {
        boolean selected = this.job == this.state.getSelectedJob();
        JobsTheme.cutBox(guiGraphics, getX(), getY(), getWidth(), getHeight(),
                selected || isHoveredOrFocused() ? JobsTheme.SELECTED : JobsTheme.INSET,
                selected || isHoveredOrFocused() ? JobsTheme.CYAN : JobsTheme.DIVIDER);
        if (selected)
        {
            guiGraphics.fill(getX(), getY() + 2, getX() + 2, getY() + getHeight() - 2, JobsTheme.CYAN);
        }
        guiGraphics.pose().pushMatrix();
        guiGraphics.pose().translate(getX() + 5, getY() + 4);
        guiGraphics.pose().scale(1.0f, 1.0f);
        guiGraphics.fakeItem(this.job.getJobInstance().getIconItem(), 0, 0);
        guiGraphics.pose().popMatrix();
        int statusWidth = 26;
        Component status;
        int statusColor;
        int jobNameColor;
        if (this.job.getLevel() > 0)
        {
            status = JobsPlus.literal("보유");
            statusColor = JobsTheme.CYAN;
            jobNameColor = JobsTheme.TEXT;
        }
        else
        {
            status = JobsPlus.literal("미보유");
            statusColor = JobsTheme.MUTED;
            jobNameColor = JobsTheme.MUTED;
        }

        if (this.availablePowerupCount > 0)
        {
            statusWidth = 40;
            status = JobsPlus.translatable("gui.jobs.new_powerups_short", this.availablePowerupCount);
            statusColor = JobsTheme.WARNING;
        }

        int nameWidth = Math.max(1, getWidth() - 31 - statusWidth);
        Component name = getMessage();
        if (this.job.getLevel() > 0)
        {
            name = name.copy().append(" LV" + this.job.getLevel());
        }
        JobsTheme.text(guiGraphics, name, getX() + 25, getY() + 8, nameWidth, jobNameColor);
        JobsTheme.textRight(guiGraphics, status, getX() + getWidth() - 4, getY() + 8,
                statusWidth, statusColor);
    }
}
