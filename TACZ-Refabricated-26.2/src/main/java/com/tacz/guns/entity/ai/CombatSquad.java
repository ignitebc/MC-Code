package com.tacz.guns.entity.ai;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.List;

/**
 * 같은 대상을 노리는 주변의 총·활 몬스터 무리.
 * <p>
 * 무리가 셋 이상이면 일부가 측면으로 돌아 들어가고, 무리 중 아무도 쏘지 않는 공백이 생기면 숨어 있던 몬스터가
 * 일찍 나와 쏜다. 모두가 정면에서 같은 박자로 숨고 나오면 플레이어가 쉬는 틈을 정확히 알 수 있기 때문이다.
 */
final class CombatSquad {
    /** 무리로 보는 반경(칸) */
    private static final double SQUAD_RADIUS = 24.0;

    private final List<Mob> mates;

    private CombatSquad(List<Mob> mates) {
        this.mates = mates;
    }

    /** 이 몬스터와 같은 대상을 노리는 주변 총·활 몬스터를 모은다. 자기 자신은 빠진다. */
    static CombatSquad scan(Mob self, LivingEntity threat) {
        AABB area = self.getBoundingBox().inflate(SQUAD_RADIUS);
        List<Mob> mates = self.level().getEntitiesOfClass(Mob.class, area,
                other -> other != self && other.isAlive() && isRangedFighter(other)
                        && MonsterFriendlyFire.currentTarget(other) == threat);
        return new CombatSquad(mates);
    }

    /** 무리에 필요한 측면 역할 수. 셋 이상이면 하나, 다섯 이상이면 둘이다. */
    static int neededFlankers(int squadSize) {
        if (squadSize >= 5) {
            return 2;
        }
        if (squadSize >= 3) {
            return 1;
        }
        return 0;
    }

    /** 자기 자신을 포함한 무리 수 */
    int size() {
        return this.mates.size() + 1;
    }

    int flankerCount() {
        int count = 0;
        for (Mob mate : this.mates) {
            if (mate instanceof CoverCombatant combatant && combatant.tacz$isFlanker()) {
                count++;
            }
        }
        return count;
    }

    /** 무리 중 누군가 지금 쏘고 있는지. 혼자면 false다. */
    boolean anyoneFiring(long gameTime) {
        for (Mob mate : this.mates) {
            if (mate instanceof CoverCombatant combatant && combatant.tacz$isFiring(gameTime)) {
                return true;
            }
        }
        return false;
    }

    boolean hasMates() {
        return !this.mates.isEmpty();
    }

    /**
     * 대상에서 측면 역할이 아닌 무리 쪽을 향하는 평균 방향(수평 단위 벡터). 무리의 정면이다.
     * 정면을 정할 무리가 없으면 null
     */
    @Nullable
    Vec3 frontDirection(Vec3 threatFeet) {
        double sumX = 0;
        double sumZ = 0;
        for (Mob mate : this.mates) {
            if (mate instanceof CoverCombatant combatant && combatant.tacz$isFlanker()) {
                continue;
            }
            Vec3 toMate = new Vec3(mate.getX() - threatFeet.x, 0, mate.getZ() - threatFeet.z);
            if (toMate.lengthSqr() < 1.0E-4) {
                continue;
            }
            Vec3 unit = toMate.normalize();
            sumX += unit.x;
            sumZ += unit.z;
        }
        Vec3 sum = new Vec3(sumX, 0, sumZ);
        if (sum.lengthSqr() < 1.0E-4) {
            return null;
        }
        return sum.normalize();
    }

    private static boolean isRangedFighter(Mob mob) {
        return MonsterFriendlyFire.isArmed(mob)
                || mob.getMainHandItem().getItem() instanceof ProjectileWeaponItem
                || mob.getOffhandItem().getItem() instanceof ProjectileWeaponItem;
    }
}
