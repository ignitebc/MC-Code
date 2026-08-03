package com.daqem.jobsplus.integration.arc.reward.rewards.farmer;

import com.daqem.arc.api.action.data.ActionData;
import com.daqem.arc.api.action.data.type.ActionDataType;
import com.daqem.arc.api.action.result.ActionResult;
import com.daqem.arc.api.player.ArcServerPlayer;
import com.daqem.arc.api.reward.AbstractReward;
import com.daqem.arc.api.reward.serializer.IRewardSerializer;
import com.daqem.arc.api.reward.type.IRewardType;
import com.daqem.arc.event.triggers.BlockEvents;
import com.daqem.arc.player.SkillActivationNotifier;
import com.daqem.jobsplus.integration.arc.reward.type.JobsPlusRewardType;
import com.google.gson.JsonObject;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;

public class RangeHarvestReward extends AbstractReward
{

    private static final ThreadLocal<Boolean> HARVESTING_RANGE =
            ThreadLocal.withInitial(() -> false);

    public RangeHarvestReward(double chance, int priority)
    {
        super(chance, priority);
    }

    @Override
    public IRewardType<?> getType()
    {
        return JobsPlusRewardType.RANGE_HARVEST;
    }

    @Override
    public ActionResult apply(ActionData actionData)
    {
        if (HARVESTING_RANGE.get())
        {
            return new ActionResult();
        }

        BlockPos centerPos = actionData.getData(ActionDataType.BLOCK_POSITION);
        if (!(actionData.getData(ActionDataType.WORLD) instanceof ServerLevel serverLevel)
                || !(actionData.getPlayer() instanceof ArcServerPlayer arcServerPlayer)
                || !(arcServerPlayer.arc$getPlayer() instanceof ServerPlayer serverPlayer)
                || !serverPlayer.getMainHandItem().is(ItemTags.HOES)
                || centerPos == null)
        {
            return new ActionResult();
        }

        int harvestedCount = 0;
        HARVESTING_RANGE.set(true);
        try
        {
            for (int xOffset = -1; xOffset <= 1; xOffset++)
            {
                for (int zOffset = -1; zOffset <= 1; zOffset++)
                {
                    if (xOffset == 0 && zOffset == 0)
                    {
                        continue;
                    }

                    BlockPos cropPos = centerPos.offset(xOffset, 0, zOffset);
                    BlockState cropState = serverLevel.getBlockState(cropPos);
                    if (cropState.getBlock() instanceof CropBlock cropBlock
                            && cropBlock.isMaxAge(cropState))
                    {
                        BlockEvents.onHarvestCrop(arcServerPlayer, cropState, cropPos, serverLevel);
                        serverLevel.destroyBlock(cropPos, true, serverPlayer, 512);
                        harvestedCount++;
                    }
                }
            }
        }
        finally
        {
            HARVESTING_RANGE.set(false);
        }

        if (harvestedCount > 0)
        {
            SkillActivationNotifier.notifySkillActivated(
                    serverPlayer,
                    Component.translatable("jobsplus.skill.range_harvest", harvestedCount));
        }

        return new ActionResult();
    }

    public static class Serializer implements IRewardSerializer<RangeHarvestReward>
    {

        @Override
        public RangeHarvestReward fromJson(JsonObject jsonObject, double chance, int priority)
        {
            return new RangeHarvestReward(chance, priority);
        }

        @Override
        public RangeHarvestReward fromNetwork(
                RegistryFriendlyByteBuf friendlyByteBuf,
                double chance,
                int priority)
        {
            return new RangeHarvestReward(chance, priority);
        }
    }
}
