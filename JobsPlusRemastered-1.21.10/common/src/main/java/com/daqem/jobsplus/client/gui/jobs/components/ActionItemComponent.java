package com.daqem.jobsplus.client.gui.jobs.components;

import com.daqem.arc.api.action.IAction;
import com.daqem.jobsplus.JobsPlus;
import com.daqem.jobsplus.integration.arc.reward.rewards.job.JobExpReward;
import com.daqem.uilib.gui.component.EmptyComponent;
import com.daqem.uilib.gui.component.text.multiline.MultiLineTextComponent;
import net.minecraft.network.chat.Component;

public class ActionItemComponent extends EmptyComponent
{

    private static final int HEADER_COLOR = 0xFFB22222;
    private static final int CONTENT_COLOR = 0xFF1E1410;
    private static final int CONTENT_GAP = 2;

    public ActionItemComponent(IAction action, String jobPath, int width)
    {
        super(0, 0, width, 0);

        JobExpReward jobExpReward = action.getRewards().stream()
                .filter(JobExpReward.class::isInstance)
                .map(JobExpReward.class::cast)
                .findFirst()
                .orElse(null);
        if (jobExpReward == null)
        {
            return;
        }

        String actionPath = action.getLocation().getPath();
        int pathSeparatorIndex = actionPath.lastIndexOf('/');
        if (pathSeparatorIndex >= 0)
        {
            actionPath = actionPath.substring(pathSeparatorIndex + 1);
        }
        String translationPath = "gui.jobs.experience.details." + jobPath + "." + actionPath;
        MultiLineTextComponent headerComponent = new MultiLineTextComponent(
                0, 0, getWidth(), JobsPlus.translatable(translationPath + ".header"), HEADER_COLOR);

        Component experienceText = jobExpReward.getMin() == jobExpReward.getMax()
                ? JobsPlus.translatable("gui.jobs.experience.reward", jobExpReward.getMin())
                : JobsPlus.translatable(
                        "gui.jobs.experience.reward.range",
                        jobExpReward.getMin(),
                        jobExpReward.getMax());
        Component contentText = experienceText.copy()
                .append("\n")
                .append(JobsPlus.translatable(translationPath + ".description"));
        MultiLineTextComponent contentComponent = new MultiLineTextComponent(
                0,
                headerComponent.getHeight() + CONTENT_GAP,
                getWidth(),
                contentText,
                CONTENT_COLOR);

        this.addComponent(headerComponent);
        this.addComponent(contentComponent);
        this.setHeight(headerComponent.getHeight() + CONTENT_GAP + contentComponent.getHeight());
    }
}
