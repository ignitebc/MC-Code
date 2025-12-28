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
     * 플레이어가 직업선택권 등으로 얻은 추가 슬롯
     */
    int jobsplus$getExtraJobSlots();

    /**
     * extra_job_slots 누적 증가(또는 감소)
     */
    void jobsplus$addExtraJobSlots(int delta);

    /**
     * 실제 적용되는 최대 직업 수
     * 정책:
     * - 기본 무료 직업 수(amount_of_free_jobs)만큼은 누구나 즉시 선택 가능
     * - 직업선택권 사용 시 extra_job_slots가 증가하며, 그만큼 추가로 직업 선택 가능
     * - 단, 최종 최대치는 config.max_jobs(예: 7)로 제한
     */
    default int jobsplus$getEffectiveMaxJobs() {
        int base = Math.max(0, JobsPlusConfig.amountOfFreeJobs.get()); // 기본 1
        int extra = Math.max(0, jobsplus$getExtraJobSlots());          // 티켓 누적
        int cap = Math.max(0, JobsPlusConfig.maxJobs.get());           // 최종 상한 7

        long desired = (long) base + (long) extra;
        if (desired > Integer.MAX_VALUE) desired = Integer.MAX_VALUE;

        return (int) Math.min((long) cap, desired);
    }

    /**
     * 무료로 선택 가능한 직업 수(= 코인/가격과 무관하게 선택 가능)
     * 정책: 티켓으로 늘린 슬롯도 무료 선택으로 취급
     */
    default int jobsplus$getEffectiveFreeJobs() {
        return jobsplus$getEffectiveMaxJobs();
    }
}
