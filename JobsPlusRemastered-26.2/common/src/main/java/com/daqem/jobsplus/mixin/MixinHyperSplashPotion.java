package com.daqem.jobsplus.mixin;

import com.daqem.arc.player.EffectAmplifierScope;
import com.daqem.jobsplus.player.job.hyper.HyperAlchemyHandler;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownSplashPotion;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.HitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ThrownSplashPotion.class)
public abstract class MixinHyperSplashPotion
{
    @Unique private boolean jobsplus$rolled;
    @Unique private boolean jobsplus$enhanced;
    @Unique private boolean jobsplus$notified;

    @WrapMethod(method = "onHitAsPotion")
    private void jobsplus$rollOnce(ServerLevel level, ItemStack item, HitResult hit, Operation<Void> original)
    {
        if (!jobsplus$rolled)
        {
            jobsplus$rolled = true;
            jobsplus$enhanced = HyperAlchemyHandler.roll((ThrownSplashPotion) (Object) this, item);
        }
        // 대상 범위·거리별 지속시간·몹의 효과는 원래 물약 처리를 유지한다.
        original.call(level, item, hit);
    }

    @WrapOperation(method = "onHitAsPotion", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/LivingEntity;addEffect(Lnet/minecraft/world/effect/MobEffectInstance;Lnet/minecraft/world/entity/Entity;)Z"))
    private boolean jobsplus$enhanceEffect(LivingEntity target, MobEffectInstance effect, Entity source,
                                           Operation<Boolean> original)
    {
        if (!jobsplus$enhanced || !(target instanceof ServerPlayer player)) return original.call(target, effect, source);
        ThrownSplashPotion potion = (ThrownSplashPotion) (Object) this;
        try (var scope = HyperAlchemyHandler.scope(player, potion.getOwner(), effect))
        {
            boolean applied = original.call(target, HyperAlchemyHandler.enhanced(player, effect), source);
            if (applied) jobsplus$notify();
            return applied;
        }
    }

    @WrapOperation(method = "onHitAsPotion", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/effect/MobEffect;applyInstantaneousEffect(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/entity/LivingEntity;ID)V"))
    private void jobsplus$enhanceInstant(MobEffect effect, ServerLevel level, Entity direct, Entity owner,
                                         LivingEntity target, int amplifier, double scale, Operation<Void> original)
    {
        if (!jobsplus$enhanced || !(target instanceof ServerPlayer player))
        {
            original.call(effect, level, direct, owner, target, amplifier, scale);
            return;
        }
        MobEffectInstance instance = new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(effect), 1, amplifier);
        try (var scope = HyperAlchemyHandler.scope(player, owner, instance))
        {
            int enhanced = EffectAmplifierScope.resolve(player, instance, 0);
            // 즉시 회복도 원본 호출을 대체하여 기본 회복과 강화 회복이 두 번 들어가지 않게 한다.
            original.call(effect, level, direct, owner, target, enhanced, scale);
            jobsplus$notify();
        }
    }

    @Unique
    private void jobsplus$notify()
    {
        if (!jobsplus$notified)
        {
            jobsplus$notified = true;
            HyperAlchemyHandler.notifyCaster((ThrownSplashPotion) (Object) this);
        }
    }
}
