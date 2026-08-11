package com.daqem.arc.api.action.data;

import com.daqem.arc.api.action.data.type.IActionDataType;
import com.daqem.arc.api.action.holder.IActionHolder;
import com.daqem.arc.api.action.type.ActionType;
import com.daqem.arc.api.action.type.IActionType;
import com.daqem.arc.api.player.ArcPlayer;
import com.daqem.arc.api.action.result.ActionResult;
import com.daqem.arc.event.events.ActionEvent;
import com.daqem.arc.player.PlayerActionCache;
import dev.architectury.event.EventResult;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

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

    public <T> void setData(IActionDataType<T> actionDataType, T data) {
        this.actionData.put(actionDataType, data);
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

        // 이동처럼 매 틱 호출되는 경로이므로 매번 전체 액션을 모아 필터·정렬하는 대신
        // 타입별로 정렬해 둔 플레이어 캐시를 그대로 사용한다.
        List<PlayerActionCache.ActionEntry> actionEntries = this.player.arc$getActionsOfType(this.actionType);
        ActionResult mergedResult = new ActionResult();
        for (PlayerActionCache.ActionEntry actionEntry : actionEntries) {
            this.setSourceActionHolder(actionEntry.holder());
            mergedResult = mergedResult.merge(actionEntry.action().perform(this));
        }
        return mergedResult;
    }

    @Override
    public IActionHolder getSourceActionHolder() {
        return sourceActionHolder;
    }

    public void setSourceActionHolder(IActionHolder sourceActionHolder) {
        this.sourceActionHolder = sourceActionHolder;
    }
}
