package com.daqem.arc.client.gui.action.components;

import com.daqem.uilib.gui.component.EmptyComponent;
import com.daqem.uilib.gui.widget.ScrollContainerWidget;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ScrollContainerComponent extends EmptyComponent {

    ScrollContainerWidget scrollContainerWidget;

    public ScrollContainerComponent(int x, int y, int width, int height, List<ScrollItemComponent> items) {
        super(x, y, width, height);
        this.scrollContainerWidget = new ScrollContainerWidget(width, height) {
            @Override
            protected void extractScrollbar(@NotNull GuiGraphicsExtractor guiGraphics, int i, int j) {
                super.extractScrollbar(guiGraphics, i, j);
                if (!this.scrollable()) {
                    guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, Identifier.withDefaultNamespace("widget/scroller"), this.scrollBarX(), this.getY(), 6, this.getHeight());
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
