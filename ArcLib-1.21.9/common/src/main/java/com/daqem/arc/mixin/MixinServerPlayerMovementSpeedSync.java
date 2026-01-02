package com.daqem.arc.mixin;

import com.daqem.arc.api.player.ArcPlayer;
import com.daqem.arc.player.MovementSpeedAttributeSync;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayer.class)
public class MixinServerPlayerMovementSpeedSync {

    @Inject(method = "tick()V", at = @At("TAIL"))
    private void arc$syncMovementSpeedModifier(CallbackInfo ci) {
        if ((Object) this instanceof ArcPlayer arcPlayer) {
            MovementSpeedAttributeSync.sync(arcPlayer);
        }
    }
}
