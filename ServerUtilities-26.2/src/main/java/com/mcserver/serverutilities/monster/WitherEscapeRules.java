package com.mcserver.serverutilities.monster;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * 오버월드의 위더가 우리에 갇혀 움직이지 못하면 가장 가까운 플레이어 뒤로 옮긴다.
 *
 * <p>위더를 가둬 두고 소환한 몹만 잡는 방식을 막는다. 플레이어를 공격 대상으로 잡은 동안 10초 넘게 2블록도
 * 움직이지 못했고, 그 사이 블록에 부딪혔거나 대상과 수평으로 8블록 넘게 떨어져 있으면 갇힌 것으로 본다.
 * 자유롭게 싸우는 위더는 대상 가까이 다가가 맴돌므로 이 조건에 걸리지 않는다.
 */
public final class WitherEscapeRules {
    private static final long STUCK_TICKS = 10 * 20;
    private static final double STUCK_MOVE_DISTANCE = 2.0;
    private static final double FAR_FROM_TARGET_DISTANCE = 8.0;
    /** 순간이동할 플레이어를 찾는 거리. 이 안에 플레이어가 없으면 옮기지 않는다. */
    private static final double NEAREST_PLAYER_RANGE = 64.0;
    /** 플레이어 뒤로 떨어뜨릴 거리 후보(블록). 앞의 값부터 빈자리를 찾는다. */
    private static final double[] BEHIND_DISTANCES = {3.0, 4.0, 2.0};
    /** 뒤쪽이 막혔을 때 띄워 볼 높이 후보(블록) */
    private static final double[] LIFT_HEIGHTS = {0.0, 1.0, 2.0};
    private static final Map<UUID, Watch> WATCHES = new HashMap<>();

    private WitherEscapeRules() { }

    public static void check(ServerLevel level, WitherBoss wither, Player target, long gameTime) {
        UUID witherId = wither.getUUID();
        Vec3 position = wither.position();
        Watch watch = WATCHES.get(witherId);
        boolean moved = watch == null || watch.anchor().distanceTo(position) > STUCK_MOVE_DISTANCE;
        if (moved) {
            WATCHES.put(witherId, new Watch(position, gameTime, false));
            return;
        }

        boolean blocked = watch.blocked() || wither.horizontalCollision || wither.verticalCollision;
        boolean stuckLongEnough = gameTime - watch.since() >= STUCK_TICKS;
        boolean farFromTarget = horizontalDistance(wither, target) > FAR_FROM_TARGET_DISTANCE;
        if (stuckLongEnough && (blocked || farFromTarget)) {
            teleportBehindNearestPlayer(level, wither);
            WATCHES.remove(witherId);
            return;
        }
        WATCHES.put(witherId, new Watch(watch.anchor(), watch.since(), blocked));
    }

    /** 플레이어를 노리지 않는 위더의 기록은 버린다. 다시 노리기 시작하면 처음부터 잰다. */
    public static void forgetExcept(Set<UUID> engagedWithers) {
        WATCHES.keySet().retainAll(engagedWithers);
    }

    public static void shutdown() {
        WATCHES.clear();
    }

    private static double horizontalDistance(Entity from, Entity to) {
        double dx = to.getX() - from.getX();
        double dz = to.getZ() - from.getZ();
        return Math.sqrt(dx * dx + dz * dz);
    }

    private static void teleportBehindNearestPlayer(ServerLevel level, WitherBoss wither) {
        Player player = nearestPlayer(level, wither);
        if (player == null) return;

        // 플레이어가 바라보는 방향의 반대쪽이 뒤다. 위아래를 보고 있어도 수평 방향만 쓴다.
        Vec3 behind = Vec3.directionFromRotation(0.0F, player.getYRot()).reverse();
        for (double distance : BEHIND_DISTANCES) {
            for (double lift : LIFT_HEIGHTS) {
                Vec3 destination = player.position().add(behind.scale(distance)).add(0.0, lift, 0.0);
                if (canFit(level, wither, destination)) {
                    wither.teleportTo(destination.x, destination.y, destination.z);
                    return;
                }
            }
        }
    }

    private static Player nearestPlayer(ServerLevel level, WitherBoss wither) {
        Player nearest = null;
        double nearestDistance = NEAREST_PLAYER_RANGE * NEAREST_PLAYER_RANGE;
        for (ServerPlayer player : level.players()) {
            boolean fightable = player.isAlive() && !player.isCreative() && !player.isSpectator();
            if (!fightable) continue;
            double distance = player.distanceToSqr(wither);
            if (distance <= nearestDistance) {
                nearest = player;
                nearestDistance = distance;
            }
        }
        return nearest;
    }

    /** 위더의 몸이 목적지에서 블록·물·용암에 걸리지 않는지 */
    private static boolean canFit(ServerLevel level, WitherBoss wither, Vec3 destination) {
        AABB body = wither.getBoundingBox().move(destination.subtract(wither.position()));
        return level.noCollision(wither, body) && !level.containsAnyLiquid(body);
    }

    private record Watch(Vec3 anchor, long since, boolean blocked) { }
}
