package com.daqem.arc.data.reward.entity;

import com.daqem.arc.api.action.data.ActionData;
import com.daqem.arc.api.action.data.type.ActionDataType;
import com.daqem.arc.api.action.result.ActionResult;
import com.daqem.arc.api.reward.AbstractReward;
import com.daqem.arc.api.reward.serializer.IRewardSerializer;
import com.daqem.arc.api.reward.type.IRewardType;
import com.daqem.arc.api.reward.type.RewardType;
import com.google.gson.JsonObject;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.Entity;

public class EntityOnFireReward extends AbstractReward {

    private final int fireTicks;

    public EntityOnFireReward(double chance, int priority, int fireTicks) {
        super(chance, priority);
        this.fireTicks = fireTicks;
    }

    @Override
    public Component getDescription() {
        return getDescription(String.format("%.1f", fireTicks / 20F));
    }

    @Override
    public IRewardType<?> getType() {
        return RewardType.ENTITY_ON_FIRE;
    }

    @Override
    public ActionResult apply(ActionData actionData) {
        Entity entity = actionData.getData(ActionDataType.ENTITY);
        if (entity != null) {
            entity.setRemainingFireTicks(fireTicks);
        }
        return new ActionResult();
    }

    public int getFireTicks() {
        return fireTicks;
    }

    public static class Serializer implements IRewardSerializer<EntityOnFireReward> {

        @Override
        public EntityOnFireReward fromJson(JsonObject jsonObject, double chance, int priority) {
            return new EntityOnFireReward(
                    chance,
                    priority,
                    GsonHelper.getAsInt(jsonObject, "ticks"));
        }

        @Override
        public EntityOnFireReward fromNetwork(RegistryFriendlyByteBuf friendlyByteBuf, double chance, int priority) {
            return new EntityOnFireReward(
                    chance,
                    priority,
                    friendlyByteBuf.readInt());
        }

        @Override
        public void toNetwork(RegistryFriendlyByteBuf friendlyByteBuf, EntityOnFireReward type) {
            IRewardSerializer.super.toNetwork(friendlyByteBuf, type);
            friendlyByteBuf.writeInt(type.fireTicks);
        }
    }
}
