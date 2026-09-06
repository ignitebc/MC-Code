package com.daqem.jobsplus.client.gui.jobs.widgets;

import com.daqem.jobsplus.client.gui.theme.JobsTheme;
import com.daqem.jobsplus.client.gui.jobs.JobsScreenState;
import com.daqem.jobsplus.client.gui.jobs.tab.RightTab;
import com.daqem.uilib.gui.widget.CustomButtonWidget;
import net.minecraft.client.gui.GuiGraphicsExtractor;

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
    protected void extractContents(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick)
    {
        boolean selected = this.state.getSelectedRightTab() == this.tab;
        JobsTheme.tab(guiGraphics, getX(), getY(), getWidth(), getHeight(), isHoveredOrFocused(), selected);
        JobsTheme.label(guiGraphics, getMessage(), getX(), getY(), getWidth(), getHeight(),
                selected || isHoveredOrFocused() ? JobsTheme.TEXT : JobsTheme.MUTED);
    }
}
