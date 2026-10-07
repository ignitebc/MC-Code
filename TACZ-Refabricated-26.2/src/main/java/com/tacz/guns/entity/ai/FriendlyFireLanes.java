package com.tacz.guns.entity.ai;

import com.tacz.guns.entity.shooter.MonsterGunController;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.Comparator;

/**
 * 총을 든 몬스터의 사선(눈에서 대상까지의 선)을 다룬다.
 * <p>
 * 사선에 다른 몬스터가 있으면 쏘지 않고, 다른 몬스터의 사선 위에 섰으면 옆으로 비켜선다.
 * 오발은 피해를 주지 않지만({@link MonsterFriendlyFire}), 아군에 막힌 탄은 플레이어에게 닿지 못하고 탄약만 버린다.
 */
public final class FriendlyFireLanes {
    /** 사선의 반폭(칸). 몬스터 탄은 조금씩 퍼지므로 몸통 둘레에 여유를 둔다. */
    private static final double LANE_MARGIN = 0.5;
    /** 다른 몬스터의 사선을 살피는 반경(칸). 이보다 먼 몬스터의 사선은 따지지 않는다. */
    private static final double ALLY_SCAN_RADIUS = 32.0;

    private FriendlyFireLanes() {
    }

    /** 사선 한 줄 */
    public record Lane(Vec3 start, Vec3 end) {
        /** 이 상자가 사선에 걸리는지. 탄 퍼짐만큼 상자를 넓혀서 본다. */
        public boolean crosses(AABB box) {
            AABB widened = box.inflate(LANE_MARGIN);
            return widened.contains(this.start) || widened.clip(this.start, this.end).isPresent();
        }

        /** 사선 위에서 이 점과 가장 가까운 점 */
        public Vec3 closestPoint(Vec3 point) {
            Vec3 direction = this.end.subtract(this.start);
            double lengthSqr = direction.lengthSqr();
            if (lengthSqr < 1.0E-6) {
                return this.start;
            }
            double progress = Mth.clamp(point.subtract(this.start).dot(direction) / lengthSqr, 0.0, 1.0);
            return this.start.add(direction.scale(progress));
        }
    }

    /** 총을 든 몬스터가 지금 대상에게 쏘는 사선. 총이 없거나 대상이 없으면 null */
    @Nullable
    public static Lane laneOf(Mob shooter) {
        if (!MonsterFriendlyFire.isArmed(shooter)) {
            return null;
        }
        LivingEntity target = MonsterFriendlyFire.currentTarget(shooter);
        if (target == null || !target.isAlive()) {
            return null;
        }
        return new Lane(shooter.getEyePosition(), aimPoint(target));
    }

    /** 이 몬스터의 사선을 다른 몬스터가 막고 있는지 */
    public static boolean isAllyInLane(Mob shooter, LivingEntity target) {
        return findAllyInLane(shooter, target) != null;
    }

    /**
     * 사선 때문에 옆으로 비켜서야 하면 옆걸음 방향을 돌려준다.
     * 자기 사선을 다른 몬스터가 막았거나, 자기가 다른 몬스터의 사선 위에 섰을 때다.
     *
     * @return 이동 제어에 넘길 옆걸음 값의 부호(+1 또는 -1). 비켜설 필요가 없으면 0
     */
    public static int dodgeDirection(Mob mob, LivingEntity target) {
        Mob blocker = findAllyInLane(mob, target);
        if (blocker != null) {
            return sideAwayFrom(mob, blocker.position());
        }
        Lane crossedLane = findCrossedAllyLane(mob);
        if (crossedLane != null) {
            return sideAwayFrom(mob, crossedLane.closestPoint(mob.position()));
        }
        return 0;
    }

    /** 몬스터 총기가 겨누는 지점. {@link MonsterGunController}와 같은 몸통 가운데다. */
    public static Vec3 aimPoint(LivingEntity target) {
        return new Vec3(target.getX(), target.getY(0.5), target.getZ());
    }

    /** 쏜 몬스터에게 가장 가까운, 사선을 막은 몬스터 */
    @Nullable
    private static Mob findAllyInLane(Mob shooter, LivingEntity target) {
        Lane lane = new Lane(shooter.getEyePosition(), aimPoint(target));
        AABB area = new AABB(lane.start(), lane.end()).inflate(LANE_MARGIN + 1.0);
        return shooter.level().getEntitiesOfClass(Mob.class, area,
                        other -> isBystander(other, shooter, target) && lane.crosses(other.getBoundingBox()))
                .stream()
                .min(Comparator.comparingDouble(shooter::distanceToSqr))
                .orElse(null);
    }

    /** 이 몬스터가 서 있는 자리를 지나가는, 다른 총 든 몬스터의 사선 */
    @Nullable
    private static Lane findCrossedAllyLane(Mob mob) {
        AABB area = mob.getBoundingBox().inflate(ALLY_SCAN_RADIUS);
        for (Mob ally : mob.level().getEntitiesOfClass(Mob.class, area,
                other -> other != mob && other.isAlive() && MonsterFriendlyFire.isArmed(other))) {
            Lane lane = laneOf(ally);
            boolean aimingAtMe = MonsterFriendlyFire.currentTarget(ally) == mob;
            if (lane != null && !aimingAtMe && lane.crosses(mob.getBoundingBox())) {
                return lane;
            }
        }
        return null;
    }

    private static boolean isBystander(Mob other, Mob shooter, LivingEntity target) {
        return other != shooter && other != target && other.isAlive() && MonsterGunController.isMonster(other);
    }

    /**
     * 이 점에서 멀어지는 옆걸음 부호.
     * 옆걸음 값이 양수면 몸이 향한 방향의 왼쪽(바닐라 이동 입력 기준)으로 간다.
     */
    private static int sideAwayFrom(Mob mob, Vec3 point) {
        float yawRadians = mob.getYRot() * Mth.DEG_TO_RAD;
        double leftX = Mth.cos(yawRadians);
        double leftZ = Mth.sin(yawRadians);
        double side = (point.x - mob.getX()) * leftX + (point.z - mob.getZ()) * leftZ;
        if (side > 0) {
            return -1;
        }
        return 1;
    }
}
