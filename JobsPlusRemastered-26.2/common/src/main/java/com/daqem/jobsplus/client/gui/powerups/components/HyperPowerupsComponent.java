package com.daqem.jobsplus.client.gui.powerups.components;

import com.daqem.jobsplus.client.gui.theme.JobsTheme;
import com.daqem.uilib.gui.component.EmptyComponent;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;

/** 하이퍼 스킬 탭의 자리 표시자. 기능 구현 전까지 안내 문구만 보여준다. */
public class HyperPowerupsComponent extends EmptyComponent
{
    public HyperPowerupsComponent(int x, int y, int width, int height)
    {
        super(x, y, width, height);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY,
                                   float partialTick, int parentWidth, int parentHeight)
    {
        JobsTheme.label(graphics, Component.literal("하이퍼 스킬은 준비 중입니다"),
                getTotalX(), getTotalY(), getWidth(), getHeight(), JobsTheme.MUTED);
    }
}
