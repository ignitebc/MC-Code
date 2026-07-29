package com.daqem.arc.client.gui.action.widgets;

import com.daqem.uilib.gui.widget.ButtonWidget;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

public class PageSwitchButtonWidget extends ButtonWidget {

    private final Identifier spriteLocation;

    public PageSwitchButtonWidget(int x, int y, Identifier spriteLocation, OnPress onPress, MutableComponent message) {
        super(x, y, 18, 18, Component.empty(), onPress, supplier -> CommonComponents.joinForNarration(message, supplier.get()));
        this.spriteLocation = spriteLocation;
    }

    @Override
    protected void extractContents(@NotNull GuiGraphicsExtractor guiGraphics, int i, int j, float f) {
        super.extractContents(guiGraphics, i, j, f);
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
