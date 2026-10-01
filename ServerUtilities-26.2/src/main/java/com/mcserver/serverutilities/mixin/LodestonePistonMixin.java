package com.mcserver.serverutilities.mixin;

import com.mcserver.serverutilities.lodestone.LodestoneOwnership;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.piston.PistonBaseBlock;
import net.minecraft.world.level.block.piston.PistonStructureResolver;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.List;

@Mixin(PistonBaseBlock.class)
public abstract class LodestonePistonMixin {
    @Redirect(method = "moveBlocks", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/block/piston/PistonStructureResolver;getToPush()Ljava/util/List;"))
    private List<BlockPos> serverutilities$moveLodestoneOwnership(PistonStructureResolver resolver,
                                                                Level level, BlockPos pistonPos,
                                                                Direction direction, boolean extending) {
        List<BlockPos> positions = resolver.getToPush();
        if (level instanceof ServerLevel serverLevel) {
            // 이동 판정에 성공한 경우만 도달한다. 블록 교체 전에 기존 소유자를 가져온다.
            LodestoneOwnership.moveLodestones(serverLevel, positions, resolver.getPushDirection());
        }
        return positions;
    }
}
