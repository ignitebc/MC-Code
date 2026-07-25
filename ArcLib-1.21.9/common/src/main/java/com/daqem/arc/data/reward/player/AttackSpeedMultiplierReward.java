package com.daqem.arc.data.reward.player;

import com.daqem.arc.api.action.data.ActionData;
import com.daqem.arc.api.action.result.ActionResult;
import com.daqem.arc.api.reward.AbstractReward;
import com.daqem.arc.api.reward.serializer.IRewardSerializer;
import com.daqem.arc.api.reward.type.IRewardType;
import com.daqem.arc.api.reward.type.RewardType;
import com.google.gson.JsonObject;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.util.GsonHelper;

public class AttackSpeedMultiplierReward extends AbstractReward {

    private final float multiplier;

    public AttackSpeedMultiplierReward(double chance, int priority, float multiplier) {
        super(chance, priority);
        this.multiplier = multiplier;
    }

    @Override
    public Component getDescription() {
        return getDescription(multiplier);
    }

    @Override
    public ActionResult apply(ActionData actionData) {
        return new ActionResult().withAttackSpeedModifier(multiplier);
    }

    @Override
    public IRewardType<?> getType() {
        return RewardType.ATTACK_SPEED_MULTIPLIER;
    }

    public float getMultiplier() {
        return multiplier;
    }

    public static class Serializer implements IRewardSerializer<AttackSpeedMultiplierReward> {

        @Override
        public AttackSpeedMultiplierReward fromJson(JsonObject jsonObject, double chance, int priority) {
            return new AttackSpeedMultiplierReward(
                    chance,
                    priority,
                    GsonHelper.getAsFloat(jsonObject, "multiplier"));
        }

        @Override
        public AttackSpeedMultiplierReward fromNetwork(RegistryFriendlyByteBuf friendlyByteBuf, double chance, int priority) {
            return new AttackSpeedMultiplierReward(
                    chance,
                    priority,
                    friendlyByteBuf.readFloat());
        }

        @Override
        public void toNetwork(RegistryFriendlyByteBuf friendlyByteBuf, AttackSpeedMultiplierReward type) {
            IRewardSerializer.super.toNetwork(friendlyByteBuf, type);
            friendlyByteBuf.writeFloat(type.multiplier);
        }
    }
}
