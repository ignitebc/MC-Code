package com.daqem.arc.client.gui.action.components;

import com.daqem.uilib.gui.component.EmptyComponent;
import com.daqem.uilib.gui.widget.ScrollContainerWidget;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

public class ScrollContainerComponent extends EmptyComponent {

    ScrollContainerWidget scrollContainerWidget;

    public ScrollContainerComponent(int x, int y, int width, int height, List<ScrollItemComponent> items) {
        super(x, y, width, height);
        this.scrollContainerWidget = new ScrollContainerWidget(width, height) {
            @Override
            protected void renderScrollbar(GuiGraphics guiGraphics, int i, int j) {
                super.renderScrollbar(guiGraphics, i, j);
                if (!this.scrollbarVisible()) {
                    guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, ResourceLocation.withDefaultNamespace("widget/scroller"), this.scrollBarX(), this.getY(), 6, this.getHeight());
                }
            }
        };
        this.scrollContainerWidget.addComponents(items);

        this.addWidget(this.scrollContainerWidget);
    }

    public void setItems(List<ScrollItemComponent> items) {
        this.scrollContainerWidget.clearComponents();
        this.scrollContainerWidget.addComponents(items);
    }
}
