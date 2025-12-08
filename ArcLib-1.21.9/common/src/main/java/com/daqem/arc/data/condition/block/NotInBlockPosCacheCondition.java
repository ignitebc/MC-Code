package com.daqem.arc.data.condition.block;

import com.daqem.arc.api.action.data.ActionData;
import com.daqem.arc.api.action.data.type.ActionDataType;
import com.daqem.arc.api.condition.AbstractCondition;
import com.daqem.arc.api.condition.ICondition;
import com.daqem.arc.api.condition.serializer.IConditionSerializer;
import com.daqem.arc.api.condition.type.ConditionType;
import com.daqem.arc.api.condition.type.IConditionType;
import com.daqem.arc.api.player.ArcServerPlayer;
import com.daqem.arc.player.BlockPosCache;
import com.google.gson.JsonObject;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public class NotInBlockPosCacheCondition  extends AbstractCondition {

    public NotInBlockPosCacheCondition(boolean inverted) {
        super(inverted);
    }

    @Override
    public boolean isMet(ActionData actionData) {
        if (actionData.getPlayer() instanceof ArcServerPlayer serverPlayer) {
            BlockPos blockPos = actionData.getData(ActionDataType.BLOCK_POSITION);
            if (blockPos != null) {
                BlockPosCache blockPosCache = serverPlayer.arc$getBlockPosCache();
                return !blockPosCache.contains(blockPos);
            }
        }
        return true;
    }

    @Override
    public IConditionType<? extends ICondition> getType() {
        return ConditionType.NOT_IN_BLOCK_POS_CACHE;
    }

    public static class Serializer implements IConditionSerializer<NotInBlockPosCacheCondition> {

        @Override
        public NotInBlockPosCacheCondition fromJson(ResourceLocation location, JsonObject jsonObject, boolean inverted) {
            return new NotInBlockPosCacheCondition(inverted);
        }

        @Override
        public NotInBlockPosCacheCondition fromNetwork(ResourceLocation location, RegistryFriendlyByteBuf friendlyByteBuf, boolean inverted) {
            return new NotInBlockPosCacheCondition(inverted);
        }

        @Override
        public void toNetwork(RegistryFriendlyByteBuf friendlyByteBuf, NotInBlockPosCacheCondition type) {
            IConditionSerializer.super.toNetwork(friendlyByteBuf, type);
        }
    }
}
