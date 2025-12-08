package com.daqem.arc.data.condition.entity;

import com.daqem.arc.api.action.data.ActionData;
import com.daqem.arc.api.action.data.type.ActionDataType;
import com.daqem.arc.api.condition.AbstractCondition;
import com.daqem.arc.api.condition.serializer.IConditionSerializer;
import com.daqem.arc.api.condition.type.ConditionType;
import com.daqem.arc.api.condition.type.IConditionType;
import com.google.gson.*;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Shearable;

public class ReadyForShearingCondition extends AbstractCondition {

    public ReadyForShearingCondition(boolean inverted) {
        super(inverted);
    }

    @Override
    public boolean isMet(ActionData actionData) {
        Entity entity = actionData.getData(ActionDataType.ENTITY);
        return entity instanceof Shearable shearable && shearable.readyForShearing();
    }

    @Override
    public IConditionType<?> getType() {
        return ConditionType.READY_FOR_SHEARING;
    }

    public static class Serializer implements IConditionSerializer<ReadyForShearingCondition> {

        @Override
        public ReadyForShearingCondition fromJson(ResourceLocation location, JsonObject jsonObject, boolean inverted) {
            return new ReadyForShearingCondition(inverted);
        }

        @Override
        public ReadyForShearingCondition fromNetwork(ResourceLocation location, RegistryFriendlyByteBuf friendlyByteBuf, boolean inverted) {
            return new ReadyForShearingCondition(inverted);
        }

        @Override
        public void toNetwork(RegistryFriendlyByteBuf friendlyByteBuf, ReadyForShearingCondition type) {
            IConditionSerializer.super.toNetwork(friendlyByteBuf, type);
        }
    }
}
