package com.daqem.jobsplus.client.gui.jobs.components;

import com.daqem.arc.api.action.IAction;
import com.daqem.jobsplus.JobsPlus;
import com.daqem.jobsplus.client.gui.jobs.JobsScreenState;
import com.daqem.jobsplus.integration.arc.reward.rewards.job.JobExpReward;
import com.daqem.uilib.gui.component.EmptyComponent;
import com.daqem.uilib.gui.component.text.multiline.MultiLineTextComponent;

import java.util.Comparator;
import java.util.List;

public class ExperienceScrollContentComponent extends EmptyComponent
{

    private static final int SECTION_GAP = 9;
    private static final int GUIDE_CONTENT_GAP = 2;
    private static final int HEADER_COLOR = 0xFFB22222;
    private static final int CONTENT_COLOR = 0xFF1E1410;

    public ExperienceScrollContentComponent(JobsScreenState state, int width)
    {
        super(0, 0, width, 0);

        List<IAction> actions = state.getSelectedJob().getJobInstance().getActions().stream()
                .filter(action -> action.getRewards().stream().anyMatch(JobExpReward.class::isInstance))
                .sorted(Comparator.comparingDouble(this::getMaximumExperience)
                        .thenComparing(action -> action.getLocation().getPath()))
                .toList();

        MultiLineTextComponent guideHeaderComponent = new MultiLineTextComponent(
                0,
                0,
                getWidth(),
                JobsPlus.translatable("gui.jobs.experience.guide.header"),
                HEADER_COLOR);
        MultiLineTextComponent guideContentComponent = new MultiLineTextComponent(
                0,
                guideHeaderComponent.getHeight() + GUIDE_CONTENT_GAP,
                getWidth(),
                JobsPlus.translatable("gui.jobs.experience.guide.description"),
                CONTENT_COLOR);
        this.addComponent(guideHeaderComponent);
        this.addComponent(guideContentComponent);

        int yOffset = guideHeaderComponent.getHeight()
                + GUIDE_CONTENT_GAP
                + guideContentComponent.getHeight()
                + SECTION_GAP;
        String jobPath = state.getSelectedJob().getJobInstance().getLocation().getPath();
        for (IAction action : actions)
        {
            ActionItemComponent actionItemComponent = new ActionItemComponent(action, jobPath, getWidth());
            actionItemComponent.setY(yOffset);
            this.addComponent(actionItemComponent);
            yOffset += actionItemComponent.getHeight() + SECTION_GAP;
        }

        this.setHeight(Math.max(0, yOffset - SECTION_GAP));
    }

    private double getMaximumExperience(IAction action)
    {
        return action.getRewards().stream()
                .filter(JobExpReward.class::isInstance)
                .map(JobExpReward.class::cast)
                .mapToDouble(JobExpReward::getMax)
                .findFirst()
                .orElse(0);
    }
}
