package com.daqem.jobsplus.mixin;

import com.daqem.jobsplus.player.job.powerup.FirepowerBoost;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** 사냥꾼 화력 증강의 장탄 수 보너스를 TACZ 최대 장탄 계산에 넣는다. TACZ가 없으면 적용되지 않는다. */
@Pseudo
@Mixin(targets = "com.tacz.guns.util.ShooterMagazineBonus", remap = false)
public abstract class MixinFirepowerBoostMagazine
{
    @Inject(method = "extraRounds", at = @At("HEAD"), cancellable = true, remap = false)
    private static void jobsplus$firepowerBoost(LivingEntity shooter, Identifier gunId, String gunType,
                                                CallbackInfoReturnable<Integer> cir)
    {
        int extraRounds = FirepowerBoost.extraRounds(shooter, gunId, gunType);
        if (extraRounds > 0)
        {
            cir.setReturnValue(extraRounds);
        }
    }
}
