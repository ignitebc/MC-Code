package com.daqem.jobsplus.accessor;

import com.daqem.jobsplus.player.job.hyper.HyperPlayerState;

public interface HyperPlayerAccess
{
    HyperPlayerState jobsplus$getHyperState();

    byte jobsplus$getShieldVisual();

    void jobsplus$setShieldVisual(byte state);
}
