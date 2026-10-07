package com.tacz.guns.entity.ai;

import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;

/**
 * 보이지 않는 대상을 마지막으로 본 곳으로 가서 그 주변을 살핀다.
 * <p>
 * 벽 너머 대상의 지금 위치로 곧장 길을 찾으면 몬스터가 벽을 꿰뚫어 보는 것처럼 보인다.
 * 기억한 위치까지 가서도 보이지 않으면 그 둘레를 돌며 찾고, 다시 보이면 기억이 새로 바뀐다.
 */
final class ThreatSearch {
    /** 기억한 위치에 이 거리(칸) 안으로 오면 도착한 것으로 본다. */
    private static final double ARRIVE_DISTANCE_SQR = 2.5 * 2.5;
    /** 기억한 위치가 이만큼(칸) 바뀌면 새로 찾아간다. */
    private static final double MOVED_DISTANCE_SQR = 1.0;
    /** 기억한 위치로 가는 길을 다시 계산하는 간격(틱) */
    private static final int REPATH_INTERVAL_TICKS = 10;
    /** 도착한 뒤 둘레의 다른 곳으로 옮겨 가며 살피는 간격(틱)과 반경(칸) */
    private static final int MIN_WANDER_INTERVAL_TICKS = 40;
    private static final int MAX_WANDER_INTERVAL_TICKS = 60;
    private static final double MIN_WANDER_RADIUS = 2.0;
    private static final double MAX_WANDER_RADIUS = 6.0;
    /** 둘레를 살필 때는 조심스럽게 걷는다. */
    private static final double WANDER_SPEED_RATIO = 0.8;

    @Nullable
    private Vec3 destination;
    private boolean arrived;
    private int repathCooldown;
    private int wanderCooldown;

    void reset() {
        this.destination = null;
        this.arrived = false;
        this.repathCooldown = 0;
        this.wanderCooldown = 0;
    }

    void tick(PathfinderMob mob, Vec3 lastKnown, double speed) {
        boolean newDestination = this.destination == null || this.destination.distanceToSqr(lastKnown) > MOVED_DISTANCE_SQR;
        if (newDestination) {
            this.destination = lastKnown;
            this.arrived = false;
            this.repathCooldown = 0;
        }
        if (!this.arrived) {
            mob.getLookControl().setLookAt(lastKnown.x, lastKnown.y + 1.5, lastKnown.z);
            if (mob.position().distanceToSqr(lastKnown) <= ARRIVE_DISTANCE_SQR) {
                this.arrived = true;
                this.wanderCooldown = 0;
            } else {
                if (--this.repathCooldown <= 0) {
                    this.repathCooldown = REPATH_INTERVAL_TICKS;
                    mob.getNavigation().moveTo(lastKnown.x, lastKnown.y, lastKnown.z, speed);
                }
                return;
            }
        }
        if (--this.wanderCooldown > 0) {
            return;
        }
        RandomSource random = mob.getRandom();
        this.wanderCooldown = MIN_WANDER_INTERVAL_TICKS
                + random.nextInt(MAX_WANDER_INTERVAL_TICKS - MIN_WANDER_INTERVAL_TICKS + 1);
        double angle = random.nextDouble() * Math.PI * 2;
        double distance = MIN_WANDER_RADIUS + random.nextDouble() * (MAX_WANDER_RADIUS - MIN_WANDER_RADIUS);
        mob.getNavigation().moveTo(lastKnown.x + Math.cos(angle) * distance, lastKnown.y,
                lastKnown.z + Math.sin(angle) * distance, speed * WANDER_SPEED_RATIO);
    }
}
