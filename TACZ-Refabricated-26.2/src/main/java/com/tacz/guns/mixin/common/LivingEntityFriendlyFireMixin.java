package com.tacz.guns.mixin.common;

import com.tacz.guns.entity.ai.MonsterFriendlyFire;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 몬스터끼리의 오발을 피해 처리 맨 앞에서 무시한다.
 * <p>
 * 피해량만 0으로 바꾸면 바닐라가 여전히 쏜 몬스터를 마지막 공격자로 기록해 맞은 몬스터가 반격한다.
 * 피해 처리를 시작하기 전에 끝내야 반격 대상도, 피격 효과도 남지 않는다.
 */
@Mixin(LivingEntity.class)
public abstract class LivingEntityFriendlyFireMixin {
    @Inject(method = "hurtServer", at = @At("HEAD"), cancellable = true)
    private void tacz$ignoreMonsterFriendlyFire(ServerLevel level, DamageSource source, float amount,
                                                CallbackInfoReturnable<Boolean> cir) {
        if (MonsterFriendlyFire.isFriendlyFire((LivingEntity) (Object) this, source)) {
            cir.setReturnValue(false);
        }
    }
}
