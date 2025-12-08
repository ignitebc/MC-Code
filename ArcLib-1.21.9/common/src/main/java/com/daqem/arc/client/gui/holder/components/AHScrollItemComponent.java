package com.daqem.arc.client.gui.holder.components;

import com.daqem.arc.Arc;
import com.daqem.arc.api.action.holder.IActionHolder;
import com.daqem.uilib.gui.component.sprite.SpriteComponent;
import com.daqem.uilib.gui.component.text.TextComponent;

public class AHScrollItemComponent extends SpriteComponent {

    public AHScrollItemComponent(IActionHolder actionHolder) {
        super(0, 0, 141, 25, Arc.getId("item_background"));

        TextComponent nameComponent = new TextComponent(5, 3, Arc.literal(actionHolder.getLocation().toString()), 0xFF2252F0);
        TextComponent actionsComponent = new TextComponent(5, 13, Arc.translatable("screen.action_holders.actions_count", actionHolder.getActions().size()));

        this.addComponent(nameComponent);
        this.addComponent(actionsComponent);
    }
}
