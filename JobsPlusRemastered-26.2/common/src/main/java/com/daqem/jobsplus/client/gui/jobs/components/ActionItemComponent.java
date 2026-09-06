package com.daqem.jobsplus.client.gui.jobs.components;

import com.daqem.jobsplus.client.gui.theme.JobsTheme;
import com.daqem.arc.api.action.IAction;
import com.daqem.jobsplus.JobsPlus;
import com.daqem.jobsplus.integration.arc.reward.rewards.job.JobBitcoinReward;
import com.daqem.jobsplus.integration.arc.reward.rewards.job.JobExpReward;
import com.daqem.uilib.gui.component.EmptyComponent;
import com.daqem.uilib.gui.component.text.multiline.MultiLineTextComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.item.ItemStack;
import com.daqem.arc.data.condition.block.BlocksCondition;
import com.daqem.arc.data.condition.item.ItemsCondition;

public class ActionItemComponent extends EmptyComponent
{

    private final ItemStack icon;
    private boolean bitcoinReward;

    private static final int HEADER_COLOR = JobsTheme.CYAN;
    private static final int CONTENT_COLOR = JobsTheme.TEXT;
    private static final int CONTENT_GAP = 2;
    private static final int CONTENT_LINE_SPACING = 1;

    public ActionItemComponent(IAction action, String jobPath, int width, ItemStack fallbackIcon)
    {
        super(0, 0, width, 0);
        this.icon = findIcon(action, fallbackIcon);

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

        this.bitcoinReward = jobBitcoinReward != null;

        String actionPath = action.getLocation().getPath();
        int pathSeparatorIndex = actionPath.lastIndexOf('/');
        if (pathSeparatorIndex >= 0)
        {
            actionPath = actionPath.substring(pathSeparatorIndex + 1);
        }
        String translationPath = "gui.jobs.experience.details." + jobPath + "." + actionPath;
        MultiLineTextComponent headerComponent = new MultiLineTextComponent(
                44, 7, getWidth() - 51, JobsPlus.translatable(translationPath + ".header"), HEADER_COLOR);

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
                44,
                7 + headerComponent.getHeight() + CONTENT_GAP,
                getWidth() - 51,
                contentText,
                CONTENT_COLOR);
        contentComponent.setLineSpacing(CONTENT_LINE_SPACING);

        this.addComponent(headerComponent);
        this.addComponent(contentComponent);
        this.setHeight(Math.max(68, 14 + headerComponent.getHeight() + CONTENT_GAP + contentComponent.getHeight()));
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY,
                                   float partialTick, int parentWidth, int parentHeight) {
        if (getHeight() > 0) {
            JobsTheme.texture(graphics, JobsTheme.Skin.INSET, getTotalX(), getTotalY(), getWidth(), getHeight());
            JobsTheme.texture(graphics, JobsTheme.Skin.SLOT, getTotalX() + 6, getTotalY() + 6, 32, 32);
            graphics.pose().pushMatrix();
            graphics.pose().translate(getTotalX() + 10, getTotalY() + 10);
            graphics.pose().scale(1.5f, 1.5f);
            graphics.fakeItem(icon, 0, 0);
            graphics.pose().popMatrix();
            JobsTheme.label(graphics, Component.literal("EXP"), getTotalX() + 6, getTotalY() + 42, 32, 9, JobsTheme.CYAN);
            if (bitcoinReward) {
                JobsTheme.label(graphics, Component.literal("BTC"), getTotalX() + 6, getTotalY() + 54, 32, 9, JobsTheme.WARNING);
            }
        }
    }

    private static ItemStack findIcon(IAction action, ItemStack fallback) {
        for (var condition : action.getConditions()) {
            if (condition.isInverted()) {
                continue;
            }
            if (condition instanceof BlocksCondition blocks && !blocks.getBlocks().isEmpty()) {
                ItemStack stack = new ItemStack(blocks.getBlocks().getFirst());
                if (!stack.isEmpty()) {
                    return stack;
                }
            }
            if (condition instanceof ItemsCondition items && !items.getItems().isEmpty()) {
                return new ItemStack(items.getItems().getFirst());
            }
        }
        return fallback.copy();
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
