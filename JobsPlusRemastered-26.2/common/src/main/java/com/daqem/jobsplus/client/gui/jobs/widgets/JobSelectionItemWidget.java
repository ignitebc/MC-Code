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
        super(0, 0, width, 23, job.getJobInstance().getName(), SPRITES, button -> state.setSelectedJob(job));
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
        guiGraphics.pose().translate(getX() + 5, getY() + 4);
        guiGraphics.pose().scale(1.0f, 1.0f);
        guiGraphics.fakeItem(this.job.getJobInstance().getIconItem(), 0, 0);
        guiGraphics.pose().popMatrix();
        JobsTheme.text(guiGraphics, getMessage(), getX() + 25, getY() + 8, getWidth() - 56,
                this.job.getLevel() > 0 ? JobsTheme.TEXT : JobsTheme.MUTED);
        JobsTheme.text(guiGraphics, JobsPlus.literal(this.job.getLevel() > 0 ? "보유" : "미보유"),
                getX() + getWidth() - 27, getY() + 8, 24,
                this.job.getLevel() > 0 ? JobsTheme.CYAN : JobsTheme.MUTED);
    }
}
