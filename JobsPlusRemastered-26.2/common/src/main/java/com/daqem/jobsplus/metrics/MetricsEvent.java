package com.daqem.jobsplus.metrics;

import net.minecraft.server.level.ServerPlayer;

import java.util.UUID;

/**
 * events.csv 한 행을 만드는 빌더.
 * 이벤트마다 쓰는 열이 다르므로 쓰지 않는 열은 빈 칸으로 남기고, 열에 맞지 않는 값은 detail에 "키=값;" 형식으로 적는다.
 */
public final class MetricsEvent
{
    static final String HEADER = "timestamp_ms,event,player_uuid,player_name,job_id,target_id,value,value_before,value_after,coins_before,coins_after,job_level,detail";

    private final long timestamp;
    private final String event;
    private String playerUuid = "";
    private String playerName = "";
    private String jobId = "";
    private String targetId = "";
    private String value = "";
    private String valueBefore = "";
    private String valueAfter = "";
    private String coinsBefore = "";
    private String coinsAfter = "";
    private String jobLevel = "";
    private final StringBuilder detail = new StringBuilder();

    private MetricsEvent(String event, long timestamp)
    {
        this.event = event;
        this.timestamp = timestamp;
    }

    public static MetricsEvent of(String event)
    {
        return new MetricsEvent(event, System.currentTimeMillis());
    }

    static MetricsEvent of(String event, long timestamp)
    {
        return new MetricsEvent(event, timestamp);
    }

    public MetricsEvent player(ServerPlayer player)
    {
        if (player == null)
        {
            return this;
        }
        return player(player.getUUID(), player.getName().getString());
    }

    public MetricsEvent player(UUID uuid, String name)
    {
        this.playerUuid = uuid == null ? "" : uuid.toString();
        this.playerName = name == null ? "" : name;
        return this;
    }

    public MetricsEvent job(Object jobId)
    {
        this.jobId = string(jobId);
        return this;
    }

    public MetricsEvent target(Object targetId)
    {
        this.targetId = string(targetId);
        return this;
    }

    public MetricsEvent value(Object value)
    {
        this.value = string(value);
        return this;
    }

    public MetricsEvent before(Object valueBefore)
    {
        this.valueBefore = string(valueBefore);
        return this;
    }

    public MetricsEvent after(Object valueAfter)
    {
        this.valueAfter = string(valueAfter);
        return this;
    }

    public MetricsEvent coins(int coinsBefore, int coinsAfter)
    {
        this.coinsBefore = Integer.toString(coinsBefore);
        this.coinsAfter = Integer.toString(coinsAfter);
        return this;
    }

    public MetricsEvent jobLevel(int jobLevel)
    {
        this.jobLevel = Integer.toString(jobLevel);
        return this;
    }

    public MetricsEvent detail(String key, Object detailValue)
    {
        if (!detail.isEmpty())
        {
            detail.append(';');
        }
        // 구분자가 값에 섞이면 분석 시 키=값 쌍이 깨지므로 치환한다.
        detail.append(key).append('=').append(string(detailValue).replace(';', ' ').replace('=', ' '));
        return this;
    }

    /** 메트릭 버퍼에 추가한다. 파일에는 다음 저장 주기에 기록된다. */
    public void record()
    {
        JobsPlusMetrics.recordEvent(this);
    }

    String toCsvLine()
    {
        return timestamp + ","
                + MetricsCsv.text(event) + ","
                + MetricsCsv.text(playerUuid) + ","
                + MetricsCsv.text(playerName) + ","
                + MetricsCsv.text(jobId) + ","
                + MetricsCsv.text(targetId) + ","
                + MetricsCsv.text(value) + ","
                + MetricsCsv.text(valueBefore) + ","
                + MetricsCsv.text(valueAfter) + ","
                + coinsBefore + ","
                + coinsAfter + ","
                + jobLevel + ","
                + MetricsCsv.text(detail.toString());
    }

    long timestamp()
    {
        return timestamp;
    }

    private static String string(Object value)
    {
        return value == null ? "" : value.toString();
    }
}
