package com.mcserver.serverutilities.mixin;

import com.mcserver.serverutilities.tier.EquipmentTierRules;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(AbstractArrow.class)
abstract class EquipmentTierArrowDamageMixin {
    @Shadow private double baseDamage;

    // 화살 피해는 올림까지 끝난 뒤에야 등급 차이가 그대로 남으므로 대상에게 넘기기 직전 값을 바꾼다.
    @ModifyArg(method = "onHitEntity",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/Entity;hurtOrSimulate(Lnet/minecraft/world/damagesource/DamageSource;F)Z"),
            index = 1)
    private float serverutilities$applyWeaponTier(float damage) {
        AbstractArrow arrow = (AbstractArrow) (Object) this;
        double speed = arrow.getDeltaMovement().length();
        return EquipmentTierRules.scaleArrowDamage(arrow.getWeaponItem(), speed, this.baseDamage, damage);
    }
}
