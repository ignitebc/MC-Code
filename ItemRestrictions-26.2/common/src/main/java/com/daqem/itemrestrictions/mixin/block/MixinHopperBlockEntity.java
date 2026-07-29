package com.daqem.itemrestrictions.mixin.block;

import com.daqem.itemrestrictions.chunk.ChunkProtection;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.HopperBlock;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 호퍼가 소유권 경계 너머의 보관함으로 아이템을 내보내는 것을 막는다.
 */
@Mixin(HopperBlockEntity.class)
public abstract class MixinHopperBlockEntity {

    @Inject(at = @At("HEAD"), method = "ejectItems", cancellable = true)
    private static void itemrestrictions$blockCrossChunkTransfer(Level level, BlockPos pos,
                                                                 HopperBlockEntity hopper,
                                                                 CallbackInfoReturnable<Boolean> callback) {
        BlockState blockState = level.getBlockState(pos);
        if (!blockState.hasProperty(HopperBlock.FACING)) {
            return;
        }

        Direction facing = blockState.getValue(HopperBlock.FACING);
        BlockPos destination = pos.relative(facing);
        if (ChunkProtection.crossesOwnershipBoundary(level, pos, destination)) {
            callback.setReturnValue(false);
        }
    }
}
