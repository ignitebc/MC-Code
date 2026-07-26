package com.daqem.jobsplus.integration.arc.reward.rewards.farmer;

import com.daqem.arc.api.action.data.ActionData;
import com.daqem.arc.api.action.data.type.ActionDataType;
import com.daqem.arc.api.action.result.ActionResult;
import com.daqem.arc.api.reward.AbstractReward;
import com.daqem.arc.api.reward.serializer.IRewardSerializer;
import com.daqem.arc.api.reward.type.IRewardType;
import com.daqem.jobsplus.event.block.CropReplantManager;
import com.daqem.jobsplus.integration.arc.reward.type.JobsPlusRewardType;
import com.daqem.jobsplus.mixin.CropBlockAccessor;
import com.google.gson.JsonObject;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;

public class AutoReplantReward extends AbstractReward
{

    public AutoReplantReward(double chance, int priority)
    {
        super(chance, priority);
    }

    @Override
    public IRewardType<?> getType()
    {
        return JobsPlusRewardType.AUTO_REPLANT;
    }

    @Override
    public ActionResult apply(ActionData actionData)
    {
        BlockState blockState = actionData.getData(ActionDataType.BLOCK_STATE);
        BlockPos blockPos = actionData.getData(ActionDataType.BLOCK_POSITION);
        if (blockState != null
                && blockState.getBlock() instanceof CropBlock cropBlock
                && blockPos != null
                && actionData.getData(ActionDataType.WORLD) instanceof ServerLevel serverLevel)
        {
            Player player = actionData.getPlayer().arc$getPlayer();
            if (player.getItemInHand(InteractionHand.OFF_HAND).getItem()
                    == ((CropBlockAccessor) cropBlock).jobsplus$getBaseSeedId().asItem())
            {
                CropReplantManager.schedule(serverLevel, blockPos, cropBlock);
            }
        }
        return new ActionResult();
    }

    public static class Serializer implements IRewardSerializer<AutoReplantReward>
    {

        @Override
        public AutoReplantReward fromJson(JsonObject jsonObject, double chance, int priority)
        {
            return new AutoReplantReward(chance, priority);
        }

        @Override
        public AutoReplantReward fromNetwork(
                RegistryFriendlyByteBuf friendlyByteBuf,
                double chance,
                int priority)
        {
            return new AutoReplantReward(chance, priority);
        }
    }
}
