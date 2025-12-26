package com.daqem.jobsplus.client.gui.jobs.widgets;

import net.minecraft.network.chat.Component;

public final class ShopTooltipState
{
    private static Component hoveredName;
    private static int mouseX;
    private static int mouseY;

    private ShopTooltipState() {}

    public static void clear()
    {
        hoveredName = null;
    }

    public static void setHoveredName(Component name, int x, int y)
    {
        hoveredName = name;
        mouseX = x;
        mouseY = y;
    }

    public static Component getHoveredName()
    {
        return hoveredName;
    }

    public static int getMouseX()
    {
        return mouseX;
    }

    public static int getMouseY()
    {
        return mouseY;
    }
}
