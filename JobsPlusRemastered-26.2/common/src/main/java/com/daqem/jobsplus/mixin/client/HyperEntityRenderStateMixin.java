package com.daqem.jobsplus.mixin.client;

import com.daqem.jobsplus.client.hyper.HyperShieldRenderState;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(EntityRenderState.class)
public abstract class HyperEntityRenderStateMixin implements HyperShieldRenderState
{
    @Unique private byte jobsplus$shieldVisual;

    @Override
    public byte jobsplus$getShieldVisual()
    {
        return jobsplus$shieldVisual;
    }

    @Override
    public void jobsplus$setShieldVisual(byte visual)
    {
        jobsplus$shieldVisual = visual;
    }
}
