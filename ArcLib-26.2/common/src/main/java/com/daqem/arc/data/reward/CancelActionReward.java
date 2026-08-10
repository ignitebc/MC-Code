package com.daqem.arc.data.reward;

import com.daqem.arc.api.action.data.ActionData;
import com.daqem.arc.api.action.result.ActionResult;
import com.daqem.arc.api.reward.AbstractReward;
import com.daqem.arc.api.reward.serializer.IRewardSerializer;
import com.daqem.arc.api.reward.type.IRewardType;
import com.daqem.arc.api.reward.type.RewardType;
import com.daqem.arc.player.SkillActivationNotifier;
import com.google.gson.JsonObject;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.GsonHelper;
import org.jetbrains.annotations.Nullable;

public class CancelActionReward extends AbstractReward {

    // 발동 시 플레이어에게 보낼 채팅 메시지의 번역 키. 없으면 알림을 보내지 않는다.
    @Nullable
    private final String activationMessageKey;

    public CancelActionReward(double chance, int priority) {
        this(chance, priority, null);
    }

    public CancelActionReward(double chance, int priority, @Nullable String activationMessageKey) {
        super(chance, priority);
        this.activationMessageKey = activationMessageKey;
    }

    @Override
    public ActionResult apply(ActionData actionData) {
        notifyActivation(actionData);
        return new ActionResult().withCancelAction(true);
    }

    private void notifyActivation(ActionData actionData) {
        if (activationMessageKey == null) {
            return;
        }
        if (actionData.getPlayer().arc$getPlayer() instanceof ServerPlayer serverPlayer) {
            SkillActivationNotifier.notifySkillActivated(
                    serverPlayer,
                    Component.translatable(
                            activationMessageKey,
                            SkillActivationNotifier.resolveSkillName(actionData)));
        }
    }

    @Override
    public IRewardType<?> getType() {
        return RewardType.CANCEL_ACTION;
    }

    public static class Serializer implements IRewardSerializer<CancelActionReward> {

        @Override
        public CancelActionReward fromJson(JsonObject jsonObject, double chance, int priority) {
            String activationMessageKey = GsonHelper.getAsString(jsonObject, "activation_message", null);
            return new CancelActionReward(chance, priority, activationMessageKey);
        }

        @Override
        public CancelActionReward fromNetwork(RegistryFriendlyByteBuf friendlyByteBuf, double chance, int priority) {
            String activationMessageKey = null;
            if (friendlyByteBuf.readBoolean()) {
                activationMessageKey = friendlyByteBuf.readUtf();
            }
            return new CancelActionReward(chance, priority, activationMessageKey);
        }

        @Override
        public void toNetwork(RegistryFriendlyByteBuf friendlyByteBuf, CancelActionReward type) {
            IRewardSerializer.super.toNetwork(friendlyByteBuf, type);
            friendlyByteBuf.writeBoolean(type.activationMessageKey != null);
            if (type.activationMessageKey != null) {
                friendlyByteBuf.writeUtf(type.activationMessageKey);
            }
        }
    }
}
