package com.daqem.arc.api.action.data;

import com.daqem.arc.api.action.data.type.IActionDataType;
import com.daqem.arc.api.action.type.ActionType;
import com.daqem.arc.api.player.ArcPlayer;

import java.util.HashMap;
import java.util.Map;

public class ActionDataBuilder {

    private final ArcPlayer player;
    private final ActionType<?> actionType;
    private final Map<IActionDataType<?>, Object> actionData;

    public ActionDataBuilder(ArcPlayer player, ActionType<?> actionType) {
        this.player = player;
        this.actionType = actionType;
        this.actionData = new HashMap<>();
    }

    public <T> ActionDataBuilder withData(IActionDataType<T> actionDataType, T value) {
        this.actionData.put(actionDataType, value);
        return this;
    }

    public ActionData build() {
        return new ActionData(this.player, this.actionType, this.actionData);
    }
}
