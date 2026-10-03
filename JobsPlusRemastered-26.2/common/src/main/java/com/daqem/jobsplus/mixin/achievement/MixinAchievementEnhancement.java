package com.daqem.jobsplus.mixin.achievement;

import com.daqem.jobsplus.achievement.AchievementManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = "fuzs.illagerinvasion.common.world.inventory.ImbuingMenu", remap = false)
public abstract class MixinAchievementEnhancement
{
    @Shadow @Final private Container input;
    @Unique private int jobsplus$previousEnhancement;

    @Inject(method = "enhanceEquipment", at = @At("HEAD"), remap = false)
    private void jobsplus$beforeEnhance(Player player, Level level, BlockPos pos, CallbackInfo ci)
    {
        jobsplus$previousEnhancement = jobsplus$enhancement(input.getItem(0));
    }

    @Inject(method = "enhanceEquipment", at = @At("RETURN"), remap = false)
    private void jobsplus$afterEnhance(Player player, Level level, BlockPos pos, CallbackInfo ci)
    {
        ItemStack equipment = input.getItem(0);
        int after = jobsplus$enhancement(equipment);
        // 강화 버튼은 원석이 준비된 상태에서만 이 메서드를 부르므로 호출 한 번이 원석을 쓴 시도 한 번이다.
        if (player instanceof ServerPlayer serverPlayer)
        {
            AchievementManager.add(serverPlayer, "enhance_attempts", 1);
        }
        if (player instanceof ServerPlayer serverPlayer && !equipment.isEmpty() && after > jobsplus$previousEnhancement)
        {
            AchievementManager.recordEnhancement(serverPlayer, equipment, after);
            input.setChanged();
        }
    }

    @Unique
    private static int jobsplus$enhancement(ItemStack stack)
    {
        return stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getIntOr("EnhancementLevel", 0);
    }
}
