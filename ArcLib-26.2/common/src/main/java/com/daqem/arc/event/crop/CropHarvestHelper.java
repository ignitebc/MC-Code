package com.daqem.arc.event.crop;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BambooStalkBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CocoaBlock;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.NetherWartBlock;
import net.minecraft.world.level.block.PitcherCropBlock;
import net.minecraft.world.level.block.SweetBerryBushBlock;
import net.minecraft.world.level.block.state.BlockState;

public final class CropHarvestHelper {

    private CropHarvestHelper() {
    }

    public static boolean isFullyGrown(BlockState blockState, Level level, BlockPos blockPos) {
        Block block = blockState.getBlock();
        if (block instanceof CropBlock cropBlock) {
            return cropBlock.isMaxAge(blockState);
        }
        if (block instanceof NetherWartBlock) {
            return blockState.getValue(NetherWartBlock.AGE) == NetherWartBlock.MAX_AGE;
        }
        if (block instanceof CocoaBlock) {
            return blockState.getValue(CocoaBlock.AGE) == CocoaBlock.MAX_AGE;
        }
        if (block instanceof PitcherCropBlock) {
            return blockState.getValue(PitcherCropBlock.AGE) == PitcherCropBlock.MAX_AGE;
        }
        if (block instanceof SweetBerryBushBlock) {
            return blockState.getValue(SweetBerryBushBlock.AGE) == SweetBerryBushBlock.MAX_AGE;
        }
        if (block instanceof BambooStalkBlock) {
            return hasMatchingStemBelow(level, blockPos, Blocks.BAMBOO);
        }
        if (blockState.is(Blocks.SUGAR_CANE)) {
            return hasMatchingStemBelow(level, blockPos, Blocks.SUGAR_CANE);
        }
        return blockState.is(Blocks.MELON)
                || blockState.is(Blocks.PUMPKIN)
                || blockState.is(Blocks.TORCHFLOWER);
    }

    private static boolean hasMatchingStemBelow(Level level, BlockPos blockPos, Block block) {
        return level != null
                && blockPos != null
                && level.getBlockState(blockPos.below()).is(block);
    }
}
