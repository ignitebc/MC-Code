package com.mcserver.serverutilities.structure;

import com.mcserver.serverutilities.ServerUtilities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.RandomizableContainer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.storage.loot.LootTable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 새로 생성되는 illagerinvasion 구조물의 보상 상자 옆에 같은 상자를 확률로 하나 더 놓아 구조물당 상자 수를 조금 늘린다.
 * <p>
 * 구조물이 청크에 배치된 직후 그 청크에 들어 있는 보상 상자만 살핀다. 추가 상자는 원래 상자의 좌우 칸을 먼저 보고,
 * 둘 다 막혀 있으면 벽에 붙은 앞뒤 칸에 놓는다. 원래 상자와 같은 방향의 단일 상자라 이중 상자로 합쳐지지 않고,
 * 놓을 칸이 없으면 건너뛴다. 추첨은 월드 시드와 상자 좌표로 정해 같은 시드면 같은 자리에 생긴다.
 * <p>
 * 미궁 일반 상자는 방마다 가장 많아 개수 대신 전리품 확률만 올렸다. 구조물은 청크가 처음 생성될 때만 배치되므로
 * 이미 생성된 구조물은 바뀌지 않는다. illagerinvasion이 없으면 해당 구조물이 생기지 않아 아무 일도 하지 않는다.
 */
public final class StructureBonusChests {
    private static final String MOD_NAMESPACE = "illagerinvasion";
    /** 보상 상자 전리품 표별 추가 상자 확률. 목록에 없는 표는 상자를 더하지 않는다. */
    private static final Map<String, Double> EXTRA_CHEST_CHANCE = Map.of(
            "chests/labyrinth", 0.30,
            "chests/labyrinth_map", 0.30,
            "chests/illager_fort_ground", 0.25,
            "chests/illager_fort_tower", 0.25,
            "chests/illusioner_tower_entrance", 0.25,
            "chests/illusioner_tower_stairs", 0.25,
            "chests/sorcerer_hut", 0.25,
            "chests/firecaller_hut", 0.25);
    /** 상자마다 추첨 난수를 월드 시드에서 갈라 내는 값. 다른 구조물 난수와 겹치지 않게 정한 임의의 상수다. */
    private static final long SELECTION_SALT = 0x4368657374426F6EL;

    private StructureBonusChests() { }

    private record RewardChest(BlockPos pos, BlockState state, ResourceKey<LootTable> lootTable, double chance) { }

    /** 구조물의 한 청크 배치가 끝난 직후 부른다. 이 청크에 들어 있는 보상 상자 옆에만 상자를 더한다. */
    public static void placeInChunk(StructureStart start, WorldGenLevel level, BoundingBox chunkBox, ChunkPos chunkPos) {
        if (!ServerUtilities.config().structureChests()) return;
        if (!isModStructure(start, level)) return;
        for (RewardChest chest : findRewardChests(start, level, chunkBox, chunkPos)) {
            tryAddChest(level, chunkBox, chest);
        }
    }

    private static boolean isModStructure(StructureStart start, WorldGenLevel level) {
        Identifier structureId = level.registryAccess().lookupOrThrow(Registries.STRUCTURE).getKey(start.getStructure());
        return structureId != null && MOD_NAMESPACE.equals(structureId.getNamespace());
    }

    /** 이 구조물 조각 안에 있는 보상 상자. 상자를 더하는 동안 블록 엔티티 목록이 바뀌므로 먼저 모아 둔다. */
    private static List<RewardChest> findRewardChests(StructureStart start, WorldGenLevel level, BoundingBox chunkBox,
                                                      ChunkPos chunkPos) {
        ChunkAccess chunk = level.getChunk(chunkPos.x(), chunkPos.z());
        List<RewardChest> chests = new ArrayList<>();
        for (BlockPos pos : List.copyOf(chunk.getBlockEntitiesPos())) {
            BlockState state = level.getBlockState(pos);
            boolean candidate = state.getBlock() instanceof ChestBlock && chunkBox.isInside(pos) && insideStructure(start, pos);
            if (!candidate) continue;
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (!(blockEntity instanceof RandomizableContainer container)) continue;
            ResourceKey<LootTable> lootTable = container.getLootTable();
            if (lootTable == null) continue;
            Double chance = extraChestChance(lootTable.identifier());
            if (chance != null) chests.add(new RewardChest(pos.immutable(), state, lootTable, chance));
        }
        return chests;
    }

