package com.daqem.arc.data.condition.block.crop;

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
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;

import java.util.Collection;
import java.util.Optional;

public class CropAgeCondition extends AbstractCondition {

    private final int age;

    public CropAgeCondition(boolean inverted, int age) {
        super(inverted);
        this.age = age;
    }

    @Override
    public Component getDescription() {
        return getDescription(age);
    }

    @Override
    public boolean isMet(ActionData actionData) {
        BlockState blockState = actionData.getData(ActionDataType.BLOCK_STATE);
        if (blockState != null) {
            Collection<Property<?>> properties = blockState.getProperties();
            Optional<Property<?>> optionalAgeProperty = properties.stream()
                    .filter(property -> property.getName().equals("age"))
                    .findFirst();
            if (optionalAgeProperty.isPresent()) {
                IntegerProperty ageProperty = (IntegerProperty) optionalAgeProperty.get();
                Optional<Integer> optionalAgeValue = blockState.getOptionalValue(ageProperty);
                if (optionalAgeValue.isPresent()) {
                    return optionalAgeValue.get() == this.age;
                }
            }
        }
        return false;
    }

    @Override
    public IConditionType<?> getType() {
        return ConditionType.CROP_AGE;
    }

    public int getAge() {
        return age;
    }

    public static class Serializer implements IConditionSerializer<CropAgeCondition> {

        @Override
        public CropAgeCondition fromJson(ResourceLocation location, JsonObject jsonObject, boolean inverted) {
            return new CropAgeCondition(
                    inverted,
                    jsonObject.get("age").getAsInt());
        }

        @Override
        public CropAgeCondition fromNetwork(ResourceLocation location, RegistryFriendlyByteBuf friendlyByteBuf, boolean inverted) {
            return new CropAgeCondition(
                    inverted,
                    friendlyByteBuf.readInt());
        }

        @Override
        public void toNetwork(RegistryFriendlyByteBuf friendlyByteBuf, CropAgeCondition type) {
            IConditionSerializer.super.toNetwork(friendlyByteBuf, type);
            friendlyByteBuf.writeInt(type.age);
        }
    }
}
