package com.daqem.jobsplus.client.gui.jobs.widgets;

import com.daqem.jobsplus.client.gui.jobs.JobsScreenState;
import com.daqem.jobsplus.client.gui.jobs.tab.AchievementTab;
import com.daqem.jobsplus.client.gui.theme.JobsTheme;
import com.daqem.uilib.gui.widget.CustomButtonWidget;
import net.minecraft.client.gui.GuiGraphicsExtractor;

/** 업적 탭에서 업적과 칭호 화면을 전환하는 하위 탭 버튼. */
public class AchievementTabWidget extends CustomButtonWidget
{
    /** 바로 위 상위 탭(16px, 0.85배)보다 한 단계 작게 두어 하위 탭임을 드러낸다. */
    public static final int HEIGHT = JobsTheme.BUTTON_HEIGHT;
    private static final float LABEL_SCALE = 0.75f;

    private final JobsScreenState state;
    private final AchievementTab tab;

    public AchievementTabWidget(JobsScreenState state, AchievementTab tab, int x, int width)
    {
        super(x, 0, width, HEIGHT, tab.getName(), null,
                button -> state.setSelectedAchievementTab(tab));
        this.state = state;
        this.tab = tab;
    }

    @Override
    protected void extractContents(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick)
    {
        boolean selected = this.state.getSelectedAchievementTab() == this.tab;
        JobsTheme.tab(guiGraphics, getX(), getY(), getWidth(), getHeight(),
                isHoveredOrFocused(), selected);
        int textColor = selected || isHoveredOrFocused() ? JobsTheme.TEXT : JobsTheme.MUTED;
        JobsTheme.label(guiGraphics, getMessage(), getX(), getY(), getWidth(), getHeight(), textColor,
                LABEL_SCALE);
    }
}
