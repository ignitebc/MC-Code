package com.daqem.arc.data.reward.experience;

import com.daqem.arc.api.action.data.ActionData;
import com.daqem.arc.api.player.ArcPlayer;
import com.daqem.arc.api.action.result.ActionResult;
import com.daqem.arc.api.reward.AbstractReward;
import com.daqem.arc.api.reward.serializer.IRewardSerializer;
import com.daqem.arc.api.reward.type.IRewardType;
import com.daqem.arc.api.reward.type.RewardType;
import com.google.gson.*;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.GsonHelper;

public class ExpReward extends AbstractReward {

    private final int min;
    private final int max;

    public ExpReward(double chance, int priority, int min, int max) {
        super(chance, priority);
        this.min = min;
        this.max = max;

        if (min > max) {
            throw new IllegalArgumentException("min cannot be greater than max for ExpActionReward.");
        }
    }

    @Override
    public Component getDescription() {
        return getDescription(min, max);
    }

    @Override
    public IRewardType<?> getType() {
        return RewardType.EXP;
    }

    @Override
    public ActionResult apply(ActionData actionData) {
        ArcPlayer player = actionData.getPlayer();
        int exp = ((ServerPlayer) player).getRandom().nextInt(min, max + 1);
        ((ServerPlayer) player).giveExperiencePoints(exp);
        return new ActionResult();
    }

    public int getMin() {
        return min;
    }

    public int getMax() {
        return max;
    }

    public static class Serializer implements IRewardSerializer<ExpReward> {

        @Override
        public ExpReward fromJson(JsonObject jsonObject, double chance, int priority) {
            return new ExpReward(
                    chance,
                    priority,
                    GsonHelper.getAsInt(jsonObject, "min"),
                    GsonHelper.getAsInt(jsonObject, "max"));
        }

        @Override
        public ExpReward fromNetwork(RegistryFriendlyByteBuf friendlyByteBuf, double chance, int priority) {
            return new ExpReward(
                    chance,
                    priority,
                    friendlyByteBuf.readInt(),
                    friendlyByteBuf.readInt());
        }

        @Override
        public void toNetwork(RegistryFriendlyByteBuf friendlyByteBuf, ExpReward type) {
            IRewardSerializer.super.toNetwork(friendlyByteBuf, type);
            friendlyByteBuf.writeInt(type.min);
            friendlyByteBuf.writeInt(type.max);
        }
    }
}
