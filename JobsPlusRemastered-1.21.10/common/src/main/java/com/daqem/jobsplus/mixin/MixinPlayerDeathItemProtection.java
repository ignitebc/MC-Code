package com.daqem.jobsplus.mixin;

import com.daqem.jobsplus.player.JobsServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Player.class)
public abstract class MixinPlayerDeathItemProtection
{

    @Inject(
            method = "dropEquipment(Lnet/minecraft/server/level/ServerLevel;)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private void jobsplus$keepProtectedInventory(ServerLevel level, CallbackInfo ci)
    {
        if ((Object) this instanceof JobsServerPlayer jobsServerPlayer
                && jobsServerPlayer.jobsplus$isDeathItemProtected())
        {
            ci.cancel();
        }
    }
}
