package com.daqem.jobsplus.metrics;

import com.daqem.arc.api.action.IAction;
import com.daqem.arc.api.action.data.ActionData;
import com.daqem.jobsplus.integration.arc.holder.holders.job.JobInstance;
import com.daqem.jobsplus.player.JobsServerPlayer;
import com.daqem.jobsplus.player.coupon.RewardCouponLedger;
import com.daqem.jobsplus.player.job.Job;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 직업 액션 실행 횟수와 그 액션이 지급한 EXP·BTC를 5초 버킷으로 묶는다.
 * <p>
 * Arc가 직업 액션의 보상 적용을 시작하면 실행 프레임을 쌓고, 보상 안에서 기록되는 EXP·BTC를 그 프레임의 액션에 붙인다.
 * EXP 배율 스킬처럼 보상 안에서 실행되는 스킬 액션의 보너스도 원래 직업 액션으로 묶인다.
 * 쿠폰 배율·직업 레벨·게임 모드·밸런스 버전이 다르면 다른 행으로 나누어, 분석에서 쿠폰 구간과 관리자 모드,
 * 수치 변경 전후를 걸러낼 수 있게 한다.
 */
final class ActionMetrics
{
    static final long BUCKET_MILLIS = 5_000L;
    static final String HEADER = "bucket_start_ms,bucket_ms,player_uuid,player_name,job_id,action_id,trigger,job_level,game_mode,"
            + "exp_coupon_multiplier,btc_coupon_multiplier,balance_version,count,exp_base,exp_coupon_bonus,exp_skill_bonus,exp_total,btc";

    /** 직업 액션 밖에서 기록된 보상. 정상 경로에서는 나오지 않으며 나오면 누락 경로를 찾는 단서가 된다. */
    private static final String UNKNOWN = "";

    private final Map<Key, Bucket> buckets = new LinkedHashMap<>();
    private final ThreadLocal<ArrayDeque<Frame>> frames = ThreadLocal.withInitial(ArrayDeque::new);

    void beginAction(IAction action, ActionData actionData)
    {
        if (!(actionData.getSourceActionHolder() instanceof JobInstance jobInstance))
        {
            return;
        }
        if (!(actionData.getPlayer() instanceof JobsServerPlayer jobsPlayer))
        {
            return;
        }
        ServerPlayer player = jobsPlayer.jobsplus$getServerPlayer();
        Job job = jobsPlayer.jobsplus$getJob(jobInstance);
        if (player == null || job == null)
        {
            return;
        }

        Key key = createKey(System.currentTimeMillis(), player, jobInstance.getLocation().toString(), job.getLevel(),
                action.getLocation().toString(), action.getType().getLocation().toString());
        frames.get().push(new Frame(action, actionData, key));
        bucket(key, player).count++;
    }

    void endAction(IAction action, ActionData actionData)
    {
        ArrayDeque<Frame> stack = frames.get();
        Frame top = stack.peek();
        if (top != null && top.action() == action && top.actionData() == actionData)
        {
            stack.pop();
        }
    }

    void addExperience(ServerPlayer player, Job job, double baseExperience, double couponBonus, double skillBonus)
    {
        Key key = currentKey(player, job.getJobInstance().getLocation().toString(), job.getLevel());
        Bucket bucket = bucket(key, player);
        bucket.experienceBase += baseExperience;
        bucket.experienceCouponBonus += couponBonus;
        bucket.experienceSkillBonus += skillBonus;
    }

    void addBitcoin(ServerPlayer player, JobInstance jobInstance, int jobLevel, int amount)
    {
        Key key = currentKey(player, jobInstance.getLocation().toString(), jobLevel);
        bucket(key, player).bitcoin += amount;
    }

