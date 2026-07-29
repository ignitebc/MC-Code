package com.daqem.arc.data.reward.entity;

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

public class DamageMultiplierReward extends AbstractReward {

    private final double multiplier;

    public DamageMultiplierReward(double chance, int priority, double multiplier) {
        super(chance, priority);
        this.multiplier = multiplier;
    }

    @Override
    public Component getDescription() {
        return getDescription(multiplier);
    }

    @Override
    public ActionResult apply(ActionData actionData) {
        return new ActionResult().withDamageModifier((float) multiplier);
    }

    @Override
    public IRewardType<?> getType() {
        return RewardType.DAMAGE_MULTIPLIER;
    }

    public double getMultiplier() {
        return multiplier;
    }

    public static class Serializer implements IRewardSerializer<DamageMultiplierReward> {

        @Override
        public DamageMultiplierReward fromJson(JsonObject jsonObject, double chance, int priority) {
            return new DamageMultiplierReward(
                    chance,
                    priority,
                    GsonHelper.getAsDouble(jsonObject, "multiplier"));
        }

        @Override
        public DamageMultiplierReward fromNetwork(RegistryFriendlyByteBuf friendlyByteBuf, double chance, int priority) {
            return new DamageMultiplierReward(
                    chance,
                    priority,
                    friendlyByteBuf.readDouble());
        }

        @Override
        public void toNetwork(RegistryFriendlyByteBuf friendlyByteBuf, DamageMultiplierReward type) {
            IRewardSerializer.super.toNetwork(friendlyByteBuf, type);
            friendlyByteBuf.writeDouble(type.multiplier);
        }
    }
}
