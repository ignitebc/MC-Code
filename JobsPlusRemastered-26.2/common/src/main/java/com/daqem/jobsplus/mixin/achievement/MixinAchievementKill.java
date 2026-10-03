package com.daqem.jobsplus.mixin.achievement;

import com.autovw.advancednetherite.common.entity.DialgaPetEntity;
import com.daqem.arc.api.player.ArcServerPlayer;
import com.daqem.arc.event.triggers.KillCreditTracker;
import com.daqem.jobsplus.achievement.AchievementManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class MixinAchievementKill
{
    @Shadow protected boolean dead;
    @Unique private boolean jobsplus$killRecorded;

    @Inject(method = "die", at = @At("RETURN"))
    private void jobsplus$recordConfirmedKill(DamageSource source, CallbackInfo ci)
    {
        LivingEntity victim = (LivingEntity) (Object) this;
        if (!dead || jobsplus$killRecorded || victim instanceof EnderDragon || victim.level().isClientSide())
        {
            return;
        }
        jobsplus$killRecorded = true;
        ArcServerPlayer killer = KillCreditTracker.findKiller(victim, source);
        if (killer != null)
        {
            AchievementManager.recordKill(killer.arc$getServerPlayer(), victim, source);
            return;
        }
        // 펫이 처치하면 플레이어 처치 공로는 없다. 펫 처치 업적만 주인에게 기록한다.
        if (source.getEntity() instanceof DialgaPetEntity pet && pet.getOwner() instanceof ServerPlayer owner)
        {
            AchievementManager.recordPetKill(owner, victim);
        }
    }

    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    private void jobsplus$saveKillRecorded(ValueOutput output, CallbackInfo ci)
    {
        if (jobsplus$killRecorded)
        {
            output.putBoolean("JobsPlusKillRecorded", true);
        }
    }

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    private void jobsplus$loadKillRecorded(ValueInput input, CallbackInfo ci)
    {
        jobsplus$killRecorded = input.getBooleanOr("JobsPlusKillRecorded", false);
        if (((LivingEntity) (Object) this).getHealth() > 0)
        {
            jobsplus$killRecorded = false;
        }
    }
}
