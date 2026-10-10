package com.daqem.jobsplus.integration.arc.reward.rewards.job;

import com.daqem.arc.api.action.data.ActionData;
import com.daqem.arc.api.action.result.ActionResult;
import com.daqem.arc.api.reward.AbstractReward;
import com.daqem.arc.api.reward.serializer.IRewardSerializer;
import com.daqem.arc.api.reward.type.IRewardType;
import com.daqem.jobsplus.integration.arc.holder.holders.job.JobInstance;
import com.daqem.jobsplus.integration.arc.reward.type.JobsPlusRewardType;
import com.daqem.jobsplus.metrics.JobsPlusMetrics;
import com.daqem.jobsplus.player.JobsServerPlayer;
import com.daqem.jobsplus.player.coupon.RewardCouponLedger;
import com.daqem.jobsplus.player.job.Job;
import com.google.gson.*;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.GsonHelper;

public class JobExpReward extends AbstractReward
{

    private final double min;
    private final double max;

    public JobExpReward(double chance, int priority, double min, double max)
    {
        super(chance, priority);
        this.min = min;
        this.max = max;

        if (min > max)
        {
            throw new IllegalArgumentException("min cannot be greater than max for JobExpActionReward.");
        }
    }

    @Override
    public IRewardType<?> getType()
    {
        return JobsPlusRewardType.JOB_EXP;
    }

    @Override
    public ActionResult apply(ActionData actionData)
    {
        if (actionData.getSourceActionHolder() instanceof JobInstance jobInstance)
        {
            if (actionData.getPlayer() instanceof JobsServerPlayer jobsServerPlayer)
            {
                Job job = jobsServerPlayer.jobsplus$getJob(jobInstance);
                if (job != null)
                {
                    double exp;
                    if (min == max)
                    {
                        exp = min;
                    }
                    else if (min == Math.rint(min) && max == Math.rint(max))
                    {
                        exp = actionData.getPlayer().arc$getPlayer().getRandom().nextInt((int) min, (int) max + 1);
                    }
                    else
                    {
                        exp = min + actionData.getPlayer().arc$getPlayer().getRandom().nextDouble() * (max - min);
                    }

                    ServerPlayer serverPlayer = jobsServerPlayer.jobsplus$getServerPlayer();
                    MinecraftServer server = serverPlayer.level().getServer();
                    double baseExp = exp;
                    if (server != null)
                    {
                        exp *= RewardCouponLedger.get(server).getExperienceMultiplier(serverPlayer.getUUID());
                    }

                    // 최대 레벨에서는 EXP가 쌓이지 않으므로 지급되지 않은 EXP를 메트릭에 남기지 않는다.
                    boolean grantable = !job.isMaxLevel();
                    job.addExperience(exp);
                    if (grantable)
                    {
                        // 쿠폰 제외 기본 EXP/h를 따로 계산할 수 있도록 기본 몫과 쿠폰 몫을 나누어 남긴다.
                        JobsPlusMetrics.recordExperience(serverPlayer, job, baseExp, exp - baseExp);
                    }
                }
            }
        }
        return new ActionResult();
    }

    @Override
    public Component getDescription()
    {
        return this.getDescription(this.min, this.max);
    }

    public double getMin()
    {
        return min;
    }

    public double getMax()
    {
        return max;
    }

    public static class Serializer implements IRewardSerializer<JobExpReward>
    {
        @Override
        public JobExpReward fromJson(JsonObject jsonObject, double chance, int priority)
        {
            return new JobExpReward(chance, priority, GsonHelper.getAsDouble(jsonObject, "min"), GsonHelper.getAsDouble(jsonObject, "max"));
        }

        @Override
        public JobExpReward fromNetwork(RegistryFriendlyByteBuf friendlyByteBuf, double chance, int priority)
        {
            return new JobExpReward(chance, priority, friendlyByteBuf.readDouble(), friendlyByteBuf.readDouble());
        }

        @Override
        public void toNetwork(RegistryFriendlyByteBuf friendlyByteBuf, JobExpReward type)
        {
            IRewardSerializer.super.toNetwork(friendlyByteBuf, type);
            friendlyByteBuf.writeDouble(type.min);
            friendlyByteBuf.writeDouble(type.max);
        }
    }
}
