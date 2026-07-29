package com.daqem.arc.data.reward.effect;

import com.daqem.arc.api.action.data.ActionData;
import com.daqem.arc.api.action.result.ActionResult;
import com.daqem.arc.api.action.data.type.ActionDataType;
import com.daqem.arc.api.reward.AbstractReward;
import com.daqem.arc.api.reward.serializer.IRewardSerializer;
import com.daqem.arc.api.reward.type.IRewardType;
import com.daqem.arc.api.reward.type.RewardType;
import com.google.gson.*;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;

public class RemoveEffectReward extends AbstractReward {

    public RemoveEffectReward(double chance, int priority) {
        super(chance, priority);
    }

    @Override
    public ActionResult apply(ActionData actionData) {
        MobEffectInstance effect = actionData.getData(ActionDataType.MOB_EFFECT_INSTANCE);
        if (effect != null) {
            Player player = actionData.getPlayer().arc$getPlayer();
            player.getActiveEffectsMap().keySet()
                    .stream()
                    .filter(mobEffect2 -> mobEffect2.value().getDescriptionId()
                            .equals(effect.getEffect().value().getDescriptionId()))
                    .findFirst()
                    .ifPresent(player::removeEffect);
        }
        return new ActionResult();
    }

    @Override
    public IRewardType<?> getType() {
        return RewardType.REMOVE_EFFECT;
    }

    public static class Serializer implements IRewardSerializer<RemoveEffectReward> {

        @Override
        public RemoveEffectReward fromJson(JsonObject jsonObject, double chance, int priority) {
            return new RemoveEffectReward(chance, priority);
        }

        @Override
        public RemoveEffectReward fromNetwork(RegistryFriendlyByteBuf friendlyByteBuf, double chance, int priority) {
            return new RemoveEffectReward(chance, priority);
        }

        @Override
        public void toNetwork(RegistryFriendlyByteBuf friendlyByteBuf, RemoveEffectReward type) {
            IRewardSerializer.super.toNetwork(friendlyByteBuf, type);
        }
    }
}
