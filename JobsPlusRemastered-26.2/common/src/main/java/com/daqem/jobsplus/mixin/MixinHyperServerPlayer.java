package com.daqem.jobsplus.mixin;

import com.daqem.jobsplus.player.job.hyper.HyperSkillHandler;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = ServerPlayer.class, priority = 1100)
public abstract class MixinHyperServerPlayer
{
    @Inject(method = "tick", at = @At("HEAD"))
    private void jobsplus$tickHyper(CallbackInfo ci)
    {
        HyperSkillHandler.tick((ServerPlayer) (Object) this);
    }

    @Inject(method = "hurtServer", at = @At("HEAD"), cancellable = true)
    private void jobsplus$blockHyperDamage(ServerLevel level, DamageSource source, float amount,
                                          CallbackInfoReturnable<Boolean> cir)
    {
        if (HyperSkillHandler.blocksDamage((ServerPlayer) (Object) this, source, amount)) cir.setReturnValue(false);
    }

    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    private void jobsplus$saveHyperRuntime(ValueOutput output, CallbackInfo ci)
    {
        HyperSkillHandler.state((ServerPlayer) (Object) this).save(output);
    }

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    private void jobsplus$loadHyperRuntime(ValueInput input, CallbackInfo ci)
    {
        HyperSkillHandler.state((ServerPlayer) (Object) this).load(input);
    }

    @Inject(method = "restoreFrom", at = @At("TAIL"))
    private void jobsplus$copyHyperCooldowns(ServerPlayer previous, boolean alive, CallbackInfo ci)
    {
        HyperSkillHandler.state((ServerPlayer) (Object) this).copyCooldowns(HyperSkillHandler.state(previous), alive);
    }

    public boolean arc$allowsMovementRewards()
    {
        ServerPlayer player = (ServerPlayer) (Object) this;
        var state = HyperSkillHandler.state(player);
        return !state.leapProtected && state.suppressMovementUntil < HyperSkillHandler.now(player);
    }
}
