package com.daqem.arc.mixin;

import com.daqem.arc.event.triggers.BlockBreakExpTracker;
import com.daqem.arc.event.triggers.BlockDropTracker;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Block.class)
public abstract class MixinBlock {

    @Inject(at = @At("HEAD"), method = "popExperience")
    public void popExperience(ServerLevel serverLevel, BlockPos blockPos, int amount, CallbackInfo ci) {
        BlockBreakExpTracker.record(blockPos, serverLevel.getGameTime(), amount);
    }

    @Inject(at = @At("HEAD"), method = "popResource(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/item/ItemStack;)V")
    private static void popResource(Level level, BlockPos blockPos, ItemStack itemStack, CallbackInfo ci) {
        if (!level.isClientSide()) {
            BlockDropTracker.record(blockPos, level.getGameTime(), itemStack);
        }
    }
}
