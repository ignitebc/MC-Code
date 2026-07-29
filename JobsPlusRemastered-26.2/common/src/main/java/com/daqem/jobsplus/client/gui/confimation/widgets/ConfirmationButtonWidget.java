package com.daqem.jobsplus.client.gui.confimation.widgets;

import com.daqem.jobsplus.JobsPlus;
import com.daqem.uilib.gui.widget.CustomButtonWidget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.network.chat.Component;

public class ConfirmationButtonWidget extends CustomButtonWidget
{

    public ConfirmationButtonWidget(int x, int y, int width, int height, Component message, OnPress onPress)
    {
        super(x, y, width, height, message, new WidgetSprites(JobsPlus.getId("confirmation/button"), JobsPlus.getId("confirmation/button_hovered")), onPress);
    }

    @Override
    protected void extractLabel(GuiGraphicsExtractor guiGraphics)
    {
        Font font = Minecraft.getInstance().font;
        int textColor = 0xFFEAF0FF;
        if (this.isHovered())
        {
            textColor = 0xFFFFFFFF;
        }

        int textX = this.getX() + (this.getWidth() - font.width(this.getMessage())) / 2 + 1;
        int textY = this.getY() + (this.getHeight() - 6) / 2;
        guiGraphics.text(font, this.getMessage(), textX, textY, textColor, false);
    }
}
