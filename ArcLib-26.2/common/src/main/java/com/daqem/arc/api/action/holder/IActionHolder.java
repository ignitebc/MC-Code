package com.daqem.arc.api.action.holder;

import com.daqem.arc.api.action.IAction;
import com.daqem.arc.api.action.data.ActionData;
import com.daqem.arc.api.action.holder.serializer.IActionHolderSerializer;
import com.daqem.arc.api.action.holder.type.IActionHolderType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public interface IActionHolder {

    Identifier getLocation();

    /**
     * 스킬 발동 알림 등에 노출할 표시 이름. 구현체(직업·파워업 등)가 재정의하며,
     * 이름이 없는 홀더는 null을 반환해 일반 문구로 대체된다.
     */
    default @Nullable Component getDisplayName() {
        return null;
    }

    List<IAction> getActions();

    void addAction(IAction action);

    void removeAction(IAction action);

    void clearActions();

    IActionHolderType<?> getType();

    IActionHolderSerializer<?> getSerializer();

    default boolean passedHolderCondition(ActionData actionData) {
        return true;
    }

    void addActions(List<IAction> actionHolderActions);
}
