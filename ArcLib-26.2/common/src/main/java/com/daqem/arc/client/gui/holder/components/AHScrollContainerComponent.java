package com.daqem.arc.client.gui.holder.components;

import com.daqem.arc.api.action.holder.IActionHolder;
import com.daqem.uilib.gui.component.EmptyComponent;
import com.daqem.uilib.gui.widget.ScrollContainerWidget;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class AHScrollContainerComponent extends EmptyComponent {

    public AHScrollContainerComponent(List<IActionHolder> actionHolders) {
        super(8, 17, 151, 175);

        ScrollContainerWidget scrollContainerWidget = new ScrollContainerWidget(151, 175) {
            @Override
            protected void extractScrollbar(@NotNull GuiGraphicsExtractor guiGraphics, int i, int j) {
                super.extractScrollbar(guiGraphics, i, j);
                if (!this.scrollable()) {
                    guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, Identifier.withDefaultNamespace("widget/scroller"), this.scrollBarX(), this.getY(), 6, this.getHeight());
                }
            }
        };
        scrollContainerWidget.addComponents(actionHolders.stream().map(AHScrollItemComponent::new).toList());

        this.addWidget(scrollContainerWidget);
    }
}
