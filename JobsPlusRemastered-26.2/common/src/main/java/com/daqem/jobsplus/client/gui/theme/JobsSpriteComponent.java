package com.daqem.jobsplus.client.gui.theme;

import com.daqem.uilib.gui.component.sprite.SpriteComponent;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;

/** Keeps UILib positioning and children while drawing the JobsPlus skin. */
public class JobsSpriteComponent extends SpriteComponent {
    private final Identifier sprite;

    public JobsSpriteComponent(int x, int y, int width, int height, Identifier sprite) {
        super(x, y, width, height, sprite);
        this.sprite = sprite;
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY,
                                   float partialTick, int parentWidth, int parentHeight) {
        JobsTheme.sprite(graphics, sprite, getTotalX(), getTotalY(), getWidth(), getHeight());
    }
}
