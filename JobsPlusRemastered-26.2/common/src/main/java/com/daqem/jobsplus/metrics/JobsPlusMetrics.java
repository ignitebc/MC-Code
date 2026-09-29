package com.daqem.jobsplus.metrics;

import com.daqem.arc.api.action.IAction;
import com.daqem.arc.api.action.data.ActionData;
import com.daqem.arc.api.action.result.ActionResult;
import com.daqem.arc.event.events.ActionEvent;
import com.daqem.jobsplus.JobsPlus;
import com.daqem.jobsplus.config.JobsPlusConfig;
import com.daqem.jobsplus.integration.arc.holder.holders.job.JobInstance;
import com.daqem.jobsplus.player.JobsServerPlayer;
import com.daqem.jobsplus.player.job.Job;
import dev.architectury.event.events.common.LifecycleEvent;
import dev.architectury.event.events.common.PlayerEvent;
import dev.architectury.event.events.common.TickEvent;
import dev.architectury.platform.Mod;
import dev.architectury.platform.Platform;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * 실제 서버 플레이의 직업 밸런스 분석용 메트릭.
 * <p>
 * logs/jobsplus-metrics/v2/&lt;시즌&gt; 아래에 파일별로 기록한다. 시즌 이름은 설정 metrics.season으로 정한다.
 * <ul>
 *     <li>actions.csv: 5초 버킷 × 플레이어 × 직업 액션별 실행 횟수, EXP(기본·쿠폰·스킬 보너스), BTC</li>
 *     <li>events.csv: 접속·종료, 5분 접속 표시, 레벨업, 스킬 구매·실패·전환, 직업 선택, 쿠폰 사용, 관리자 명령</li>
 *     <li>snapshots.csv: 접속·종료 시와 접속 중 1시간마다 직업별 레벨·EXP·스킬·코인 상태</li>
 *     <li>activity.csv: 1분마다 위치·이동량·입력 흔적·잠수 여부</li>
 *     <li>balance_rewards.csv, balance_powerups.csv: 밸런스 버전별 실제 적용 보상표와 스킬 가격표</li>
 * </ul>
 * 빈도가 높은 액션 기록은 메모리에서 묶었다가 저장 주기마다 한 번에 추가한다. 상점·주식 거래는 기록하지 않는다.
 */
public final class JobsPlusMetrics
{
    public static final int SCHEMA_VERSION = 2;

    /** 서버가 비정상 종료돼도 잃는 기록이 1분 이내가 되도록 1분마다 저장한다. */
    private static final int FLUSH_INTERVAL_TICKS = 20 * 60;
    private static final int HEARTBEAT_INTERVAL_TICKS = 20 * 60 * 5;
    private static final String ACTIONS_FILE = "actions.csv";
    private static final String EVENTS_FILE = "events.csv";
    private static final String SNAPSHOTS_FILE = "snapshots.csv";
    private static final String ACTIVITY_FILE = "activity.csv";
    private static final String BALANCE_REWARDS_FILE = "balance_rewards.csv";
    private static final String BALANCE_POWERUPS_FILE = "balance_powerups.csv";
    private static final long SNAPSHOT_INTERVAL_MILLIS = 60L * 60L * 1000L;
    /** 디스크 오류가 계속될 때 메모리가 한없이 늘지 않도록 파일별 대기 행 수를 제한한다. */
    private static final int MAX_PENDING_LINES = 500_000;
    private static final Object LOCK = new Object();

    private static final ActionMetrics ACTIONS = new ActionMetrics();
    private static final PlayerActivity ACTIVITY = new PlayerActivity();
    private static final Map<String, PendingFile> PENDING_FILES = new LinkedHashMap<>();
    private static final Map<UUID, String> ONLINE_PLAYERS = new LinkedHashMap<>();
    private static final Map<UUID, Long> LAST_SNAPSHOTS = new LinkedHashMap<>();
    private static final Set<String> WRITTEN_BALANCE_VERSIONS = new HashSet<>();

    private static Path directory;
    private static int ticksUntilFlush = FLUSH_INTERVAL_TICKS;
    private static int ticksUntilHeartbeat = HEARTBEAT_INTERVAL_TICKS;
    private static boolean registered;
    private static boolean listenerFailureLogged;
    private static volatile String balanceVersion = "";
    private static volatile boolean balanceDirty;

    private JobsPlusMetrics()
    {
    }

