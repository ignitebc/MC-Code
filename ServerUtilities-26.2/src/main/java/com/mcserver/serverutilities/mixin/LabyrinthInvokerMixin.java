package com.mcserver.serverutilities.mixin;

import com.mcserver.serverutilities.monster.LabyrinthInvokers;
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

/** 미궁의 한 청크 배치가 끝나면 그 청크에 들어 있는 방에 찬란한 기원자를 놓는다. 다른 구조물은 바로 돌아간다. */
@Mixin(StructureStart.class)
public abstract class LabyrinthInvokerMixin {
    @Inject(method = "placeInChunk", at = @At("TAIL"))
    private void serverutilities$placeLabyrinthInvokers(WorldGenLevel level, StructureManager structureManager,
                                                         ChunkGenerator generator, RandomSource random,
                                                         BoundingBox chunkBox, ChunkPos chunkPos, CallbackInfo ci) {
        LabyrinthInvokers.placeInChunk((StructureStart) (Object) this, level, chunkBox);
    }
}
