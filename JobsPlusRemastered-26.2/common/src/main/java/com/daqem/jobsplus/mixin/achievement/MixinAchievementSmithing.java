package com.daqem.jobsplus.mixin.achievement;

import com.daqem.jobsplus.achievement.AchievementManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.SmithingMenu;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** 대장장이 작업대 결과물을 실제로 꺼낸 순간만 센다. 결과 칸에 미리 보이는 것만으로는 인정하지 않는다. */
@Mixin(SmithingMenu.class)
public abstract class MixinAchievementSmithing
{
    @Inject(method = "onTake", at = @At("HEAD"))
    private void jobsplus$recordForge(Player player, ItemStack result, CallbackInfo ci)
    {
        if (player instanceof ServerPlayer serverPlayer)
        {
            AchievementManager.recordForge(serverPlayer, result);
        }
    }
}
