package com.daqem.arc.data.reward.effect;

import com.daqem.arc.api.action.data.ActionData;
import com.daqem.arc.api.action.result.ActionResult;
import com.daqem.arc.api.action.data.type.ActionDataType;
import com.daqem.arc.api.reward.AbstractReward;
import com.daqem.arc.api.reward.serializer.IRewardSerializer;
import com.daqem.arc.api.reward.type.IRewardType;
import com.daqem.arc.api.reward.type.RewardType;
import com.google.gson.*;
import com.mojang.authlib.GameProfile;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;

import java.util.Objects;
import java.util.UUID;

public class EffectDurationMultiplierReward extends AbstractReward {

    private final double multiplier;

    public EffectDurationMultiplierReward(double chance, int priority, double multiplier) {
        super(chance, priority);
        this.multiplier = multiplier;
    }

    @Override
    public Component getDescription() {
        return getDescription(multiplier);
    }

    @Override
    public ActionResult apply(ActionData actionData) {
        MobEffectInstance effect = actionData.getData(ActionDataType.MOB_EFFECT_INSTANCE);
        if (effect != null) {
            if (actionData.getPlayer().arc$getPlayer() instanceof ServerPlayer player){
                MobEffectInstance newEffect = new MobEffectInstance(effect.getEffect(), Mth.floor(effect.getDuration() * multiplier), effect.getAmplifier(), effect.isAmbient(), effect.isVisible());
                player.addEffect(newEffect, new ServerPlayer(Objects.requireNonNull(player.level().getServer()), player.level(), new GameProfile(UUID.randomUUID(), "a"), player.clientInformation()));
            }
        }
        return new ActionResult();
    }

    @Override
    public IRewardType<?> getType() {
        return RewardType.EFFECT_DURATION_MULTIPLIER;
    }

    public double getMultiplier() {
        return multiplier;
    }

    public static class Serializer implements IRewardSerializer<EffectDurationMultiplierReward> {

        @Override
        public EffectDurationMultiplierReward fromJson(JsonObject jsonObject, double chance, int priority) {
            return new EffectDurationMultiplierReward(
                    chance,
                    priority,
                    GsonHelper.getAsDouble(jsonObject, "multiplier"));
        }

        @Override
        public EffectDurationMultiplierReward fromNetwork(RegistryFriendlyByteBuf friendlyByteBuf, double chance, int priority) {
            return new EffectDurationMultiplierReward(
                    chance,
                    priority,
                    friendlyByteBuf.readDouble());
        }

        @Override
        public void toNetwork(RegistryFriendlyByteBuf friendlyByteBuf, EffectDurationMultiplierReward type) {
            IRewardSerializer.super.toNetwork(friendlyByteBuf, type);
            friendlyByteBuf.writeDouble(type.multiplier);
        }
    }
}
