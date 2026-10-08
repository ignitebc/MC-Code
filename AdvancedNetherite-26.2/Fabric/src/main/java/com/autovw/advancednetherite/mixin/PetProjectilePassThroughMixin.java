package com.autovw.advancednetherite.mixin;

import com.autovw.advancednetherite.common.pet.PetManager;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.Projectile;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 주인이 쏘거나 던진 투사체(화살·석궁 화살·폭죽, 삼지창, 포션, 눈덩이, 돌풍구 등)는 자기 펫에 부딪히지 않고 그대로 통과한다.
 * <p>
 * 펫은 주인의 공격에 피해를 받지 않는데, 부딪히면 화살이 튕겨 나가 펫 뒤에서 싸우는 몹을 맞힐 수 없다.
 */
@Mixin(Projectile.class)
public abstract class PetProjectilePassThroughMixin
{
    @Inject(method = "canHitEntity", at = @At("HEAD"), cancellable = true)
    private void advancednetherite$passThroughOwnersPet(Entity target, CallbackInfoReturnable<Boolean> cir)
    {
        Entity owner = ((Projectile) (Object) this).getOwner();
        if (PetManager.isPetOf(target, owner))
        {
            cir.setReturnValue(false);
        }
    }
}
