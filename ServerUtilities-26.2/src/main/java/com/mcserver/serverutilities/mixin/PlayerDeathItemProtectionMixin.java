package com.mcserver.serverutilities.mixin;

import com.mcserver.serverutilities.death.DeathProtectedPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Player.class)
public abstract class PlayerDeathItemProtectionMixin {
    @Inject(method = "dropEquipment(Lnet/minecraft/server/level/ServerLevel;)V", at = @At("HEAD"), cancellable = true)
    private void serverutilities$keepInventory(ServerLevel level, CallbackInfo ci) {
        if ((Object) this instanceof DeathProtectedPlayer player && player.serverutilities$isDeathProtected()) {
            ci.cancel();
        }
    }
}
