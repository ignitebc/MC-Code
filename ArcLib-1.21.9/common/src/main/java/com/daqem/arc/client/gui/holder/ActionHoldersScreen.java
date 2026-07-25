package com.daqem.arc.client.gui.holder;

import com.daqem.arc.Arc;
import com.daqem.arc.api.action.holder.IActionHolder;
import com.daqem.arc.client.gui.holder.components.ActionHoldersComponent;
import com.daqem.uilib.gui.AbstractScreen;
import com.daqem.uilib.gui.background.BlurredBackground;
import com.daqem.uilib.gui.component.text.TextComponent;

import java.util.List;

public class ActionHoldersScreen extends AbstractScreen {

    private final List<IActionHolder> actionHolders;

    public ActionHoldersScreen(List<IActionHolder> actionHolders) {
        super(Arc.translatable("screen.action_holders"));
        this.actionHolders = actionHolders;

        setBackground(new BlurredBackground());
    }

    @Override
    protected void init() {
        ActionHoldersComponent actionHoldersComponent = new ActionHoldersComponent(this.actionHolders);
        actionHoldersComponent.center();

        TextComponent titleComponent = new TextComponent(8, 7, this.title, 0xFF555555);
        actionHoldersComponent.addComponent(titleComponent);

        this.addComponent(actionHoldersComponent);

        super.init();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
