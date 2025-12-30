package com.daqem.jobsplus.event.player;

import dev.architectury.event.events.common.TickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.raid.Raid;
import net.minecraft.world.phys.AABB;

import java.util.List;

/**
 * Raid(흉조 레이드) 또는 위더가 활성 상태일 때, 겉날개(활강) 사용(= isFallFlying) 시 플레이어를 즉사시키고 서버 전체에
 * 메시지를 브로드캐스트한다.
 *
 * 의도: 일부러 겉날개를 사용하지 못하도록 난이도 상승.
 */
public final class EventKillElytraDuringRaidOrWither
{

    // 위더 감지 범위(블록) 80블록
    private static final double WITHER_DETECT_RANGE = 80.0D;

    private static final Component KILL_MESSAGE = Component.literal("전투 중 비행은 금지되어 있습니다 ~");

    private EventKillElytraDuringRaidOrWither()
    {
    }

    public static void registerEvent()
    {
        TickEvent.PLAYER_POST.register(player ->
        {
            if (!(player instanceof ServerPlayer serverPlayer))
            {
                return;
            }

            // 겉날개 활강 상태일 때만 검사
            if (!serverPlayer.isFallFlying())
            {
                return;
            }

            if (!(serverPlayer.level() instanceof ServerLevel level))
            {
                return;
            }

            // 1) 레이드(흉조) 활성 여부: 플레이어 위치 기준
            if (isRaidActiveAtPlayer(level, serverPlayer))
            {
                killWithBroadcast(serverPlayer, level);
                return;
            }

            // 2) 위더 활성 여부: 플레이어 주변 150블록 내 살아있는 위더 존재
            if (isWitherActiveNearby(level, serverPlayer))
            {
                killWithBroadcast(serverPlayer, level);
            }
        });
    }

    private static boolean isRaidActiveAtPlayer(ServerLevel level, ServerPlayer player)
    {
        Raid raid = level.getRaidAt(player.blockPosition());
        return raid != null && raid.isActive() && !raid.isStopped();
    }

    private static boolean isWitherActiveNearby(ServerLevel level, ServerPlayer player)
    {
        AABB box = player.getBoundingBox().inflate(WITHER_DETECT_RANGE);
        List<WitherBoss> withers = level.getEntitiesOfClass(WitherBoss.class, box, WitherBoss::isAlive);
        return !withers.isEmpty();
    }

    private static void killWithBroadcast(ServerPlayer player, ServerLevel level)
    {
        // 서버 전체 브로드캐스트 (예시코드와 동일 패턴)
        if (player.level().getServer() != null)
        {
            player.level().getServer().getPlayerList().broadcastSystemMessage(KILL_MESSAGE, false);
        }

        // 즉사 처리 (1.21.10 매핑 기준)
        player.kill(level);
    }
}
