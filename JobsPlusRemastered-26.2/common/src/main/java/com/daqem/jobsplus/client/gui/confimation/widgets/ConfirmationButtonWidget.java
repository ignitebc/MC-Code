package com.daqem.jobsplus.client.gui.confimation.widgets;

import com.daqem.jobsplus.client.gui.theme.JobsTheme;
import com.daqem.uilib.gui.widget.CustomButtonWidget;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;

public class ConfirmationButtonWidget extends CustomButtonWidget
{
    private final boolean primary;

    public ConfirmationButtonWidget(int x, int y, int width, int height, Component message,
                                    boolean primary, OnPress onPress)
    {
        super(x, y, width, height, message, null, onPress);
        this.primary = primary;
    }

    @Override
    protected void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick)
    {
        JobsTheme.button(graphics, getX(), getY(), getWidth(), getHeight(),
                this.active, isHoveredOrFocused(), false, this.primary);
        JobsTheme.label(graphics, getMessage(), getX(), getY(), getWidth(), getHeight(),
                this.active ? JobsTheme.TEXT : JobsTheme.DISABLED);
    }
}
