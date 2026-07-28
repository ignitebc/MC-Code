package com.daqem.itemrestrictions.mixin.block;

import com.daqem.itemrestrictions.chunk.ChunkProtection;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.piston.PistonBaseBlock;
import net.minecraft.world.level.block.piston.PistonStructureResolver;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 소유 청크 경계를 넘나드는 피스톤 동작을 막는다.
 * <p>
 * 끈끈이 피스톤은 남의 청크에서 블록을 뽑아낼 수 있고, 밀기는 최대 12칸까지 연쇄되어 구조물을 밀어낼 수 있다.
 * 설치 반경으로 막으면 제약이 너무 커지므로 실제 동작 시점에 판정한다.
 */
@Mixin(PistonBaseBlock.class)
public abstract class MixinPistonBaseBlock {

    @Inject(at = @At("HEAD"), method = "moveBlocks", cancellable = true)
    private void itemrestrictions$blockCrossChunkPiston(Level level, BlockPos pos, Direction direction,
                                                        boolean extending, CallbackInfoReturnable<Boolean> callback) {
        BlockPos pistonHeadPos = pos.relative(direction);
        if (ChunkProtection.crossesOwnershipBoundary(level, pos, pistonHeadPos)) {
            callback.setReturnValue(false);
            return;
        }

        PistonStructureResolver resolver = new PistonStructureResolver(level, pos, direction, extending);
        if (!resolver.resolve()) {
            return;
        }

        Direction pushDirection = resolver.getPushDirection();
        for (BlockPos blockToPush : resolver.getToPush()) {
            BlockPos destination = blockToPush.relative(pushDirection);
            if (itemrestrictions$isBlockedMovement(level, pos, blockToPush, destination)) {
                callback.setReturnValue(false);
                return;
            }
        }

        for (BlockPos blockToDestroy : resolver.getToDestroy()) {
            if (ChunkProtection.crossesOwnershipBoundary(level, pos, blockToDestroy)) {
                callback.setReturnValue(false);
                return;
            }
        }
    }

    @Unique
    private static boolean itemrestrictions$isBlockedMovement(Level level, BlockPos pistonPos, BlockPos source,
                                                               BlockPos destination) {
        if (ChunkProtection.crossesOwnershipBoundary(level, pistonPos, source)) {
            return true;
        }
        return ChunkProtection.crossesOwnershipBoundary(level, source, destination);
    }
}
