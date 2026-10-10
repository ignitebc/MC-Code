package com.mcserver.serverutilities.monster;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.Vex;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

/**
 * 벽을 통과하지 못하는 벡스가 대상을 쫓는다. 바닐라 돌진 공격 대신 쓴다.
 * <p>
 * 대상이 보이고 사이가 블록에 막히지 않았으면 바닐라처럼 대상 눈 쪽으로 곧장 돌진한다. 보이지 않으면 비행 길찾기로
 * 열린 문·창문·구멍처럼 뚫린 길을 따라 다가가고, 시야가 트이면 다시 돌진한다. 갈 길이 없는 막힌 공간의 대상에게는
 * 다가가지 않고 소환자 곁을 떠돈다.
 */
public class VexPathChaseGoal extends Goal {
    /** 바닐라 돌진과 같은 속도 */
    private static final double CHARGE_SPEED = 1.0;
    private static final double PATH_SPEED = 1.0;
    /** 길이 없을 때 소환자 곁으로 돌아가는 속도와, 이보다 가까우면 그 자리에서 기다리는 거리(블록) */
    private static final double OWNER_RETURN_SPEED = 0.5;
    private static final double OWNER_HOVER_DISTANCE = 4.0;
    /** 경로를 다시 계산하는 간격(틱) */
    private static final int REPATH_INTERVAL_TICKS = 10;
    /** 한 번 때린 뒤 다시 때리기까지의 간격(틱). 몸이 붙어 있는 동안 매 틱 피해를 주지 않게 한다. */
    private static final int HIT_COOLDOWN_TICKS = 20;

    private final Vex vex;
    private int repathCooldown;
    private int hitCooldown;

    public VexPathChaseGoal(Vex vex) {
        this.vex = vex;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        return VexFlightRules.enabled() && isValidTarget(this.vex.getTarget());
    }

    @Override
    public boolean canContinueToUse() {
        return canUse();
    }

    @Override
    public void start() {
        this.repathCooldown = 0;
    }

    @Override
    public void stop() {
        this.vex.setIsCharging(false);
        this.vex.getNavigation().stop();
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        LivingEntity target = this.vex.getTarget();
        if (target == null) return;
        if (this.hitCooldown > 0) this.hitCooldown--;
        hitIfTouching(target);

        if (hasClearLine(target)) {
            this.vex.getNavigation().stop();
            Vec3 eye = target.getEyePosition();
            this.vex.getMoveControl().setWantedPosition(eye.x, eye.y, eye.z, CHARGE_SPEED);
            this.vex.setIsCharging(true);
            return;
        }
        this.vex.setIsCharging(false);
        if (--this.repathCooldown > 0) return;
        this.repathCooldown = REPATH_INTERVAL_TICKS;
        boolean pathFound = this.vex.getNavigation().moveTo(target, PATH_SPEED);
        if (!pathFound) hoverNearOwner();
    }

    private void hitIfTouching(LivingEntity target) {
        boolean touching = this.vex.getBoundingBox().intersects(target.getBoundingBox());
        if (!touching || this.hitCooldown > 0) return;
        if (!(this.vex.level() instanceof ServerLevel level)) return;
        this.vex.doHurtTarget(level, target);
        this.hitCooldown = HIT_COOLDOWN_TICKS;
    }

    /** 대상이 보이고, 벡스 몸 중심에서 대상 눈까지 블록이 없는지. 나뭇잎·유리처럼 충돌하는 블록은 막힌 것으로 본다. */
    private boolean hasClearLine(LivingEntity target) {
        if (!this.vex.getSensing().hasLineOfSight(target)) return false;
        Vec3 from = this.vex.getBoundingBox().getCenter();
        HitResult hit = this.vex.level().clip(new ClipContext(from, target.getEyePosition(),
                ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this.vex));
        return hit.getType() == HitResult.Type.MISS;
    }

    /** 대상에게 갈 길이 없으면 벽을 뚫는 대신 소환자 곁으로 돌아가 기다린다. 소환자가 없으면 그 자리에서 기다린다. */
    private void hoverNearOwner() {
        LivingEntity owner = this.vex.getOwner();
        boolean farFromOwner = owner != null && owner.isAlive()
                && this.vex.distanceToSqr(owner) > OWNER_HOVER_DISTANCE * OWNER_HOVER_DISTANCE;
        if (farFromOwner) {
            this.vex.getNavigation().moveTo(owner, OWNER_RETURN_SPEED);
            return;
        }
        this.vex.getNavigation().stop();
    }

    private static boolean isValidTarget(LivingEntity target) {
        return target != null && target.isAlive();
    }
}
