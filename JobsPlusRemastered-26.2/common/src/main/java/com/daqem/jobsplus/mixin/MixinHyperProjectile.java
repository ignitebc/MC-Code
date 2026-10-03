package com.daqem.jobsplus.mixin;

import com.daqem.jobsplus.player.job.hyper.HyperAlchemyHandler;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.Projectile;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Projectile.class)
public abstract class MixinHyperProjectile
{
    @Inject(method = "shootFromRotation", at = @At("HEAD"))
    private void jobsplus$rememberAlchemist(Entity owner, float pitch, float yaw, float offset,
                                            float velocity, float uncertainty, CallbackInfo ci)
    {
        HyperAlchemyHandler.markThrown((Projectile) (Object) this, owner);
    }
}
