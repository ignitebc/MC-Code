package com.mcserver.serverutilities.mixin;

import com.mcserver.serverutilities.ServerUtilities;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(Player.class)
public abstract class HungerExhaustionMixin {
    @ModifyVariable(method = "causeFoodExhaustion", at = @At("HEAD"), ordinal = 0, argsOnly = true)
    private float serverutilities$scaleExhaustion(float exhaustion) {
        var config = ServerUtilities.config();
        // 원격 접속자의 클라이언트 설치 여부와 무관하게 서버에서만 보정한다.
        if ((Object) this instanceof ServerPlayer && config.hunger()) {
            return exhaustion * config.hungerMultiplier();
        }
        return exhaustion;
    }
}
