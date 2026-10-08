package com.daqem.jobsplus.mixin;

import com.daqem.jobsplus.player.job.powerup.AlchemistPotionBoost;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.arrow.Arrow;
import net.minecraft.world.item.alchemy.PotionContents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.function.Consumer;

/**
 * 연금술사가 쏜 화살촉 화살의 해로운 효과를 맞은 몹에게 '오래 끓인 약'·'농축 정제'만큼 키워 건다.
 * <p>
 * 바닐라는 효과마다 람다 안에서 효과를 거는데, 람다 이름에 기대지 않도록 효과 목록을 넘기는 호출을 감싸 효과를 바꿔 넘긴다.
 */
@Mixin(Arrow.class)
public abstract class MixinAlchemistTippedArrow
{
    @WrapOperation(method = "doPostHurtEffects", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/item/alchemy/PotionContents;forEachEffect(Ljava/util/function/Consumer;F)V"))
    private void jobsplus$boostEffectsOnMob(PotionContents contents, Consumer<MobEffectInstance> applyEffect,
                                            float durationScale, Operation<Void> original,
                                            @Local(argsOnly = true) LivingEntity target)
    {
        if (target instanceof Player)
        {
            original.call(contents, applyEffect, durationScale);
            return;
        }
        Arrow arrow = (Arrow) (Object) this;
        AlchemistPotionBoost boost = AlchemistPotionBoost.of(arrow.getOwner());
        boolean amplify = boost.rollAmplifier(arrow.getRandom());
        Consumer<MobEffectInstance> boostedApply = effect -> applyEffect.accept(boost.applyToMob(effect, target, amplify));
        original.call(contents, boostedApply, durationScale);
    }
}
