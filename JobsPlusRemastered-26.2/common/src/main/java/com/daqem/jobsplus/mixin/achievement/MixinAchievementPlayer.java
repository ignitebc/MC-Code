package com.daqem.jobsplus.mixin.achievement;

import com.daqem.jobsplus.achievement.AchievementManager;
import com.daqem.jobsplus.achievement.AchievementPlayer;
import com.mojang.serialization.Codec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stat;
import net.minecraft.stats.Stats;
import net.minecraft.world.inventory.EnchantmentMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

@Mixin(ServerPlayer.class)
public abstract class MixinAchievementPlayer implements AchievementPlayer
{
    @Unique private final Set<String> jobsplus$claimedAchievements = new HashSet<>();

    @Override
    public Set<String> jobsplus$getClaimedAchievements()
    {
        return jobsplus$claimedAchievements;
    }

    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    private void jobsplus$saveAchievementClaims(ValueOutput output, CallbackInfo ci)
    {
        output.store("JobsPlusAchievementClaims", Codec.STRING.listOf(), new ArrayList<>(jobsplus$claimedAchievements));
    }

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    private void jobsplus$loadAchievementClaims(ValueInput input, CallbackInfo ci)
    {
        jobsplus$claimedAchievements.clear();
        input.read("JobsPlusAchievementClaims", Codec.STRING.listOf()).ifPresent(jobsplus$claimedAchievements::addAll);
    }

    @Inject(method = "restoreFrom", at = @At("TAIL"))
    private void jobsplus$restoreAchievementClaims(ServerPlayer oldPlayer, boolean alive, CallbackInfo ci)
    {
        jobsplus$claimedAchievements.clear();
        if (oldPlayer instanceof AchievementPlayer old)
        {
            jobsplus$claimedAchievements.addAll(old.jobsplus$getClaimedAchievements());
        }
    }

    @Inject(method = "awardStat(Lnet/minecraft/stats/Stat;I)V", at = @At("TAIL"))
    private void jobsplus$countMovement(Stat<?> stat, int amount, CallbackInfo ci)
    {
        ServerPlayer player = (ServerPlayer) (Object) this;
        if (amount <= 0)
        {
            return;
        }
        // 바닐라 서버 통계는 걷기·달리기·비행을 따로 기록한다. Action의 누적 거리와 중복 합산하지 않는다.
        if (stat.equals(Stats.CUSTOM.get(Stats.WALK_ONE_CM)) || stat.equals(Stats.CUSTOM.get(Stats.SPRINT_ONE_CM)))
        {
            AchievementManager.add(player, "walk_cm", amount);
        }
        if (stat.equals(Stats.CUSTOM.get(Stats.AVIATE_ONE_CM)))
        {
            AchievementManager.add(player, "elytra_cm", amount);
        }
    }

    @Inject(method = "onEnchantmentPerformed", at = @At("TAIL"))
    private void jobsplus$countEnchantment(ItemStack stack, int levels, CallbackInfo ci)
    {
        ServerPlayer player = (ServerPlayer) (Object) this;
        if (player.containerMenu instanceof EnchantmentMenu && levels > 0)
        {
            AchievementManager.add(player, "enchants", 1);
        }
    }
}
