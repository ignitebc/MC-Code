package com.mcserver.serverutilities.mixin;

import com.mcserver.serverutilities.death.DeathChests;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 채굴·폭발·위더·드래곤·피스톤·명령어·다른 모드의 블록 교체가 모두 이 메서드를 거치므로,
 * 유품 상자를 다른 블록으로 바꾸려는 모든 시도를 서버에서 한 곳에서 거부한다.
 */
@Mixin(Level.class)
abstract class DeathChestProtectionMixin {
    @Inject(method = "setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;II)Z",
            at = @At("HEAD"), cancellable = true)
    private void serverutilities$protectDeathChest(BlockPos pos, BlockState state, int flags, int recursionLeft,
                                                   CallbackInfoReturnable<Boolean> cir) {
        Level level = (Level) (Object) this;
        if (level.isClientSide() || level.isOutsideBuildHeight(pos) || DeathChests.isRemoving()) return;
        // 유품 상자끼리의 상태 갱신(물 잠김 등)은 허용한다.
        if (state.is(DeathChests.BLOCK)) return;
        if (level.getBlockState(pos).is(DeathChests.BLOCK)) cir.setReturnValue(false);
    }
}
