package com.mcserver.serverutilities.mixin;

import com.mcserver.serverutilities.level.ToolPlacedBlockData;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Level.class)
abstract class ToolBlockTrackingMixin {
    @Inject(method = "setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;II)Z",
            at = @At("RETURN"))
    private void serverutilities$updatePlacedBlockRecord(BlockPos pos, BlockState state, int flags, int depth,
                                                         CallbackInfoReturnable<Boolean> callback) {
        if (!callback.getReturnValue()) return;
        if (!((Object) this instanceof ServerLevel level)) return;
        if (state.is(Blocks.MOVING_PISTON)) {
            // 피스톤으로 이동한 블록은 자연 블록으로 인정하지 않는다. 설치 기록 우회를 막는다.
            ToolPlacedBlockData.get(level).add(pos);
        } else if (!ToolPlacedBlockData.isTrackedBlock(state) && level.getBlockState(pos) == state) {
            ToolPlacedBlockData.get(level).remove(pos);
        }
    }
}
