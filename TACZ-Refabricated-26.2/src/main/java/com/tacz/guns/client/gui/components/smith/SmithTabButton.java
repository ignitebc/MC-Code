package com.tacz.guns.client.gui.components.smith;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

/** 화면 맨 위의 큰 분류 탭 (총기 / 부착물 / 탄약 / 기타). */
public class SmithTabButton extends Button {
    private final boolean selected;

    public SmithTabButton(int x, int y, int width, Component label, boolean selected, Button.OnPress onPress) {
        super(x, y, width, SmithTheme.TAB_HEIGHT, label, onPress, DEFAULT_NARRATION);
        this.selected = selected;
    }

    @Override
    protected void extractContents(GuiGraphicsExtractor gui, int mouseX, int mouseY, float partialTick) {
        SmithTheme.tab(gui, getX(), getY(), getWidth(), getHeight(), isHoveredOrFocused(), this.selected);
        SmithTheme.label(gui, getMessage(), getX(), getY(), getWidth(), getHeight(),
                this.selected || isHoveredOrFocused() ? SmithTheme.TEXT : SmithTheme.MUTED);
    }
}
