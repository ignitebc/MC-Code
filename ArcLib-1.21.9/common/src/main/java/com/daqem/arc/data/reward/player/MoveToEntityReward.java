package com.daqem.arc.data.reward.player;

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
import net.minecraft.world.entity.player.Player;

public class MoveToEntityReward extends AbstractReward {

    private final float force;

    public MoveToEntityReward(double chance, int priority, float force) {
        super(chance, priority);
        this.force = force;
    }

    @Override
    public Component getDescription() {
        return getDescription(force);
    }

    @Override
    public ActionResult apply(ActionData actionData) {
        Player player = actionData.getPlayer().arc$getPlayer();
        Entity entity = actionData.getData(ActionDataType.ENTITY);
        if (entity != null) {
            player.setDeltaMovement((entity.position().x - player.position().x) / 2, force, (entity.position().z - player.position().z) / 2);
            player.hurtMarked = true;
        }
        return new ActionResult();
    }

    @Override
    public IRewardType<?> getType() {
        return RewardType.MOVE_TO_ENTITY;
    }

    public float getForce() {
        return force;
    }

    public static class Serializer implements IRewardSerializer<MoveToEntityReward> {

        @Override
        public MoveToEntityReward fromJson(JsonObject jsonObject, double chance, int priority) {
            return new MoveToEntityReward(
                    chance,
                    priority,
                    GsonHelper.getAsFloat(jsonObject, "force"));
        }

        @Override
        public MoveToEntityReward fromNetwork(RegistryFriendlyByteBuf friendlyByteBuf, double chance, int priority) {
            return new MoveToEntityReward(
                    chance,
                    priority,
                    friendlyByteBuf.readFloat());
        }

        @Override
        public void toNetwork(RegistryFriendlyByteBuf friendlyByteBuf, MoveToEntityReward type) {
            IRewardSerializer.super.toNetwork(friendlyByteBuf, type);
            friendlyByteBuf.writeFloat(type.force);
        }
    }
}
