package com.daqem.jobsplus.integration.arc.reward.type;

import com.daqem.arc.api.reward.IReward;
import com.daqem.arc.api.reward.type.IRewardType;
import com.daqem.arc.api.reward.type.RewardType;
import com.daqem.jobsplus.JobsPlus;
import com.daqem.jobsplus.integration.arc.reward.rewards.entity.EntityDropMultiplierReward;
import com.daqem.jobsplus.integration.arc.reward.rewards.job.JobBitcoinReward;
import com.daqem.jobsplus.integration.arc.reward.rewards.job.JobCoinReward;
import com.daqem.jobsplus.integration.arc.reward.rewards.job.JobExpMultiplierReward;
import com.daqem.jobsplus.integration.arc.reward.rewards.job.JobExpReward;

public interface JobsPlusRewardType<T extends IReward> extends RewardType<T>
{

    IRewardType<JobExpReward> JOB_EXP = RewardType.register(JobsPlus.getId("job_exp"), new JobExpReward.Serializer());

    IRewardType<JobExpMultiplierReward> JOB_EXP_MULTIPLIER = RewardType.register(JobsPlus.getId("job_exp_multiplier"), new JobExpMultiplierReward.Serializer());

    IRewardType<JobCoinReward> JOB_COIN = RewardType.register(JobsPlus.getId("job_coin"), new JobCoinReward.Serializer());

    // bitcoin reward
    IRewardType<JobBitcoinReward> BITCOIN_REWARD = RewardType.register(JobsPlus.getId("bitcoin_reward"), new JobBitcoinReward.Serializer());

    // entity drop multiplier
    IRewardType<EntityDropMultiplierReward> ENTITY_DROP_MULTIPLIER = RewardType.register(JobsPlus.getId("entity_drop_multiplier"), new EntityDropMultiplierReward.Serializer());

    static void init()
    {
    }
}
