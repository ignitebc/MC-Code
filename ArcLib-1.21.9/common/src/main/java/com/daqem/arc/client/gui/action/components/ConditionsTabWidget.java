package com.daqem.arc.client.gui.action.components;

import com.daqem.arc.Arc;
import com.daqem.uilib.gui.component.sprite.SpriteComponent;

public class ConditionsTabWidget extends AbstractTabWidget {

    public ConditionsTabWidget(int x, int y, boolean selected, SpriteComponent iconComponent, ActionComponent actionComponent) {
        super(x, y, selected, iconComponent, actionComponent, Arc.translatable("screen.action.button.conditions"));
    }
}
