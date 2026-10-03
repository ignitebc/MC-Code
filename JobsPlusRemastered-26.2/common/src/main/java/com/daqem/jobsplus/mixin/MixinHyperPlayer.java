package com.daqem.jobsplus.mixin;

import com.daqem.jobsplus.accessor.HyperPlayerAccess;
import com.daqem.jobsplus.player.job.hyper.HyperLeapMovement;
import com.daqem.jobsplus.player.job.hyper.HyperPlayerState;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Player.class)
public abstract class MixinHyperPlayer implements HyperPlayerAccess
{
    @Unique
    private static final EntityDataAccessor<Byte> JOBSPLUS_SHIELD =
            SynchedEntityData.defineId(Player.class, EntityDataSerializers.BYTE);
    @Unique
    private final HyperPlayerState jobsplus$hyperState = new HyperPlayerState();

    @Inject(method = "defineSynchedData", at = @At("TAIL"))
    private void jobsplus$defineShield(SynchedEntityData.Builder builder, CallbackInfo ci)
    {
        builder.define(JOBSPLUS_SHIELD, (byte) 0);
    }

    @Override
    public HyperPlayerState jobsplus$getHyperState()
    {
        return jobsplus$hyperState;
    }

    @Override
    public byte jobsplus$getShieldVisual()
    {
        return ((Player) (Object) this).getEntityData().get(JOBSPLUS_SHIELD);
    }

    @Override
    public void jobsplus$setShieldVisual(byte state)
    {
        ((Player) (Object) this).getEntityData().set(JOBSPLUS_SHIELD, state);
    }

    @ModifyVariable(method = "travel", at = @At("HEAD"), argsOnly = true)
    private Vec3 jobsplus$leapMovement(Vec3 input)
    {
        return HyperLeapMovement.beforeTravel((Player) (Object) this, input);
    }
}
