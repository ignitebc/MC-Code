package com.tacz.guns.entity.ai;

import com.tacz.guns.api.item.IGun;
import com.tacz.guns.entity.EntityKineticBullet;
import com.tacz.guns.entity.shooter.MonsterGunController;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;

import javax.annotation.Nullable;

/**
 * 몬스터끼리 쏜 탄에 맞아 서로 싸우지 않게 하는 규칙.
 * <p>
 * 몬스터가 쏜 총탄·화살과 그 총탄의 폭발이, 쏜 몬스터가 노리지 않던 다른 몬스터에 맞으면 오발로 본다.
 * 오발은 피해를 주지 않고 맞은 몬스터가 쏜 몬스터를 공격 대상으로 삼지도 않는다. 탄은 맞은 자리에서 멈춘다.
 * 쏜 몬스터가 원래 노리던 몬스터(피글린과 위더 스켈레톤처럼 바닐라에서 서로 적인 경우)는 그대로 맞는다.
 */
public final class MonsterFriendlyFire {
    private MonsterFriendlyFire() {
    }

    /** 이 피해가 몬스터끼리의 오발인지 */
    public static boolean isFriendlyFire(LivingEntity victim, DamageSource source) {
        if (!MonsterGunController.isMonster(victim)) {
            return false;
        }
        if (!(source.getEntity() instanceof Mob shooter) || shooter == victim || !MonsterGunController.isMonster(shooter)) {
            return false;
        }
        Entity direct = source.getDirectEntity();
        boolean rangedAttack = direct instanceof EntityKineticBullet || direct instanceof AbstractArrow;
        if (!rangedAttack) {
            return false;
        }
        return currentTarget(shooter) != victim;
    }

    /** 총을 든 몬스터인지 */
    public static boolean isArmed(@Nullable Entity entity) {
        return entity instanceof Mob mob && MonsterGunController.isMonster(mob)
                && mob.getMainHandItem().getItem() instanceof IGun;
    }

    /** 둘 다 총을 든 몬스터라 서로 노리지 않는 사이인지 */
    public static boolean isArmedTruce(@Nullable Entity first, @Nullable Entity second) {
        return isArmed(first) && isArmed(second);
    }

    /**
     * 몬스터가 지금 노리는 대상. 일반 몬스터는 공격 대상 필드를, 피글린처럼 Brain을 쓰는 몬스터는 기억을 본다.
     * 기억을 등록하지 않은 몬스터에서 기억을 꺼내면 예외가 나므로 값이 있는지 먼저 확인한다.
     */
    @Nullable
    public static LivingEntity currentTarget(Mob mob) {
        LivingEntity target = mob.getTarget();
        if (target != null) {
            return target;
        }
        if (mob.getBrain().hasMemoryValue(MemoryModuleType.ATTACK_TARGET)) {
            return mob.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).orElse(null);
        }
        return null;
    }
}
