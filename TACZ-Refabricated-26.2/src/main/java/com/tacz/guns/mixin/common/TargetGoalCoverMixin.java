package com.tacz.guns.mixin.common;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.tacz.guns.entity.ai.CoverCombatant;
import com.tacz.guns.entity.ai.GunfireAlert;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.ai.sensing.Sensing;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

/**
 * 엄폐 중이거나 총소리 경보를 받은 몬스터가 공격 대상을 버리지 않게 한다.
 * <p>
 * 바닐라 대상 지정은 대상이 3초 동안 보이지 않거나 추적 범위 밖으로 나가면 대상을 버린다.
 * 숨어 있는 시간이 이보다 길고, 멀리서 쏜 플레이어는 추적 범위 밖에 있으므로 그대로 두면 바로 싸움이 끝난다.
 * 엄폐 행동이 붙잡았거나 경보가 유지되는 동안에만 보이는 것으로, 경보 중에는 추적 범위도 넓은 것으로 친다.
 */
@Mixin(TargetGoal.class)
public abstract class TargetGoalCoverMixin {
    @Shadow
    @Final
    protected Mob mob;

    @Shadow
    protected LivingEntity targetMob;

    @WrapOperation(
            method = "canContinueToUse",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/ai/sensing/Sensing;hasLineOfSight(Lnet/minecraft/world/entity/Entity;)Z"
            )
    )
    private boolean tacz$keepTargetInCover(Sensing sensing, Entity target, Operation<Boolean> original) {
        if (original.call(sensing, target)) {
            return true;
        }
        if (!(this.mob instanceof CoverCombatant combatant)) {
            return false;
        }
        long gameTime = this.mob.level().getGameTime();
        return combatant.tacz$isHoldingTarget(target, gameTime) || combatant.tacz$isAlertedTo(target, gameTime);
    }

    @WrapOperation(
            method = "canContinueToUse",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/ai/goal/target/TargetGoal;getFollowDistance()D"
            )
    )
    private double tacz$extendFollowDistanceWhenAlerted(TargetGoal goal, Operation<Double> original) {
        double followDistance = original.call(goal);
        LivingEntity target = this.mob.getTarget();
        if (target == null) {
            target = this.targetMob;
        }
        boolean alerted = target != null && this.mob instanceof CoverCombatant combatant
                && combatant.tacz$isAlertedTo(target, this.mob.level().getGameTime());
        if (!alerted) {
            return followDistance;
        }
        return Math.max(followDistance, GunfireAlert.ALERT_FOLLOW_DISTANCE);
    }
}
