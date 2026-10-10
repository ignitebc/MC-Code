package com.daqem.uilib.api.widget;

import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import org.jetbrains.annotations.NotNull;

public interface IWidget extends Renderable, GuiEventListener, LayoutElement, NarratableEntry {

    @Override
    default @NotNull ScreenRectangle getRectangle() {
        return LayoutElement.super.getRectangle();
    }

    /*
     * 이것은 AbstractWidgetMixin에 구현되어 있다.
     * 위젯 클래스에 IWidget을 붙여야 하므로 컴파일 오류를 피하려고 여기에 기본 메서드를 둔다.
     */
    default int uilib$getParentX() {
        return 0;
    }
    default int uilib$getParentY() {
        return 0;
    }
    default void uilib$updateParentPosition(int parentX, int parentY) {
    }
}
