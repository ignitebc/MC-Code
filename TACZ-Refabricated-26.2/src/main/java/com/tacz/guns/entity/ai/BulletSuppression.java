package com.tacz.guns.entity.ai;

import com.tacz.guns.entity.shooter.MonsterGunController;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * 플레이어가 쏜 탄이 몬스터 가까이를 지나가면 그 몬스터를 제압한다.
 * <p>
 * 제압당한 몬스터는 숨어 있으면 몸을 내밀지 않고, 쏘더라도 조준이 흔들린다. 탄이 머리 위나 옆으로 스쳐 가는데도
 * 아무렇지 않게 정해진 박자대로 나와 쏘면 제압 사격이 의미가 없기 때문이다.
 * 아무 대상과도 싸우지 않던 몬스터는 처음 스친 탄에 쏜 쪽을 알아챈다({@link GunfireAlert#onNearMiss}).
 */
public final class BulletSuppression {
    /** 탄이 몸 중심에서 이 거리(칸) 안을 지나가면 스친 것으로 본다. */
    private static final double NEAR_MISS_RADIUS = 2.5;
    /** 한 번 스칠 때마다 제압이 이어지는 시간(2초). 계속 쏘면 계속 늘어난다. */
    private static final int SUPPRESS_TICKS = 40;

    private BulletSuppression() {
    }

    /**
     * 탄이 이번 틱에 지나간 구간을 알린다. 블록에 막혔으면 막힌 지점까지만 넘긴다.
     */
    public static void onBulletPath(Projectile bullet, Vec3 start, Vec3 end) {
        if (!(bullet.getOwner() instanceof Player shooter) || shooter.isSpectator()) {
            return;
        }
        if (!(bullet.level() instanceof ServerLevel level)) {
            return;
        }
        long gameTime = level.getGameTime();
        AABB area = new AABB(start, end).inflate(NEAR_MISS_RADIUS);
        for (Mob mob : level.getEntitiesOfClass(Mob.class, area, BulletSuppression::canBeSuppressed)) {
            Vec3 center = mob.getBoundingBox().getCenter();
            if (distanceToSegmentSqr(center, start, end) > NEAR_MISS_RADIUS * NEAR_MISS_RADIUS) {
                continue;
            }
            CoverCombatant combatant = (CoverCombatant) mob;
            boolean firstNearMiss = !combatant.tacz$isSuppressed(gameTime);
            combatant.tacz$suppress(gameTime + SUPPRESS_TICKS);
            if (firstNearMiss) {
                GunfireAlert.onNearMiss(mob, shooter);
            }
        }
    }

    private static boolean canBeSuppressed(Mob mob) {
        return mob.isAlive() && !mob.isNoAi() && MonsterGunController.isMonster(mob);
    }

    private static double distanceToSegmentSqr(Vec3 point, Vec3 start, Vec3 end) {
        Vec3 segment = end.subtract(start);
        double lengthSqr = segment.lengthSqr();
        if (lengthSqr < 1.0E-6) {
            return point.distanceToSqr(start);
        }
        double progress = point.subtract(start).dot(segment) / lengthSqr;
        progress = Math.max(0.0, Math.min(1.0, progress));
        return point.distanceToSqr(start.add(segment.scale(progress)));
    }
}
