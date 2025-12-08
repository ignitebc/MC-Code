package com.daqem.arc.api.reward.type;

import com.daqem.arc.api.reward.IReward;
import com.daqem.arc.api.reward.serializer.IRewardSerializer;
import net.minecraft.resources.ResourceLocation;

public interface IRewardType<T extends IReward> {

    ResourceLocation getLocation();

    IRewardSerializer<T> getSerializer();
}
