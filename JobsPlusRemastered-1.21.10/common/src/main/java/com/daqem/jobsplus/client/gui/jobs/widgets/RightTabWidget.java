package com.daqem.jobsplus.client.gui.jobs.widgets;

import com.daqem.jobsplus.JobsPlus;
import com.daqem.jobsplus.client.gui.jobs.JobsScreenState;
import com.daqem.jobsplus.client.gui.jobs.tab.RightTab;
import com.daqem.uilib.gui.widget.CustomButtonWidget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.util.ARGB;

public class RightTabWidget extends CustomButtonWidget
{
    private final JobsScreenState state;
    private final RightTab tab;

    public RightTabWidget(JobsScreenState state, RightTab tab, int width, int height)
    {
        super(0, 0, width, height, tab.getName(), null, button -> state.setSelectedRightTab(tab));
        this.state = state;
        this.tab = tab;
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick)
    {
        boolean selected = this.state.getSelectedRightTab() == this.tab;
        int textX = this.getX() + (this.getWidth() - Minecraft.getInstance().font.width(this.getMessage())) / 2;
        int textColor = selected || this.isHoveredOrFocused()
                ? this.state.getSelectedJob().getJobInstance().getColorDecimal()
                : 0x1E1410;

        guiGraphics.blitSprite(
                RenderPipelines.GUI_TEXTURED,
                JobsPlus.getId("jobs/tab_bottom"),
                this.getX(),
                this.getY(),
                this.getWidth(),
                this.getHeight(),
                ARGB.white(this.alpha)
        );
        guiGraphics.drawString(
                Minecraft.getInstance().font,
                this.getMessage(),
                textX,
                this.getY() + 6,
                ARGB.color(this.alpha, textColor),
                false
        );
    }
}
