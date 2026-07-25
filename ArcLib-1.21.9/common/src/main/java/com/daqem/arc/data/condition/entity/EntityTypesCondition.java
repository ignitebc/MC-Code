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

import java.util.ArrayList;
import java.util.List;

public class EntityTypesCondition extends AbstractCondition {

    private final List<EntityType<?>> entityTypes;

    public EntityTypesCondition(boolean inverted, List<EntityType<?>> entityTypes) {
        super(inverted);
        this.entityTypes = entityTypes;
    }

    @Override
    public Component getDescription() {
        return getDescription((Object[]) entityTypes.stream().map(EntityType::getDescription).toArray(Component[]::new));
    }

    @Override
    public boolean isMet(ActionData actionData) {
        Entity entity = actionData.getData(ActionDataType.ENTITY);
        return entity != null && entityTypes.contains(entity.getType());
    }

    @Override
    public IConditionType<?> getType() {
        return ConditionType.ENTITY_TYPES;
    }

    public List<EntityType<?>> getEntityTypes() {
        return entityTypes;
    }

    public static class Serializer implements IConditionSerializer<EntityTypesCondition> {

        @Override
        public EntityTypesCondition fromJson(ResourceLocation location, JsonObject jsonObject, boolean inverted) {
            return new EntityTypesCondition(
                    inverted,
                    getEntityTypes(jsonObject, "entity_types"));
        }

        @Override
        public EntityTypesCondition fromNetwork(ResourceLocation location, RegistryFriendlyByteBuf friendlyByteBuf, boolean inverted) {
            int entityTypesSize = friendlyByteBuf.readVarInt();

            List<EntityType<?>> entityTypes = new ArrayList<>();

            for (int i = 0; i < entityTypesSize; i++) {
                entityTypes.add(BuiltInRegistries.ENTITY_TYPE.byId(friendlyByteBuf.readVarInt()));
            }

            return new EntityTypesCondition(
                    inverted,
                    entityTypes);
        }

        @Override
        public void toNetwork(RegistryFriendlyByteBuf friendlyByteBuf, EntityTypesCondition type) {
            IConditionSerializer.super.toNetwork(friendlyByteBuf, type);
            friendlyByteBuf.writeVarInt(type.entityTypes.size());
            for (EntityType<?> entityType : type.entityTypes) {
                friendlyByteBuf.writeVarInt(BuiltInRegistries.ENTITY_TYPE.getId(entityType));
            }
        }
    }
}
