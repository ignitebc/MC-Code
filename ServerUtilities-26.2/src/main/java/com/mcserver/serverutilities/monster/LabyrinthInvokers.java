package com.mcserver.serverutilities.monster;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.pools.SinglePoolElement;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * 새로 생성되는 illagerinvasion 미궁마다 찬란한 기원자를 1~5마리(마릿수마다 20%) 미궁 방에 한 마리씩 흩어 놓는다.
 * <p>
 * 미궁 방은 저마다 몹을 따로 추첨해 미궁 전체의 마릿수를 맞출 수 없으므로, 구조물이 청크에 배치될 때 미궁 전체의 방 목록에서
 * 직접 고른다. 고르는 일은 월드 시드와 미궁 시작 청크로 정해 여러 청크에 걸친 미궁도 어느 청크에서든 같은 방을 고른다.
 * 기원자는 고른 방의 바닥(엘리트 몹 연결점 바로 위)이 들어 있는 청크가 생성될 때 한 번만 놓인다.
 * <p>
 * 탑 꼭대기의 기존 기원자 묶음은 illagerinvasion 데이터에서 비웠다. 구조물은 청크가 처음 생성될 때만 배치되므로 이미 생성된 미궁은 바뀌지 않는다.
 * illagerinvasion이 없으면 미궁이 생기지 않아 아무 일도 하지 않는다.
 */
public final class LabyrinthInvokers {
    private static final Identifier LABYRINTH = Identifier.fromNamespaceAndPath("illagerinvasion", "labyrinth");
    private static final Identifier INVOKER = Identifier.fromNamespaceAndPath("illagerinvasion", "invoker");
    private static final String MOD_NAMESPACE = "illagerinvasion";
    private static final String ROOM_TEMPLATE_PREFIX = "labyrinth/rooms/";
    /** 방마다 바닥에 하나씩 있는 엘리트 몹 연결점의 풀. 위를 향하고 생성 후 조약돌 바닥이 된다. */
    private static final Identifier ROOM_MOB_POOL = Identifier.fromNamespaceAndPath("illagerinvasion", "mobs/labyrinth_elite");
    private static final int MIN_INVOKERS = 1;
    private static final int MAX_INVOKERS = 5;
    /** 방을 고르는 난수를 월드 시드에서 갈라 내는 값. 다른 구조물 난수와 겹치지 않게 정한 임의의 상수다. */
    private static final long SELECTION_SALT = 0x4C61627952696E74L;
    /** 연결점 목록의 순서만 섞는 난수. 방마다 연결점이 하나라 결과에 영향이 없고, 생성 난수를 소모하지 않게 따로 쓴다. */
    private static final long JIGSAW_ORDER_SEED = 0L;
    /** 방이 마릿수보다 적어 한 방에 둘 이상 놓을 때 겹치지 않게 옆으로 미는 거리(블록) */
    private static final double STACK_OFFSET = 0.3;

    private LabyrinthInvokers() { }

    /** 미궁의 한 청크 배치가 끝난 직후 부른다. 이 청크에 들어 있는 기원자 자리에만 기원자를 놓는다. */
    public static void placeInChunk(StructureStart start, WorldGenLevel level, BoundingBox chunkBox) {
        if (!isLabyrinth(start, level)) return;
        EntityType<?> invokerType = BuiltInRegistries.ENTITY_TYPE.getOptional(INVOKER).orElse(null);
        if (invokerType == null) return;
        List<BlockPos> roomSpots = roomSpots(start, level.getLevel().getStructureManager());
        if (roomSpots.isEmpty()) return;

        RandomSource selection = RandomSource.create(level.getSeed() ^ start.getChunkPos().pack() ^ SELECTION_SALT);
        int invokerCount = MIN_INVOKERS + selection.nextInt(MAX_INVOKERS - MIN_INVOKERS + 1);
        Collections.shuffle(roomSpots, new Random(selection.nextLong()));
        for (int index = 0; index < invokerCount; index++) {
            // 방이 마릿수보다 적으면 앞의 방부터 한 마리씩 더 넣어 전체 마릿수를 지킨다.
            BlockPos spot = roomSpots.get(index % roomSpots.size());
            int stackLayer = index / roomSpots.size();
            if (chunkBox.isInside(spot)) spawnInvoker(level, invokerType, spot, stackLayer);
        }
    }

    private static boolean isLabyrinth(StructureStart start, WorldGenLevel level) {
        Identifier structureId = level.registryAccess().lookupOrThrow(Registries.STRUCTURE).getKey(start.getStructure());
        return LABYRINTH.equals(structureId);
    }

    /** 미궁 방마다 기원자가 설 바닥 칸. 조각 목록은 저장된 순서 그대로라 어느 청크에서 불러도 같은 목록이 나온다. */
    private static List<BlockPos> roomSpots(StructureStart start, StructureTemplateManager templates) {
        List<BlockPos> spots = new ArrayList<>();
        for (StructurePiece piece : start.getPieces()) {
            if (!(piece instanceof PoolElementStructurePiece poolPiece)) continue;
            if (!(poolPiece.getElement() instanceof SinglePoolElement element)) continue;
            Identifier template = element.getTemplateLocation();
            boolean room = MOD_NAMESPACE.equals(template.getNamespace()) && template.getPath().startsWith(ROOM_TEMPLATE_PREFIX);
            if (!room) continue;
            List<StructureTemplate.JigsawBlockInfo> jigsaws = element.getShuffledJigsawBlocks(templates,
                    poolPiece.getPosition(), poolPiece.getRotation(), RandomSource.create(JIGSAW_ORDER_SEED));
            for (StructureTemplate.JigsawBlockInfo jigsaw : jigsaws) {
                if (ROOM_MOB_POOL.equals(jigsaw.pool().identifier())) spots.add(jigsaw.info().pos().above());
            }
        }
        return spots;
    }

    /** 구조물 몹처럼 생성 이유를 STRUCTURE로 두고, 플레이어가 멀어져도 사라지지 않게 한다. */
    private static void spawnInvoker(WorldGenLevel level, EntityType<?> invokerType, BlockPos spot, int stackLayer) {
        if (!(invokerType.create(level.getLevel(), EntitySpawnReason.STRUCTURE) instanceof Mob invoker)) return;
        double x = spot.getX() + 0.5 + stackLayer * STACK_OFFSET;
        float yaw = level.getRandom().nextFloat() * 360.0F;
        invoker.snapTo(x, spot.getY(), spot.getZ() + 0.5, yaw, 0.0F);
        invoker.finalizeSpawn(level, level.getCurrentDifficultyAt(spot), EntitySpawnReason.STRUCTURE, null);
        invoker.setPersistenceRequired();
        level.addFreshEntityWithPassengers(invoker);
    }
}
