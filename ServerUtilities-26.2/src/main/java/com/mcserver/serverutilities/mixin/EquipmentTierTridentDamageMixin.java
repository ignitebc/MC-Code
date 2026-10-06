package com.mcserver.serverutilities.mixin;

import com.mcserver.serverutilities.tier.EquipmentTierRules;
import net.minecraft.world.entity.projectile.arrow.ThrownTrident;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(ThrownTrident.class)
abstract class EquipmentTierTridentDamageMixin {
    // 던진 삼지창은 공격력 속성 대신 고정 피해 8을 쓴다. 찌르기 인챈트가 더해지기 전의 이 값에만 등급을 건다.
    @ModifyConstant(method = "onHitEntity", constant = @Constant(floatValue = 8.0F))
    private float serverutilities$applyTridentTier(float baseDamage) {
        ThrownTrident trident = (ThrownTrident) (Object) this;
        return EquipmentTierRules.scaleThrownTridentDamage(trident.getWeaponItem(), baseDamage);
    }
}
