package com.daqem.jobsplus.mixin;

import com.daqem.jobsplus.player.job.hyper.FoodAbsorptionStack;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.consume_effects.ApplyStatusEffectsConsumeEffect;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ApplyStatusEffectsConsumeEffect.class)
public abstract class MixinFoodAbsorption
{
    // 황금사과류의 흡수 효과만 농부 하이퍼 중첩으로 넘긴다. 재생·저항·화염 저항은 바닐라가 그대로 넣는다.
    @WrapOperation(method = "apply", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/LivingEntity;addEffect(Lnet/minecraft/world/effect/MobEffectInstance;)Z"))
    private boolean jobsplus$stackFoodAbsorption(LivingEntity entity, MobEffectInstance effect,
                                                Operation<Boolean> original, @Local(argsOnly = true) ItemStack food)
    {
        if (FoodAbsorptionStack.addFromFood(entity, food, effect))
        {
            return true;
        }
        return original.call(entity, effect);
    }
}
