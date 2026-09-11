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
 * 플레이어별 경험치·비트코인 획득 확률 쿠폰 만료 시각을 월드에 저장한다.
 * 효과 시간은 실제 시간 기준이며 서버 재시작/재접속 후에도 만료 시각을 유지한다.
 */
public final class RewardCouponLedger extends SavedData
{
    public static final long DURATION_MILLIS = 10L * 60L * 1000L;

    private static final Identifier FILE_ID = JobsPlus.getId("reward_coupons");

    public record CouponState(long experienceDoubleExpiresAt, long bitcoinDoubleExpiresAt, long bitcoinTripleExpiresAt,
                              long experienceTripleExpiresAt)
    {
        private static final CouponState EMPTY = new CouponState(0L, 0L, 0L, 0L);

        public static final Codec<CouponState> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.LONG.optionalFieldOf("experience_double_expires_at", 0L)
                        .forGetter(CouponState::experienceDoubleExpiresAt),
                Codec.LONG.optionalFieldOf("bitcoin_double_expires_at", 0L)
                        .forGetter(CouponState::bitcoinDoubleExpiresAt),
                Codec.LONG.optionalFieldOf("bitcoin_triple_expires_at", 0L)
                        .forGetter(CouponState::bitcoinTripleExpiresAt),
                // 기존 저장 데이터에는 이 필드가 없으므로 비활성 상태로 읽는다.
                Codec.LONG.optionalFieldOf("experience_triple_expires_at", 0L)
                        .forGetter(CouponState::experienceTripleExpiresAt)
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

    public int getExperienceMultiplier(UUID playerId)
    {
        long now = System.currentTimeMillis();
        if (getExperienceTripleRemainingMillis(playerId, now) > 0L) return 3;
        return getExperienceDoubleRemainingMillis(playerId, now) > 0L ? 2 : 1;
    }

    public boolean isBitcoinDoubleActive(UUID playerId)
    {
        return getBitcoinDoubleRemainingMillis(playerId, System.currentTimeMillis()) > 0L;
    }

    public int getBitcoinChanceMultiplier(UUID playerId)
    {
        long now = System.currentTimeMillis();
        if (getBitcoinTripleRemainingMillis(playerId, now) > 0L) return 3;
        return getBitcoinDoubleRemainingMillis(playerId, now) > 0L ? 2 : 1;
    }

    public long getExperienceDoubleRemainingMillis(UUID playerId, long now)
    {
        return getRemainingMillis(getState(playerId).experienceDoubleExpiresAt(), now);
    }

    public long getExperienceTripleRemainingMillis(UUID playerId, long now)
    {
        return getRemainingMillis(getState(playerId).experienceTripleExpiresAt(), now);
    }

    public long getBitcoinDoubleRemainingMillis(UUID playerId, long now)
    {
        return getRemainingMillis(getState(playerId).bitcoinDoubleExpiresAt(), now);
    }

    public long getBitcoinTripleRemainingMillis(UUID playerId, long now)
    {
        return getRemainingMillis(getState(playerId).bitcoinTripleExpiresAt(), now);
    }

    public long activateExperienceDouble(UUID playerId)
    {
        return activateExperience(playerId, 2);
    }

    public long activateExperienceTriple(UUID playerId)
    {
        return activateExperience(playerId, 3);
    }

    private long activateExperience(UUID playerId, int multiplier)
    {
        long now = System.currentTimeMillis();
        CouponState state = getState(playerId);
        long otherExpiresAt = multiplier == 2 ? state.experienceTripleExpiresAt() : state.experienceDoubleExpiresAt();
        if (otherExpiresAt > now)
        {
            // 배율 교체로 기존 시간을 승급하거나 2×3배로 중첩하는 것을 막는다.
            return 0L;
        }
        long currentExpiresAt = multiplier == 2 ? state.experienceDoubleExpiresAt() : state.experienceTripleExpiresAt();
        long expiresAt = extend(currentExpiresAt, now);
        this.states.put(playerId, new CouponState(
                multiplier == 2 ? expiresAt : state.experienceDoubleExpiresAt(),
                state.bitcoinDoubleExpiresAt(), state.bitcoinTripleExpiresAt(),
                multiplier == 3 ? expiresAt : state.experienceTripleExpiresAt()));
        this.setDirty();
        return expiresAt;
    }

    public long activateBitcoinDouble(UUID playerId)
    {
        return activateBitcoin(playerId, 2);
    }

    public long activateBitcoinTriple(UUID playerId)
    {
        return activateBitcoin(playerId, 3);
    }

    private long activateBitcoin(UUID playerId, int multiplier)
    {
        long now = System.currentTimeMillis();
        CouponState state = getState(playerId);
        long otherExpiresAt = multiplier == 2 ? state.bitcoinTripleExpiresAt() : state.bitcoinDoubleExpiresAt();
        if (otherExpiresAt > now)
        {
            // 저배율 쿠폰의 남은 시간을 고배율로 바꾸거나 2×3 중첩으로 악용하지 못하게 한다.
            return 0L;
        }
        long currentExpiresAt = multiplier == 2 ? state.bitcoinDoubleExpiresAt() : state.bitcoinTripleExpiresAt();
        long expiresAt = extend(currentExpiresAt, now);
        this.states.put(playerId, new CouponState(state.experienceDoubleExpiresAt(),
                multiplier == 2 ? expiresAt : state.bitcoinDoubleExpiresAt(),
                multiplier == 3 ? expiresAt : state.bitcoinTripleExpiresAt(),
                state.experienceTripleExpiresAt()));
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
