package com.daqem.arc.api.action.data;

import com.daqem.arc.api.action.data.type.IActionDataType;
import com.daqem.arc.api.action.holder.IActionHolder;
import com.daqem.arc.api.action.type.IActionType;
import com.daqem.arc.api.player.ArcPlayer;
import com.daqem.arc.api.action.result.ActionResult;
import org.jetbrains.annotations.Nullable;

public interface IActionData {

    <T> @Nullable T getData(IActionDataType<T> actionDataType);

    ArcPlayer getPlayer();

    IActionType<?> getActionType();

    ActionResult sendToAction();

    IActionHolder getSourceActionHolder();

}