    public static void registerEvents()
    {
        if (registered)
        {
            return;
        }
        registered = true;

        LifecycleEvent.SERVER_STARTED.register(JobsPlusMetrics::initializeServer);
        LifecycleEvent.SERVER_STOPPING.register(JobsPlusMetrics::shutdownServer);
        PlayerEvent.PLAYER_JOIN.register(JobsPlusMetrics::onPlayerJoin);
        PlayerEvent.PLAYER_QUIT.register(JobsPlusMetrics::onPlayerQuit);
        TickEvent.SERVER_POST.register(JobsPlusMetrics::onServerTick);
        ActionEvent.REWARDS_APPLYING.register(JobsPlusMetrics::onRewardsApplying);
        ActionEvent.REWARDS_APPLIED.register(JobsPlusMetrics::onRewardsApplied);
    }

    /**
     * 직업 EXP 보상의 기본 EXP와 쿠폰으로 늘어난 몫을 나누어 기록한다.
     * 스킬 보너스는 {@link #recordSkillExperience}로 따로 들어온다.
     */
    public static void recordExperience(ServerPlayer player, Job job, double baseExperience, double couponBonus)
    {
        if (player == null || job == null || baseExperience + couponBonus <= 0.0D)
        {
            return;
        }
        synchronized (LOCK)
        {
            ACTIONS.addExperience(player, job, baseExperience, couponBonus, 0.0D);
        }
    }

    /** EXP 배율 스킬이 추가로 지급한 EXP를 기록한다. */
    public static void recordSkillExperience(ServerPlayer player, Job job, double skillBonus)
    {
        if (player == null || job == null || skillBonus <= 0.0D)
        {
            return;
        }
        synchronized (LOCK)
        {
            ACTIONS.addExperience(player, job, 0.0D, 0.0D, skillBonus);
        }
    }

    /** 실제로 지급한 BTC만 기록한다. 쿠폰으로 확률이 오른 구간은 btc_coupon_multiplier 열로 구분된다. */
    public static void recordBitcoin(ServerPlayer player, JobInstance jobInstance, int amount)
    {
        if (player == null || jobInstance == null || amount <= 0)
        {
            return;
        }
        int jobLevel = 0;
        if (player instanceof JobsServerPlayer jobsPlayer)
        {
            Job job = jobsPlayer.jobsplus$getJob(jobInstance);
            if (job != null)
            {
                jobLevel = job.getLevel();
            }
        }
        synchronized (LOCK)
        {
            ACTIONS.addBitcoin(player, jobInstance, jobLevel, amount);
        }
    }

    /** 직업·스킬 데이터를 다시 읽으면 다음 틱에 보상표를 다시 확인한다. */
    public static void markBalanceDirty()
    {
        balanceDirty = true;
    }

    static String balanceVersion()
    {
        return balanceVersion;
    }

    static void recordEvent(MetricsEvent event)
    {
        synchronized (LOCK)
        {
            pending(EVENTS_FILE, MetricsEvent.HEADER).add(event.toCsvLine());
        }
    }

    private static void onRewardsApplying(IAction action, ActionData actionData)
    {
        if (!(actionData.getPlayer() instanceof JobsServerPlayer))
        {
            return;
        }
        try
        {
            synchronized (LOCK)
            {
                UUID playerUuid = ACTIONS.beginAction(action, actionData);
                if (playerUuid != null)
                {
                    ACTIVITY.recordJobAction(playerUuid);
                }
            }
        }
        catch (RuntimeException exception)
        {
            // 메트릭 오류가 직업 보상 지급을 막으면 안 되므로 삼키고 한 번만 남긴다.
            if (!listenerFailureLogged)
            {
                listenerFailureLogged = true;
                JobsPlus.LOGGER.error("Failed to record Jobs+ action metrics.", exception);
            }
        }
    }

    private static void onRewardsApplied(IAction action, ActionData actionData, ActionResult result)
    {
        if (!(actionData.getPlayer() instanceof JobsServerPlayer))
        {
            return;
        }
        synchronized (LOCK)
        {
            ACTIONS.endAction(action, actionData);
        }
    }

