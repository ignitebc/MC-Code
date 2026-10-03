package com.tacz.guns.entity.ai;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.RangedBowAttackGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.Items;

import java.util.EnumSet;

/**
 * 활을 든 몬스터가 원거리 무기를 든 플레이어를 상대할 때, 엄폐물 뒤에서 활시위를 당겼다가 옆으로 나와 쏘고 다시 숨게 한다.
 * <p>
 * 바닐라 활 공격 Goal을 감싸서 엄폐할 상황이 아니면 바닐라 동작을 그대로 쓴다. 엄폐 Goal을 따로 두면
 * 이동 권한을 뺏긴 바닐라 활 공격이 멈추면서 당기던 활시위를 놓고, 시야가 없을 때는 대상에게 걸어 나오므로
 * 한 Goal 안에서 처리한다.
 */
public class CoverBowAttackGoal<T extends Monster & RangedAttackMob> extends Goal {
    /** 대상이 이 거리 안으로 붙으면 엄폐하지 않고 바닐라처럼 물러나며 쏜다. */
    private static final double CLOSE_DISTANCE = 4.0;
    /** 활의 사격 거리. 바닐라 스켈레톤 활 공격과 같다. */
    private static final double BOW_RANGE = 15.0;
    /** 활시위를 끝까지 당기는 시간(틱). 바닐라 활 공격과 같다. */
    private static final int FULL_DRAW_TICKS = 20;
    private static final double MOVE_SPEED = 1.0;

    private final T mob;
    private final RangedBowAttackGoal<T> vanillaGoal;
    private final CoverTactics tactics;
    private final RangedThreat threat = new RangedThreat();

    /** @param vanillaGoal 엄폐하지 않을 때 쓰는 바닐라 활 공격. 난이도별 공격 간격 설정을 그대로 따른다. */
    public CoverBowAttackGoal(T mob, RangedBowAttackGoal<T> vanillaGoal) {
        this.mob = mob;
        this.vanillaGoal = vanillaGoal;
        this.tactics = new CoverTactics(mob, MOVE_SPEED);
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        return this.vanillaGoal.canUse();
    }

    @Override
    public boolean canContinueToUse() {
        return this.vanillaGoal.canContinueToUse();
    }

    @Override
    public void start() {
        this.vanillaGoal.start();
        this.tactics.reset();
    }

    @Override
    public void stop() {
        this.vanillaGoal.stop();
        this.tactics.reset();
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        LivingEntity target = this.mob.getTarget();
        if (target != null && shouldCover(target)) {
            if ((this.tactics.hasSpot() || this.tactics.trySearch(target, BOW_RANGE))
                    && this.tactics.tick(target, isFullyDrawn(), coverShot(target))) {
                return;
            }
        } else if (this.tactics.hasSpot()) {
            // 엄폐할 상황이 끝났다. 엄폐 칸을 잃은 경우와 달리 다음 탐색을 미루지 않는다.
            this.tactics.reset();
        }
        // 엄폐 칸이 없으면 바닐라 활 공격이 이어받는다. 당겨 둔 활시위도 그대로 이어서 쓴다.
        this.vanillaGoal.tick();
    }

    /**
     * 엄폐 중 활을 다룬다. 숨으러 가거나 숨어 있는 동안 활시위를 미리 당겨 두고,
     * 노출 칸에서 대상이 보이면 바로 쏜다.
     *
     * @return 이번 틱에 화살을 쐈으면 true
     */
    private boolean coverShot(LivingEntity target) {
        if (!this.mob.isUsingItem()) {
            this.mob.startUsingItem(ProjectileUtil.getWeaponHoldingHand(this.mob, Items.BOW));
            return false;
        }
        if (this.tactics.phase() != CoverTactics.Phase.PEEK || !isFullyDrawn()
                || !this.mob.getSensing().hasLineOfSight(target)) {
            return false;
        }
        int drawTicks = this.mob.getTicksUsingItem();
        this.mob.stopUsingItem();
        this.mob.performRangedAttack(target, BowItem.getPowerForTime(drawTicks));
        return true;
    }

    private boolean isFullyDrawn() {
        return this.mob.isUsingItem() && this.mob.getTicksUsingItem() >= FULL_DRAW_TICKS;
    }

    private boolean shouldCover(LivingEntity target) {
        if (this.mob.isPassenger() || this.mob.isInWater()) {
            return false;
        }
        if (this.mob.distanceToSqr(target) <= CLOSE_DISTANCE * CLOSE_DISTANCE) {
            return false;
        }
        return this.threat.isThreat(target, this.mob.level().getGameTime());
    }
}
