package fuzs.illagerinvasion.common.world.entity.ai.goal;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.RangedBowAttackGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.RangedAttackMob;

public class RangedBowAttackWithoutStrafingGoal<T extends Monster & RangedAttackMob> extends RangedBowAttackGoal<T> {
    /**
     * NeoForge가 이 클래스의 제네릭 타입을 바꾸므로 access widener를 쓰지 않는다.
     */
    private final T mob;
    private int attackIntervalBase;

    public RangedBowAttackWithoutStrafingGoal(T mob, double speedModifier, int attackIntervalMin, float attackRadius) {
        super(mob, speedModifier, attackIntervalMin, attackRadius);
        this.mob = mob;
        this.attackIntervalBase = attackIntervalMin;
    }

    @Override
    public void setMinAttackInterval(int attackCooldown) {
        super.setMinAttackInterval(attackCooldown);
        this.attackIntervalBase = attackCooldown;
    }

    @Override
    public void tick() {
        // 옆걸음 동작 끄기
        this.strafingTime = Integer.MIN_VALUE;
        // 대상이 가까울수록 몹이 더 빨리 쏘게 한다. 옆걸음이 생기면서 사라졌던 동작이다
        LivingEntity livingEntity = this.mob.getTarget();
        if (livingEntity != null) {
            double distanceToTargetSqr = this.mob.distanceToSqr(livingEntity);
            this.attackIntervalMin = this.attackIntervalBase -
                    (int) ((1.0 - Math.min(distanceToTargetSqr / this.attackRadiusSqr, 1.0)) * 20.0);
        }
        super.tick();
    }
}
