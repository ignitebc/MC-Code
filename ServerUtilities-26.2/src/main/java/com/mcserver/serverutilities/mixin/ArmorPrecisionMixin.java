package com.mcserver.serverutilities.mixin;

import com.mcserver.serverutilities.ServerUtilities;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.Attributes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(LivingEntity.class)
public abstract class ArmorPrecisionMixin {
    @Shadow
    public abstract double getAttributeValue(Holder<Attribute> attribute);

    // 바닐라는 방어도 속성값을 정수로 내려 피해 계산에 넘긴다.
    // 등급별 소수 방어도를 살리려면 속성값을 그대로 넘겨야 한다.
    // 방어 강도는 바닐라도 내리지 않으므로 손대지 않는다.
    @ModifyArg(
            method = "getDamageAfterArmorAbsorb",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/damagesource/CombatRules;getDamageAfterAbsorb"
                            + "(Lnet/minecraft/world/entity/LivingEntity;F"
                            + "Lnet/minecraft/world/damagesource/DamageSource;FF)F"),
            index = 3,
            require = 1,
            allow = 1)
    private float serverutilities$preciseArmor(float flooredArmor) {
        if (!ServerUtilities.config().armorCurve()) {
            return flooredArmor;
        }
        return (float) this.getAttributeValue(Attributes.ARMOR);
    }
}
