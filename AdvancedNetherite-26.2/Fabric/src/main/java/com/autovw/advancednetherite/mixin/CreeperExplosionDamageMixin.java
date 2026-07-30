package com.autovw.advancednetherite.mixin;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Creeper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * 크리퍼 폭발의 엔티티 피해만 증폭한다. 폭발 반경과 지형 파괴는 그대로 둔다.
 *
 * 어려움 난이도의 실효 배율을 기본 1.5배에서 2.0배로 올리기 위한 4/3 보정이다.
 * (밀착 최대 피해 기준 73.5 -> 98)
 */
@Mixin(LivingEntity.class)
public abstract class CreeperExplosionDamageMixin
{
    private static final float CREEPER_DAMAGE_MULTIPLIER = 4.0F / 3.0F;

    @ModifyVariable(method = "hurtServer", at = @At("HEAD"), ordinal = 0, argsOnly = true)
    private float advancednetherite$scaleCreeperExplosionDamage(float amount,
                                                                ServerLevel serverLevel,
                                                                DamageSource damageSource)
    {
        boolean isCreeperExplosion = damageSource.is(DamageTypeTags.IS_EXPLOSION)
                && damageSource.getEntity() instanceof Creeper;
        if (isCreeperExplosion)
        {
            return amount * CREEPER_DAMAGE_MULTIPLIER;
        }
        return amount;
    }
}
