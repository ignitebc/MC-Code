package com.daqem.arc.client.gui.action;

import com.daqem.arc.Arc;
import com.daqem.arc.api.action.IAction;
import com.daqem.arc.client.gui.action.components.ActionComponent;
import com.daqem.arc.client.gui.action.widgets.PageSwitchButtonWidget;
import com.daqem.uilib.gui.AbstractScreen;
import com.daqem.uilib.gui.background.BlurredBackground;
import net.minecraft.client.input.KeyEvent;
import org.lwjgl.glfw.GLFW;

import java.util.List;

public class ActionScreen extends AbstractScreen {

    private static final int KEY_BACK = GLFW.GLFW_KEY_LEFT;
    private static final int KEY_FORWARD = GLFW.GLFW_KEY_RIGHT;

    private final List<IAction> actions;
    private IAction selectedAction;
    public ActionComponent actionComponent;
    public PageSwitchButtonWidget arrowLeftComponent;
    public PageSwitchButtonWidget arrowRightComponent;

    public ActionScreen(List<IAction> actions, IAction selectedAction) {
        super(Arc.translatable("screen.action"));
        this.actions = actions;
        this.selectedAction = selectedAction;

        setBackground(new BlurredBackground());
    }

    @Override
    public void init() {
        this.actionComponent = new ActionComponent(getCurrentIndex(), selectedAction);
        this.actionComponent.center();

        this.arrowLeftComponent = new PageSwitchButtonWidget(0, 0, Arc.getId("left_button"), button -> moveToPreviousActionComponent(), Arc.translatable("screen.action.button.previous"));
        this.arrowRightComponent = new PageSwitchButtonWidget(0, 0, Arc.getId("right_button"), button -> moveToNextActionComponent(), Arc.translatable("screen.action.button.next"));

        this.addComponent(actionComponent);
        this.addWidget(this.arrowLeftComponent);
        this.addWidget(this.arrowRightComponent);

        super.init();

        this.arrowLeftComponent.setX(this.actionComponent.getX());
        this.arrowLeftComponent.setY(this.actionComponent.getY() + this.actionComponent.getHeight() + 5);
        this.arrowRightComponent.setX(this.actionComponent.getX() + this.actionComponent.getWidth() - this.arrowRightComponent.getWidth());
        this.arrowRightComponent.setY(this.actionComponent.getY() + this.actionComponent.getHeight() + 5);
    }

    private int getCurrentIndex() {
        return actions.indexOf(selectedAction);
    }

    @Override
    public boolean keyPressed(KeyEvent keyEvent) {
        if (keyEvent.key() == KEY_BACK) {
            moveToPreviousActionComponent();
            return true;
        } else if (keyEvent.key() == KEY_FORWARD) {
            moveToNextActionComponent();
            return true;
        }
        return super.keyPressed(keyEvent);
    }

    private void moveToActionComponent(IAction action) {
        this.selectedAction = action;
        this.repositionElements();
    }

    private IAction getNextAction() {
        int nextIndex = getCurrentIndex() + 1;
        if (nextIndex >= actions.size()) {
            nextIndex = 0;
        }
        if (nextIndex == getCurrentIndex()) {
            return selectedAction;
        }
        return actions.get(nextIndex);
    }

    private IAction getPreviousAction() {
        int previousIndex = getCurrentIndex() - 1;
        if (previousIndex < 0) {
            previousIndex = actions.size() - 1;
        }
        if (previousIndex == getCurrentIndex()) {
            return selectedAction;
        }
        return actions.get(previousIndex);
    }

    private void moveToPreviousActionComponent() {
        moveToActionComponent(getPreviousAction());
    }

    private void moveToNextActionComponent() {
        moveToActionComponent(getNextAction());
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
