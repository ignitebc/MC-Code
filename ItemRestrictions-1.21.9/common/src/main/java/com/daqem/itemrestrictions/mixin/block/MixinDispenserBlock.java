package com.daqem.itemrestrictions.mixin.block;

import com.daqem.itemrestrictions.chunk.ChunkProtection;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 디스펜서가 소유권 경계 너머에 유체나 폭발물 등을 직접 설치하는 것을 막는다.
 */
@Mixin(DispenserBlock.class)
public abstract class MixinDispenserBlock {

    @Inject(at = @At("HEAD"), method = "dispenseFrom", cancellable = true)
    private void itemrestrictions$blockCrossChunkDispense(ServerLevel level, BlockState blockState, BlockPos pos,
                                                           CallbackInfo callback) {
        Direction facing = blockState.getValue(DispenserBlock.FACING);
        BlockPos target = pos.relative(facing);
        if (ChunkProtection.crossesOwnershipBoundary(level, pos, target)) {
            callback.cancel();
        }
    }
}
