package com.daqem.arc.data.condition.block.crop;

import com.daqem.arc.api.action.data.ActionData;
import com.daqem.arc.api.action.data.type.ActionDataType;
import com.daqem.arc.api.condition.AbstractCondition;
import com.daqem.arc.api.condition.serializer.IConditionSerializer;
import com.daqem.arc.api.condition.type.ConditionType;
import com.daqem.arc.api.condition.type.IConditionType;
import com.google.gson.*;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;

import java.util.Collection;
import java.util.Optional;

public class CropFullyGrownCondition extends AbstractCondition {

    public CropFullyGrownCondition(boolean inverted) {
        super(inverted);
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
                Collection<Integer> possibleValues = ageProperty.getPossibleValues();
                Integer lastValue = possibleValues.stream().reduce((a, b) -> b).orElse(null);
                if (lastValue != null) {
                    int fullyGrownAge = lastValue;
                    Optional<Integer> optionalAgeValue = blockState.getOptionalValue(ageProperty);
                    if (optionalAgeValue.isPresent()) {
                        return optionalAgeValue.get() == fullyGrownAge;
                    }
                }
            }
        }
        return false;
    }

    @Override
    public IConditionType<?> getType() {
        return ConditionType.CROP_FULLY_GROWN;
    }

    public static class Serializer implements IConditionSerializer<CropFullyGrownCondition> {

        @Override
        public CropFullyGrownCondition fromJson(ResourceLocation location, JsonObject jsonObject, boolean inverted) {
            return new CropFullyGrownCondition(inverted);
        }

        @Override
        public CropFullyGrownCondition fromNetwork(ResourceLocation location, RegistryFriendlyByteBuf friendlyByteBuf, boolean inverted) {
            return new CropFullyGrownCondition(inverted);
        }

        @Override
        public void toNetwork(RegistryFriendlyByteBuf friendlyByteBuf, CropFullyGrownCondition type) {
            IConditionSerializer.super.toNetwork(friendlyByteBuf, type);
        }
    }
}
