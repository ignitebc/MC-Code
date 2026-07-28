package com.daqem.arc.player;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

public record CachedBlockPos(ResourceKey<Level> dimension, long packedPos) {

    public static final Codec<CachedBlockPos> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Level.RESOURCE_KEY_CODEC.fieldOf("dimension").forGetter(CachedBlockPos::dimension),
            Codec.LONG.fieldOf("position").forGetter(CachedBlockPos::packedPos)
    ).apply(instance, CachedBlockPos::new));

    public static CachedBlockPos of(Level level, Vec3i pos) {
        long packedPos = BlockPos.asLong(pos.getX(), pos.getY(), pos.getZ());
        return new CachedBlockPos(level.dimension(), packedPos);
    }
}
