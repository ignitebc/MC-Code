package com.daqem.uilib.gui.widget;

import com.daqem.uilib.api.widget.IWidget;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.util.function.Function;
import java.util.function.Supplier;

public class CycleButtonWidget<T> extends CycleButton<T> implements IWidget {

    public CycleButtonWidget(int i, int j, int k, int l, Component component, int m, T object, ValueListSupplier<T> valueListSupplier, Function<T, Component> function, Function<CycleButton<T>, MutableComponent> function2, OnValueChange<T> onValueChange, OptionInstance.TooltipSupplier<T> tooltipSupplier, boolean displayOnlyValue) {
        this(i, j, k, l, getInitialMessage(component, object, function, displayOnlyValue), component, m, object, () -> object, valueListSupplier, function, function2, onValueChange, tooltipSupplier, getDisplayState(displayOnlyValue), (cycleButton, value) -> null);
    }

    public CycleButtonWidget(int i, int j, int k, int l, Component component, Component component2, int m, T object, Supplier<T> supplier, ValueListSupplier<T> valueListSupplier, Function<T, Component> function, Function<CycleButton<T>, MutableComponent> function2, OnValueChange<T> onValueChange, OptionInstance.TooltipSupplier<T> tooltipSupplier, DisplayState displayState, SpriteSupplier<T> spriteSupplier) {
        super(i, j, k, l, component, component2, m, object, supplier, valueListSupplier, function, function2, onValueChange, tooltipSupplier, displayState, spriteSupplier);
    }

    public CycleButtonWidget(int i, int j, int k, int l, Component component, Component component2, int m, T object, Supplier<T> supplier, ValueListSupplier<T> valueListSupplier, Function<T, Component> function) {
        super(i, j, k, l, component, component2, m, object, supplier, valueListSupplier, function, CycleButton::createDefaultNarrationMessage, (cycleButton, o) -> {
        }, o -> null, DisplayState.NAME_AND_VALUE, (cycleButton, object1) -> null);
    }

    private static DisplayState getDisplayState(boolean displayOnlyValue) {
        if (displayOnlyValue) {
            return DisplayState.VALUE;
        }

        return DisplayState.NAME_AND_VALUE;
    }

    private static <T> Component getInitialMessage(Component name, T value, Function<T, Component> valueStringifier, boolean displayOnlyValue) {
        Component valueComponent = valueStringifier.apply(value);
        if (displayOnlyValue) {
            return valueComponent;
        }

        return CommonComponents.optionNameValue(name, valueComponent);
    }
}
