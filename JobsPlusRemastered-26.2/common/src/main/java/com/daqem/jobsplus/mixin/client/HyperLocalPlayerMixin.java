package com.daqem.jobsplus.mixin.client;

import com.daqem.jobsplus.client.hyper.ClientHyperSkills;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LocalPlayer.class)
public abstract class HyperLocalPlayerMixin
{
    // KeyboardInput.tick 이후 자동 점프가 입력을 다시 켜도 비행·활공 전환으로 도약을 끊지 않는다.
    @ModifyExpressionValue(method = "aiStep", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/player/Input;jump()Z"))
    private boolean jobsplus$suppressLeapJump(boolean jumping)
    {
        return jumping && !ClientHyperSkills.shouldSuppressJump((LocalPlayer) (Object) this);
    }

    @Inject(method = "applyInput", at = @At("HEAD"))
    private void jobsplus$suppressLeapMovementInput(CallbackInfo ci)
    {
        ClientHyperSkills.suppressJumpInput((LocalPlayer) (Object) this);
    }
}
