package com.daqem.jobsplus.client.gui.powerups.components;

import com.daqem.jobsplus.JobsPlus;
import com.daqem.jobsplus.client.gui.confimation.ConfirmationScreen;
import com.daqem.jobsplus.client.gui.confimation.ConfirmationScreenState;
import com.daqem.jobsplus.client.gui.jobs.widgets.AbstractScrollWidget;
import com.daqem.jobsplus.client.gui.powerups.PowerupsScreenState;
import com.daqem.jobsplus.client.gui.powerups.widgets.HyperSkillSlotWidget;
import com.daqem.jobsplus.client.gui.theme.JobsTheme;
import com.daqem.jobsplus.networking.c2s.ServerboundHyperSkillPacket;
import com.daqem.jobsplus.player.job.Job;
import com.daqem.jobsplus.player.job.hyper.HyperSkillRules;
import com.daqem.jobsplus.player.job.hyper.HyperSkillState;
import com.daqem.uilib.gui.component.EmptyComponent;
import com.daqem.uilib.gui.widget.CustomButtonWidget;
import dev.architectury.networking.NetworkManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import java.util.List;

/** 전 직업이 공유하는 8개 슬롯과 선택한 하이퍼의 해금·강화·활성 상태. */
public class HyperPowerupsComponent extends EmptyComponent
{
    private static final int RING_WIDTH = 232;
    private static final int RING_HEIGHT = 220;
    private static final int DETAILS_HEIGHT = 188;
    private static final int CONTENT_GAP = 12;
    // 목록의 레벨 정렬과 관계없이 슬롯 위치를 고정한다.
    private static final List<SlotPosition> SLOT_POSITIONS = List.of(
            new SlotPosition(HyperSkillRules.MINER, 44, 0),
            new SlotPosition(HyperSkillRules.DIGGER, 120, 0),
            new SlotPosition(HyperSkillRules.FARMER, 0, 56),
            new SlotPosition(HyperSkillRules.FISHERMAN, 164, 56),
            new SlotPosition(HyperSkillRules.HUNTER, 0, 112),
            new SlotPosition(HyperSkillRules.SMITH, 164, 112),
            new SlotPosition(HyperSkillRules.ALCHEMIST, 44, 168),
            new SlotPosition(HyperSkillRules.ADVENTURER, 120, 168));

    private final PowerupsScreenState state;
    private final EmptyComponent content;
    private final int detailsX;
    private final int detailsY;
    private final int detailsWidth;
    private final HyperButton actionButton;
    private final HyperButton toggleButton;

    public HyperPowerupsComponent(PowerupsScreenState state, int x, int y, int width, int height)
    {
        super(x, y, width, height);
        this.state = state;
        int contentWidth = Math.max(1, width - 12);
        int contentHeight = RING_HEIGHT;
        int ringX = 0;
        if (contentWidth >= RING_WIDTH + CONTENT_GAP + 240)
        {
            this.detailsX = RING_WIDTH + CONTENT_GAP;
            this.detailsY = 8;
            this.detailsWidth = contentWidth - this.detailsX;
        }
        else
        {
            ringX = Math.max(0, (contentWidth - RING_WIDTH) / 2);
            this.detailsX = 0;
            this.detailsY = RING_HEIGHT + CONTENT_GAP;
            this.detailsWidth = contentWidth;
            contentHeight = this.detailsY + DETAILS_HEIGHT;
        }

        this.content = new EmptyComponent(0, 0, contentWidth, contentHeight)
        {
            @Override
            public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY,
                                           float partialTick, int parentWidth, int parentHeight)
            {
                renderDetails(graphics);
            }
        };
        for (SlotPosition position : SLOT_POSITIONS)
        {
            this.content.addWidget(new HyperSkillSlotWidget(
                    state, position.jobLocation(), ringX + position.x(), position.y()));
        }

