package com.daqem.arc.client.gui.action.components;

import com.daqem.arc.Arc;
import com.daqem.uilib.gui.component.sprite.SpriteComponent;

public class RewardsTabWidget extends AbstractTabWidget {
    public RewardsTabWidget(int x, int y, boolean selected, SpriteComponent iconComponent, ActionComponent actionComponent) {
        super(x, y, selected, iconComponent, actionComponent, Arc.translatable("screen.action.button.rewards"));
    }
}
