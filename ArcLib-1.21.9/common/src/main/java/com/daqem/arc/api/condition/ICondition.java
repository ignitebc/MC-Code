package com.daqem.arc.api.condition;

import com.daqem.arc.api.action.data.ActionData;
import com.daqem.arc.api.condition.serializer.IConditionSerializer;
import com.daqem.arc.api.condition.type.IConditionType;
import com.daqem.arc.data.serializer.ArcSerializable;
import net.minecraft.network.chat.Component;

public interface ICondition extends ArcSerializable {

    IConditionType<? extends ICondition> getType();

    IConditionSerializer<? extends ICondition> getSerializer();

    boolean isMet(ActionData actionData);

    boolean isInverted();

    Component getName();

    Component getDescription(Object... args);

    Component getDescription();
}
