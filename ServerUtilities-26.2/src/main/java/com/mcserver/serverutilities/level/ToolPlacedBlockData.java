package com.mcserver.serverutilities.level;

import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

import java.util.stream.LongStream;

/** 차원별 설치 블록 좌표를 저장해 재시작 후에도 설치/파괴 EXP 반복을 차단한다. */
public final class ToolPlacedBlockData extends SavedData {
    private static final Codec<ToolPlacedBlockData> CODEC = Codec.LONG_STREAM.xmap(
            ToolPlacedBlockData::new, data -> LongStream.of(data.positions.toLongArray()))
            .fieldOf("positions").codec();
    private static final SavedDataType<ToolPlacedBlockData> TYPE = new SavedDataType<>(
            Identifier.fromNamespaceAndPath("serverutilities", "tool_placed_blocks"),
            ToolPlacedBlockData::new, CODEC, DataFixTypes.SAVED_DATA_COMMAND_STORAGE);

    private final LongOpenHashSet positions;

    private ToolPlacedBlockData() {
        positions = new LongOpenHashSet();
    }

    private ToolPlacedBlockData(LongStream savedPositions) {
        positions = new LongOpenHashSet(savedPositions.toArray());
    }

    public static ToolPlacedBlockData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(TYPE);
    }

    public static boolean isTrackedBlock(BlockState state) {
        if (state.is(BlockTags.MINEABLE_WITH_PICKAXE)) return true;
        if (state.is(BlockTags.MINEABLE_WITH_SHOVEL)) return true;
        if (state.is(BlockTags.MINEABLE_WITH_AXE)) return true;
        return false;
    }

    public boolean contains(BlockPos pos) {
        if (positions.contains(pos.asLong())) return true;
        return false;
    }

    public void add(BlockPos pos) {
        if (positions.add(pos.asLong())) setDirty();
    }

    public void remove(BlockPos pos) {
        if (positions.remove(pos.asLong())) setDirty();
    }
}
