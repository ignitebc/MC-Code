package com.daqem.jobsplus.client.gui.powerups.components;

import com.daqem.jobsplus.JobsPlus;
import com.daqem.jobsplus.client.gui.powerups.PowerupsScreenState;
import com.daqem.jobsplus.client.gui.theme.JobsTheme;
import com.daqem.uilib.gui.component.EmptyComponent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;

public class CoinsComponent extends EmptyComponent
{
    private final PowerupsScreenState state;

    public CoinsComponent(PowerupsScreenState state)
    {
        super(0, 0, (int) Math.ceil(Minecraft.getInstance().font.width(Integer.toString(state.getCoins()))
                * JobsTheme.LABEL_SCALE) + 24, JobsTheme.BUTTON_HEIGHT);
        this.state = state;
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY,
                                   float partialTick, int parentWidth, int parentHeight)
    {
        JobsTheme.sprite(graphics, JobsPlus.getId("jobs/coins"), getTotalX() + 4, getTotalY() + 3, 7, 8);
        JobsTheme.label(graphics, Component.literal(Integer.toString(this.state.getCoins())),
                getTotalX() + 14, getTotalY(), getWidth() - 14, getHeight(), JobsTheme.TEXT);
    }
}
