package com.daqem.arc.data.reward.effect;

import com.daqem.arc.api.action.data.ActionData;
import com.daqem.arc.api.action.result.ActionResult;
import com.daqem.arc.api.action.data.type.ActionDataType;
import com.daqem.arc.api.reward.AbstractReward;
import com.daqem.arc.api.reward.serializer.IRewardSerializer;
import com.daqem.arc.api.reward.type.IRewardType;
import com.daqem.arc.api.reward.type.RewardType;
import com.daqem.arc.player.SkillActivationNotifier;
import com.google.gson.*;
import com.mojang.authlib.GameProfile;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.UUID;

public class EffectAmplifierAdditionReward extends AbstractReward {

    private final int addition;

    // 발동 시 플레이어에게 보낼 채팅 메시지의 번역 키. 없으면 알림을 보내지 않는다.
    @Nullable
    private final String activationMessageKey;

    public EffectAmplifierAdditionReward(double chance, int priority, int addition) {
        this(chance, priority, addition, null);
    }

    public EffectAmplifierAdditionReward(double chance, int priority, int addition, @Nullable String activationMessageKey) {
        super(chance, priority);
        this.addition = addition;
        this.activationMessageKey = activationMessageKey;
    }

    @Override
    public Component getDescription() {
        return getDescription(addition);
    }

    @Override
    public ActionResult apply(ActionData actionData) {
        MobEffectInstance effect = actionData.getData(ActionDataType.MOB_EFFECT_INSTANCE);
        if (effect != null) {
            if (actionData.getPlayer().arc$getPlayer() instanceof ServerPlayer player){
                MobEffectInstance newEffect = new MobEffectInstance(effect.getEffect(), effect.getDuration(), Mth.floor(effect.getAmplifier() + addition), effect.isAmbient(), effect.isVisible());
                player.addEffect(newEffect, new ServerPlayer(Objects.requireNonNull(player.level().getServer()), player.level(), new GameProfile(UUID.randomUUID(), "a"), player.clientInformation()));
                if (activationMessageKey != null) {
                    Component effectName = newEffect.getEffect().value().getDisplayName();
                    SkillActivationNotifier.notifySkillActivated(player, Component.translatable(activationMessageKey, effectName));
                }
            }
        }
        return new ActionResult();
    }

    @Override
    public IRewardType<?> getType() {
        return RewardType.EFFECT_AMPLIFIER_ADDITION;
    }

    public int getAddition() {
        return addition;
    }

    public static class Serializer implements IRewardSerializer<EffectAmplifierAdditionReward> {

        @Override
        public EffectAmplifierAdditionReward fromJson(JsonObject jsonObject, double chance, int priority) {
            return new EffectAmplifierAdditionReward(
                    chance,
                    priority,
                    GsonHelper.getAsInt(jsonObject, "addition"),
                    GsonHelper.getAsString(jsonObject, "activation_message", null));
        }

        @Override
        public EffectAmplifierAdditionReward fromNetwork(RegistryFriendlyByteBuf friendlyByteBuf, double chance, int priority) {
            int addition = friendlyByteBuf.readInt();
            String activationMessageKey = null;
            if (friendlyByteBuf.readBoolean()) {
                activationMessageKey = friendlyByteBuf.readUtf();
            }
            return new EffectAmplifierAdditionReward(chance, priority, addition, activationMessageKey);
        }

        @Override
        public void toNetwork(RegistryFriendlyByteBuf friendlyByteBuf, EffectAmplifierAdditionReward type) {
            IRewardSerializer.super.toNetwork(friendlyByteBuf, type);
            friendlyByteBuf.writeInt(type.addition);
            friendlyByteBuf.writeBoolean(type.activationMessageKey != null);
            if (type.activationMessageKey != null) {
                friendlyByteBuf.writeUtf(type.activationMessageKey);
            }
        }
    }
}
