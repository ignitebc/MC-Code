package com.daqem.jobsplus.player;

import com.daqem.jobsplus.config.JobsPlusConfig;
import com.daqem.jobsplus.integration.arc.holder.holders.powerup.PowerupInstance;
import com.daqem.jobsplus.player.job.Job;
import com.daqem.jobsplus.player.job.powerup.Powerup;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

public interface JobsServerPlayer extends JobsPlayer {

    ServerPlayer jobsplus$getServerPlayer();

    void jobsplus$updateJob(Job job);

    void jobsplus$updateActionHolders(Job job);

    @Nullable
    Powerup jobsplus$getPowerup(PowerupInstance powerupInstance);

    /**
     * 플레이어별로 추가된 직업 슬롯(전역 maxJobs 외).
     */
    int jobsplus$getExtraJobSlots();

    /**
     * 플레이어별 추가 직업 슬롯 증감.
     */
    void jobsplus$addExtraJobSlots(int delta);

    /**
     * 플레이어에게 적용되는 실제 최대 직업 수.
     * - 전역 설정(maxJobs) + 개인 추가 슬롯
     */
    default int jobsplus$getEffectiveMaxJobs() {
        return JobsPlusConfig.maxJobs.get() + Math.max(0, jobsplus$getExtraJobSlots());
    }
}
