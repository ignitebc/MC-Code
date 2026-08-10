package com.daqem.jobsplus.integration.arc.reward.rewards.entity;

import com.daqem.arc.api.action.data.ActionData;
import com.daqem.arc.api.action.data.type.ActionDataType;
import com.daqem.arc.api.action.result.ActionResult;
import com.daqem.arc.api.reward.AbstractReward;
import com.daqem.arc.api.reward.IReward;
import com.daqem.arc.api.reward.serializer.IRewardSerializer;
import com.daqem.arc.api.reward.type.IRewardType;
import com.daqem.arc.player.SkillActivationNotifier;
import com.daqem.jobsplus.integration.arc.reward.type.JobsPlusRewardType;
import com.daqem.jobsplus.accessor.DropMultiplierAccessor;
import com.google.gson.JsonObject;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

public class EntityDropMultiplierReward extends AbstractReward
{

    private final int multiplier;

    public EntityDropMultiplierReward(double chance, int priority, int multiplier)
    {
        super(chance, priority);
        this.multiplier = Math.max(1, multiplier);
    }

    @Override
    public IRewardType<? extends IReward> getType()
    {
        return JobsPlusRewardType.ENTITY_DROP_MULTIPLIER;
    }

    @Override
    public ActionResult apply(ActionData actionData)
    {

        Entity target = actionData.getData(ActionDataType.ENTITY);
        if (target instanceof LivingEntity living
                && actionData.getPlayer().arc$getPlayer() instanceof ServerPlayer serverPlayer)
        {
            DropMultiplierAccessor accessor = (DropMultiplierAccessor) living;
            accessor.jobsplus$setDropMultiplier(this.multiplier);
            accessor.jobsplus$setDropRewardPlayer(serverPlayer.getUUID());
            accessor.jobsplus$setDropSkillName(SkillActivationNotifier.resolveSkillName(actionData));
        }

        return new ActionResult();
    }

    public int getMultiplier()
    {
        return multiplier;
    }

    public static class Serializer implements IRewardSerializer<EntityDropMultiplierReward>
    {

        @Override
        public EntityDropMultiplierReward fromJson(JsonObject jsonObject, double chance, int priority)
        {
            int multiplier = jsonObject.has("multiplier") ? jsonObject.get("multiplier").getAsInt() : 1;
            return new EntityDropMultiplierReward(chance, priority, multiplier);
        }

        @Override
        public EntityDropMultiplierReward fromNetwork(RegistryFriendlyByteBuf friendlyByteBuf, double chance, int priority)
        {
            int multiplier = friendlyByteBuf.readInt();
            return new EntityDropMultiplierReward(chance, priority, multiplier);
        }

        @Override
        public void toNetwork(RegistryFriendlyByteBuf friendlyByteBuf, EntityDropMultiplierReward reward)
        {
            IRewardSerializer.super.toNetwork(friendlyByteBuf, reward);
            friendlyByteBuf.writeInt(reward.multiplier);
        }
    }
}
