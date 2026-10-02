package com.daqem.jobsplus.mixin.achievement;

import com.daqem.jobsplus.achievement.AchievementManager;
import com.daqem.jobsplus.achievement.AchievementStorage;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.raid.Raid;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Set;
import java.util.UUID;

@Mixin(Raid.class)
public abstract class MixinAchievementRaid
{
    @Shadow @Final private Set<UUID> heroesOfTheVillage;
    @Unique private boolean jobsplus$wasVictory;

    @Inject(method = "tick", at = @At("HEAD"))
    private void jobsplus$beforeRaidTick(ServerLevel level, CallbackInfo ci)
    {
        jobsplus$wasVictory = ((Raid) (Object) this).isVictory();
    }

    @Inject(method = "tick", at = @At("RETURN"))
    private void jobsplus$afterRaidTick(ServerLevel level, CallbackInfo ci)
    {
        Raid raid = (Raid) (Object) this;
        if (jobsplus$wasVictory || !raid.isVictory())
        {
            return;
        }
        var raidId = level.getRaids().getId(raid);
        if (raidId.isEmpty())
        {
            return;
        }
        String eventId = "raid:" + level.dimension().identifier() + ":" + raidId.getAsInt();
        for (UUID hero : heroesOfTheVillage)
        {
            ServerPlayer player = level.getServer().getPlayerList().getPlayer(hero);
            if (player != null && player.level() == level && player.isAlive() && AchievementManager.isEligible(player))
            {
                AchievementManager.recordOnce(level.getServer(), hero, eventId, "raids");
            }
        }
        AchievementStorage.save();
    }
}
