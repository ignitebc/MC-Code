package com.daqem.jobsplus.accessor;

import java.util.UUID;

public interface DropMultiplierAccessor {

    int jobsplus$getDropMultiplier();

    void jobsplus$setDropMultiplier(int multiplier);

    UUID jobsplus$getDropRewardPlayer();

    void jobsplus$setDropRewardPlayer(UUID playerUuid);

    void jobsplus$clearDropMultiplier();
}
