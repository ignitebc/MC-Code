package com.daqem.arc.client.gui.holder.components;

import com.daqem.arc.api.action.holder.IActionHolder;
import com.daqem.uilib.gui.component.EmptyComponent;
import com.daqem.uilib.gui.widget.ScrollContainerWidget;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

public class AHScrollContainerComponent extends EmptyComponent {

    public AHScrollContainerComponent(List<IActionHolder> actionHolders) {
        super(8, 17, 151, 175);

        ScrollContainerWidget scrollContainerWidget = new ScrollContainerWidget(151, 175) {
            @Override
            protected void renderScrollbar(GuiGraphics guiGraphics, int i, int j) {
                super.renderScrollbar(guiGraphics, i, j);
                if (!this.scrollbarVisible()) {
                    guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, ResourceLocation.withDefaultNamespace("widget/scroller"), this.scrollBarX(), this.getY(), 6, this.getHeight());
                }
            }
        };
        scrollContainerWidget.addComponents(actionHolders.stream().map(AHScrollItemComponent::new).toList());

        this.addWidget(scrollContainerWidget);
    }
}
