package com.daqem.arc.client.gui.action.components;

import com.daqem.arc.Arc;
import com.daqem.uilib.gui.component.sprite.SpriteComponent;
import com.daqem.uilib.gui.component.text.TruncatedTextComponent;
import com.daqem.uilib.gui.component.text.multiline.MultiLineTextComponent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ScrollItemComponent extends SpriteComponent {

    private static final int PADDING = 4;
    private static final int TEXT_SPACING = 2;
    private final static int WIDTH = 152;

    private final Component description;

    public ScrollItemComponent(Component name, Component description) {
        super(0, 0, WIDTH, 0, Arc.getId("item_background"));
        this.description = description;
        int lineHeight = Minecraft.getInstance().font.lineHeight;

        TruncatedTextComponent nameText = new TruncatedTextComponent(PADDING, PADDING, WIDTH - (PADDING * 2), name);
        MultiLineTextComponent descriptionText = new MultiLineTextComponent(PADDING, PADDING + lineHeight + TEXT_SPACING, WIDTH - (PADDING * 2), description, 0xFF555555);

        this.addComponent(nameText);
        this.addComponent(descriptionText);

        this.setHeight(
                PADDING + // Top padding
                lineHeight + // Name height
                TEXT_SPACING + // Spacing between name and description
                descriptionText.getLines().size() * lineHeight + // Description height
                PADDING // Bottom padding
        );
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick, int parentWidth, int parentHeight) {
        super.render(guiGraphics, mouseX, mouseY, partialTick, parentWidth, parentHeight);
        if (getRectangle().containsPoint(mouseX, mouseY)) {
            guiGraphics.setTooltipForNextFrame(
                    Minecraft.getInstance().font,
                    Language.getInstance().getVisualOrder(List.of(description)),
                    mouseX,
                    mouseY
            );
        }
    }

    @Override
    public @NotNull ScreenRectangle getRectangle() {
        return new ScreenRectangle(this.getTotalX(), this.getTotalY(), this.getWidth(), this.getHeight());
    }
}
