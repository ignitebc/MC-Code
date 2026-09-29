package com.daqem.jobsplus.metrics;

import com.daqem.jobsplus.player.JobsServerPlayer;
import com.daqem.jobsplus.player.job.Job;
import com.daqem.jobsplus.player.job.powerup.Powerup;
import com.daqem.jobsplus.player.job.powerup.PowerupState;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.Permissions;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 플레이어의 직업 상태를 snapshots.csv 행으로 만든다.
 * 메트릭 수집이 시즌 도중에 시작되거나 관리자 조작이 섞여도, 스냅샷으로 그 시점의 레벨·스킬·코인을 바로 알 수 있다.
 * 직업마다 한 행이며 직업이 없으면 직업 열을 비운 한 행을 쓴다.
 */
final class PlayerSnapshots
{
    static final String HEADER = "timestamp_ms,reason,player_uuid,player_name,game_mode,operator,coins,max_jobs,"
            + "job_id,job_level,job_experience,experience_to_next,owned_powerups,active_powerups,active_powerup_ids";

    private PlayerSnapshots()
    {
    }

    static List<String> capture(ServerPlayer player, String reason, long now)
    {
        List<String> lines = new ArrayList<>();
        if (!(player instanceof JobsServerPlayer jobsPlayer))
        {
            return lines;
        }

        String prefix = now + ","
                + MetricsCsv.text(reason) + ","
                + player.getUUID() + ","
                + MetricsCsv.text(player.getName().getString()) + ","
                + MetricsCsv.text(player.gameMode().getName()) + ","
                + player.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER) + ","
                + jobsPlayer.jobsplus$getCoins() + ","
                + jobsPlayer.jobsplus$getEffectiveMaxJobs() + ",";

        List<Job> jobs = jobsPlayer.jobsplus$getJobs();
        if (jobs.isEmpty())
        {
            lines.add(prefix + ",,,,,,");
            return lines;
        }

        for (Job job : jobs)
        {
            List<Powerup> powerups = job.getPowerupManager().getAllPowerups();
            List<String> activePowerupIds = powerups.stream()
                    .filter(powerup -> powerup.getState() == PowerupState.ACTIVE)
                    .map(powerup -> powerup.getPowerupLocation().toString())
                    .sorted()
                    .toList();
            lines.add(prefix
                    + MetricsCsv.text(job.getJobInstance().getLocation().toString()) + ","
                    + job.getLevel() + ","
                    + job.getExperience() + ","
                    + (job.isMaxLevel() ? 0 : job.getExperienceForNextLevel()) + ","
                    + powerups.size() + ","
                    + activePowerupIds.size() + ","
                    + MetricsCsv.text(activePowerupIds.stream().collect(Collectors.joining(";"))));
        }
        return lines;
    }
}
