package com.mcserver.serverutilities.mixin;

import com.mcserver.serverutilities.monster.VexFlightRules;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 벡스의 바닐라 돌진 공격과 무작위 이동은 벽을 무시하고 일직선으로 날아간다.
 * 벽 통과 금지 규칙이 켜져 있으면 쉬게 하고, 경로를 따르는 Goal이 대신 움직인다. 규칙을 끄면 다시 바닐라대로 쓴다.
 */
@Mixin(targets = {
        "net.minecraft.world.entity.monster.Vex$VexChargeAttackGoal",
        "net.minecraft.world.entity.monster.Vex$VexRandomMoveGoal"
})
public abstract class VexVanillaMoveGoalMixin {
    @Inject(method = "canUse", at = @At("HEAD"), cancellable = true)
    private void serverutilities$yieldToPathGoals(CallbackInfoReturnable<Boolean> cir) {
        if (VexFlightRules.enabled()) cir.setReturnValue(false);
    }
}
