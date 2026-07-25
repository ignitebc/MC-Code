package com.daqem.arc.api.condition.type;

import com.daqem.arc.api.condition.ICondition;
import com.daqem.arc.api.condition.serializer.IConditionSerializer;
import net.minecraft.resources.ResourceLocation;

public interface IConditionType<T extends ICondition> {

    ResourceLocation getLocation();

    IConditionSerializer<T> getSerializer();
}
