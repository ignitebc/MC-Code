package com.mcserver.serverutilities.mixin;

import net.minecraft.world.entity.boss.wither.WitherBoss;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(WitherBoss.class)
public abstract class WitherSpawnHealthMixin {
    // 첫 heal 호출은 소환 중 충전이다. 전투 중 자연 회복량에는 배율을 적용하지 않는다.
    @ModifyArg(method = "customServerAiStep",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/boss/wither/WitherBoss;heal(F)V", ordinal = 0),
            index = 0, require = 1, allow = 1)
    private float serverutilities$scaleSpawnHealing(float amount) {
        WitherBoss wither = (WitherBoss) (Object) this;
        return amount * (wither.getMaxHealth() / 300.0F);
    }
}
