package com.daqem.jobsplus.player.job.hyper;

import com.daqem.arc.player.SkillActivationNotifier;
import com.daqem.jobsplus.JobsPlus;
import com.daqem.jobsplus.player.JobsServerPlayer;
import com.daqem.jobsplus.player.job.Job;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

/** 실제 플레이어 채굴이 끝난 뒤 같은 면의 주변 여덟 칸을 정상 채굴 경로로 처리한다. */
public final class HyperMiningHandler
{
    private HyperMiningHandler() {}

    public static void mine(ServerPlayer player, BlockPos center, Direction face,
                            BlockState centerState, ItemStack usedTool)
    {
        if (!player.gameMode.isSurvival() || player.isShiftKeyDown()
                || !(player instanceof JobsServerPlayer jobsPlayer))
        {
            return;
        }
        if (usedTool.isEmpty() || player.getMainHandItem() != usedTool)
        {
            return;
        }
        Identifier jobLocation;
        if (usedTool.is(ItemTags.PICKAXES))
        {
            jobLocation = HyperSkillRules.MINER;
        }
        else if (usedTool.is(ItemTags.SHOVELS))
        {
            jobLocation = HyperSkillRules.DIGGER;
        }
        else
        {
            return;
        }

        Job job = jobsPlayer.jobsplus$getJob(jobLocation);
        if (job == null || job.getLevel() < HyperSkillRules.REQUIRED_JOB_LEVEL)
        {
            return;
        }
        HyperSkillState skill = job.getHyperSkill();
        if (skill.level() == 0 || !skill.active() || !isTarget(centerState, usedTool, jobLocation))
        {
            return;
        }
        if (player.getRandom().nextInt(100) >= HyperSkillRules.getActivationChance(skill.level()))
        {
            return;
        }

        ServerLevel level = player.level();
        float centerHardness = centerState.getDestroySpeed(level, center);
        int minedCount = 0;
        for (int first = -1; first <= 1; first++)
        {
            for (int second = -1; second <= 1; second++)
            {
                if (first == 0 && second == 0)
                {
                    continue;
                }
                // 도구가 파괴되거나 다른 도구로 교체되면 남은 칸을 처리하지 않는다.
                if (usedTool.isEmpty() || player.getMainHandItem() != usedTool)
                {
                    notifyResult(player, jobLocation, minedCount);
                    return;
                }
                BlockPos target = offset(center, face.getAxis(), first, second);
                if (!level.hasChunkAt(target) || !level.getWorldBorder().isWithinBounds(target)
                        || !player.isWithinBlockInteractionRange(target, 1.5D)
                        || !level.mayInteract(player, target) || !player.mayInteract(level, target))
                {
                    continue;
                }
                BlockState targetState = level.getBlockState(target);
                if (!isTarget(targetState, usedTool, jobLocation))
                {
                    continue;
                }
                float hardness = targetState.getDestroySpeed(level, target);
                // 부드러운 블록 한 칸으로 흑요석 등 더 단단한 블록을 즉시 캐지 못하게 한다.
                if (hardness < 0 || hardness > centerHardness)
                {
                    continue;
                }
                // Fabric의 보호/파괴 완료 이벤트, 전리품, 내구도, Arc 보상 처리를 모두 거친다.
                // 파괴 완료 이벤트를 여기서 다시 호출하면 EXP·BTC가 중복 지급되므로 호출하지 않는다.
                if (player.gameMode.destroyBlock(target))
                {
                    minedCount++;
                }
            }
        }
        notifyResult(player, jobLocation, minedCount);
    }

    private static boolean isTarget(BlockState state, ItemStack tool, Identifier jobLocation)
    {
        if (state.isAir() || state.hasBlockEntity())
        {
            return false;
        }
        if (HyperSkillRules.MINER.equals(jobLocation))
        {
            if (!state.is(BlockTags.MINEABLE_WITH_PICKAXE))
            {
                return false;
            }
        }
        else if (!state.is(BlockTags.MINEABLE_WITH_SHOVEL))
        {
            return false;
        }
        if (state.requiresCorrectToolForDrops() && !tool.isCorrectToolForDrops(state))
        {
            return false;
        }
        return true;
    }

    private static BlockPos offset(BlockPos center, Direction.Axis axis, int first, int second)
    {
        if (axis == Direction.Axis.X)
        {
            return center.offset(0, first, second);
        }
        if (axis == Direction.Axis.Y)
        {
            return center.offset(first, 0, second);
        }
        return center.offset(first, second, 0);
    }

    private static void notifyResult(ServerPlayer player, Identifier jobLocation, int minedCount)
    {
        if (minedCount > 0)
        {
            SkillActivationNotifier.notifySkillActivated(player,
                    JobsPlus.translatable("hyper.mined", HyperSkillRules.getName(jobLocation), minedCount));
        }
    }
}
