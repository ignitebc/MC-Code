package com.daqem.arc.data.condition.movement;

import com.daqem.arc.api.action.data.ActionData;
import com.daqem.arc.api.action.data.type.ActionDataType;
import com.daqem.arc.api.condition.AbstractCondition;
import com.daqem.arc.api.condition.serializer.IConditionSerializer;
import com.daqem.arc.api.condition.type.ConditionType;
import com.daqem.arc.api.condition.type.IConditionType;
import com.daqem.arc.api.player.ArcServerPlayer;
import com.google.gson.JsonObject;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;

public class DistanceCondition extends AbstractCondition {

    private final int distanceInBlocks;

    public DistanceCondition(boolean inverted, int distanceInBlocks) {
        super(inverted);
        this.distanceInBlocks = distanceInBlocks;
    }

    @Override
    public Component getDescription() {
        return getDescription(distanceInBlocks);
    }

    @Override
    public boolean isMet(ActionData actionData) {
        if (!(actionData.getPlayer() instanceof ArcServerPlayer serverPlayer)) {
            return false;
        }

        Integer totalDistanceMovedInCm = actionData.getData(ActionDataType.DISTANCE_IN_CM);
        if (totalDistanceMovedInCm == null) {
            return false;
        }

        int thresholdInCm = distanceInBlocks * 100;
        if (thresholdInCm <= 0) {
            return false;
        }

        int lastDistanceInCm = serverPlayer.arc$getLastDistanceInCm(this);

        // 이동 통계가 초기화되면 기준점이 현재 거리보다 커져 다시는 발동하지 않는다.
        if (totalDistanceMovedInCm < lastDistanceInCm) {
            serverPlayer.arc$setLastDistanceInCm(this, totalDistanceMovedInCm);
            return false;
        }

        if (totalDistanceMovedInCm - lastDistanceInCm < thresholdInCm) {
            return false;
        }

        // 기준점을 임계값만큼만 올려 초과분을 다음 판정으로 넘긴다.
        serverPlayer.arc$setLastDistanceInCm(this, lastDistanceInCm + thresholdInCm);
        return true;
    }

    @Override
    public IConditionType<?> getType() {
        return ConditionType.DISTANCE;
    }

    public int getDistanceInBlocks() {
        return distanceInBlocks;
    }

    public static class Serializer implements IConditionSerializer<DistanceCondition> {

        @Override
        public DistanceCondition fromJson(ResourceLocation location, JsonObject jsonObject, boolean inverted) {
            return new DistanceCondition(
                    inverted,
                    GsonHelper.getAsInt(jsonObject, "distance_in_blocks"));
        }

        @Override
        public DistanceCondition fromNetwork(ResourceLocation location, RegistryFriendlyByteBuf friendlyByteBuf, boolean inverted) {
            return new DistanceCondition(
                    inverted,
                    friendlyByteBuf.readVarInt());
        }

        @Override
        public void toNetwork(RegistryFriendlyByteBuf friendlyByteBuf, DistanceCondition type) {
            IConditionSerializer.super.toNetwork(friendlyByteBuf, type);
            friendlyByteBuf.writeVarInt(type.distanceInBlocks);
        }
    }
}
