package com.tacz.guns.mixin.common;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.tacz.guns.entity.ai.CoverCombatant;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.ai.sensing.Sensing;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

/**
 * 엄폐 중인 몬스터가 공격 대상을 버리지 않게 한다.
 * <p>
 * 바닐라 대상 지정은 대상이 3초 동안 보이지 않으면 대상을 버린다. 숨어 있는 시간이 이보다 길어
 * 그대로 두면 숨는 순간 전투가 끝난다. 엄폐 행동이 붙잡은 동안에만 보이는 것으로 친다.
 */
@Mixin(TargetGoal.class)
public abstract class TargetGoalCoverMixin {
    @Shadow
    @Final
    protected Mob mob;

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
        return this.mob instanceof CoverCombatant combatant
                && combatant.tacz$isHoldingTarget(target, this.mob.level().getGameTime());
    }
}
