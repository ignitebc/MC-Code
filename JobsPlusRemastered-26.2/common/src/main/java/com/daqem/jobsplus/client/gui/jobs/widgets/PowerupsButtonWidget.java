package com.daqem.jobsplus.client.gui.jobs.widgets;

import com.daqem.jobsplus.client.gui.theme.JobsTheme;
import com.daqem.jobsplus.JobsPlus;
import com.daqem.jobsplus.client.gui.jobs.JobsScreenState;
import com.daqem.jobsplus.client.gui.powerups.PowerupsScreen;
import com.daqem.jobsplus.client.gui.powerups.PowerupsScreenState;
import com.daqem.uilib.gui.widget.CustomButtonWidget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;

public class PowerupsButtonWidget extends CustomButtonWidget
{

    private final static Component MESSAGE = JobsPlus.translatable("gui.jobs.powerups");

    private final JobsScreenState state;

    public PowerupsButtonWidget(JobsScreenState state)
    {
        super(211, 212, Minecraft.getInstance().font.width(MESSAGE) + 14, JobsTheme.BUTTON_HEIGHT, MESSAGE, null, button -> Minecraft.getInstance().gui.setScreen(new PowerupsScreen(new PowerupsScreenState(state.getSelectedJob(), state.getCoins()), Minecraft.getInstance().gui.screen())));
        this.state = state;
    }

    @Override
    protected void extractContents(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick)
    {
        JobsTheme.button(guiGraphics, getX(), getY(), getWidth(), getHeight(),
                this.active, isHoveredOrFocused(), false, true);
        JobsTheme.label(guiGraphics, getMessage(), getX(), getY(), getWidth(), getHeight(),
                this.active ? JobsTheme.TEXT : JobsTheme.DISABLED);
    }
}
