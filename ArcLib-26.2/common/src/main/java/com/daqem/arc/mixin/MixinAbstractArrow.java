package com.daqem.arc.mixin;

import com.daqem.arc.api.IArcAbstractArrow;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.EntityHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractArrow.class)
public abstract class MixinAbstractArrow implements IArcAbstractArrow {

    @Unique
    private int arc$fireDurationTicks;

    @Unique
    private int arc$poisonDurationTicks;

    @Unique
    private int arc$poisonAmplifier;

    @Shadow protected abstract ItemStack getPickupItem();

    @Override
    public ItemStack arc$getPickupItem() {
        return getPickupItem();
    }

    @Override
    public int arc$getFireDurationTicks() {
        return arc$fireDurationTicks;
    }

    @Override
    public void arc$setFireDurationTicks(int fireDurationTicks) {
        this.arc$fireDurationTicks = fireDurationTicks;
    }

    @Override
    public int arc$getPoisonDurationTicks() {
        return arc$poisonDurationTicks;
    }

    @Override
    public void arc$setPoisonDurationTicks(int poisonDurationTicks) {
        this.arc$poisonDurationTicks = poisonDurationTicks;
    }

    @Override
    public int arc$getPoisonAmplifier() {
        return arc$poisonAmplifier;
    }

    @Override
    public void arc$setPoisonAmplifier(int poisonAmplifier) {
        this.arc$poisonAmplifier = poisonAmplifier;
    }

    @ModifyArg(
            method = "onHitEntity",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/Entity;igniteForSeconds(F)V"),
            index = 0)
    private float arc$modifyFireDuration(float fireDurationSeconds) {
        return arc$fireDurationTicks > 0 ? arc$fireDurationTicks / 20F : fireDurationSeconds;
    }

    /**
     * 화살이 피해를 정상적으로 입힌 뒤에만 독을 건다.
     * 무적 시간이나 피해 무효로 중간에 반환되는 경로에서는 발동하지 않는다.
     */
    @Inject(method = "onHitEntity", at = @At("TAIL"))
    private void arc$applyPoison(EntityHitResult entityHitResult, CallbackInfo callbackInfo) {
        if (arc$poisonDurationTicks <= 0) {
            return;
        }
        if (entityHitResult.getEntity() instanceof LivingEntity target) {
            target.addEffect(new MobEffectInstance(MobEffects.POISON, arc$poisonDurationTicks, arc$poisonAmplifier));
        }
    }
}
