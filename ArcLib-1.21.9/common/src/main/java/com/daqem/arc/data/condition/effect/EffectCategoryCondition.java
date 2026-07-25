package com.daqem.arc.data.condition.effect;

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
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;

public class EffectCategoryCondition extends AbstractCondition {

    private final MobEffectCategory effectCategory;

    public EffectCategoryCondition(boolean inverted, MobEffectCategory effectCategory) {
        super(inverted);
        this.effectCategory = effectCategory;
    }

    @Override
    public Component getDescription() {
        return getDescription(effectCategory.name().toLowerCase());
    }

    @Override
    public boolean isMet(ActionData actionData) {
        MobEffectInstance effectInstance = actionData.getData(ActionDataType.MOB_EFFECT_INSTANCE);
        return effectInstance != null && effectInstance.getEffect().value().getCategory() == this.effectCategory;
    }

    @Override
    public IConditionType<?> getType() {
        return ConditionType.EFFECT_CATEGORY;
    }

    public MobEffectCategory getEffectCategory() {
        return effectCategory;
    }

    public static class Serializer implements IConditionSerializer<EffectCategoryCondition> {

        @Override
        public EffectCategoryCondition fromJson(ResourceLocation location, JsonObject jsonObject, boolean inverted) {
            return new EffectCategoryCondition(
                    inverted,
                    getMobEffectCategory(jsonObject, "category"));
        }

        @Override
        public EffectCategoryCondition fromNetwork(ResourceLocation location, RegistryFriendlyByteBuf friendlyByteBuf, boolean inverted) {
            return new EffectCategoryCondition(
                    inverted,
                    friendlyByteBuf.readEnum(MobEffectCategory.class));
        }

        @Override
        public void toNetwork(RegistryFriendlyByteBuf friendlyByteBuf, EffectCategoryCondition type) {
            IConditionSerializer.super.toNetwork(friendlyByteBuf, type);
            friendlyByteBuf.writeEnum(type.effectCategory);
        }
    }
}
