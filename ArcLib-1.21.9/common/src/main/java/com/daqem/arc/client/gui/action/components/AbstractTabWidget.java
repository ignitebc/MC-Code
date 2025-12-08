package com.daqem.arc.client.gui.action.components;

import com.daqem.arc.Arc;
import com.daqem.uilib.gui.component.sprite.SpriteComponent;
import com.daqem.uilib.gui.widget.ButtonWidget;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.ARGB;

public abstract class AbstractTabWidget extends ButtonWidget {

    private static final int TAB_WIDTH = 26;
    private static final int TAB_HEIGHT = 32;
    private static final WidgetSprites SPRITES = new WidgetSprites(
            Arc.getId("tab_top_unselected"),
            Arc.getId("tab_top_selected")
    );

    private boolean selected;
    private final SpriteComponent iconComponent;

    public AbstractTabWidget(int x, int y, boolean selected, SpriteComponent iconComponent, ActionComponent actionComponent, MutableComponent message) {
        super(x, y, TAB_WIDTH, TAB_HEIGHT, Component.empty(), button ->
                        actionComponent.selectTab((AbstractTabWidget) button),
                supplier -> CommonComponents.joinForNarration(message, supplier.get()));
        this.selected = selected;
        this.iconComponent = iconComponent;
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.iconComponent.setX(getX() + 4);
        this.iconComponent.setY(getY() + 6 + (isSelected() ? 0 : 2));

        guiGraphics.blitSprite(
                RenderPipelines.GUI_TEXTURED,
                SPRITES.get(isSelected(), isSelected()),
                this.getX(),
                this.getY(),
                this.getWidth(),
                this.getHeight(),
                ARGB.white(this.alpha)
        );
        this.iconComponent.renderBase(guiGraphics, mouseX, mouseY, partialTick, getWidth(), getHeight());
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }
}
