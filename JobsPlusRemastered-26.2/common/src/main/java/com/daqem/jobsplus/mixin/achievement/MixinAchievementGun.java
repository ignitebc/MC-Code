package com.daqem.jobsplus.mixin.achievement;

import com.daqem.jobsplus.achievement.AchievementManager;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.UUID;
import java.util.function.Consumer;

/** 실제 EXP가 변경되는 지점만 연결한다. 못 찾은 총기·가짜 탄약·최대 레벨 조기 반환은 집계되지 않는다. */
@Pseudo
@Mixin(targets = "com.tacz.guns.util.GunLevelManager", remap = false)
public abstract class MixinAchievementGun
{
    @Redirect(method = "addExperience", at = @At(value = "INVOKE",
            target = "Lcom/tacz/guns/util/ItemNbtUtils;updateTag(Lnet/minecraft/world/item/ItemStack;Ljava/util/function/Consumer;)V",
            remap = false), remap = false)
    private static void jobsplus$recordGunExperience(ItemStack gun, Consumer<CompoundTag> update,
                                                    ServerPlayer player, UUID instanceId, Identifier model)
    {
        int before = gun.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getIntOr("GunLevelExp", 0);
        CustomData.update(DataComponents.CUSTOM_DATA, gun, update);
        int after = gun.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getIntOr("GunLevelExp", 0);
        AchievementManager.recordEquipment(player, instanceId, "gun", model.toString(), before, after);
    }
}
