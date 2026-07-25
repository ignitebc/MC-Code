package com.daqem.arc.data.condition;

import com.daqem.arc.api.action.data.ActionData;
import com.daqem.arc.api.condition.AbstractCondition;
import com.daqem.arc.api.condition.ICondition;
import com.daqem.arc.api.condition.serializer.IConditionSerializer;
import com.daqem.arc.api.condition.type.ConditionType;
import com.daqem.arc.api.condition.type.IConditionType;
import com.daqem.arc.registry.ArcRegistry;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;

import java.util.ArrayList;
import java.util.List;

public class NotCondition extends AbstractCondition {

    private final List<ICondition> conditions = new ArrayList<>();

    public NotCondition(boolean inverted, List<ICondition> conditions) {
        super(inverted);
        this.conditions.addAll(conditions);
    }

    @Override
    public boolean isMet(ActionData actionData) {
        return conditions.stream().noneMatch(condition -> condition.isMet(actionData));
    }

    @Override
    public IConditionType<?> getType() {
        return ConditionType.NOT;
    }

    public List<ICondition> getConditions() {
        return conditions;
    }

    public static class Serializer implements IConditionSerializer<NotCondition> {

        @Override
        @SuppressWarnings("unchecked")
        public NotCondition fromJson(ResourceLocation location, JsonObject jsonObject, boolean inverted) {
            List<ICondition> tempConditions = new ArrayList<>();
            JsonArray jsonArray = GsonHelper.getAsJsonArray(jsonObject, "conditions");
            jsonArray.forEach(jsonElement -> {
                JsonObject conditionObject = jsonElement.getAsJsonObject();
                ResourceLocation type = getResourceLocation(conditionObject, "type");
                IConditionSerializer<ICondition> conditionSerializer = (IConditionSerializer<ICondition>) ArcRegistry.CONDITION
                        .getOptional(type)
                        .map(IConditionType::getSerializer)
                        .orElseThrow(() -> new JsonParseException("Unknown condition type: " + type));
                if (conditionSerializer == null) {
                    throw new JsonParseException("Unknown condition type: " + type);
                }
                tempConditions.add(conditionSerializer.fromJson(location, conditionObject));
            });
            return new NotCondition(inverted, tempConditions);
        }

        @Override
        public NotCondition fromNetwork(ResourceLocation location, RegistryFriendlyByteBuf friendlyByteBuf, boolean inverted) {
            List<ICondition> tempConditions = new ArrayList<>();
            int size = friendlyByteBuf.readVarInt();
            for (int i = 0; i < size; i++) {
                ICondition condition = IConditionSerializer.fromNetwork(friendlyByteBuf);
                if (condition != null) {
                    tempConditions.add(condition);
                }
            }

            return new NotCondition(
                    inverted, tempConditions);
        }

        @Override
        public void toNetwork(RegistryFriendlyByteBuf friendlyByteBuf, NotCondition type) {
            IConditionSerializer.super.toNetwork(friendlyByteBuf, type);
            friendlyByteBuf.writeVarInt(type.conditions.size());
            type.conditions.forEach(condition ->
                    IConditionSerializer.toNetwork(condition, friendlyByteBuf, condition.getType().getLocation()));
        }
    }
}
