package com.daqem.arc.data.reward.block;

import com.daqem.arc.api.action.data.ActionData;
import com.daqem.arc.api.action.result.ActionResult;
import com.daqem.arc.api.reward.AbstractReward;
import com.daqem.arc.api.reward.serializer.IRewardSerializer;
import com.daqem.arc.api.reward.type.IRewardType;
import com.daqem.arc.api.reward.type.RewardType;
import com.google.gson.*;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.util.GsonHelper;

public class DestroySpeedMultiplierReward extends AbstractReward {

    private final float multiplier;

    public DestroySpeedMultiplierReward(double chance, int priority, float multiplier) {
        super(chance, priority);
        this.multiplier = multiplier;
    }

    @Override
    public Component getDescription() {
        return getDescription(multiplier);
    }

    @Override
    public ActionResult apply(ActionData actionData) {
        return new ActionResult().withDestroySpeedModifier(multiplier);
    }

    @Override
    public IRewardType<?> getType() {
        return RewardType.DESTROY_SPEED_MULTIPLIER;
    }

    public float getMultiplier() {
        return multiplier;
    }

    public static class Serializer implements IRewardSerializer<DestroySpeedMultiplierReward> {

        @Override
        public DestroySpeedMultiplierReward fromJson(JsonObject jsonObject, double chance, int priority) {
            return new DestroySpeedMultiplierReward(
                    chance,
                    priority,
                    GsonHelper.getAsFloat(jsonObject, "multiplier"));
        }

        @Override
        public DestroySpeedMultiplierReward fromNetwork(RegistryFriendlyByteBuf friendlyByteBuf, double chance, int priority) {
            return new DestroySpeedMultiplierReward(
                    chance,
                    priority,
                    friendlyByteBuf.readFloat());
        }

        @Override
        public void toNetwork(RegistryFriendlyByteBuf friendlyByteBuf, DestroySpeedMultiplierReward type) {
            IRewardSerializer.super.toNetwork(friendlyByteBuf, type);
            friendlyByteBuf.writeFloat(type.multiplier);
        }
    }
}
