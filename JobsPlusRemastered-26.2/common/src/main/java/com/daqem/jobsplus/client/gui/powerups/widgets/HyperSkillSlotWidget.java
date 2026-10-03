package com.daqem.jobsplus.client.gui.powerups.widgets;

import com.daqem.jobsplus.JobsPlus;
import com.daqem.jobsplus.client.gui.powerups.PowerupsScreenState;
import com.daqem.jobsplus.client.gui.theme.JobsTheme;
import com.daqem.jobsplus.player.job.Job;
import com.daqem.jobsplus.player.job.hyper.HyperSkillRules;
import com.daqem.uilib.gui.widget.CustomButtonWidget;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

/** 공통 하이퍼 화면의 직업 슬롯. 미구현 직업도 선택하여 예정 안내를 볼 수 있다. */
public class HyperSkillSlotWidget extends CustomButtonWidget
{
    public static final int SLOT_WIDTH = 68;
    public static final int SLOT_HEIGHT = 52;
    private static final int ICON_SIZE = 28;

    private final PowerupsScreenState state;
    private final Identifier jobLocation;
    private final float scale;

    public HyperSkillSlotWidget(PowerupsScreenState state, Identifier jobLocation, int x, int y, float scale)
    {
        super(x, y, Math.max(1, Math.round(SLOT_WIDTH * scale)), Math.max(1, Math.round(SLOT_HEIGHT * scale)),
                getJobName(state, jobLocation), null,
                button -> state.setSelectedHyperJobLocation(jobLocation));
        this.state = state;
        this.jobLocation = jobLocation;
        this.scale = scale;
    }

    public static Component getJobName(PowerupsScreenState state, Identifier jobLocation)
    {
        Job job = state.getJob(jobLocation);
        if (job != null)
        {
            return job.getJobInstance().getName();
        }
        return JobsPlus.translatable("job." + jobLocation.getNamespace() + "." + jobLocation.getPath() + ".name");
    }

    @Override
    protected void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick)
    {
        this.active = !this.state.isHyperRequestPending();
        Job job = this.state.getJob(this.jobLocation);
        boolean implemented = HyperSkillRules.supports(this.jobLocation);
        boolean hasIcon = HyperSkillRules.hasIcon(this.jobLocation);
        boolean available = false;
        if (implemented && job != null && job.getLevel() >= HyperSkillRules.REQUIRED_JOB_LEVEL)
        {
            available = true;
        }
        int border = JobsTheme.DISABLED;
        if (available)
        {
            border = JobsTheme.WARNING;
            if (job.getHyperSkill().level() > 0)
            {
                border = JobsTheme.SUCCESS;
            }
        }
        JobsTheme.cutBox(graphics, getX(), getY(), getWidth(), getHeight(), JobsTheme.INSET, border);
        if (this.jobLocation.equals(this.state.getSelectedHyperJobLocation()))
        {
            JobsTheme.inputFrame(graphics, getX(), getY(), getWidth(), getHeight(), JobsTheme.CYAN);
        }
        else if (isHoveredOrFocused())
        {
            JobsTheme.inputFrame(graphics, getX(), getY(), getWidth(), getHeight(), JobsTheme.TEXT);
        }

        int iconSize = Math.max(1, Math.round(ICON_SIZE * this.scale));
        int iconX = getX() + (getWidth() - iconSize) / 2;
        int iconY = getY() + Math.round(3 * this.scale);
        if (hasIcon)
        {
            Identifier icon = JobsPlus.getId("textures/gui/hyper/" + this.jobLocation.getPath() + ".png");
            graphics.blit(RenderPipelines.GUI_TEXTURED, icon, iconX, iconY,
                    0.0F, 0.0F, iconSize, iconSize, ICON_SIZE, ICON_SIZE, ICON_SIZE, ICON_SIZE);
            if (!available)
            {
                graphics.fill(iconX, iconY, iconX + iconSize, iconY + iconSize, 0x99081D25);
            }
        }
        else
        {
            // 이미지가 없는 예정 슬롯은 같은 X 모양을 공유한다.
            for (int offset = Math.round(4 * this.scale); offset < iconSize - Math.round(4 * this.scale); offset++)
            {
                graphics.fill(iconX + offset, iconY + offset,
                        iconX + offset + 2, iconY + offset + 2, JobsTheme.DISABLED);
                graphics.fill(iconX + iconSize - offset - 2, iconY + offset,
                        iconX + iconSize - offset, iconY + offset + 2, JobsTheme.DISABLED);
            }
        }

        Component status = JobsPlus.translatable("hyper.not_available");
        int statusColor = JobsTheme.MUTED;
        if (implemented)
        {
            status = JobsPlus.translatable("hyper.slot_requires_level");
            if (available)
            {
                status = JobsPlus.translatable("hyper.slot_open");
                statusColor = JobsTheme.WARNING;
                if (job.getHyperSkill().level() > 0)
                {
                    Component usage = JobsPlus.translatable("hyper.toggle_off");
                    if (job.getHyperSkill().active())
                    {
                        usage = JobsPlus.translatable("hyper.toggle_on");
                    }
                    status = JobsPlus.translatable("hyper.slot_level", job.getHyperSkill().level(), usage);
                    statusColor = JobsTheme.SUCCESS;
                }
            }
        }
        Component jobName = getJobName(this.state, this.jobLocation);
        setMessage(jobName.copy().append(Component.literal(" · ")).append(status));
        JobsTheme.label(graphics, jobName, getX() + 2, getY() + Math.round(32 * this.scale),
                Math.max(1, getWidth() - 4), Math.max(1, Math.round(9 * this.scale)), JobsTheme.TEXT,
                JobsTheme.LABEL_SCALE * this.scale);
        JobsTheme.label(graphics, status, getX() + 2, getY() + Math.round(42 * this.scale),
                Math.max(1, getWidth() - 4), Math.max(1, Math.round(8 * this.scale)), statusColor,
                JobsTheme.LABEL_SCALE * this.scale);
    }
}
