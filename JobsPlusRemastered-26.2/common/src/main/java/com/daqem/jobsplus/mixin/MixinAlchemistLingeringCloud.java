package com.daqem.jobsplus.mixin;

import com.daqem.jobsplus.player.job.powerup.AlchemistPotionBoost;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

/**
 * 연금술사가 던진 잔류 물약 구름의 해로운 효과를 그 안의 몹에게 '오래 끓인 약'·'농축 정제'만큼 키워 건다.
 * <p>
 * 구름은 같은 몹에게 효과를 여러 번 다시 걸기 때문에 '농축 정제' 판정은 구름마다 한 번만 굴린다.
 * 매번 굴리면 구름 안에 오래 있을수록 강화될 확률이 계속 올라간다.
 */
@Mixin(AreaEffectCloud.class)
public abstract class MixinAlchemistLingeringCloud
{
    @Unique private AlchemistPotionBoost jobsplus$mobBoost;
    @Unique private boolean jobsplus$mobAmplify;

    @WrapOperation(method = "serverTick", at = @At(value = "INVOKE",
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
            AreaEffectCloud cloud = (AreaEffectCloud) (Object) this;
            jobsplus$mobBoost = AlchemistPotionBoost.of(cloud.getOwner());
            jobsplus$mobAmplify = jobsplus$mobBoost.rollAmplifier(cloud.getRandom());
        }
        return original.call(target, jobsplus$mobBoost.applyToMob(effect, target, jobsplus$mobAmplify), source);
    }
}
