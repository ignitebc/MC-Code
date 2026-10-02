package com.mcserver.serverutilities.mixin;

import com.mcserver.serverutilities.level.ToolLevelRules;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** 실제 낚시 전리품이 생성된 회수 한 번에만 EXP를 준다. 입질 시간은 변경하지 않는다. */
@Mixin(FishingHook.class)
abstract class ToolFishingExperienceMixin {
    @Unique private boolean serverutilities$caughtLoot;

    @Inject(method = "retrieve", at = @At("HEAD"))
    private void serverutilities$beginFishingRetrieve(ItemStack rod, CallbackInfoReturnable<Integer> callback) {
        serverutilities$caughtLoot = false;
        FishingHook hook = (FishingHook) (Object) this;
        if (hook.getPlayerOwner() instanceof ServerPlayer) ToolLevelRules.ensureLevel(rod);
    }

    @Redirect(method = "retrieve", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/Level;addFreshEntity(Lnet/minecraft/world/entity/Entity;)Z"))
    private boolean serverutilities$recordFishingLoot(Level level, Entity entity) {
        boolean spawned = level.addFreshEntity(entity);
        if (spawned && entity instanceof ItemEntity item && !item.getItem().isEmpty()) {
            serverutilities$caughtLoot = true;
        }
        return spawned;
    }

    @Inject(method = "retrieve", at = @At("RETURN"))
    private void serverutilities$rewardFishingRetrieve(ItemStack rod, CallbackInfoReturnable<Integer> callback) {
        boolean caughtLoot = serverutilities$caughtLoot;
        serverutilities$caughtLoot = false;
        if (!caughtLoot || callback.getReturnValue() <= 0) return;
        FishingHook hook = (FishingHook) (Object) this;
        if (!(hook.getPlayerOwner() instanceof ServerPlayer player)) return;
        if (!ToolLevelRules.isFishingRod(rod)) return;
        ToolLevelRules.addExperience(player, rod);
    }
}
