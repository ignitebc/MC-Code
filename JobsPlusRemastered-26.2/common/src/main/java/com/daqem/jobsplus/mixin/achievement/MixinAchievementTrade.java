package com.daqem.jobsplus.mixin.achievement;

import com.daqem.jobsplus.achievement.AchievementManager;
import net.minecraft.advancements.triggers.TradeTrigger;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.npc.villager.AbstractVillager;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** 주민과 떠돌이 상인은 거래를 한 번 마칠 때마다 이 발전 과제 판정을 부른다. */
@Mixin(TradeTrigger.class)
public abstract class MixinAchievementTrade
{
    @Inject(method = "trigger", at = @At("HEAD"))
    private void jobsplus$recordTrade(ServerPlayer player, AbstractVillager villager, ItemStack result, CallbackInfo ci)
    {
        AchievementManager.add(player, "villager_trades", 1);
    }
}
