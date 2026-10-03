package com.daqem.jobsplus.mixin.client;

import com.daqem.jobsplus.accessor.HyperPlayerAccess;
import com.daqem.jobsplus.client.hyper.HyperShieldRenderer;
import com.daqem.jobsplus.client.hyper.HyperShieldRenderState;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderer.class)
public abstract class HyperEntityRendererMixin
{
    @Inject(method = "extractRenderState", at = @At("TAIL"))
    private void jobsplus$extractShield(Entity entity, EntityRenderState state, float partialTick, CallbackInfo ci)
    {
        byte visual = entity instanceof HyperPlayerAccess access ? access.jobsplus$getShieldVisual() : 0;
        ((HyperShieldRenderState) state).jobsplus$setShieldVisual(visual);
    }

    @Inject(method = "submit", at = @At("TAIL"))
    private void jobsplus$submitShield(EntityRenderState state, PoseStack poseStack, SubmitNodeCollector collector,
                                      CameraRenderState camera, CallbackInfo ci)
    {
        HyperShieldRenderer.submit(state, poseStack, collector);
    }
}
