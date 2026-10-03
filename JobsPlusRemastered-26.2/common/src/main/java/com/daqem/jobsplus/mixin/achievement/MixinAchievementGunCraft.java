package com.daqem.jobsplus.mixin.achievement;

import com.daqem.jobsplus.achievement.AchievementManager;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;

/**
 * 총기 작업대가 재료를 차감하고 완성품을 떨어뜨리는 순간만 센다. 재료가 부족하면 이 지점까지 오지 않는다.
 * <p>
 * TACZ에 의존하지 않도록 제작 처리 람다의 완성품 생성을 감싼다. 완성품이 총기인지는 아이템 ID로 확인한다.
 */
@Pseudo
@Mixin(targets = "com.tacz.guns.inventory.GunSmithTableMenu", remap = false)
public abstract class MixinAchievementGunCraft
{
    @WrapOperation(method = "lambda$doCraft$0", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/Level;addFreshEntity(Lnet/minecraft/world/entity/Entity;)Z"), remap = false)
    private boolean jobsplus$recordGunCraft(Level level, Entity entity, Operation<Boolean> original,
                                           @Local(argsOnly = true) Player player)
    {
        boolean added = original.call(level, entity);
        if (added && player instanceof ServerPlayer serverPlayer && entity instanceof ItemEntity item
                && BuiltInRegistries.ITEM.getKey(item.getItem().getItem()).toString().equals("tacz:modern_kinetic_gun"))
        {
            AchievementManager.add(serverPlayer, "guns_crafted", 1);
        }
        return added;
    }
}