    /** 닫힌 버킷을 CSV 행으로 꺼낸다. includeCurrent가 false면 아직 채워지는 현재 버킷은 남긴다. */
    List<String> drain(boolean includeCurrent, long now)
    {
        long currentBucketStart = bucketStart(now);
        List<Map.Entry<Key, Bucket>> ready = new ArrayList<>();
        Iterator<Map.Entry<Key, Bucket>> iterator = buckets.entrySet().iterator();
        while (iterator.hasNext())
        {
            Map.Entry<Key, Bucket> entry = iterator.next();
            if (includeCurrent || entry.getKey().bucketStart() < currentBucketStart)
            {
                ready.add(entry);
                iterator.remove();
            }
        }
        ready.sort(Comparator.comparingLong(entry -> entry.getKey().bucketStart()));

        List<String> lines = new ArrayList<>(ready.size());
        for (Map.Entry<Key, Bucket> entry : ready)
        {
            lines.add(toCsvLine(entry.getKey(), entry.getValue()));
        }
        return lines;
    }

    void clear()
    {
        buckets.clear();
        frames.remove();
    }

    private Key currentKey(ServerPlayer player, String jobId, int jobLevel)
    {
        UUID uuid = player.getUUID();
        // push는 앞쪽에 쌓으므로 순회 순서가 가장 안쪽(최근) 프레임부터다.
        for (Frame frame : frames.get())
        {
            if (frame.key().playerUuid().equals(uuid) && frame.key().jobId().equals(jobId))
            {
                return frame.key();
            }
        }
        return createKey(System.currentTimeMillis(), player, jobId, jobLevel, UNKNOWN, UNKNOWN);
    }

    private Key createKey(long now, ServerPlayer player, String jobId, int jobLevel, String actionId, String trigger)
    {
        UUID uuid = player.getUUID();
        int experienceCouponMultiplier = 1;
        int bitcoinCouponMultiplier = 1;
        MinecraftServer server = player.level().getServer();
        if (server != null)
        {
            RewardCouponLedger ledger = RewardCouponLedger.get(server);
            experienceCouponMultiplier = ledger.getExperienceMultiplier(uuid);
            bitcoinCouponMultiplier = ledger.getBitcoinChanceMultiplier(uuid);
        }
        return new Key(bucketStart(now), uuid, jobId, actionId, trigger, jobLevel, player.gameMode().getName(),
                experienceCouponMultiplier, bitcoinCouponMultiplier, JobsPlusMetrics.balanceVersion());
    }

    private Bucket bucket(Key key, ServerPlayer player)
    {
        return buckets.computeIfAbsent(key, ignored -> new Bucket(player.getName().getString()));
    }

    private static long bucketStart(long now)
    {
        return now - Math.floorMod(now, BUCKET_MILLIS);
    }

    private static String toCsvLine(Key key, Bucket bucket)
    {
        double experienceTotal = bucket.experienceBase + bucket.experienceCouponBonus + bucket.experienceSkillBonus;
        return key.bucketStart() + ","
                + BUCKET_MILLIS + ","
                + key.playerUuid() + ","
                + MetricsCsv.text(bucket.playerName) + ","
                + MetricsCsv.text(key.jobId()) + ","
                + MetricsCsv.text(key.actionId()) + ","
                + MetricsCsv.text(key.trigger()) + ","
                + key.jobLevel() + ","
                + MetricsCsv.text(key.gameMode()) + ","
                + key.experienceCouponMultiplier() + ","
                + key.bitcoinCouponMultiplier() + ","
                + MetricsCsv.text(key.balanceVersion()) + ","
                + bucket.count + ","
                + MetricsCsv.number(bucket.experienceBase) + ","
                + MetricsCsv.number(bucket.experienceCouponBonus) + ","
                + MetricsCsv.number(bucket.experienceSkillBonus) + ","
                + MetricsCsv.number(experienceTotal) + ","
                + bucket.bitcoin;
    }

    private record Key(long bucketStart, UUID playerUuid, String jobId, String actionId, String trigger, int jobLevel,
                       String gameMode, int experienceCouponMultiplier, int bitcoinCouponMultiplier, String balanceVersion)
    {
    }

    private record Frame(IAction action, ActionData actionData, Key key)
    {
    }

    private static final class Bucket
    {
        private final String playerName;
        private long count;
        private double experienceBase;
        private double experienceCouponBonus;
        private double experienceSkillBonus;
        private long bitcoin;

        private Bucket(String playerName)
        {
            this.playerName = playerName;
        }
    }
}