    private static void initializeServer(MinecraftServer server)
    {
        synchronized (LOCK)
        {
            String season = seasonFolder();
            directory = server.getServerDirectory()
                    .resolve("logs")
                    .resolve("jobsplus-metrics")
                    .resolve("v" + SCHEMA_VERSION)
                    .resolve(season);
            ACTIONS.clear();
            ACTIVITY.clear();
            PENDING_FILES.clear();
            ONLINE_PLAYERS.clear();
            LAST_SNAPSHOTS.clear();
            WRITTEN_BALANCE_VERSIONS.clear();
            MetricsCsv.resetVerifiedFiles();
            ticksUntilFlush = FLUSH_INTERVAL_TICKS;
            ticksUntilHeartbeat = HEARTBEAT_INTERVAL_TICKS;
            listenerFailureLogged = false;
            balanceVersion = "";

            try
            {
                WRITTEN_BALANCE_VERSIONS.addAll(MetricsCsv.readFirstColumn(directory.resolve(BALANCE_REWARDS_FILE), BalanceTable.REWARDS_HEADER));
            }
            catch (IOException exception)
            {
                JobsPlus.LOGGER.error("Failed to read recorded Jobs+ balance versions.", exception);
            }
            refreshBalanceVersion();

            // 재시작 전 세션이 LOGOUT 없이 끊겼다면 분석에서 이 행을 기준으로 이전 세션을 닫는다.
            pending(EVENTS_FILE, MetricsEvent.HEADER).add(MetricsEvent.of("SERVER_START")
                    .target(balanceVersion)
                    .detail("schema", SCHEMA_VERSION)
                    .detail("season", season)
                    .detail("minecraft", server.getServerVersion())
                    .detail("jobsplus", modVersion(JobsPlus.MOD_ID))
                    .detail("arc", modVersion("arc"))
                    .toCsvLine());
        }
        flush(false);
    }

    private static void shutdownServer(MinecraftServer server)
    {
        long now = System.currentTimeMillis();
        synchronized (LOCK)
        {
            for (Map.Entry<UUID, String> entry : ONLINE_PLAYERS.entrySet())
            {
                pending(EVENTS_FILE, MetricsEvent.HEADER).add(
                        MetricsEvent.of("LOGOUT", now).player(entry.getKey(), entry.getValue()).detail("reason", "server_stop").toCsvLine());
                ServerPlayer player = server.getPlayerList().getPlayer(entry.getKey());
                if (player != null)
                {
                    snapshot(player, "SERVER_STOP", now);
                }
            }
            pending(ACTIVITY_FILE, PlayerActivity.HEADER).addAll(ACTIVITY.finishAll(server));
            ONLINE_PLAYERS.clear();
            LAST_SNAPSHOTS.clear();
            pending(EVENTS_FILE, MetricsEvent.HEADER).add(MetricsEvent.of("SERVER_STOP", now).target(balanceVersion).toCsvLine());
        }
        flush(true);
    }

    private static void onPlayerJoin(ServerPlayer player)
    {
        UUID uuid = player.getUUID();
        String name = player.getName().getString();

        synchronized (LOCK)
        {
            if (!ONLINE_PLAYERS.containsKey(uuid))
            {
                pending(EVENTS_FILE, MetricsEvent.HEADER).add(MetricsEvent.of("LOGIN").player(uuid, name).toCsvLine());
            }
            ONLINE_PLAYERS.put(uuid, name);
            snapshot(player, "LOGIN", System.currentTimeMillis());
        }
        flush(false);
    }

    private static void onPlayerQuit(ServerPlayer player)
    {
        UUID uuid = player.getUUID();
        String name = player.getName().getString();

        synchronized (LOCK)
        {
            if (ONLINE_PLAYERS.remove(uuid) != null)
            {
                pending(EVENTS_FILE, MetricsEvent.HEADER).add(MetricsEvent.of("LOGOUT").player(uuid, name).toCsvLine());
                snapshot(player, "LOGOUT", System.currentTimeMillis());
            }
            ACTIVITY.finish(player, pending(ACTIVITY_FILE, PlayerActivity.HEADER));
            LAST_SNAPSHOTS.remove(uuid);
        }
        flush(false);
    }

    private static void onServerTick(MinecraftServer server)
    {
        boolean shouldFlush;
        synchronized (LOCK)
        {
            if (balanceDirty && directory != null)
            {
                refreshBalanceVersion();
            }
            ACTIVITY.tick(server, pending(ACTIVITY_FILE, PlayerActivity.HEADER));
            ticksUntilFlush--;
            shouldFlush = ticksUntilFlush <= 0;
            if (shouldFlush)
            {
                ticksUntilFlush = FLUSH_INTERVAL_TICKS;
            }

            ticksUntilHeartbeat--;
            if (ticksUntilHeartbeat <= 0)
            {
                ticksUntilHeartbeat = HEARTBEAT_INTERVAL_TICKS;
                long now = System.currentTimeMillis();
                for (Map.Entry<UUID, String> entry : ONLINE_PLAYERS.entrySet())
                {
                    pending(EVENTS_FILE, MetricsEvent.HEADER).add(
                            MetricsEvent.of("ONLINE", now).player(entry.getKey(), entry.getValue()).toCsvLine());
                    Long lastSnapshot = LAST_SNAPSHOTS.get(entry.getKey());
                    ServerPlayer player = server.getPlayerList().getPlayer(entry.getKey());
                    if (player != null && (lastSnapshot == null || now - lastSnapshot >= SNAPSHOT_INTERVAL_MILLIS))
                    {
                        snapshot(player, "PERIODIC", now);
                    }
                }
            }
        }

        if (shouldFlush)
        {
            flush(false);
        }
    }

