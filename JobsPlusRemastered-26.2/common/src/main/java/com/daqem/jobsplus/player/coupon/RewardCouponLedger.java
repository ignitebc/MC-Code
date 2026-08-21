package com.daqem.jobsplus.player.coupon;

import com.daqem.jobsplus.JobsPlus;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.UUIDUtil;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 플레이어별 직업 보상 2배 쿠폰 만료 시각을 월드에 저장한다.
 * 효과 시간은 실제 시간 기준이며 서버 재시작/재접속 후에도 만료 시각을 유지한다.
 */
public final class RewardCouponLedger extends SavedData
{
    public static final long DURATION_MILLIS = 10L * 60L * 1000L;

    private static final Identifier FILE_ID = JobsPlus.getId("reward_coupons");

    public record CouponState(long experienceDoubleExpiresAt, long bitcoinDoubleExpiresAt)
    {
        private static final CouponState EMPTY = new CouponState(0L, 0L);

        public static final Codec<CouponState> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.LONG.optionalFieldOf("experience_double_expires_at", 0L)
                        .forGetter(CouponState::experienceDoubleExpiresAt),
                Codec.LONG.optionalFieldOf("bitcoin_double_expires_at", 0L)
                        .forGetter(CouponState::bitcoinDoubleExpiresAt)
        ).apply(instance, CouponState::new));
    }

    public static final Codec<RewardCouponLedger> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.unboundedMap(UUIDUtil.STRING_CODEC, CouponState.CODEC)
                    .optionalFieldOf("players", Map.of())
                    .forGetter(RewardCouponLedger::getStates)
    ).apply(instance, RewardCouponLedger::new));

    public static final SavedDataType<RewardCouponLedger> TYPE = new SavedDataType<>(
            FILE_ID,
            RewardCouponLedger::new,
            CODEC,
            DataFixTypes.SAVED_DATA_COMMAND_STORAGE
    );

    private final Map<UUID, CouponState> states = new LinkedHashMap<>();

    public RewardCouponLedger()
    {
    }

    private RewardCouponLedger(Map<UUID, CouponState> states)
    {
        this.states.putAll(states);
    }

    public static RewardCouponLedger get(MinecraftServer server)
    {
        return server.overworld().getDataStorage().computeIfAbsent(TYPE);
    }

    public boolean isExperienceDoubleActive(UUID playerId)
    {
        return getExperienceDoubleRemainingMillis(playerId, System.currentTimeMillis()) > 0L;
    }

    public boolean isBitcoinDoubleActive(UUID playerId)
    {
        return getBitcoinDoubleRemainingMillis(playerId, System.currentTimeMillis()) > 0L;
    }

    public long getExperienceDoubleRemainingMillis(UUID playerId, long now)
    {
        return getRemainingMillis(getState(playerId).experienceDoubleExpiresAt(), now);
    }

    public long getBitcoinDoubleRemainingMillis(UUID playerId, long now)
    {
        return getRemainingMillis(getState(playerId).bitcoinDoubleExpiresAt(), now);
    }

    public long activateExperienceDouble(UUID playerId)
    {
        long now = System.currentTimeMillis();
        CouponState state = getState(playerId);
        long expiresAt = extend(state.experienceDoubleExpiresAt(), now);
        this.states.put(playerId, new CouponState(expiresAt, state.bitcoinDoubleExpiresAt()));
        this.setDirty();
        return expiresAt;
    }

    public long activateBitcoinDouble(UUID playerId)
    {
        long now = System.currentTimeMillis();
        CouponState state = getState(playerId);
        long expiresAt = extend(state.bitcoinDoubleExpiresAt(), now);
        this.states.put(playerId, new CouponState(state.experienceDoubleExpiresAt(), expiresAt));
        this.setDirty();
        return expiresAt;
    }

    private CouponState getState(UUID playerId)
    {
        return this.states.getOrDefault(playerId, CouponState.EMPTY);
    }

    private Map<UUID, CouponState> getStates()
    {
        return Map.copyOf(this.states);
    }

    private static long getRemainingMillis(long expiresAt, long now)
    {
        if (expiresAt <= now)
        {
            return 0L;
        }
        return expiresAt - now;
    }

    private static long extend(long currentExpiresAt, long now)
    {
        long base = Math.max(currentExpiresAt, now);
        if (base > Long.MAX_VALUE - DURATION_MILLIS)
        {
            return Long.MAX_VALUE;
        }
        return base + DURATION_MILLIS;
    }
}
