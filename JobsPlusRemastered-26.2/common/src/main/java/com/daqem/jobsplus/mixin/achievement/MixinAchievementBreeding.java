package com.daqem.jobsplus.mixin.achievement;

import com.daqem.jobsplus.achievement.AchievementManager;
import net.minecraft.advancements.triggers.BredAnimalsTrigger;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.animal.Animal;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** 바닐라가 새끼가 태어날 때 번식을 시킨 플레이어에게 한 번 부르는 발전 과제 판정을 그대로 쓴다. */
@Mixin(BredAnimalsTrigger.class)
public abstract class MixinAchievementBreeding
{
    @Inject(method = "trigger", at = @At("HEAD"))
    private void jobsplus$recordBreeding(ServerPlayer player, Animal parent, Animal partner, AgeableMob child,
                                        CallbackInfo ci)
    {
        AchievementManager.add(player, "animals_bred", 1);
    }
}
