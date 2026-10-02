package com.mcserver.serverutilities.mixin;

import com.mcserver.serverutilities.level.ToolPlacedBlockData;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(FallingBlockEntity.class)
abstract class ToolFallingBlockMixin {
    @Redirect(method = "tick", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/Level;setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;I)Z"))
    private boolean serverutilities$recordLandedBlock(Level level, BlockPos pos, BlockState state, int flags) {
        boolean placed = level.setBlock(pos, state, flags);
        if (placed && level instanceof ServerLevel serverLevel && ToolPlacedBlockData.isTrackedBlock(state)) {
            // 낙하 이동으로 설치 기록을 지우고 다시 채굴하는 반복도 제외한다.
            ToolPlacedBlockData.get(serverLevel).add(pos);
        }
        return placed;
    }
}
