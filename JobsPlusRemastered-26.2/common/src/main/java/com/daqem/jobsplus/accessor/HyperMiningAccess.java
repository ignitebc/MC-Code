package com.daqem.jobsplus.accessor;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

/** 보호 이벤트를 통과해 중앙 블록이 실제 파괴되었다는 사실을 기록한다. */
public interface HyperMiningAccess
{
    void jobsplus$afterBlockBreak(BlockPos pos, BlockState state);
}
