package com.daqem.jobsplus.client.gui.jobs.components;

import com.daqem.jobsplus.client.gui.theme.JobsTheme;
import com.daqem.arc.api.action.IAction;
import com.daqem.jobsplus.JobsPlus;
import com.daqem.jobsplus.integration.arc.reward.rewards.job.JobBitcoinReward;
import com.daqem.jobsplus.integration.arc.reward.rewards.job.JobExpReward;
import com.daqem.uilib.gui.component.EmptyComponent;
import com.daqem.uilib.gui.component.text.multiline.MultiLineTextComponent;
import net.minecraft.network.chat.Component;

public class ActionItemComponent extends EmptyComponent
{

    private static final int HEADER_COLOR = JobsTheme.CYAN;
    private static final int CONTENT_COLOR = JobsTheme.TEXT;
    private static final int CONTENT_GAP = 2;
    private static final int CONTENT_LINE_SPACING = 1;

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

        JobBitcoinReward jobBitcoinReward = action.getRewards().stream()
                .filter(JobBitcoinReward.class::isInstance)
                .map(JobBitcoinReward.class::cast)
                .findFirst()
                .orElse(null);

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
                ? JobsPlus.translatable("gui.jobs.experience.reward", formatExperience(jobExpReward.getMin()))
                : JobsPlus.translatable(
                        "gui.jobs.experience.reward.range",
                        formatExperience(jobExpReward.getMin()),
                        formatExperience(jobExpReward.getMax()));
        experienceText = experienceText.copy()
                .append("\n")
                .append(JobsPlus.translatable("gui.jobs.experience.reward.chance"));
        if (jobBitcoinReward != null)
        {
            experienceText = experienceText.copy()
                    .append("\n")
                    .append(JobsPlus.translatable(
                            "gui.jobs.bitcoin.reward",
                            jobBitcoinReward.getAmount()))
                    .append("\n")
                    .append(JobsPlus.translatable(
                            "gui.jobs.bitcoin.reward.chance",
                            formatChance(jobBitcoinReward.getChance())));
        }
        Component contentText = experienceText.copy()
                .append("\n")
                .append(JobsPlus.translatable(translationPath + ".description"));
        MultiLineTextComponent contentComponent = new MultiLineTextComponent(
                0,
                headerComponent.getHeight() + CONTENT_GAP,
                getWidth(),
                contentText,
                CONTENT_COLOR);
        contentComponent.setLineSpacing(CONTENT_LINE_SPACING);

        this.addComponent(headerComponent);
        this.addComponent(contentComponent);
        this.setHeight(headerComponent.getHeight() + CONTENT_GAP + contentComponent.getHeight());
    }

    private String formatChance(double chance)
    {
        if (chance == Math.rint(chance))
        {
            return Long.toString((long) chance);
        }
        return Double.toString(chance);
    }

    private String formatExperience(double experience)
    {
        if (experience == Math.rint(experience))
        {
            return Long.toString((long) experience);
        }
        return Double.toString(experience);
    }
}
