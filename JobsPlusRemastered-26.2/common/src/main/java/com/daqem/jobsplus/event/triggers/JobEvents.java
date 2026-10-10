package com.daqem.jobsplus.event.triggers;

import com.daqem.arc.api.action.data.ActionDataBuilder;
import com.daqem.arc.api.player.ArcPlayer;
import com.daqem.itemrestrictions.data.ItemRestriction;
import com.daqem.jobsplus.JobsPlus;
import com.daqem.jobsplus.config.JobsPlusConfig;
import com.daqem.jobsplus.integration.arc.action.type.JobsPlusActionType;
import com.daqem.jobsplus.integration.arc.data.type.JobsPlusActionDataType;
import com.daqem.jobsplus.integration.arc.holder.holders.job.JobInstance;
import com.daqem.jobsplus.metrics.MetricsEvent;
import com.daqem.jobsplus.networking.s2c.ClientboundLevelUpJobPacket;
import com.daqem.jobsplus.networking.s2c.ClientboundUnlockItemRestrictionPacket;
import com.daqem.jobsplus.player.JobsPlayer;
import com.daqem.jobsplus.player.job.Job;
import dev.architectury.networking.NetworkManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class JobEvents
{

    private static final ScheduledExecutorService SCHEDULER = Executors.newSingleThreadScheduledExecutor(runnable ->
    {
        Thread thread = new Thread(runnable, "JobsPlus-LevelUpEffects");
        thread.setDaemon(true);
        return thread;
    });

    public static void onJobLevelUp(JobsPlayer player, Job job)
    {
        if (player instanceof ArcPlayer arcPlayer)
        {
            new ActionDataBuilder(arcPlayer, JobsPlusActionType.JOB_LEVEL_UP).withData(JobsPlusActionDataType.ONLY_FOR_JOB, job).build().sendToAction();
        }
        
        if (player.jobsplus$getPlayer() instanceof ServerPlayer serverPlayer)
        {
            NetworkManager.sendToPlayer(serverPlayer, new ClientboundLevelUpJobPacket(job.getJobInstance().getLocation(), job.getLevel()));

            List<ItemRestriction> itemRestrictions = job.getJobInstance().getItemRestrictions().entrySet().stream().filter(entry -> entry.getValue() == job.getLevel()).map(Map.Entry::getKey).toList();

            for (ItemRestriction itemRestriction : itemRestrictions)
            {
                NetworkManager.sendToPlayer(serverPlayer, new ClientboundUnlockItemRestrictionPacket(itemRestriction.getLocation()));
            }

            triggerLevelUpEffects(serverPlayer);

            // 플레이어 코인 얻는 이벤트
            int coinsBefore = player.jobsplus$getCoins();
            player.jobsplus$addCoins(JobsPlusConfig.COINS_PER_LEVEL_UP);
            JobInstance jobInstance = job.getJobInstance();
            // 코인은 직업 공용이므로 어느 직업 레벨업에서 들어온 코인인지 남겨야 스킬 구매 재원을 추적할 수 있다.
            MetricsEvent.of("LEVEL_UP")
                    .player(serverPlayer)
                    .job(jobInstance.getLocation())
                    .before(job.getLevel() - 1)
                    .after(job.getLevel())
                    .coins(coinsBefore, player.jobsplus$getCoins())
                    .jobLevel(job.getLevel())
                    .record();
            serverPlayer.level().getServer().getPlayerList().broadcastSystemMessage(JobsPlus.translatable("job.level_up", serverPlayer.getName().copy().withStyle(style -> style.withColor(jobInstance.getColorDecimal())), JobsPlus.literal(String.valueOf(job.getLevel())).withStyle(style -> style.withColor(jobInstance.getColorDecimal())), jobInstance.getName().getString()), false);
        }
    }

    public static void onJobExperience(JobsPlayer player, Job job, double experience)
    {
        // 메트릭은 기본·쿠폰 몫을 아는 JobExpReward와 스킬 보너스를 아는 JobExpMultiplierReward에서 나누어 기록한다.
        if (player instanceof ArcPlayer arcPlayer)
        {
            new ActionDataBuilder(arcPlayer, JobsPlusActionType.JOB_EXP).withData(JobsPlusActionDataType.JOB_EXP, experience).withData(JobsPlusActionDataType.ONLY_FOR_JOB, job).build().sendToAction();
        }
    }

    private static void schedule(ServerPlayer player, Runnable task, long delayInMillis)
    {
        MinecraftServer server = player.level().getServer();
        if (server == null)
        {
            return;
        }

        SCHEDULER.schedule(() ->
        {
            if (server.isRunning())
            {
                server.execute(task);
            }
        }, delayInMillis, TimeUnit.MILLISECONDS);
    }

    public static void triggerLevelUpEffects(ServerPlayer player)
    {

        // 250ms(5틱) 뒤 첫 소리 재생
        schedule(player, () ->
        {
            playLevelUpSound(player, 0.5F, 2F);
            playEXPOrbPickupSound(player);
        }, 250);

        // 450ms(9틱) 뒤 두 번째 소리 재생
        schedule(player, () ->
        {
            playLevelUpSound(player, 1F, 2F);
            playEXPOrbPickupSound(player);
        }, 450);

        // 550ms(11틱) 뒤 마지막 소리 재생
        schedule(player, () ->
        {
            playLevelUpSound(player, 0.5F, 1.5F);
            playEXPOrbPickupSound(player);
        }, 550);
    }

    public static void playLevelUpSound(ServerPlayer player, float volume, float pitch)
    {
        player.level().playSound(null, player.blockPosition(), SoundEvents.PLAYER_LEVELUP, SoundSource.AMBIENT, volume, pitch);
    }

    public static void playEXPOrbPickupSound(ServerPlayer player)
    {
        player.level().playSound(null, player.blockPosition(), SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.AMBIENT, 1F, 1F);
    }
}
