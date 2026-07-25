package com.daqem.arc.client.gui.action.widgets;

import com.daqem.uilib.gui.widget.ButtonWidget;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.item.properties.select.ComponentContents;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;

public class PageSwitchButtonWidget extends ButtonWidget {

    private final ResourceLocation spriteLocation;

    public PageSwitchButtonWidget(int x, int y, ResourceLocation spriteLocation, OnPress onPress, MutableComponent message) {
        super(x, y, 18, 18, Component.empty(), onPress, supplier -> CommonComponents.joinForNarration(message, supplier.get()));
        this.spriteLocation = spriteLocation;
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int i, int j, float f) {
        super.renderWidget(guiGraphics, i, j, f);
        guiGraphics.blitSprite(
                RenderPipelines.GUI_TEXTURED,
                this.spriteLocation,
                getX(),
                getY(),
                getWidth(),
                getHeight()
        );
    }
}
