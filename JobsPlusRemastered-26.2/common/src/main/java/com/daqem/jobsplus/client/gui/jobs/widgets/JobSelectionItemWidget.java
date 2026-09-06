package com.daqem.jobsplus.client.gui.jobs.widgets;

import com.daqem.jobsplus.client.gui.theme.JobsTheme;
import com.daqem.jobsplus.JobsPlus;
import com.daqem.jobsplus.client.gui.jobs.JobsScreenState;
import com.daqem.jobsplus.player.job.Job;
import com.daqem.uilib.gui.widget.CustomButtonWidget;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.WidgetSprites;

public class JobSelectionItemWidget extends CustomButtonWidget
{

    private static final WidgetSprites SPRITES = new WidgetSprites(JobsPlus.getId("jobs/job_button"), JobsPlus.getId("jobs/job_button_hovered"));

    private final Job job;
    private final JobsScreenState state;

    public JobSelectionItemWidget(Job job, JobsScreenState state, int width)
    {
        super(0, 0, width, 19, job.getJobInstance().getName(), SPRITES, button -> state.setSelectedJob(job));
        this.job = job;
        this.state = state;
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
        guiGraphics.pose().translate(getX() + 4, getY() + 3);
        guiGraphics.pose().scale(0.75f, 0.75f);
        guiGraphics.fakeItem(this.job.getJobInstance().getIconItem(), 0, 0);
        guiGraphics.pose().popMatrix();
        JobsTheme.text(guiGraphics, getMessage(), getX() + 19, getY() + 4, getWidth() - 34,
                this.job.getLevel() > 0 ? JobsTheme.TEXT : JobsTheme.MUTED);
        if (this.job.getLevel() > 0)
        {
            JobsTheme.label(guiGraphics, JobsPlus.literal(Integer.toString(this.job.getLevel())),
                    getX() + getWidth() - 16, getY() + 1, 14, 12, JobsTheme.CYAN);
            JobsTheme.progress(guiGraphics, getX() + 19, getY() + 13, getWidth() - 24, 4,
                    this.job.getExperiencePercentage());
        }
    }
}
