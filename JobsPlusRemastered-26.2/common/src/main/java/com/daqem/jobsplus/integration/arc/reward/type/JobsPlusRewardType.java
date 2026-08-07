package com.daqem.jobsplus.integration.arc.reward.type;

import com.daqem.arc.api.reward.IReward;
import com.daqem.arc.api.reward.type.IRewardType;
import com.daqem.arc.api.reward.type.RewardType;
import com.daqem.jobsplus.JobsPlus;
import com.daqem.jobsplus.integration.arc.reward.rewards.entity.EntityDropMultiplierReward;
import com.daqem.jobsplus.integration.arc.reward.rewards.farmer.AutoReplantReward;
import com.daqem.jobsplus.integration.arc.reward.rewards.farmer.PreserveBoneMealReward;
import com.daqem.jobsplus.integration.arc.reward.rewards.farmer.RangeHarvestReward;
import com.daqem.jobsplus.integration.arc.reward.rewards.job.JobBitcoinReward;
import com.daqem.jobsplus.integration.arc.reward.rewards.job.JobCoinReward;
import com.daqem.jobsplus.integration.arc.reward.rewards.job.JobExpMultiplierReward;
import com.daqem.jobsplus.integration.arc.reward.rewards.job.JobExpReward;
import com.daqem.jobsplus.integration.arc.reward.rewards.job.SpecialItemReward;
import com.daqem.jobsplus.integration.arc.reward.rewards.smith.LapisRefundReward;

public interface JobsPlusRewardType<T extends IReward> extends RewardType<T>
{

    IRewardType<JobExpReward> JOB_EXP = RewardType.register(JobsPlus.getId("job_exp"), new JobExpReward.Serializer());

    IRewardType<JobExpMultiplierReward> JOB_EXP_MULTIPLIER = RewardType.register(JobsPlus.getId("job_exp_multiplier"), new JobExpMultiplierReward.Serializer());

    IRewardType<JobCoinReward> JOB_COIN = RewardType.register(JobsPlus.getId("job_coin"), new JobCoinReward.Serializer());

    // bitcoin reward
    IRewardType<JobBitcoinReward> BITCOIN_REWARD = RewardType.register(JobsPlus.getId("bitcoin_reward"), new JobBitcoinReward.Serializer());

    // entity drop multiplier
    IRewardType<EntityDropMultiplierReward> ENTITY_DROP_MULTIPLIER = RewardType.register(JobsPlus.getId("entity_drop_multiplier"), new EntityDropMultiplierReward.Serializer());

    IRewardType<RangeHarvestReward> RANGE_HARVEST = RewardType.register(JobsPlus.getId("range_harvest"), new RangeHarvestReward.Serializer());

    IRewardType<AutoReplantReward> AUTO_REPLANT = RewardType.register(JobsPlus.getId("auto_replant"), new AutoReplantReward.Serializer());

    IRewardType<PreserveBoneMealReward> PRESERVE_BONE_MEAL = RewardType.register(JobsPlus.getId("preserve_bone_meal"), new PreserveBoneMealReward.Serializer());

    // 지급이 확정된 뒤 보너스 계열 단계만큼 개수를 두 배로 올리는 아이템 보상
    IRewardType<SpecialItemReward> SPECIAL_ITEM = RewardType.register(JobsPlus.getId("special_item"), new SpecialItemReward.Serializer());

    // 마법 부여에 실제로 소모한 청금석을 그대로 반환하는 보상
    IRewardType<LapisRefundReward> LAPIS_REFUND = RewardType.register(JobsPlus.getId("lapis_refund"), new LapisRefundReward.Serializer());

    static void init()
    {
    }
}
