package com.daqem.jobsplus.client.gui.jobs.widgets;

import com.daqem.jobsplus.client.gui.jobs.JobsScreenState;
import com.daqem.jobsplus.client.gui.jobs.tab.UserGuideTab;
import com.daqem.jobsplus.client.gui.theme.JobsTheme;
import com.daqem.uilib.gui.widget.CustomButtonWidget;
import net.minecraft.client.gui.GuiGraphicsExtractor;

/** 게임안내의 주제를 전환하는 하위 탭 버튼. */
public class UserGuideTabWidget extends CustomButtonWidget
{
    private final JobsScreenState state;
    private final UserGuideTab tab;

    public UserGuideTabWidget(JobsScreenState state, UserGuideTab tab, int x, int width)
    {
        super(x, 0, width, JobsTheme.TAB_HEIGHT, tab.getName(), null,
                button -> state.setSelectedUserGuideTab(tab));
        this.state = state;
        this.tab = tab;
    }

    @Override
    protected void extractContents(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick)
    {
        boolean selected = this.state.getSelectedUserGuideTab() == this.tab;
        JobsTheme.tab(guiGraphics, getX(), getY(), getWidth(), getHeight(),
                isHoveredOrFocused(), selected);
        int textColor = selected || isHoveredOrFocused() ? JobsTheme.TEXT : JobsTheme.MUTED;
        JobsTheme.label(guiGraphics, getMessage(), getX(), getY(), getWidth(), getHeight(), textColor);
    }
}
