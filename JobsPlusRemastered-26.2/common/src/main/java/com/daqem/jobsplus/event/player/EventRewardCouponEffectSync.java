package com.daqem.jobsplus.event.player;

import com.daqem.jobsplus.effect.JobsPlusMobEffects;
import com.daqem.jobsplus.player.coupon.RewardCouponLedger;
import dev.architectury.event.events.common.TickEvent;
import net.minecraft.core.Holder;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;

/**
 * 저장된 쿠폰 만료 시각을 상태 효과 HUD와 동기화한다.
 * 상태 효과는 표시 전용이며 보상 배율의 근거로 사용하지 않는다.
 */
public final class EventRewardCouponEffectSync
{
    private static final long SYNC_INTERVAL_TICKS = 20L;
    private static final long MILLIS_PER_TICK = 50L;
    private static final int DURATION_DRIFT_TOLERANCE_TICKS = 40;

    private EventRewardCouponEffectSync()
    {
    }

    public static void registerEvent()
    {
        TickEvent.PLAYER_POST.register(player -> {
            if (!(player instanceof ServerPlayer serverPlayer))
            {
                return;
            }
            if (serverPlayer.level().getGameTime() % SYNC_INTERVAL_TICKS != 0L)
            {
                return;
            }
            sync(serverPlayer);
        });
    }

    public static void sync(ServerPlayer serverPlayer)
    {
        MinecraftServer server = serverPlayer.level().getServer();
        if (server == null)
        {
            return;
        }

        RewardCouponLedger ledger = RewardCouponLedger.get(server);
        long now = System.currentTimeMillis();

        syncEffect(
                serverPlayer,
                JobsPlusMobEffects.EXPERIENCE_DOUBLE,
                ledger.getExperienceDoubleRemainingMillis(serverPlayer.getUUID(), now)
        );
        syncEffect(
                serverPlayer,
                JobsPlusMobEffects.BITCOIN_DOUBLE,
                ledger.getBitcoinDoubleRemainingMillis(serverPlayer.getUUID(), now)
        );
        syncEffect(
                serverPlayer,
                JobsPlusMobEffects.BITCOIN_TRIPLE,
                ledger.getBitcoinTripleRemainingMillis(serverPlayer.getUUID(), now)
        );
    }

    private static void syncEffect(ServerPlayer player, Holder<MobEffect> effect, long remainingMillis)
    {
        MobEffectInstance current = player.getEffect(effect);
        if (remainingMillis <= 0L)
        {
            if (current != null)
            {
                player.removeEffect(effect);
            }
            return;
        }

        int remainingTicks = toTicks(remainingMillis);
        boolean needsRefresh = current == null
                || current.getAmplifier() != 0
                || Math.abs((long) current.getDuration() - remainingTicks) > DURATION_DRIFT_TOLERANCE_TICKS;
        if (!needsRefresh)
        {
            return;
        }

        if (current != null)
        {
            player.removeEffect(effect);
        }
        player.addEffect(new MobEffectInstance(effect, remainingTicks, 0, false, false, true));
    }

    private static int toTicks(long remainingMillis)
    {
        long ticks = (remainingMillis + MILLIS_PER_TICK - 1L) / MILLIS_PER_TICK;
        return (int) Math.max(1L, Math.min(ticks, Integer.MAX_VALUE));
    }
}
