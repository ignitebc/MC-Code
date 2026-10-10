package com.mcserver.serverutilities.mixin;

import com.mcserver.serverutilities.structure.StructureBonusChests;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** illagerinvasion 구조물의 한 청크 배치가 끝나면 그 청크의 보상 상자 옆에 상자를 확률로 더한다. 다른 구조물은 바로 돌아간다. */
@Mixin(StructureStart.class)
public abstract class StructureBonusChestMixin {
    @Inject(method = "placeInChunk", at = @At("TAIL"))
    private void serverutilities$placeBonusChests(WorldGenLevel level, StructureManager structureManager,
                                                  ChunkGenerator generator, RandomSource random,
                                                  BoundingBox chunkBox, ChunkPos chunkPos, CallbackInfo ci) {
        StructureBonusChests.placeInChunk((StructureStart) (Object) this, level, chunkBox, chunkPos);
    }
}
