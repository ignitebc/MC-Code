package com.mcserver.serverutilities.monster;

import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityReference;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

/**
 * 엔더 드래곤이 살아 있는 동안 드래곤 전투 지역의 플레이어마다 30초마다 엔더맨 2마리가 화가 나서 덤빈다.
 * <p>
 * 전투 지역은 바닐라 드래곤 전투가 참가자로 보는 범위(중앙 0, 128, 0에서 192블록, 드래곤 보스바가 보이는 범위)다.
 * 엔드 시티가 있는 바깥 섬은 1,000블록 넘게 떨어져 있어 바닐라 그대로다. 플레이어 주변 64블록의 화나지 않은 엔더맨 중에서
 * 무작위로 고르고, 모자라면 플레이어 주변 빈 땅에 새로 불러낸다. 분노는 바닐라 시간이 지나면 풀리고 다음 주기에 다시 고른다.
 */
public final class DragonEndermanRules {
    private static final int CHECK_INTERVAL_TICKS = 20;
    private static final long WAVE_INTERVAL_TICKS = 30 * 20;
    private static final int ENDERMEN_PER_PLAYER = 2;
    /** 바닐라 드래곤 전투의 참가자 판정과 같은 중심과 반경 */
    private static final double ARENA_CENTER_Y = 128.0;
    private static final double ARENA_RADIUS = 192.0;
    /** 플레이어 주변에서 엔더맨을 고르는 반경(블록) */
    private static final double PICK_RADIUS = 64.0;
    // 플레이어별 다음 습격 시각. 지역에 들어온 뒤 30초가 지나야 첫 습격이 온다.
    private static final Map<UUID, Long> NEXT_WAVE_TICKS = new HashMap<>();

    private DragonEndermanRules() { }

    public static void tick(MinecraftServer server) {
        ServerLevel end = server.getLevel(Level.END);
        if (end == null) return;
        long gameTime = end.getGameTime();
        if (gameTime % CHECK_INTERVAL_TICKS != 0) return;
        // 드래곤을 잡은 뒤 출구 포탈·관문을 오가는 플레이어는 습격하지 않는다.
        if (!isDragonAlive(end)) {
            NEXT_WAVE_TICKS.clear();
            return;
        }
        Set<UUID> playersInArena = new HashSet<>();
        // 한 주기에 두 플레이어가 같은 엔더맨을 나눠 갖지 않게 한다.
        Set<Integer> provokedThisCycle = new HashSet<>();
        for (ServerPlayer player : end.players()) {
            if (!isInArena(player)) continue;
            UUID playerId = player.getUUID();
            playersInArena.add(playerId);
            long nextWave = NEXT_WAVE_TICKS.computeIfAbsent(playerId, id -> gameTime + WAVE_INTERVAL_TICKS);
            if (gameTime < nextWave) continue;
            NEXT_WAVE_TICKS.put(playerId, gameTime + WAVE_INTERVAL_TICKS);
            sendWave(end, player, provokedThisCycle);
        }
        // 지역을 벗어난 플레이어는 다시 들어오면 30초부터 센다.
        NEXT_WAVE_TICKS.keySet().retainAll(playersInArena);
    }

    public static void shutdown() {
        NEXT_WAVE_TICKS.clear();
    }

    private static boolean isDragonAlive(ServerLevel end) {
        return !end.getEntities(EntityTypes.ENDER_DRAGON, EnderDragon::isAlive).isEmpty();
    }

    /** 드래곤 전투 지역 안의 생존·모험 모드 플레이어인지 */
    private static boolean isInArena(ServerPlayer player) {
        boolean fightable = player.isAlive() && !player.isCreative() && !player.isSpectator();
        if (!fightable) return false;
        return player.distanceToSqr(0.0, ARENA_CENTER_Y, 0.0) <= ARENA_RADIUS * ARENA_RADIUS;
    }

    /** 주변의 화나지 않은 엔더맨 2마리를 이 플레이어에게 화나게 한다. 모자라면 새로 불러낸다. */
    private static void sendWave(ServerLevel end, ServerPlayer player, Set<Integer> provokedThisCycle) {
        List<EnderMan> candidates = new ArrayList<>(end.getEntitiesOfClass(EnderMan.class,
                player.getBoundingBox().inflate(PICK_RADIUS),
                enderman -> enderman.isAlive() && enderman.getTarget() == null
                        && !provokedThisCycle.contains(enderman.getId())));
        Collections.shuffle(candidates, new Random(end.getRandom().nextLong()));
        int provoked = 0;
        for (EnderMan enderman : candidates) {
            if (provoked >= ENDERMEN_PER_PLAYER) break;
            provoke(enderman, player);
            provokedThisCycle.add(enderman.getId());
            provoked++;
        }
        while (provoked < ENDERMEN_PER_PLAYER) {
            EnderMan summoned = summonNear(end, player);
            // 빈 땅이 없으면 이번 주기는 넘어간다. 다음 주기에 다시 채운다.
            if (summoned == null) break;
            provoke(summoned, player);
            provokedThisCycle.add(summoned.getId());
            provoked++;
        }
    }

    /** 바닐라에서 엔더맨을 쳐다봤을 때처럼 이 플레이어를 노리고, 분노 시간이 끝날 때까지 쫓게 한다. */
    private static void provoke(EnderMan enderman, ServerPlayer player) {
        enderman.setTarget(player);
        enderman.setPersistentAngerTarget(EntityReference.of(player));
        enderman.startPersistentAngerTimer();
    }

    private static EnderMan summonNear(ServerLevel end, ServerPlayer player) {
        BlockPos feet = BossMinionRules.findSpawnPosition(end, player.blockPosition(), EntityTypes.ENDERMAN);
        if (feet == null) return null;
        EnderMan enderman = EntityTypes.ENDERMAN.create(end, EntitySpawnReason.MOB_SUMMONED);
        if (enderman == null) return null;
        float yaw = end.getRandom().nextFloat() * 360.0F;
        enderman.snapTo(feet.getX() + 0.5, feet.getY(), feet.getZ() + 0.5, yaw, 0.0F);
        enderman.finalizeSpawn(end, end.getCurrentDifficultyAt(feet), EntitySpawnReason.MOB_SUMMONED, null);
        end.addFreshEntityWithPassengers(enderman);
        return enderman;
    }
}
