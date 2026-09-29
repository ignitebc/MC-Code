package com.mcserver.serverutilities.mixin;

import com.mcserver.serverutilities.monster.CreeperExplosionRules;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(LivingEntity.class)
public abstract class CreeperExplosionDamageMixin {
    /** 크리퍼 폭발 피해에 크리퍼 레벨 배율을 곱한다. 바닐라가 같은 자리에서 주는 피해가 기준이다. */
    @ModifyVariable(method = "hurtServer", at = @At("HEAD"), ordinal = 0, argsOnly = true)
    private float serverutilities$scaleCreeperDamage(float amount, ServerLevel level, DamageSource source) {
        if (!source.is(DamageTypeTags.IS_EXPLOSION)) return amount;
        return amount * CreeperExplosionRules.multiplier(source.getEntity());
    }
}
