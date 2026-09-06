package com.daqem.jobsplus.client.gui.jobs.components;

import com.daqem.jobsplus.JobsPlus;
import com.daqem.jobsplus.client.gui.jobs.JobsScreenState;
import com.daqem.jobsplus.client.gui.theme.JobsTheme;
import com.daqem.uilib.gui.component.EmptyComponent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;

public class CoinsComponent extends EmptyComponent {
    private final JobsScreenState state;

    public CoinsComponent(JobsScreenState state) {
        super(0, 0, 20 + (int) Math.ceil(Minecraft.getInstance().font.width("직업 코인 " + state.getCoins())
                * JobsTheme.LABEL_SCALE), 14);
        this.state = state;
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY,
                                   float partialTick, int parentWidth, int parentHeight) {
        JobsTheme.sprite(graphics, JobsPlus.getId("jobs/coins"), getTotalX() + 3, getTotalY() + 3, 7, 8);
        JobsTheme.text(graphics, Component.literal("직업 코인 " + state.getCoins()),
                getTotalX() + 14, getTotalY() + 3, getWidth() - 16, JobsTheme.TEXT);
    }
}
