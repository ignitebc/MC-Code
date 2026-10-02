package com.daqem.jobsplus.mixin.achievement;

import com.daqem.jobsplus.achievement.AchievementManager;
import com.daqem.jobsplus.achievement.AchievementStorage;
import com.mojang.serialization.Codec;
import net.minecraft.core.UUIDUtil;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.enderdragon.phases.EnderDragonPhase;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** 실제 감소한 체력을 기록하고 사망 연출 완료 시 거리·생존 상태를 확인한다. */
@Mixin(EnderDragon.class)
public abstract class MixinAchievementDragon
{
    @Unique private static final Codec<Map<UUID, Double>> jobsplus$DAMAGE_CODEC = Codec.unboundedMap(
            UUIDUtil.STRING_CODEC, Codec.doubleRange(0, Double.MAX_VALUE));
    @Unique private final Map<UUID, Double> jobsplus$damage = new HashMap<>();
    @Unique private float jobsplus$healthBeforeHit;
    @Unique private boolean jobsplus$wasDying;
    @Unique private boolean jobsplus$dragonRecorded;
    @Unique private String jobsplus$damageSeason = "";

    @Inject(method = "reallyHurt", at = @At("HEAD"))
    private void jobsplus$beforeDragonHit(ServerLevel level, DamageSource source, float amount, CallbackInfo ci)
    {
        EnderDragon dragon = (EnderDragon) (Object) this;
        String season = AchievementStorage.season();
        if (!season.equals(jobsplus$damageSeason))
        {
            jobsplus$damage.clear();
            jobsplus$damageSeason = season;
        }
        jobsplus$healthBeforeHit = dragon.getHealth();
        jobsplus$wasDying = dragon.getPhaseManager().getCurrentPhase().getPhase() == EnderDragonPhase.DYING;
    }

    @Inject(method = "reallyHurt", at = @At("RETURN"))
    private void jobsplus$afterDragonHit(ServerLevel level, DamageSource source, float amount, CallbackInfo ci)
    {
        if (!(source.getEntity() instanceof ServerPlayer player) || !AchievementManager.isEligible(player))
        {
            return;
        }
        EnderDragon dragon = (EnderDragon) (Object) this;
        double remainingHealth = dragon.getHealth();
        if (!jobsplus$wasDying && dragon.getPhaseManager().getCurrentPhase().getPhase() == EnderDragonPhase.DYING)
        {
            // 바닐라는 최후의 일격 후 비행 연출을 위해 HP를 1로 되돌린다. 그 1도 유효 피해다.
            remainingHealth = 0;
        }
        double damage = Math.max(0, jobsplus$healthBeforeHit - remainingHealth);
        if (Double.isFinite(damage) && damage > 0)
        {
            jobsplus$damage.merge(player.getUUID(), damage, Double::sum);
        }
    }

    @Inject(method = "tickDeath", at = @At("RETURN"))
    private void jobsplus$completeDragon(CallbackInfo ci)
    {
        EnderDragon dragon = (EnderDragon) (Object) this;
        if (jobsplus$dragonRecorded || dragon.dragonDeathTime < 200 || !(dragon.level() instanceof ServerLevel level))
        {
            return;
        }
        jobsplus$dragonRecorded = true;
        if (!AchievementStorage.season().equals(jobsplus$damageSeason))
        {
            return;
        }
        double requiredDamage = dragon.getMaxHealth() * 0.05D;
        for (Map.Entry<UUID, Double> contribution : jobsplus$damage.entrySet())
        {
            ServerPlayer player = level.getServer().getPlayerList().getPlayer(contribution.getKey());
            if (contribution.getValue() >= requiredDamage && player != null && player.isAlive()
                    && player.level() == level && player.distanceToSqr(dragon) <= 128.0D * 128.0D
                    && AchievementManager.isEligible(player))
            {
                if (AchievementManager.recordOnce(level.getServer(), player.getUUID(), "dragon:" + dragon.getUUID(), "dragons"))
                {
                    AchievementManager.recordKill(player, dragon);
                }
            }
        }
        AchievementStorage.save();
    }

    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    private void jobsplus$saveDragonContributions(ValueOutput output, CallbackInfo ci)
    {
        output.store("JobsPlusDragonDamage", jobsplus$DAMAGE_CODEC, jobsplus$damage);
        output.putBoolean("JobsPlusDragonRecorded", jobsplus$dragonRecorded);
        output.putString("JobsPlusDragonDamageSeason", jobsplus$damageSeason);
    }

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    private void jobsplus$loadDragonContributions(ValueInput input, CallbackInfo ci)
    {
        jobsplus$damage.clear();
        jobsplus$damage.putAll(input.read("JobsPlusDragonDamage", jobsplus$DAMAGE_CODEC).orElse(Map.of()));
        jobsplus$dragonRecorded = input.getBooleanOr("JobsPlusDragonRecorded", false);
        jobsplus$damageSeason = input.getStringOr("JobsPlusDragonDamageSeason", "");
    }
}
