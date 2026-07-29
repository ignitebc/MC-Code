package com.daqem.arc.api.condition.type;

import com.daqem.arc.api.condition.ICondition;
import com.daqem.arc.api.condition.serializer.IConditionSerializer;
import net.minecraft.resources.Identifier;

public interface IConditionType<T extends ICondition> {

    Identifier getLocation();

    IConditionSerializer<T> getSerializer();
}
