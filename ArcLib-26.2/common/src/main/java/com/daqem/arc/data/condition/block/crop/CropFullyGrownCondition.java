package com.daqem.arc.data.condition.block.crop;

import com.daqem.arc.api.action.data.ActionData;
import com.daqem.arc.api.action.data.type.ActionDataType;
import com.daqem.arc.api.condition.AbstractCondition;
import com.daqem.arc.api.condition.serializer.IConditionSerializer;
import com.daqem.arc.api.condition.type.ConditionType;
import com.daqem.arc.api.condition.type.IConditionType;
import com.daqem.arc.event.crop.CropHarvestHelper;
import com.google.gson.*;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class CropFullyGrownCondition extends AbstractCondition {

    public CropFullyGrownCondition(boolean inverted) {
        super(inverted);
    }

    @Override
    public boolean isMet(ActionData actionData) {
        BlockState blockState = actionData.getData(ActionDataType.BLOCK_STATE);
        if (blockState == null) {
            return false;
        }

        Level level = actionData.getData(ActionDataType.WORLD);
        BlockPos blockPos = actionData.getData(ActionDataType.BLOCK_POSITION);
        return CropHarvestHelper.isFullyGrown(blockState, level, blockPos);
    }

    @Override
    public IConditionType<?> getType() {
        return ConditionType.CROP_FULLY_GROWN;
    }

    public static class Serializer implements IConditionSerializer<CropFullyGrownCondition> {

        @Override
        public CropFullyGrownCondition fromJson(Identifier location, JsonObject jsonObject, boolean inverted) {
            return new CropFullyGrownCondition(inverted);
        }

        @Override
        public CropFullyGrownCondition fromNetwork(Identifier location, RegistryFriendlyByteBuf friendlyByteBuf, boolean inverted) {
            return new CropFullyGrownCondition(inverted);
        }

        @Override
        public void toNetwork(RegistryFriendlyByteBuf friendlyByteBuf, CropFullyGrownCondition type) {
            IConditionSerializer.super.toNetwork(friendlyByteBuf, type);
        }
    }
}
