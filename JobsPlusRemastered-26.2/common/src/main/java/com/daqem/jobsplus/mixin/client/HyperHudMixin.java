package com.daqem.jobsplus.mixin.client;

import com.daqem.jobsplus.client.hyper.ClientHyperSkills;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.Hud;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Hud.class)
public abstract class HyperHudMixin
{
    @Shadow private boolean isHidden;

    @Inject(method = "extractRenderState", at = @At("TAIL"))
    private void jobsplus$renderHyperHud(GuiGraphicsExtractor graphics, DeltaTracker delta, CallbackInfo ci)
    {
        if (!isHidden) ClientHyperSkills.render(graphics);
    }
}
