package com.mcserver.serverutilities.mixin;

import com.mcserver.serverutilities.ServerUtilities;
import com.mcserver.serverutilities.combat.ArmorRules;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.CombatRules;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(CombatRules.class)
public abstract class ArmorDamageCurveMixin {
    // 같은 메서드 안에 마법부여 보정을 0~1로 묶는 clamp가 하나 더 있으므로 첫 번째 호출만 바꾼다.
    // 대상이 없거나 둘 이상이면 Mixin 적용을 실패시켜 버전 변경으로 인한 오적용을 드러낸다.
    @Redirect(
            method = "getDamageAfterAbsorb",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Mth;clamp(FFF)F", ordinal = 0),
            require = 1,
            allow = 1)
    private static float serverutilities$uncapEffectiveArmor(float rawArmor, float minimumArmor, float vanillaLimit) {
        if (!ServerUtilities.config().armorCurve()) {
            return Mth.clamp(rawArmor, minimumArmor, vanillaLimit);
        }
        return ArmorRules.effectiveArmor(rawArmor, minimumArmor);
    }
}
