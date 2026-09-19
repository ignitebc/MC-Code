package com.tacz.guns.client.gui.components.smith;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

/** 아이콘과 이름이 한 줄에 놓이는 목록 단추. 분류 목록과 제작 목록이 함께 쓴다. */
public class SmithRowButton extends Button {
    public static final int HEIGHT = 18;

    private final ItemStack icon;
    private final boolean selected;
    private final boolean showItemTooltip;

    public SmithRowButton(int x, int y, int width, ItemStack icon, Component label, boolean selected,
                          boolean showItemTooltip, Button.OnPress onPress) {
        super(x, y, width, HEIGHT, label, onPress, DEFAULT_NARRATION);
        this.icon = icon;
        this.selected = selected;
        this.showItemTooltip = showItemTooltip;
    }

    @Override
    protected void extractContents(GuiGraphicsExtractor gui, int mouseX, int mouseY, float partialTick) {
        SmithTheme.button(gui, getX(), getY(), getWidth(), getHeight(), true, isHoveredOrFocused(), this.selected);
        gui.item(this.icon, getX() + 2, getY() + 1);
        int color = this.selected || isHoveredOrFocused() ? SmithTheme.TEXT : SmithTheme.MUTED;
        SmithTheme.text(gui, getMessage(), getX() + 21, getY() + 5, getWidth() - 25, color);
    }

    /** 마우스를 올린 제작 결과물의 아이템 툴팁. 분류 줄에는 없다. */
    public ItemStack tooltipStack() {
        return this.showItemTooltip && isHoveredOrFocused() ? this.icon : ItemStack.EMPTY;
    }
}
