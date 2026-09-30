package com.mcserver.serverutilities.monster;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * 오버월드의 워든·위더가 플레이어를 공격 대상으로 잡으면 주변에 좀비·스켈레톤을 불러낸다.
 *
 * <p>대상을 처음 잡은 즉시 한 번, 이후 30초마다 보스별로 좀비 5마리·스켈레톤 5마리가 되도록 모자란 수만 채운다.
 * 소환한 몹은 보스 UUID가 담긴 태그로 구분하므로 서버를 다시 시작해도 남은 수를 그대로 센다. 장비와 레벨은
 * 자연 생성 몹과 같은 추첨을 거치고, 보스가 죽거나 사라져도 소환한 몹은 그대로 남는다.
 */
public final class BossMinionRules {
    private static final int CHECK_INTERVAL_TICKS = 20;
    private static final long REFILL_INTERVAL_TICKS = 30 * 20;
    private static final int ZOMBIE_COUNT = 5;
    private static final int SKELETON_COUNT = 5;
    /** 보스 중심에서 이 거리(블록) 안의 빈 땅에 소환한다. */
    private static final int SPAWN_RADIUS = 10;
    /** 한 마리당 빈 땅을 찾아보는 횟수. 못 찾은 몹은 다음 보충 때 다시 채운다. */
    private static final int SPAWN_ATTEMPTS = 16;
    private static final String OWNER_TAG_PREFIX = "serverutilities.boss_minion.";
    // 보스별 다음 보충 시각. 대상을 잠깐 놓쳤다 다시 잡아도 30초 간격을 지키도록 보스가 있는 동안 유지한다.
    private static final Map<UUID, Long> NEXT_REFILL_TICKS = new HashMap<>();

    private BossMinionRules() { }

    public static void tick(MinecraftServer server) {
        ServerLevel level = server.overworld();
        long gameTime = level.getGameTime();
        if (gameTime % CHECK_INTERVAL_TICKS != 0) return;

        Set<UUID> loadedBosses = new HashSet<>();
        Set<UUID> engagedWithers = new HashSet<>();
        for (Mob boss : findBosses(level)) {
            loadedBosses.add(boss.getUUID());
            Player target = playerTarget(boss);
            if (target == null) continue;

            if (boss instanceof WitherBoss wither) {
                engagedWithers.add(wither.getUUID());
                WitherEscapeRules.check(level, wither, target, gameTime);
            }
            refillIfDue(level, boss, target, gameTime);
        }
        // 죽었거나 청크가 내려간 보스의 기록은 버린다. 다시 불러와지면 모자란 수만 채우므로 소환 수가 늘지 않는다.
        NEXT_REFILL_TICKS.keySet().retainAll(loadedBosses);
        WitherEscapeRules.forgetExcept(engagedWithers);
    }

    public static void shutdown() {
        NEXT_REFILL_TICKS.clear();
        WitherEscapeRules.shutdown();
    }

    private static List<Mob> findBosses(ServerLevel level) {
        List<Mob> bosses = new ArrayList<>();
        bosses.addAll(level.getEntities(EntityTypes.WARDEN, Mob::isAlive));
        bosses.addAll(level.getEntities(EntityTypes.WITHER, Mob::isAlive));
        return bosses;
    }

    /** 보스가 공격 대상으로 잡은 플레이어. 크리에이티브·관전자는 대상으로 보지 않는다. */
    private static Player playerTarget(Mob boss) {
        if (!(boss.getTarget() instanceof Player player)) return null;
        boolean fightable = player.isAlive() && !player.isCreative() && !player.isSpectator();
        if (!fightable) return null;
        return player;
    }

    private static void refillIfDue(ServerLevel level, Mob boss, Player target, long gameTime) {
        UUID bossId = boss.getUUID();
        long nextRefill = NEXT_REFILL_TICKS.getOrDefault(bossId, gameTime);
        if (gameTime < nextRefill) return;
        NEXT_REFILL_TICKS.put(bossId, gameTime + REFILL_INTERVAL_TICKS);

        String ownerTag = OWNER_TAG_PREFIX + bossId;
        int missingZombies = ZOMBIE_COUNT - countMinions(level, EntityTypes.ZOMBIE, ownerTag);
        int missingSkeletons = SKELETON_COUNT - countMinions(level, EntityTypes.SKELETON, ownerTag);
        for (int i = 0; i < missingZombies; i++) {
            summon(level, boss, target, EntityTypes.ZOMBIE, ownerTag);
        }
        for (int i = 0; i < missingSkeletons; i++) {
            summon(level, boss, target, EntityTypes.SKELETON, ownerTag);
        }
    }

