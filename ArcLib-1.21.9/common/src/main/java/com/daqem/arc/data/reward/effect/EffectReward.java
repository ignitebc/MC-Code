package com.daqem.arc.data.reward.effect;

import com.daqem.arc.api.action.data.ActionData;
import com.daqem.arc.api.player.ArcPlayer;
import com.daqem.arc.api.action.result.ActionResult;
import com.daqem.arc.api.reward.AbstractReward;
import com.daqem.arc.api.reward.serializer.IRewardSerializer;
import com.daqem.arc.api.reward.type.IRewardType;
import com.daqem.arc.api.reward.type.RewardType;
import com.google.gson.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffectUtil;

public class EffectReward extends AbstractReward {

    private final MobEffect effect;
    private final int duration;
    private final int amplifier;

    public EffectReward(double chance, int priority, MobEffect effect, int duration, int amplifier) {
        super(chance, priority);
        this.effect = effect;
        this.duration = duration;
        this.amplifier = amplifier;
    }

    @Override
    public Component getDescription() {
        return getDescription(effect.getDisplayName(), MobEffectUtil.formatDuration(getMobEffectInstance(), 1.0F, 20.0F), amplifier + 1);
    }

    @Override
    public ActionResult apply(ActionData actionData) {
        ArcPlayer player = actionData.getPlayer();
        player.arc$getPlayer().addEffect(getMobEffectInstance());
        return new ActionResult();
    }

    private MobEffectInstance getMobEffectInstance() {
        return new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(effect), duration, amplifier);
    }

    @Override
    public IRewardType<?> getType() {
        return RewardType.EFFECT;
    }

    public MobEffect getEffect() {
        return effect;
    }

    public int getDuration() {
        return duration;
    }

    public int getAmplifier() {
        return amplifier;
    }

    public static class Serializer implements IRewardSerializer<EffectReward> {

        @Override
        public EffectReward fromJson(JsonObject jsonObject, double chance, int priority) {
            return new EffectReward(
                    chance,
                    priority,
                    getMobEffect(jsonObject, "effect"),
                    GsonHelper.getAsInt(jsonObject, "duration"),
                    GsonHelper.getAsInt(jsonObject, "amplifier", 0));
        }

        @Override
        public EffectReward fromNetwork(RegistryFriendlyByteBuf friendlyByteBuf, double chance, int priority) {
            return new EffectReward(
                    chance,
                    priority,
                    ByteBufCodecs.registry(Registries.MOB_EFFECT).decode(friendlyByteBuf),
                    friendlyByteBuf.readVarInt(),
                    friendlyByteBuf.readVarInt());
        }

        @Override
        public void toNetwork(RegistryFriendlyByteBuf friendlyByteBuf, EffectReward type) {
            IRewardSerializer.super.toNetwork(friendlyByteBuf, type);
            ByteBufCodecs.registry(Registries.MOB_EFFECT).encode(friendlyByteBuf, type.effect);
            friendlyByteBuf.writeVarInt(type.duration);
            friendlyByteBuf.writeVarInt(type.amplifier);
        }
    }
}
