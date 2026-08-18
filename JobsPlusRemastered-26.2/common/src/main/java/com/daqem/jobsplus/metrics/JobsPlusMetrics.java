package com.daqem.jobsplus.metrics;

import com.daqem.jobsplus.JobsPlus;
import com.daqem.jobsplus.integration.arc.holder.holders.job.JobInstance;
import com.daqem.jobsplus.player.JobsPlayer;
import com.daqem.jobsplus.player.job.Job;
import dev.architectury.event.events.common.LifecycleEvent;
import dev.architectury.event.events.common.PlayerEvent;
import dev.architectury.event.events.common.TickEvent;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

/**
 * Lightweight analysis metrics for real server play.
 * <p>
 * Only job EXP, job BTC rewards and connection state are recorded. High-frequency
 * job events are aggregated into five-second buckets in memory and flushed in one
 * append operation every five minutes. Shop/stock data is intentionally excluded.
 */
public final class JobsPlusMetrics
{
    public static final long BUCKET_MILLIS = 5_000L;

    private static final int FLUSH_INTERVAL_TICKS = 20 * 60 * 5;
    private static final String FILE_NAME = "jobsplus_metrics.csv";
    private static final String HEADER = "timestamp_ms,record_type,player_uuid,player_name,job_id,exp,btc,bucket_ms\n";
    private static final Object LOCK = new Object();

    /** bucketStart -> player UUID -> job id -> accumulated metrics */
    private static final Map<Long, Map<UUID, Map<Identifier, MetricBucket>>> BUCKETS = new LinkedHashMap<>();
    private static final List<SessionEvent> SESSION_EVENTS = new ArrayList<>();
    private static final Map<UUID, String> ONLINE_PLAYERS = new LinkedHashMap<>();

