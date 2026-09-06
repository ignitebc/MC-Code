package com.daqem.jobsplus.client.gui.theme;

import com.daqem.uilib.gui.widget.CustomButtonWidget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;

public class JobsCloseButton extends CustomButtonWidget {
    public JobsCloseButton(int x, int y) {
        super(x, y, JobsTheme.BUTTON_HEIGHT, JobsTheme.BUTTON_HEIGHT, Component.literal("닫기"), null,
                button -> {
                    if (Minecraft.getInstance().gui.screen() != null) {
                        // Use the screen's normal close path, including stock-view cleanup.
                        Minecraft.getInstance().gui.screen().onClose();
                    }
                });
    }

    @Override
    protected void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        JobsTheme.cutBox(graphics, getX(), getY(), getWidth(), getHeight(),
                isHoveredOrFocused() ? 0xFF823B49 : 0xFF482E38, JobsTheme.ERROR);
        JobsTheme.label(graphics, Component.literal("×"), getX(), getY(), getWidth(), getHeight(), JobsTheme.TEXT);
    }
}
