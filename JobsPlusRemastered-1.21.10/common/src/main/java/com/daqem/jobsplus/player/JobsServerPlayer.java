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
     * 플레이어가 직업추가권 등으로 얻은 추가 슬롯 (상한 없음)
     */
    int jobsplus$getExtraJobSlots();

    /**
     * extra_job_slots 누적 증가(또는 감소)
     */
    void jobsplus$addExtraJobSlots(int delta);

    /**
     * 실제 적용되는 최대 직업 수
     * = 전역 기본 슬롯 + 플레이어 추가 슬롯
     */
    default int jobsplus$getEffectiveMaxJobs() {
        return JobsPlusConfig.maxJobs.get() + Math.max(0, jobsplus$getExtraJobSlots());
    }
}
