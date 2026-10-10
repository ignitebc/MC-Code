package com.daqem.jobsplus.client.gui.theme;

import com.daqem.uilib.gui.widget.EditBoxWidget;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;

/** 테두리만 바꾼다. 편집·선택·검증·내레이션은 UILib가 그대로 처리한다. */
public class JobsEditBox extends EditBoxWidget {
    public JobsEditBox(Font font, int x, int y, int width, int height, Component title) {
        super(font, x, y, width, height, title);
        setTextColor(JobsTheme.TEXT);
        setTextColorUneditable(JobsTheme.DISABLED);
    }

    @Override
    public void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractWidgetRenderState(graphics, mouseX, mouseY, partialTick);
        // 기본 커서와 선택 영역을 가리지 않도록 테두리 네 줄만 덧그린다.
        int color = JobsTheme.BORDER;
        if (!getInputValidationErrors().isEmpty()) {
            color = JobsTheme.ERROR;
        } else if (isFocused()) {
            color = JobsTheme.CYAN;
        }
        JobsTheme.inputFrame(graphics, getX(), getY(), getWidth(), getHeight(), color);
    }
}
