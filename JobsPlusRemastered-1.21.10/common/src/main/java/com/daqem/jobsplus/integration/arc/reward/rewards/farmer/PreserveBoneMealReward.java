package com.daqem.jobsplus.integration.arc.reward.rewards.farmer;

import com.daqem.arc.api.action.data.ActionData;
import com.daqem.arc.api.action.result.ActionResult;
import com.daqem.arc.api.reward.AbstractReward;
import com.daqem.arc.api.reward.serializer.IRewardSerializer;
import com.daqem.arc.api.reward.type.IRewardType;
import com.daqem.jobsplus.integration.arc.reward.type.JobsPlusRewardType;
import com.daqem.jobsplus.player.PlayerItemDelivery;
import com.google.gson.JsonObject;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class PreserveBoneMealReward extends AbstractReward
{

    public PreserveBoneMealReward(double chance, int priority)
    {
        super(chance, priority);
    }

    @Override
    public IRewardType<?> getType()
    {
        return JobsPlusRewardType.PRESERVE_BONE_MEAL;
    }

    @Override
    public ActionResult apply(ActionData actionData)
    {
        if (actionData.getPlayer().arc$getPlayer() instanceof ServerPlayer serverPlayer
                && !serverPlayer.hasInfiniteMaterials())
        {
            ItemStack refundedBoneMeal = new ItemStack(Items.BONE_MEAL);
            PlayerItemDelivery.giveOrDrop(serverPlayer, refundedBoneMeal);
        }
        return new ActionResult();
    }

    public static class Serializer implements IRewardSerializer<PreserveBoneMealReward>
    {

        @Override
        public PreserveBoneMealReward fromJson(JsonObject jsonObject, double chance, int priority)
        {
            return new PreserveBoneMealReward(chance, priority);
        }

        @Override
        public PreserveBoneMealReward fromNetwork(
                RegistryFriendlyByteBuf friendlyByteBuf,
                double chance,
                int priority)
        {
            return new PreserveBoneMealReward(chance, priority);
        }
    }
}
