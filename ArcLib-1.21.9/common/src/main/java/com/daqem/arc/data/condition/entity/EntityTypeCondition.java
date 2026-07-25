package com.daqem.arc.data.condition.entity;

import com.daqem.arc.api.action.data.ActionData;
import com.daqem.arc.api.action.data.type.ActionDataType;
import com.daqem.arc.api.condition.AbstractCondition;
import com.daqem.arc.api.condition.serializer.IConditionSerializer;
import com.daqem.arc.api.condition.type.ConditionType;
import com.daqem.arc.api.condition.type.IConditionType;
import com.google.gson.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;

public class EntityTypeCondition extends AbstractCondition {

    private final EntityType<?> entityType;

    public EntityTypeCondition(boolean inverted, EntityType<?> entityType) {
        super(inverted);
        this.entityType = entityType;
    }

    @Override
    public Component getDescription() {
        return getDescription(entityType.getDescription());
    }

    @Override
    public boolean isMet(ActionData actionData) {
        Entity entity = actionData.getData(ActionDataType.ENTITY);
        return entity != null && entity.getType() == entityType;
    }

    @Override
    public IConditionType<?> getType() {
        return ConditionType.ENTITY_TYPE;
    }

    public EntityType<?> getEntityType() {
        return entityType;
    }

    public static class Serializer implements IConditionSerializer<EntityTypeCondition> {

        @Override
        public EntityTypeCondition fromJson(ResourceLocation location, JsonObject jsonObject, boolean inverted) {
            return new EntityTypeCondition(
                    inverted,
                    getEntityType(jsonObject, "entity_type"));
        }

        @Override
        public EntityTypeCondition fromNetwork(ResourceLocation location, RegistryFriendlyByteBuf friendlyByteBuf, boolean inverted) {
            return new EntityTypeCondition(
                    inverted,
                    BuiltInRegistries.ENTITY_TYPE.byId(friendlyByteBuf.readVarInt()));
        }

        @Override
        public void toNetwork(RegistryFriendlyByteBuf friendlyByteBuf, EntityTypeCondition type) {
            IConditionSerializer.super.toNetwork(friendlyByteBuf, type);
            friendlyByteBuf.writeVarInt(BuiltInRegistries.ENTITY_TYPE.getId(type.entityType));
        }
    }
}
