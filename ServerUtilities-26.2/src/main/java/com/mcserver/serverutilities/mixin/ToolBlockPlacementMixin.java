package com.mcserver.serverutilities.mixin;

import com.mcserver.serverutilities.level.ToolPlacedBlockData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockItem.class)
abstract class ToolBlockPlacementMixin {
    @Inject(method = "placeBlock", at = @At("RETURN"))
    private void serverutilities$recordPlacedBlock(BlockPlaceContext context, BlockState state,
                                                  CallbackInfoReturnable<Boolean> callback) {
        if (!callback.getReturnValue()) return;
        if (!(context.getLevel() instanceof ServerLevel level)) return;
        if (!ToolPlacedBlockData.isTrackedBlock(level.getBlockState(context.getClickedPos()))) return;
        ToolPlacedBlockData.get(level).add(context.getClickedPos());
    }
}
