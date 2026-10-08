package com.autovw.advancednetherite.mixin;

import com.autovw.advancednetherite.common.pet.PetManager;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.component.PiercingWeapon;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 창의 찌르기와 돌진 공격은 앞에 있는 대상을 모두 맞히는데, 주인의 펫은 대상에서 빼 그대로 통과한다.
 * 찌르기(PiercingWeapon)와 돌진(KineticWeapon) 모두 이 판정으로 대상을 고른다.
 */
@Mixin(PiercingWeapon.class)
public abstract class PetPiercingWeaponPassThroughMixin
{
    @Inject(method = "canHitEntity", at = @At("HEAD"), cancellable = true)
    private static void advancednetherite$passThroughOwnersPet(Entity attacker, Entity target,
                                                              CallbackInfoReturnable<Boolean> cir)
    {
        if (PetManager.isPetOf(target, attacker))
        {
            cir.setReturnValue(false);
        }
    }
}
