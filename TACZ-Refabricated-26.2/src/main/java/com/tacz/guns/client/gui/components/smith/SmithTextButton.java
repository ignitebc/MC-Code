package com.tacz.guns.client.gui.components.smith;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

/** 글자만 있는 단추. 제작 단추는 강조색, 닫기 단추는 붉은색으로 그린다. */
public class SmithTextButton extends Button {
    public enum Style {
        PRIMARY, CLOSE
    }

    private final Style style;

    public SmithTextButton(int x, int y, int width, int height, Component label, Style style, Button.OnPress onPress) {
        super(x, y, width, height, label, onPress, DEFAULT_NARRATION);
        this.style = style;
    }

    @Override
    protected void extractContents(GuiGraphicsExtractor gui, int mouseX, int mouseY, float partialTick) {
        if (this.style == Style.CLOSE) {
            SmithTheme.tinted(gui, getX(), getY(), getWidth(), getHeight(),
                    isHoveredOrFocused() ? 0xFFFF8E9B : 0xFFDB6575);
        } else {
            SmithTheme.button(gui, getX(), getY(), getWidth(), getHeight(), this.active, isHoveredOrFocused(), true);
        }
        SmithTheme.label(gui, getMessage(), getX(), getY(), getWidth(), getHeight(),
                this.active ? SmithTheme.TEXT : SmithTheme.DISABLED_TEXT);
    }
}
