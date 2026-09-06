package com.daqem.jobsplus.client.gui.theme;

import com.daqem.uilib.gui.widget.EditBoxWidget;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;

/** Only the frame changes; editing, selection, validation and narration remain in UILib. */
public class JobsEditBox extends EditBoxWidget {
    public JobsEditBox(Font font, int x, int y, int width, int height, Component title) {
        super(font, x, y, width, height, title);
        setTextColor(JobsTheme.TEXT);
        setTextColorUneditable(JobsTheme.DISABLED);
    }

    @Override
    public void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractWidgetRenderState(graphics, mouseX, mouseY, partialTick);
        // Overlay the four border lines without covering the native caret or selection.
        int color = JobsTheme.BORDER;
        if (!getInputValidationErrors().isEmpty()) {
            color = JobsTheme.ERROR;
        } else if (isFocused()) {
            color = JobsTheme.CYAN;
        }
        graphics.fill(getX(), getY(), getX() + getWidth(), getY() + 1, color);
        graphics.fill(getX(), getY() + getHeight() - 1, getX() + getWidth(), getY() + getHeight(), color);
        graphics.fill(getX(), getY(), getX() + 1, getY() + getHeight(), color);
        graphics.fill(getX() + getWidth() - 1, getY(), getX() + getWidth(), getY() + getHeight(), color);
    }
}
