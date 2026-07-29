package com.daqem.arc.api.action;

import com.daqem.arc.api.action.data.ActionData;
import com.daqem.arc.api.action.holder.type.IActionHolderType;
import com.daqem.arc.api.action.result.ActionResult;
import com.daqem.arc.api.action.serializer.IActionSerializer;
import com.daqem.arc.api.action.type.IActionType;
import com.daqem.arc.api.condition.ICondition;
import com.daqem.arc.api.reward.IReward;
import com.daqem.arc.data.serializer.ArcSerializable;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import java.util.List;

public interface IAction extends ArcSerializable {

    IActionType<?> getType();

    IActionSerializer<?> getSerializer();

    ActionResult perform(ActionData actionData);

    boolean metConditions(ActionData actionData);

    ActionResult applyRewards(ActionData actionData);

    IActionHolderType<?> getActionHolderType();

    Identifier getActionHolderLocation();

    boolean shouldPerformOnClient();

    Identifier getLocation();

    Component getName();

    Component getDescription();

    Component getShortDescription();

    List<IReward> getRewards();

    List<ICondition> getConditions();
}
