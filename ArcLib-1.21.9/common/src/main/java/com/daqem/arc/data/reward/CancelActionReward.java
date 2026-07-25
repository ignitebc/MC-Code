package com.daqem.arc.data.reward;

import com.daqem.arc.api.action.data.ActionData;
import com.daqem.arc.api.action.result.ActionResult;
import com.daqem.arc.api.reward.AbstractReward;
import com.daqem.arc.api.reward.serializer.IRewardSerializer;
import com.daqem.arc.api.reward.type.IRewardType;
import com.daqem.arc.api.reward.type.RewardType;
import com.google.gson.JsonObject;
import net.minecraft.network.RegistryFriendlyByteBuf;

public class CancelActionReward extends AbstractReward {

    public CancelActionReward(double chance, int priority) {
        super(chance, priority);
    }

    @Override
    public ActionResult apply(ActionData actionData) {
        return new ActionResult().withCancelAction(true);
    }

    @Override
    public IRewardType<?> getType() {
        return RewardType.CANCEL_ACTION;
    }

    public static class Serializer implements IRewardSerializer<CancelActionReward> {

        @Override
        public CancelActionReward fromJson(JsonObject jsonObject, double chance, int priority) {
            return new CancelActionReward(chance, priority);
        }

        @Override
        public CancelActionReward fromNetwork(RegistryFriendlyByteBuf friendlyByteBuf, double chance, int priority) {
            return new CancelActionReward(chance, priority);
        }

        @Override
        public void toNetwork(RegistryFriendlyByteBuf friendlyByteBuf, CancelActionReward type) {
            IRewardSerializer.super.toNetwork(friendlyByteBuf, type);
        }
    }
}
