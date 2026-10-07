package com.tacz.guns.mixin.common;

import com.tacz.guns.entity.ai.MonsterFriendlyFire;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 총을 든 몬스터끼리는 서로를 공격 대상으로 잡지 않는다.
 * <p>
 * 위더 스켈레톤이 피글린을 노리는 것처럼 바닐라에서 원래 적인 사이도, 둘 다 총을 들었으면 대상 지정을 무시한다.
 * 피글린 쪽은 Brain으로 대상을 고르므로 {@link PiglinSensorTruceMixin}이 따로 막는다.
 */
@Mixin(Mob.class)
public abstract class MobArmedTruceMixin {
    @Inject(method = "setTarget", at = @At("HEAD"), cancellable = true)
    private void tacz$keepArmedTruce(LivingEntity target, CallbackInfo ci) {
        if (MonsterFriendlyFire.isArmedTruce((Mob) (Object) this, target)) {
            ci.cancel();
        }
    }
}
