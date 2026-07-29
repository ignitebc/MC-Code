package com.daqem.itemrestrictions.mixin.block;

import com.daqem.itemrestrictions.chunk.ChunkProtection;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.FluidState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 소유 청크 밖에서 안으로 흘러드는 유체를 막는다.
 * <p>
 * 설치 시점 검사만으로는 계단식 지형에서 폭포가 이어질 때 도달 거리를 예측할 수 없다. 그래서
 * 실제로 칸을 넘어갈 때 판정한다. 청크 안에서 밖으로 나가는 흐름과 같은 주인의 청크 사이 흐름은 그대로 둔다.
 */
@Mixin(FlowingFluid.class)
public abstract class MixinFlowingFluid {

    @Inject(at = @At("HEAD"), method = "spreadTo", cancellable = true)
    private void itemrestrictions$blockSpreadIntoOwnedChunk(LevelAccessor levelAccessor, BlockPos pos,
                                                            BlockState blockState, Direction direction,
                                                            FluidState fluidState, CallbackInfo callback) {
        if (!(levelAccessor instanceof Level level)) {
            return;
        }
        // pos는 흘러 들어갈 자리이므로 반대 방향이 흘러나온 자리가 된다.
        BlockPos fromPos = pos.relative(direction.getOpposite());
        if (ChunkProtection.crossesIntoProtectedChunk(level, fromPos, pos)) {
            callback.cancel();
        }
    }
}
