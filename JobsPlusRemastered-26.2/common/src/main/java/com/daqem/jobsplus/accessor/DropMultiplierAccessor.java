package com.daqem.jobsplus.accessor;

import net.minecraft.network.chat.Component;

import java.util.UUID;

public interface DropMultiplierAccessor {

    int jobsplus$getDropMultiplier();

    void jobsplus$setDropMultiplier(int multiplier);

    UUID jobsplus$getDropRewardPlayer();

    void jobsplus$setDropRewardPlayer(UUID playerUuid);

    Component jobsplus$getDropSkillName();

    void jobsplus$setDropSkillName(Component skillName);

    void jobsplus$clearDropMultiplier();
}
