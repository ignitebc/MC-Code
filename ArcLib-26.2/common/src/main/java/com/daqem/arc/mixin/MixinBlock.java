package com.daqem.arc.mixin;

import com.daqem.arc.event.triggers.BlockBreakExpTracker;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
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
}
