package com.daqem.jobsplus.mixin;

import com.daqem.jobsplus.player.job.hyper.HyperSkillHandler;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(LivingEntity.class)
public abstract class MixinHyperLifeSteal
{
    @WrapMethod(method = "hurtServer")
    private boolean jobsplus$healAfterDamage(ServerLevel level, DamageSource source, float damage,
                                            Operation<Boolean> original)
    {
        LivingEntity target = (LivingEntity) (Object) this;
        float healthBefore = target.getHealth();
        boolean result = original.call(level, source, damage);
        if (result) HyperSkillHandler.onDamageDealt(target, source, healthBefore);
        return result;
    }
}