    private static boolean insideStructure(StructureStart start, BlockPos pos) {
        for (StructurePiece piece : start.getPieces()) {
            if (piece.getBoundingBox().isInside(pos)) return true;
        }
        return false;
    }

    private static Double extraChestChance(Identifier lootTableId) {
        if (!MOD_NAMESPACE.equals(lootTableId.getNamespace())) return null;
        return EXTRA_CHEST_CHANCE.get(lootTableId.getPath());
    }

    private static void tryAddChest(WorldGenLevel level, BoundingBox chunkBox, RewardChest chest) {
        RandomSource random = RandomSource.create(level.getSeed() ^ chest.pos().asLong() ^ SELECTION_SALT);
        if (random.nextDouble() >= chest.chance()) return;
        Direction facing = chest.state().getValue(ChestBlock.FACING);
        BlockPos spot = findSpot(level, chunkBox, chest.pos(), facing, random);
        if (spot == null) return;
        // 기본 상태는 단일 상자이고 물에 잠기지 않았다. 방향만 원래 상자에 맞춘다.
        BlockState extraState = chest.state().getBlock().defaultBlockState().setValue(ChestBlock.FACING, facing);
        level.setBlock(spot, extraState, Block.UPDATE_CLIENTS);
        RandomizableContainer.setBlockEntityLootTable(level, random, spot, chest.lootTable());
    }

    /** 좌우 칸을 무작위 순서로 먼저 보고, 둘 다 안 되면 벽에 붙은 앞뒤 칸을 본다. */
    private static BlockPos findSpot(WorldGenLevel level, BoundingBox chunkBox, BlockPos chestPos, Direction facing,
                                     RandomSource random) {
        Direction firstSide = facing.getClockWise();
        Direction secondSide = facing.getCounterClockWise();
        if (random.nextBoolean()) {
            firstSide = facing.getCounterClockWise();
            secondSide = facing.getClockWise();
        }
        for (Direction side : List.of(firstSide, secondSide)) {
            BlockPos spot = chestPos.relative(side);
            if (canHoldChest(level, chunkBox, spot)) return spot;
        }
        for (Direction end : List.of(facing, facing.getOpposite())) {
            BlockPos spot = chestPos.relative(end);
            if (canHoldChest(level, chunkBox, spot) && touchesWall(level, spot, chestPos)) return spot;
        }
        return null;
    }

    /** 이 청크 안의 빈 칸이고, 바닥이 있고, 위가 막히지 않아 상자를 열 수 있는 자리 */
    private static boolean canHoldChest(WorldGenLevel level, BoundingBox chunkBox, BlockPos spot) {
        if (!chunkBox.isInside(spot)) return false;
        if (!level.getBlockState(spot).isAir()) return false;
        BlockPos below = spot.below();
        boolean hasFloor = !level.getBlockState(below).getCollisionShape(level, below).isEmpty();
        BlockPos above = spot.above();
        boolean lidFree = !level.getBlockState(above).isRedstoneConductor(level, above);
        return hasFloor && lidFree;
    }

    /** 원래 상자 말고도 옆에 벽이 있어 방 한가운데나 통로를 막지 않는 자리 */
    private static boolean touchesWall(WorldGenLevel level, BlockPos spot, BlockPos chestPos) {
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            BlockPos neighbor = spot.relative(direction);
            if (neighbor.equals(chestPos)) continue;
            if (level.getBlockState(neighbor).isFaceSturdy(level, neighbor, direction.getOpposite())) return true;
        }
        return false;
    }
}
