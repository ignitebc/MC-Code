package com.daqem.arc.client.gui.holder.components;

import com.daqem.arc.Arc;
import com.daqem.arc.api.action.holder.IActionHolder;
import com.daqem.uilib.gui.component.sprite.SpriteComponent;

import java.util.List;

public class ActionHoldersComponent extends SpriteComponent {

    public ActionHoldersComponent(List<IActionHolder> actionHolders) {
        super(0, 0, 166, 200, Arc.getId("action_holders_background"));

        AHScrollContainerComponent scrollContainer = new AHScrollContainerComponent(actionHolders);

        this.addComponent(scrollContainer);
    }
}
