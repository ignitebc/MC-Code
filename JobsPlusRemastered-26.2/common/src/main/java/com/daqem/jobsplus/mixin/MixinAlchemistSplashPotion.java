package com.daqem.jobsplus.mixin;

import com.daqem.jobsplus.player.job.powerup.AlchemistPotionBoost;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownSplashPotion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

/**
 * 연금술사가 던진 투척 물약의 해로운 효과를 맞은 몹에게 '오래 끓인 약'·'농축 정제'만큼 키워 건다.
 * 플레이어에게 거는 효과는 하이퍼스킬 처리({@link MixinHyperSplashPotion})와 받는 쪽 스킬 데이터가 맡는다.
 */
@Mixin(ThrownSplashPotion.class)
public abstract class MixinAlchemistSplashPotion
{
    /** 물약 한 병의 강화 상태. 처음 몹에게 효과를 걸 때 정하고 같은 물약에 맞은 몹 모두에게 쓴다. */
    @Unique private AlchemistPotionBoost jobsplus$mobBoost;
    @Unique private boolean jobsplus$mobAmplify;

    @WrapOperation(method = "onHitAsPotion", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/LivingEntity;addEffect(Lnet/minecraft/world/effect/MobEffectInstance;Lnet/minecraft/world/entity/Entity;)Z"))
    private boolean jobsplus$boostEffectOnMob(LivingEntity target, MobEffectInstance effect, Entity source,
                                              Operation<Boolean> original)
    {
        if (target instanceof Player)
        {
            return original.call(target, effect, source);
        }
        if (jobsplus$mobBoost == null)
        {
            ThrownSplashPotion potion = (ThrownSplashPotion) (Object) this;
            jobsplus$mobBoost = AlchemistPotionBoost.of(potion.getOwner());
            jobsplus$mobAmplify = jobsplus$mobBoost.rollAmplifier(potion.getRandom());
        }
        return original.call(target, jobsplus$mobBoost.applyToMob(effect, target, jobsplus$mobAmplify), source);
    }
}