    /**
     * 대기 중인 행을 파일별로 추가한다. 평소에는 채워지는 중인 현재 5초 버킷을 남겨 한 버킷이 두 번에 나뉘어 쓰이지 않게 한다.
     * 서버 종료 때는 현재 버킷까지 모두 쓴다. 쓰기에 실패한 파일의 행은 다음 주기에 다시 시도한다.
     */
    private static void flush(boolean includeCurrentBucket)
    {
        synchronized (LOCK)
        {
            if (directory == null)
            {
                return;
            }

            List<String> actionLines = ACTIONS.drain(includeCurrentBucket, System.currentTimeMillis());
            if (!actionLines.isEmpty())
            {
                pending(ACTIONS_FILE, ActionMetrics.HEADER).addAll(actionLines);
            }

            for (Map.Entry<String, PendingFile> entry : PENDING_FILES.entrySet())
            {
                PendingFile pendingFile = entry.getValue();
                if (pendingFile.lines.isEmpty())
                {
                    continue;
                }
                try
                {
                    MetricsCsv.append(directory.resolve(entry.getKey()), pendingFile.header, pendingFile.lines);
                    pendingFile.lines.clear();
                }
                catch (IOException exception)
                {
                    JobsPlus.LOGGER.error("Failed to flush Jobs+ analysis metrics to {}.", entry.getKey(), exception);
                    if (pendingFile.lines.size() > MAX_PENDING_LINES)
                    {
                        JobsPlus.LOGGER.error("Dropping {} pending Jobs+ metric rows for {}.", pendingFile.lines.size(), entry.getKey());
                        pendingFile.lines.clear();
                    }
                }
            }
        }
    }

    /**
     * 현재 적용된 보상표의 버전을 계산하고, 처음 보는 버전이면 보상표·스킬 가격표를 파일에 남긴다.
     * 버전이 바뀌면 이후 actions.csv 행은 새 버전으로 기록된다.
     */
    private static void refreshBalanceVersion()
    {
        balanceDirty = false;
        BalanceTable table;
        try
        {
            table = BalanceTable.capture();
        }
        catch (RuntimeException exception)
        {
            JobsPlus.LOGGER.error("Failed to capture Jobs+ balance table for metrics.", exception);
            return;
        }
        if (table.version().equals(balanceVersion))
        {
            return;
        }

        String previousVersion = balanceVersion;
        balanceVersion = table.version();
        if (WRITTEN_BALANCE_VERSIONS.add(table.version()))
        {
            pending(BALANCE_REWARDS_FILE, BalanceTable.REWARDS_HEADER).addAll(table.versionedRewardRows());
            pending(BALANCE_POWERUPS_FILE, BalanceTable.POWERUPS_HEADER).addAll(table.versionedPowerupRows());
        }
        pending(EVENTS_FILE, MetricsEvent.HEADER).add(MetricsEvent.of("BALANCE_VERSION")
                .target(table.version())
                .before(previousVersion)
                .after(table.version())
                .detail("jobs", table.jobCount())
                .detail("powerups", table.powerupCount())
                .detail("reward_rows", table.rewardRowCount())
                .toCsvLine());
    }

    private static String seasonFolder()
    {
        String season = JobsPlusConfig.metricsSeason.get();
        season = season == null ? "" : season.trim().replaceAll("[^A-Za-z0-9_-]", "");
        return season.isEmpty() ? "unspecified" : season;
    }

    private static String modVersion(String modId)
    {
        return Platform.getOptionalMod(modId).map(Mod::getVersion).orElse("");
    }

    private static void snapshot(ServerPlayer player, String reason, long now)
    {
        pending(SNAPSHOTS_FILE, PlayerSnapshots.HEADER).addAll(PlayerSnapshots.capture(player, reason, now));
        LAST_SNAPSHOTS.put(player.getUUID(), now);
    }

    private static List<String> pending(String fileName, String header)
    {
        return PENDING_FILES.computeIfAbsent(fileName, ignored -> new PendingFile(header)).lines;
    }

    private static final class PendingFile
    {
        private final String header;
        private final List<String> lines = new ArrayList<>();

        private PendingFile(String header)
        {
            this.header = header;
        }
    }
}
