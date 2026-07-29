package com.daqem.arc.api.reward;

import com.daqem.arc.api.action.data.ActionData;
import com.daqem.arc.api.action.result.ActionResult;
import com.daqem.arc.api.reward.serializer.IRewardSerializer;
import com.daqem.arc.api.reward.type.IRewardType;
import com.daqem.arc.data.serializer.ArcSerializable;
import net.minecraft.network.chat.Component;

public interface IReward extends ArcSerializable {

    IRewardType<?> getType();

    IRewardSerializer<? extends IReward> getSerializer();

    double getChance();

    int getPriority();

    ActionResult apply(ActionData actionData);

    boolean passedChance(ActionData actionData);

    Component getName();

    Component getDescription(Object... args);

    Component getDescription();
}
