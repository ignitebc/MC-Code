package com.autovw.advancednetherite.mixin;

import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * 행동(이동·점프·공격·채굴 등)으로 쌓이는 배고픔 피로도를 1.5배로 늘린다.
 *
 * 자연 회복이 소모하는 피로도는 FoodData 내부에서 직접 더해지고
 * 이 메서드를 거치지 않으므로 영향을 받지 않는다.
 */
@Mixin(Player.class)
public abstract class HungerExhaustionMixin
{
    private static final float EXHAUSTION_MULTIPLIER = 1.5F;

    @ModifyVariable(method = "causeFoodExhaustion", at = @At("HEAD"), ordinal = 0, argsOnly = true)
    private float advancednetherite$scaleExhaustion(float exhaustion)
    {
        return exhaustion * EXHAUSTION_MULTIPLIER;
    }
}
