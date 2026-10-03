package com.daqem.jobsplus.mixin.client;

import com.daqem.jobsplus.client.hyper.ClientHyperSkills;
import net.minecraft.client.player.ClientInput;
import net.minecraft.client.player.KeyboardInput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyboardInput.class)
public abstract class HyperKeyboardInputMixin
{
    @Inject(method = "tick", at = @At("TAIL"))
    private void jobsplus$chargeLeap(CallbackInfo ci)
    {
        ClientHyperSkills.updateInput((ClientInput) (Object) this);
    }
}
