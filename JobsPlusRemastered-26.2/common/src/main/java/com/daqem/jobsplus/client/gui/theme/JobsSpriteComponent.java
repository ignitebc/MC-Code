package com.daqem.jobsplus.client.gui.theme;

import com.daqem.uilib.gui.component.sprite.SpriteComponent;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;

/** UILib의 배치와 자식 구성은 그대로 두고 JobsPlus 스킨만 그린다. */
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
