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
import dev.architectury.event.EventResult;
import dev.architectury.event.events.common.BlockEvent;
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
                        // destroyBlock 직접 호출은 파괴 이벤트를 거치지 않아 보호 모드가 개입할 수 없으므로,
                        // 파괴 전 이벤트를 직접 조회해 보호 구역(타인 클레임 등)의 작물은 범위 수확에서 제외한다.
                        EventResult breakResult = BlockEvent.BREAK.invoker()
                                .breakBlock(serverLevel, cropPos, cropState, serverPlayer);
                        if (breakResult.isFalse())
                        {
                            continue;
                        }

                        // 파괴 후 표준 파괴 완료 처리를 호출해 주변 작물도 중앙과 동일하게
                        // 풍년(BREAK_BLOCK)과 경험치·비트코인·되심기(HARVEST_CROP)를 칸마다 개별 판정한다.
                        // 범위 수확 자체의 재발동은 HARVESTING_RANGE 가드가 차단한다.
                        serverLevel.destroyBlock(cropPos, true, serverPlayer, 512);
                        BlockEvents.onBlockBreakComplete(serverLevel, cropPos, cropState, arcServerPlayer);
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
                    Component.translatable("jobsplus.skill.range_harvest",
                            SkillActivationNotifier.resolveSkillName(actionData), harvestedCount));
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
