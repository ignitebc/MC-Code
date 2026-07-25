package com.daqem.jobsplus.event.player;

import dev.architectury.event.events.common.TickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.raid.Raid;
import net.minecraft.world.phys.AABB;

import java.util.List;

/**
 * Raid(흉조 레이드) 또는 위더가 활성 상태일 때, 1) 겉날개(활강) 사용(= isFallFlying) 시 플레이어를 즉사시키고 서버
 * 전체에 메시지를 브로드캐스트한다. 2) 철골렘이 전투에 개입하지 못하도록, 해당 상황에서는 철골렘도 즉시 제거한다.
 *
 * 의도: - 일부러 겉날개를 사용하지 못하도록 난이도 상승. - 철골렘이 몬스터를 잡지 못하게 해서 오직 플레이어가 처리하도록 강제.
 */
public final class EventKillElytraDuringRaidOrWither
{
    // 위더 감지 범위(블록)
    private static final double WITHER_DETECT_RANGE = 80.0D;

    // 레이드 상황에서 철골렘 제거 범위(플레이어 기준, 블록)
    private static final double RAID_GOLEM_KILL_RANGE = 80.0D;

    // 위더 상황에서 철골렘 제거 범위(플레이어 기준, 블록)
    private static final double WITHER_GOLEM_KILL_RANGE = 80.0D;

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

            if (!(serverPlayer.level() instanceof ServerLevel level))
            {
                return;
            }

            // 1) 레이드(흉조) 활성 여부: 플레이어 위치 기준
            if (isRaidActiveAtPlayer(level, serverPlayer))
            {
                // 레이드 중 철골렘 즉시 제거
                killIronGolemsNearby(level, serverPlayer, RAID_GOLEM_KILL_RANGE);

                // 레이드 중 활강 즉사
                if (serverPlayer.isFallFlying())
                {
                    killWithBroadcast(serverPlayer, level);
                }
                return;
            }

            // 2) 위더 활성 여부: 플레이어 주변 WITHER_DETECT_RANGE 내 살아있는 위더 존재
            if (isWitherActiveNearby(level, serverPlayer))
            {
                // 위더 근처 철골렘 즉시 제거
                killIronGolemsNearby(level, serverPlayer, WITHER_GOLEM_KILL_RANGE);

                // 위더 근처 활강 즉사
                if (serverPlayer.isFallFlying())
                {
                    killWithBroadcast(serverPlayer, level);
                }
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

    private static void killIronGolemsNearby(ServerLevel level, ServerPlayer player, double range)
    {
        AABB box = player.getBoundingBox().inflate(range);

        // 철골렘은 엔티티 수가 많지 않으므로 단순 스캔으로도 충분
        List<IronGolem> golems = level.getEntitiesOfClass(IronGolem.class, box, IronGolem::isAlive);
        if (golems.isEmpty())
        {
            return;
        }

        for (IronGolem golem : golems)
        {
            // 안전장치: 이미 제거 진행 중이면 스킵
            if (!golem.isAlive())
            {
                continue;
            }

            // 즉사 처리 (서버 레벨 필요)
            golem.kill(level);
        }
    }

    private static void killWithBroadcast(ServerPlayer player, ServerLevel level)
    {
        // 서버 전체 브로드캐스트
        if (player.level().getServer() != null)
        {
            player.level().getServer().getPlayerList().broadcastSystemMessage(KILL_MESSAGE, false);
        }

        // 즉사 처리 (1.21.10 매핑 기준)
        player.kill(level);
    }
}