        int buttonWidth = Math.max(1, (this.detailsWidth - 6) / 2);
        int buttonY = this.detailsY + 166;
        this.actionButton = new HyperButton(this.detailsX, buttonY, buttonWidth,
                JobsPlus.translatable("hyper.open_button"), true, button -> openConfirmation());
        this.toggleButton = new HyperButton(this.detailsX + buttonWidth + 6, buttonY, buttonWidth,
                JobsPlus.translatable("hyper.toggle_off"), false, button -> toggle());
        this.content.addWidget(this.actionButton);
        this.content.addWidget(this.toggleButton);

        AbstractScrollWidget scrollWidget = new AbstractScrollWidget(width, Math.max(1, height), 20) {};
        scrollWidget.addComponent(this.content);
        this.addWidget(scrollWidget);
    }

    private int count(Identifier itemLocation)
    {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null)
        {
            return 0;
        }
        return HyperSkillRules.countItems(minecraft.player.getInventory(), itemLocation);
    }

    private boolean canPay(Job job)
    {
        if (job == null || this.state.isHyperRequestPending())
        {
            return false;
        }
        if (!HyperSkillRules.supports(job.getJobInstance().getLocation())
                || job.getLevel() < HyperSkillRules.REQUIRED_JOB_LEVEL)
        {
            return false;
        }
        HyperSkillState skill = job.getHyperSkill();
        if (skill.level() == 0)
        {
            if (this.state.getCoins() < HyperSkillRules.OPEN_COIN_COST
                    || count(HyperSkillRules.GEM) < HyperSkillRules.OPEN_GEM_COST)
            {
                return false;
            }
            return true;
        }
        if (skill.level() >= HyperSkillRules.MAX_LEVEL)
        {
            return false;
        }
        int targetLevel = skill.level() + 1;
        if (this.state.getCoins() < HyperSkillRules.UPGRADE_COIN_COST
                || count(HyperSkillRules.GEM) < HyperSkillRules.getGemCost(targetLevel)
                || count(HyperSkillRules.BITCOIN) < HyperSkillRules.getBitcoinCost(targetLevel))
        {
            return false;
        }
        return true;
    }

    private void openConfirmation()
    {
        Job job = this.state.getSelectedHyperJob();
        if (!canPay(job))
        {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        Screen previousScreen = minecraft.gui.screen();
        if (previousScreen == null)
        {
            return;
        }
        // 확인창이 열린 뒤 슬롯이나 데이터가 바뀌어도 대상 직업과 단계는 고정한다.
        Identifier jobLocation = job.getJobInstance().getLocation();
        HyperSkillState skill = job.getHyperSkill();
        ServerboundHyperSkillPacket.Action action;
        Component message;
        if (skill.level() == 0)
        {
            action = ServerboundHyperSkillPacket.Action.OPEN;
            message = JobsPlus.translatable("hyper.confirm_open", HyperSkillRules.getName(jobLocation),
                    HyperSkillRules.OPEN_GEM_COST, HyperSkillRules.OPEN_COIN_COST);
        }
        else
        {
            action = ServerboundHyperSkillPacket.Action.UPGRADE;
            int targetLevel = skill.level() + 1;
            message = JobsPlus.translatable("hyper.confirm_upgrade", targetLevel,
                    HyperSkillRules.getSuccessChance(targetLevel), HyperSkillRules.getGemCost(targetLevel),
                    HyperSkillRules.getBitcoinCost(targetLevel), HyperSkillRules.UPGRADE_COIN_COST);
        }
        minecraft.gui.setScreen(new ConfirmationScreen(previousScreen,
                new ConfirmationScreenState(message, () ->
                {
                    send(jobLocation, action, skill.revision());
                    minecraft.gui.setScreen(previousScreen);
                })));
    }

    private void toggle()
    {
        Job job = this.state.getSelectedHyperJob();
        if (job == null || !HyperSkillRules.supports(job.getJobInstance().getLocation())
                || job.getLevel() < HyperSkillRules.REQUIRED_JOB_LEVEL || job.getHyperSkill().level() == 0)
        {
            return;
        }
        send(job.getJobInstance().getLocation(), ServerboundHyperSkillPacket.Action.TOGGLE,
                job.getHyperSkill().revision());
    }

    private void send(Identifier jobLocation, ServerboundHyperSkillPacket.Action action, int revision)
    {
        if (this.state.isHyperRequestPending())
        {
            return;
        }
        this.state.setHyperRequestPending(true);
        NetworkManager.sendToServer(new ServerboundHyperSkillPacket(jobLocation, action, revision));
    }

    private void renderDetails(GuiGraphicsExtractor graphics)
    {
        int ringX = 0;
        if (this.detailsY > RING_HEIGHT)
        {
            ringX = Math.max(0, (this.content.getWidth() - RING_WIDTH) / 2);
        }
        JobsTheme.label(graphics, JobsPlus.translatable("hyper.select_skill"),
                this.content.getTotalX() + ringX + 72, this.content.getTotalY() + 88,
                88, 12, JobsTheme.MUTED);
        if (this.detailsY > RING_HEIGHT)
        {
            JobsTheme.label(graphics, JobsPlus.translatable("hyper.scroll_details"),
                    this.content.getTotalX() + ringX + 72, this.content.getTotalY() + 104,
                    88, 12, JobsTheme.CYAN);
        }
        Job job = this.state.getSelectedHyperJob();
        Identifier jobLocation = this.state.getSelectedHyperJobLocation();
        updateButtons(job);
        int jobLevel = 0;
        if (job != null)
        {
            jobLevel = job.getLevel();
        }
        line(graphics, JobsPlus.translatable("hyper.job_status",
                HyperSkillSlotWidget.getJobName(this.state, jobLocation), jobLevel), 0, JobsTheme.MUTED);
        line(graphics, HyperSkillRules.getName(jobLocation), 14, JobsTheme.CYAN);
        if (!HyperSkillRules.supports(jobLocation))
        {
            line(graphics, JobsPlus.translatable("hyper.planned_details"), 40, JobsTheme.MUTED);
            return;
        }
        HyperSkillState skill = HyperSkillState.EMPTY;
        if (job != null)
        {
            skill = job.getHyperSkill();
        }
        Component status = JobsPlus.translatable("hyper.level_status", skill.level(),
                HyperSkillRules.getActivationChance(skill.level()));
        if (skill.level() == 0)
        {
            status = JobsPlus.translatable("hyper.locked");
            if (jobLevel >= HyperSkillRules.REQUIRED_JOB_LEVEL)
            {
                status = JobsPlus.translatable("hyper.ready_to_open");
            }
        }
        line(graphics, status, 28, JobsTheme.TEXT);
        int pipWidth = Math.max(3, this.detailsWidth / HyperSkillRules.MAX_LEVEL);
        int x = this.content.getTotalX() + this.detailsX;
        int y = this.content.getTotalY() + this.detailsY;
        for (int index = 0; index < HyperSkillRules.MAX_LEVEL; index++)
        {
            int color = JobsTheme.DIVIDER;
            if (index < skill.level())
            {
                color = JobsTheme.CYAN;
            }
            graphics.fill(x + index * pipWidth, y + 42,
                    x + (index + 1) * pipWidth - 2, y + 46, color);
        }
        line(graphics, JobsPlus.translatable("hyper.description"), 56, JobsTheme.TEXT);
        line(graphics, JobsPlus.translatable("hyper.mining_rules"), 68, JobsTheme.MUTED);
        Component next = JobsPlus.translatable("hyper.open_details");
        Component cost = JobsPlus.translatable("hyper.open_cost",
                HyperSkillRules.OPEN_GEM_COST, HyperSkillRules.OPEN_COIN_COST);
        if (skill.level() > 0 && skill.level() < HyperSkillRules.MAX_LEVEL)
        {
            int target = skill.level() + 1;
            next = JobsPlus.translatable("hyper.next_details", target,
                    HyperSkillRules.getActivationChance(target), HyperSkillRules.getSuccessChance(target));
            cost = JobsPlus.translatable("hyper.upgrade_cost", HyperSkillRules.getGemCost(target),
                    HyperSkillRules.getBitcoinCost(target), HyperSkillRules.UPGRADE_COIN_COST);
        }
        else if (skill.level() == HyperSkillRules.MAX_LEVEL)
        {
            next = JobsPlus.translatable("hyper.complete");
            cost = JobsPlus.translatable("hyper.no_more_cost");
        }
        line(graphics, next, 86, JobsTheme.SUCCESS);
        line(graphics, cost, 98, JobsTheme.WARNING);
        line(graphics, JobsPlus.translatable("hyper.inventory", count(HyperSkillRules.GEM),
                count(HyperSkillRules.BITCOIN), this.state.getCoins()), 112, JobsTheme.MUTED);
        Component note = JobsPlus.translatable("hyper.failure_rule");
        if (jobLevel < HyperSkillRules.REQUIRED_JOB_LEVEL)
        {
            note = JobsPlus.translatable("hyper.requires_level");
        }
        line(graphics, note, 126, JobsTheme.MUTED);
    }

    private void updateButtons(Job job)
    {
        boolean implemented = HyperSkillRules.supports(this.state.getSelectedHyperJobLocation());
        this.actionButton.visible = implemented;
        this.toggleButton.visible = implemented;
        this.actionButton.active = canPay(job);
        this.toggleButton.active = false;
        HyperSkillState skill = HyperSkillState.EMPTY;
        if (job != null)
        {
            skill = job.getHyperSkill();
            if (implemented && skill.level() > 0 && !this.state.isHyperRequestPending()
                    && job.getLevel() >= HyperSkillRules.REQUIRED_JOB_LEVEL)
            {
                this.toggleButton.active = true;
            }
        }
        Component actionLabel = JobsPlus.translatable("hyper.upgrade_button");
        if (skill.level() == 0)
        {
            actionLabel = JobsPlus.translatable("hyper.open_button");
        }
        else if (skill.level() == HyperSkillRules.MAX_LEVEL)
        {
            actionLabel = JobsPlus.translatable("hyper.max_level");
        }
        if (this.state.isHyperRequestPending())
        {
            actionLabel = JobsPlus.translatable("hyper.waiting");
        }
        this.actionButton.setMessage(actionLabel);
        Component toggleLabel = JobsPlus.translatable("hyper.toggle_off");
        if (skill.active() && skill.level() > 0)
        {
            toggleLabel = JobsPlus.translatable("hyper.toggle_on");
        }
        this.toggleButton.setMessage(toggleLabel);
    }

    private void line(GuiGraphicsExtractor graphics, Component text, int y, int color)
    {
        JobsTheme.text(graphics, text, this.content.getTotalX() + this.detailsX,
                this.content.getTotalY() + this.detailsY + y, this.detailsWidth, color);
    }

    private record SlotPosition(Identifier jobLocation, int x, int y) {}

    private static final class HyperButton extends CustomButtonWidget
    {
        private final boolean primary;

        private HyperButton(int x, int y, int width, Component message, boolean primary, OnPress onPress)
        {
            super(x, y, width, 18, message, null, onPress);
            this.primary = primary;
        }

        @Override
        protected void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick)
        {
            JobsTheme.button(graphics, getX(), getY(), getWidth(), getHeight(),
                    this.active, isHoveredOrFocused(), false, this.primary);
            JobsTheme.label(graphics, getMessage(), getX(), getY(), getWidth(), getHeight(), JobsTheme.TEXT);
        }
    }
}