    private static int ticksUntilFlush = FLUSH_INTERVAL_TICKS;
    private static boolean registered;

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
    }

    /**
     * Records actual job EXP granted by the reward pipeline. Base EXP and powerup
     * bonus EXP are both sent here, so the bucket total is the real awarded amount.
     */
    public static void recordExperience(JobsPlayer jobsPlayer, Job job, double experience)
    {
        if (experience <= 0.0D || job == null)
        {
            return;
        }
        if (!(jobsPlayer.jobsplus$getPlayer() instanceof ServerPlayer serverPlayer))
        {
            return;
        }

        recordBucket(serverPlayer, job.getJobInstance(), experience, 0);
    }

    /** Records only BTC that was actually awarded to the player. */
    public static void recordBitcoin(ServerPlayer player, JobInstance jobInstance, int amount)
    {
        if (player == null || jobInstance == null || amount <= 0)
        {
            return;
        }
        recordBucket(player, jobInstance, 0.0D, amount);
    }

    private static void recordBucket(ServerPlayer player, JobInstance jobInstance, double experience, int bitcoin)
    {
        long now = System.currentTimeMillis();
        long bucketStart = now - Math.floorMod(now, BUCKET_MILLIS);
        UUID uuid = player.getUUID();
        Identifier jobId = jobInstance.getLocation();
        String playerName = player.getName().getString();

        synchronized (LOCK)
        {
            Map<UUID, Map<Identifier, MetricBucket>> players = BUCKETS.computeIfAbsent(
                    bucketStart,
                    ignored -> new LinkedHashMap<>()
            );
            Map<Identifier, MetricBucket> jobs = players.computeIfAbsent(
                    uuid,
                    ignored -> new LinkedHashMap<>()
            );
            MetricBucket bucket = jobs.computeIfAbsent(
                    jobId,
                    ignored -> new MetricBucket(bucketStart, uuid, playerName, jobId.toString())
            );
            bucket.experience += experience;
            bucket.bitcoin += bitcoin;
        }
    }

    private static void initializeServer(MinecraftServer server)
    {
        synchronized (LOCK)
        {
            BUCKETS.clear();
            SESSION_EVENTS.clear();
            ONLINE_PLAYERS.clear();
            ticksUntilFlush = FLUSH_INTERVAL_TICKS;
        }
    }

    private static void shutdownServer(MinecraftServer server)
    {
        long now = System.currentTimeMillis();
        synchronized (LOCK)
        {
            for (Map.Entry<UUID, String> entry : ONLINE_PLAYERS.entrySet())
            {
                SESSION_EVENTS.add(new SessionEvent(now, "LOGOUT", entry.getKey(), entry.getValue()));
            }
            ONLINE_PLAYERS.clear();
        }
        flush(server, true);
    }

    private static void onPlayerJoin(ServerPlayer player)
    {
        UUID uuid = player.getUUID();
        String name = player.getName().getString();
        long now = System.currentTimeMillis();

        synchronized (LOCK)
        {
            if (!ONLINE_PLAYERS.containsKey(uuid))
            {
                SESSION_EVENTS.add(new SessionEvent(now, "LOGIN", uuid, name));
            }
            ONLINE_PLAYERS.put(uuid, name);
        }

        MinecraftServer server = player.level().getServer();
        if (server != null)
        {
            flush(server, false);
        }
    }

    private static void onPlayerQuit(ServerPlayer player)
    {
        UUID uuid = player.getUUID();
        String name = player.getName().getString();
        long now = System.currentTimeMillis();

        synchronized (LOCK)
        {
            if (ONLINE_PLAYERS.remove(uuid) != null)
            {
                SESSION_EVENTS.add(new SessionEvent(now, "LOGOUT", uuid, name));
            }
        }

        MinecraftServer server = player.level().getServer();
        if (server != null)
        {
            flush(server, false);
        }
    }

    private static void onServerTick(MinecraftServer server)
    {
        boolean shouldFlush;
        synchronized (LOCK)
        {
            ticksUntilFlush--;
            shouldFlush = ticksUntilFlush <= 0;
            if (shouldFlush)
            {
                ticksUntilFlush = FLUSH_INTERVAL_TICKS;
                long now = System.currentTimeMillis();
                for (Map.Entry<UUID, String> entry : ONLINE_PLAYERS.entrySet())
                {
                    SESSION_EVENTS.add(new SessionEvent(now, "ONLINE", entry.getKey(), entry.getValue()));
                }
            }
        }

        if (shouldFlush)
        {
            flush(server, false);
        }
    }

    /**
     * Appends pending rows to one CSV. Normal flushes leave the current five-second
     * bucket in memory so it cannot be split across two writes. Server shutdown flushes it too.
     */
    private static void flush(MinecraftServer server, boolean includeCurrentBucket)
    {
        synchronized (LOCK)
        {
            long currentBucketStart = currentBucketStart();
            List<MetricRow> rows = new ArrayList<>();
            List<Long> bucketStartsToRemove = new ArrayList<>();

            for (Map.Entry<Long, Map<UUID, Map<Identifier, MetricBucket>>> bucketEntry : BUCKETS.entrySet())
            {
                long bucketStart = bucketEntry.getKey();
                if (!includeCurrentBucket && bucketStart >= currentBucketStart)
                {
                    continue;
                }

                for (Map<Identifier, MetricBucket> jobs : bucketEntry.getValue().values())
                {
                    for (MetricBucket bucket : jobs.values())
                    {
                        rows.add(new MetricRow(
                                bucket.timestamp,
                                "JOB_BUCKET",
                                bucket.playerUuid,
                                bucket.playerName,
                                bucket.jobId,
                                bucket.experience,
                                bucket.bitcoin,
                                BUCKET_MILLIS
                        ));
                    }
                }
                bucketStartsToRemove.add(bucketStart);
            }

            for (SessionEvent event : SESSION_EVENTS)
            {
                rows.add(new MetricRow(
                        event.timestamp,
                        event.type,
                        event.playerUuid,
                        event.playerName,
                        "",
                        0.0D,
                        0,
                        0L
                ));
            }

            if (rows.isEmpty())
            {
                return;
            }

            rows.sort(Comparator.comparingLong(MetricRow::timestamp));
            StringBuilder output = new StringBuilder(rows.size() * 96);
            for (MetricRow row : rows)
            {
                appendCsvRow(output, row);
            }

            try
            {
                Path directory = server.getServerDirectory().resolve("logs").resolve("jobsplus-metrics");
                Files.createDirectories(directory);
                Path file = directory.resolve(FILE_NAME);
                boolean needsHeader = !Files.exists(file) || Files.size(file) == 0L;

                if (needsHeader)
                {
                    output.insert(0, HEADER);
                }

                Files.writeString(
                        file,
                        output,
                        StandardCharsets.UTF_8,
                        StandardOpenOption.CREATE,
                        StandardOpenOption.WRITE,
                        StandardOpenOption.APPEND
                );

                for (Long bucketStart : bucketStartsToRemove)
                {
                    BUCKETS.remove(bucketStart);
                }
                SESSION_EVENTS.clear();
            }
            catch (IOException exception)
            {
                JobsPlus.LOGGER.error("Failed to flush Jobs+ analysis metrics.", exception);
            }
        }
    }

    private static long currentBucketStart()
    {
        long now = System.currentTimeMillis();
        return now - Math.floorMod(now, BUCKET_MILLIS);
    }

    private static void appendCsvRow(StringBuilder output, MetricRow row)
    {
        output.append(row.timestamp()).append(',')
                .append(csv(row.type())).append(',')
                .append(row.playerUuid()).append(',')
                .append(csv(row.playerName())).append(',')
                .append(csv(row.jobId())).append(',')
                .append(formatDouble(row.experience())).append(',')
                .append(row.bitcoin()).append(',')
                .append(row.bucketMillis())
                .append('\n');
    }

    private static String formatDouble(double value)
    {
        return String.format(Locale.ROOT, "%.6f", value);
    }

    private static String csv(String value)
    {
        if (value == null)
        {
            return "";
        }
        if (value.indexOf(',') < 0 && value.indexOf('"') < 0 && value.indexOf('\n') < 0 && value.indexOf('\r') < 0)
        {
            return value;
        }
        return '"' + value.replace("\"", "\"\"") + '"';
    }

    private static final class MetricBucket
    {
        private final long timestamp;
        private final UUID playerUuid;
        private final String playerName;
        private final String jobId;
        private double experience;
        private int bitcoin;

        private MetricBucket(long timestamp, UUID playerUuid, String playerName, String jobId)
        {
            this.timestamp = timestamp;
            this.playerUuid = playerUuid;
            this.playerName = playerName;
            this.jobId = jobId;
        }
    }

    private record SessionEvent(long timestamp, String type, UUID playerUuid, String playerName)
    {
    }

    private record MetricRow(
            long timestamp,
            String type,
            UUID playerUuid,
            String playerName,
            String jobId,
            double experience,
            int bitcoin,
            long bucketMillis)
    {
    }
}
