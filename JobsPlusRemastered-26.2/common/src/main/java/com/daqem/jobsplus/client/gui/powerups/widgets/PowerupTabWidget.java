package com.daqem.jobsplus.client.gui.powerups.widgets;

import com.daqem.jobsplus.client.gui.powerups.PowerupsScreenState;
import com.daqem.jobsplus.client.gui.powerups.tab.PowerupTab;
import com.daqem.jobsplus.client.gui.theme.JobsTheme;
import com.daqem.uilib.gui.widget.CustomButtonWidget;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public class PowerupTabWidget extends CustomButtonWidget
{
    private final PowerupsScreenState state;
    private final PowerupTab tab;

    public PowerupTabWidget(PowerupsScreenState state, PowerupTab tab, int x, int y, int width)
    {
        super(x, y, width, JobsTheme.TAB_HEIGHT, tab.getName(), null, button -> state.setSelectedTab(tab));
        this.state = state;
        this.tab = tab;
    }

    @Override
    protected void extractContents(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick)
    {
        boolean selected = this.state.getSelectedTab() == this.tab;
        JobsTheme.tab(guiGraphics, getX(), getY(), getWidth(), getHeight(), isHoveredOrFocused(), selected);
        JobsTheme.label(guiGraphics, getMessage(), getX(), getY(), getWidth(), getHeight(),
                selected || isHoveredOrFocused() ? JobsTheme.TEXT : JobsTheme.MUTED);
    }
}
