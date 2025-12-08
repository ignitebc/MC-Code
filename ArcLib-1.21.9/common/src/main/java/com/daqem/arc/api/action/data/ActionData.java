package com.daqem.arc.api.action.data;

import com.daqem.arc.api.action.IAction;
import com.daqem.arc.api.action.data.type.IActionDataType;
import com.daqem.arc.api.action.holder.IActionHolder;
import com.daqem.arc.api.action.type.ActionType;
import com.daqem.arc.api.action.type.IActionType;
import com.daqem.arc.api.player.ArcPlayer;
import com.daqem.arc.api.action.result.ActionResult;
import com.daqem.arc.event.events.ActionEvent;
import dev.architectury.event.EventResult;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class ActionData implements IActionData {

    private final ArcPlayer player;
    private final ActionType<?> actionType;
    private final Map<IActionDataType<?>, Object> actionData;
    private IActionHolder sourceActionHolder;

    public ActionData(ArcPlayer player, ActionType<?> actionType, Map<IActionDataType<?>, Object> actionData) {
        this.player = player;
        this.actionType = actionType;
        this.actionData = actionData;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> @Nullable T getData(IActionDataType<T> actionDataType) {
        return (T) this.actionData.get(actionDataType);
    }

    @Override
    public ArcPlayer getPlayer() {
        return player;
    }

    @Override
    public IActionType<?> getActionType() {
        return actionType;
    }

    @Override
    public ActionResult sendToAction() {
        EventResult result = ActionEvent.BEFORE_ACTION.invoker().registerBeforeAction(this);
        if (result != null && result.interruptsFurtherEvaluation()) {
            return new ActionResult().withCancelAction(true);
        }

        List<IAction> allPlayerActions = getPlayerActions();
        List<IAction> correctPlayerActions = allPlayerActions.stream()
                .filter(this::isTypeOfCurrentAction)
                .toList();

        return correctPlayerActions.stream()
                .map(this::performCurrentAction)
                .reduce(new ActionResult(), ActionResult::merge);
    }

    @Override
    public IActionHolder getSourceActionHolder() {
        return sourceActionHolder;
    }

    public void setSourceActionHolder(IActionHolder sourceActionHolder) {
        this.sourceActionHolder = sourceActionHolder;
    }

    private List<IAction> getPlayerActions() {
        return this.player.arc$getActionHolders().stream()
                .filter(Objects::nonNull)
                .flatMap(actionHolder -> actionHolder.getActions().stream())
                .collect(Collectors.toList());
    }

    private boolean isTypeOfCurrentAction(IAction action) {
        return action.getType() == this.actionType;
    }

    private ActionResult performCurrentAction(IAction action) {
        return action.perform(this);
    }
}
