package com.daqem.arc.data.condition.experience;

import com.daqem.arc.api.action.data.ActionData;
import com.daqem.arc.api.action.data.type.ActionDataType;
import com.daqem.arc.api.condition.AbstractCondition;
import com.daqem.arc.api.condition.serializer.IConditionSerializer;
import com.daqem.arc.api.condition.type.ConditionType;
import com.daqem.arc.api.condition.type.IConditionType;
import com.google.gson.*;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;

public class ExpLevelCondition extends AbstractCondition {

    private final int level;

    public ExpLevelCondition(boolean inverted, int level) {
        super(inverted);
        this.level = level;
    }

    @Override
    public Component getDescription() {
        return getDescription(level);
    }

    @Override
    public boolean isMet(ActionData actionData) {
        Integer expLevel = actionData.getData(ActionDataType.EXP_LEVEL);
        return expLevel != null && expLevel == this.level;
    }

    @Override
    public IConditionType<?> getType() {
        return ConditionType.EXP_LEVEL;
    }

    public int getLevel() {
        return level;
    }

    public static class Serializer implements IConditionSerializer<ExpLevelCondition> {

        @Override
        public ExpLevelCondition fromJson(ResourceLocation location, JsonObject jsonObject, boolean inverted) {
            return new ExpLevelCondition(
                    inverted,
                    GsonHelper.getAsInt(jsonObject, "level"));
        }

        @Override
        public ExpLevelCondition fromNetwork(ResourceLocation location, RegistryFriendlyByteBuf friendlyByteBuf, boolean inverted) {
            return new ExpLevelCondition(
                    inverted,
                    friendlyByteBuf.readVarInt());
        }

        @Override
        public void toNetwork(RegistryFriendlyByteBuf friendlyByteBuf, ExpLevelCondition type) {
            IConditionSerializer.super.toNetwork(friendlyByteBuf, type);
            friendlyByteBuf.writeVarInt(type.level);
        }
    }
}
