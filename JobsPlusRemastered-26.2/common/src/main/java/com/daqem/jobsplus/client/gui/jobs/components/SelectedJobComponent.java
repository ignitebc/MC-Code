package com.daqem.jobsplus.client.gui.jobs.components;

import com.daqem.jobsplus.JobsPlus;
import com.daqem.jobsplus.client.gui.jobs.JobsScreenState;
import com.daqem.jobsplus.client.gui.jobs.widgets.StartJobButtonWidget;
import com.daqem.jobsplus.client.gui.theme.JobsLayout;
import com.daqem.jobsplus.client.gui.theme.JobsTheme;
import com.daqem.jobsplus.integration.arc.holder.holders.job.JobInstance;
import com.daqem.jobsplus.player.job.Job;
import com.daqem.uilib.gui.component.EmptyComponent;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;

public class SelectedJobComponent extends EmptyComponent {
    private final JobsScreenState state;
    private final StartJobButtonWidget startJobButtonWidget;
    private final boolean wide;
    private final boolean spacious;

    public SelectedJobComponent(JobsScreenState state, JobsLayout layout) {
        super(layout.detailX(), layout.bodyY(), layout.detailWidth(), layout.wide() ? layout.bodyHeight() : 46);
        this.state = state;
        this.wide = layout.wide();
        this.spacious = wide && getHeight() >= 225;
        this.startJobButtonWidget = new StartJobButtonWidget(state);
        this.startJobButtonWidget.setWidth(Math.min(90, getWidth() - 16));
        this.startJobButtonWidget.setX((getWidth() - this.startJobButtonWidget.getWidth()) / 2);
        this.startJobButtonWidget.setY(wide ? getHeight() - 44 : 29);
        if (state.getSelectedJob().getLevel() == 0 && canStartNewJob()) {
            this.addWidget(this.startJobButtonWidget);
        }
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY,
                                   float partialTick, int parentWidth, int parentHeight) {
        Job selectedJob = state.getSelectedJob();
        JobInstance jobInstance = selectedJob.getJobInstance();
        int x = getTotalX();
        int y = getTotalY();
        if (wide) {
            JobsTheme.text(graphics, Component.literal("직업 상세 정보"), x + 8, y + 7, getWidth() - 16, JobsTheme.MUTED);
            int iconSize = spacious ? 64 : 42;
            int iconX = x + (getWidth() - iconSize) / 2;
            JobsTheme.cutBox(graphics, iconX, y + 24, iconSize, iconSize, JobsTheme.INSET, JobsTheme.BORDER);
            graphics.pose().pushMatrix();
            graphics.pose().translate(iconX + 5, y + 29);
            float iconScale = (iconSize - 10) / 16.0f;
            graphics.pose().scale(iconScale, iconScale);
            graphics.fakeItem(jobInstance.getIconItem(), 0, 0);
            graphics.pose().popMatrix();
            JobsTheme.label(graphics, jobInstance.getName(), x + 6, y + (spacious ? 94 : 72), getWidth() - 12, 14, JobsTheme.TEXT);
        } else {
            graphics.fakeItem(jobInstance.getIconItem(), x + 7, y + 5);
            JobsTheme.text(graphics, jobInstance.getName(), x + 29, y + 7, getWidth() - 35, JobsTheme.TEXT);
        }

        if (selectedJob.getLevel() > 0) {
            if (wide) {
                JobsTheme.label(graphics, JobsPlus.translatable("gui.jobs.level", selectedJob.getLevel()),
                        x + 6, y + (spacious ? 109 : 87), getWidth() - 12, 12, JobsTheme.CYAN);
                JobsTheme.progress(graphics, x + 8, y + (spacious ? 127 : 105), getWidth() - 16, 6, selectedJob.getExperiencePercentage());
                JobsTheme.label(graphics, JobsPlus.translatable("gui.jobs.experience", selectedJob.getExperience(),
                                selectedJob.getExperienceForNextLevel()), x + 4, y + (spacious ? 137 : 115), getWidth() - 8, 12, JobsTheme.MUTED);
            } else {
                JobsTheme.text(graphics, JobsPlus.translatable("gui.jobs.level", selectedJob.getLevel()),
                        x + 29, y + 19, getWidth() - 35, JobsTheme.CYAN);
                JobsTheme.label(graphics, JobsPlus.translatable("gui.jobs.experience", selectedJob.getExperience(),
                                selectedJob.getExperienceForNextLevel()), x + 6, y + 27, getWidth() - 12, 10, JobsTheme.MUTED);
                JobsTheme.progress(graphics, x + 8, y + 40, getWidth() - 16, 4, selectedJob.getExperiencePercentage());
            }
            this.removeWidget(this.startJobButtonWidget);
        } else {
            int infoY = y + 20;
            if (wide) {
                infoY = y + 100;
            }
            if (spacious) {
                infoY = y + 125;
            }
            if (canStartNewJob()) {
                int remainingFreeJobs = Math.max(0, state.getMaxJobs() - state.getActiveJobCount());
                Component startCost = canStartFreeJob()
                        ? JobsPlus.translatable("gui.jobs.free_remaining", remainingFreeJobs)
                        : JobsPlus.translatable("gui.jobs.price", jobInstance.getPrice());
                JobsTheme.label(graphics, startCost, x + 6, infoY, getWidth() - 12, 9, JobsTheme.MUTED);
            } else {
                JobsTheme.label(graphics, JobsPlus.translatable("gui.jobs.max_jobs", state.getMaxJobs()),
                        x + 6, infoY, getWidth() - 12, 9, JobsTheme.ERROR);
            }
            // Keep the original eligibility and ticket-slot rules; only the presentation changes.
            if (jobInstance.getPrice() > state.getCoins() && !canStartFreeJob()) {
                this.removeWidget(this.startJobButtonWidget);
            } else if (!this.getWidgets().contains(this.startJobButtonWidget) && canStartNewJob()) {
                this.addWidget(this.startJobButtonWidget);
                this.updateParentPosition(getParentX(), getParentY(), parentWidth, parentHeight);
            }
        }
        if (spacious) {
            JobsTheme.sprite(graphics, JobsPlus.getId("jobs/coins"), x + 9, y + 159, 7, 8);
            JobsTheme.text(graphics, Component.literal("직업코인  " + state.getCoins()),
                    x + 21, y + 159, getWidth() - 29, JobsTheme.TEXT);
            JobsTheme.text(graphics, jobInstance.getDescription(), x + 8, y + 179,
                    getWidth() - 16, JobsTheme.MUTED);
        }
    }

    private boolean canStartNewJob() {
        return this.state.getActiveJobCount() < this.state.getMaxJobs();
    }

    private boolean canStartFreeJob() {
        return this.state.getActiveJobCount() < this.state.getMaxJobs();
    }
}