    private static <T extends Mob> int countMinions(ServerLevel level, EntityType<T> type, String ownerTag) {
        return level.getEntities(type, minion -> minion.isAlive() && minion.entityTags().contains(ownerTag)).size();
    }

    private static <T extends Mob> void summon(ServerLevel level, Mob boss, Player target, EntityType<T> type,
                                               String ownerTag) {
        BlockPos feet = findSpawnPosition(level, boss.blockPosition(), type);
        if (feet == null) return;
        T minion = type.create(level, EntitySpawnReason.MOB_SUMMONED);
        if (minion == null) return;

        float yaw = level.getRandom().nextFloat() * 360.0F;
        minion.snapTo(feet.getX() + 0.5, feet.getY(), feet.getZ() + 0.5, yaw, 0.0F);
        minion.finalizeSpawn(level, level.getCurrentDifficultyAt(feet), EntitySpawnReason.MOB_SUMMONED,
                spawnGroupData(minion));
        minion.addTag(ownerTag);
        minion.setTarget(target);
        // 월드에 추가될 때 자연 생성 몹과 같은 장비·레벨 추첨을 거친다.
        level.addFreshEntityWithPassengers(minion);
    }

    /** 좀비는 아기나 닭 기수로 나오지 않게 어른으로만 소환한다. */
    private static SpawnGroupData spawnGroupData(Mob minion) {
        if (minion instanceof Zombie) return new Zombie.ZombieGroupData(false, false);
        return null;
    }

    /** 보스에서 {@link #SPAWN_RADIUS} 안의, 발밑이 단단하고 몸이 들어갈 빈 자리. 없으면 null */
    private static BlockPos findSpawnPosition(ServerLevel level, BlockPos center, EntityType<?> type) {
        RandomSource random = level.getRandom();
        for (int attempt = 0; attempt < SPAWN_ATTEMPTS; attempt++) {
            int offsetX = random.nextIntBetweenInclusive(-SPAWN_RADIUS, SPAWN_RADIUS);
            int offsetZ = random.nextIntBetweenInclusive(-SPAWN_RADIUS, SPAWN_RADIUS);
            BlockPos feet = findFloor(level, center, offsetX, offsetZ, type);
            if (feet != null) return feet;
        }
        return null;
    }

    /** 보스 높이에 가까운 층부터 아래·위를 번갈아 보며 설 수 있는 자리를 찾는다. */
    private static BlockPos findFloor(ServerLevel level, BlockPos center, int offsetX, int offsetZ,
                                      EntityType<?> type) {
        for (int distanceY = 0; distanceY <= SPAWN_RADIUS; distanceY++) {
            int distanceSquared = offsetX * offsetX + distanceY * distanceY + offsetZ * offsetZ;
            if (distanceSquared > SPAWN_RADIUS * SPAWN_RADIUS) break;

            BlockPos below = center.offset(offsetX, -distanceY, offsetZ);
            if (canStand(level, below, type)) return below;
            BlockPos above = center.offset(offsetX, distanceY, offsetZ);
            if (distanceY > 0 && canStand(level, above, type)) return above;
        }
        return null;
    }

    /** 발밑 블록 윗면이 단단하고, 몹의 몸이 들어갈 자리가 비어 있으며 물·용암이 없는지 */
    private static boolean canStand(ServerLevel level, BlockPos feet, EntityType<?> type) {
        boolean usableArea = level.isLoaded(feet) && level.getWorldBorder().isWithinBounds(feet);
        if (!usableArea) return false;

        BlockPos groundPos = feet.below();
        BlockState ground = level.getBlockState(groundPos);
        boolean sturdyGround = ground.isFaceSturdy(level, groundPos, Direction.UP);
        AABB body = type.getSpawnAABB(feet.getX() + 0.5, feet.getY(), feet.getZ() + 0.5);
        boolean emptyBody = level.noCollision(body) && !level.containsAnyLiquid(body);
        return sturdyGround && emptyBody;
    }
}
