package com.mcserver.serverutilities.spawn;

import com.mcserver.serverutilities.ServerUtilities;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.PlayerSpawnFinder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.LevelData;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.phys.Vec3;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * 최초 접속자의 자리를 기준으로 이후 접속자를 무작위 좌표에 떨어뜨리고, 그 자리를 개인 리스폰 지점으로 고정한다.
 * 모든 상태는 서버 스레드에서만 다룬다. 좌표 탐색 결과도 서버 스레드로 되돌려 처리한다.
 */
public final class SpawnScatterRules {
    // 개인 리스폰 지점이 막혔을 때 둘러볼 청크 범위. 넓힐수록 사망 시 청크 적재가 늘어난다.
    private static final int FALLBACK_CHUNK_RADIUS = 2;
    private static final double HOLD_TOLERANCE = 0.01;
    private static final Component WAIT_MESSAGE =
            Component.literal("시작 위치를 정하는 중입니다. 잠시만 기다려 주세요.");
    private static final Component FAILED_MESSAGE =
            Component.literal("시작 위치를 정하지 못했습니다. 다시 접속하면 재시도합니다.");
    private static final Component HOME_MESSAGE =
            Component.literal("침대를 찾지 못해 처음 배정받은 자리에서 다시 시작합니다.");
    private static final Component BLOCKED_MESSAGE =
            Component.literal("리스폰 지점이 막혀 근처의 빈 자리에서 다시 시작합니다.");
    private static final Map<UUID, Hold> HOLDS = new HashMap<>();
    // 좌표를 찾는 중인 플레이어. 탐색 도중 재접속해도 두 번 배정하지 않는다.
    private static final Set<UUID> PENDING = new HashSet<>();

    private static Path anchorPath;
    private static SpawnAnchor anchor;

    private SpawnScatterRules() { }

    public static void initialize(MinecraftServer server) throws IOException {
        anchorPath = server.getWorldPath(LevelResource.ROOT).resolve("serverutilities-spawn.properties");
        anchor = SpawnAnchor.read(anchorPath).orElse(null);
    }

    public static void shutdown() {
        HOLDS.clear();
        PENDING.clear();
        anchorPath = null;
        anchor = null;
    }

    public static SpawnAnchor anchor() { return anchor; }

    public static void onJoin(ServerPlayer player) {
        if (!ServerUtilities.config().spawnScatter()) return;
        PersonalSpawnAccess state = (PersonalSpawnAccess) player;
        if (state.serverutilities$personalSpawn() != null) return;

        ServerLevel overworld = player.level().getServer().overworld();
        // 기준 좌표는 오버월드에서만 잡는다. 다른 차원에서 접속했다면 다음 접속으로 미룬다.
        if (player.level() != overworld) return;

        if (anchor == null) {
            // 최초 1인은 선 자리를 그대로 쓰고, 그 좌표가 이후 접속자의 기준이 된다.
            BlockPos start = player.blockPosition();
            if (!rememberAnchor(new SpawnAnchor(start.getX(), start.getY(), start.getZ()))) return;
            assignPersonalSpawn(player, overworld, start);
            return;
        }
        ServerPlayer.RespawnConfig existing = player.getRespawnConfig();
        if (existing != null) {
            // 이미 침대를 쓰던 플레이어는 옮기지 않고 그 자리를 개인 리스폰 지점으로 기록만 한다.
            state.serverutilities$setPersonalSpawn(existing.respawnData().pos());
            return;
        }
        beginPlacement(player, overworld, randomStart(player.getRandom()));
    }

    public static void onRespawn(ServerPlayer oldPlayer, ServerPlayer newPlayer, boolean alive) {
        if (alive) return;
        if (!ServerUtilities.config().spawnScatter()) return;
        BlockPos personal = ((PersonalSpawnAccess) newPlayer).serverutilities$personalSpawn();
        if (personal == null) return;
        // 침대가 깨졌거나 개인 좌표가 막히면 바닐라가 리스폰 기록을 버리고 월드 스폰으로 보낸다.
        if (newPlayer.getRespawnConfig() != null) return;

        ServerLevel overworld = newPlayer.level().getServer().overworld();
        // 막힘이 풀리면 원래 자리로 돌아가도록 개인 좌표 자체는 그대로 둔다.
        setRespawnConfig(newPlayer, overworld, personal);
        BlockPos open = findOpenSpot(overworld, personal);
        if (open == null) {
            beginPlacement(newPlayer, overworld, personal);
            return;
        }
        newPlayer.sendSystemMessage(open.equals(personal) ? HOME_MESSAGE : BLOCKED_MESSAGE);
        moveTo(newPlayer, overworld, Vec3.atBottomCenterOf(open));
    }

    public static void onLeave(ServerPlayer player) {
        // 잠금 상태가 저장되면 다음 접속에서도 무적으로 남으므로 반드시 되돌린다.
        release(player);
    }

    public static void tick(ServerPlayer player) {
        Hold hold = HOLDS.get(player.getUUID());
        if (hold == null) return;
        // 좌표가 정해질 때까지 낙하와 이동을 막는다.
        player.setDeltaMovement(Vec3.ZERO);
        player.resetFallDistance();
        Vec3 position = hold.position();
        boolean drifted = player.position().distanceToSqr(position) > HOLD_TOLERANCE;
        if (drifted) player.teleportTo(position.x, position.y, position.z);
    }

    private static BlockPos randomStart(RandomSource random) {
        int radius = ServerUtilities.config().spawnScatterRadius();
        int rollX = random.nextInt(radius * 2 + 1);
        int rollZ = random.nextInt(radius * 2 + 1);
        return new BlockPos(anchor.scatteredX(radius, rollX), anchor.y(), anchor.scatteredZ(radius, rollZ));
    }

    private static boolean rememberAnchor(SpawnAnchor candidate) {
        try {
            SpawnAnchor.write(anchorPath, candidate);
        } catch (IOException exception) {
            // 기준 좌표를 남기지 못하면 이후 접속자의 기준이 흔들리므로 배정을 미룬다.
            ServerUtilities.LOGGER.error("시작 위치 기준 좌표를 저장하지 못했습니다.", exception);
            return false;
        }
        anchor = candidate;
        return true;
    }

    private static void beginPlacement(ServerPlayer player, ServerLevel level, BlockPos suggestion) {
        hold(player);
        player.sendSystemMessage(WAIT_MESSAGE);
        MinecraftServer server = level.getServer();
        UUID id = player.getUUID();
        // 이미 탐색 중이면 그 결과를 그대로 쓴다. 잠금만 다시 걸어 두면 된다.
        if (!PENDING.add(id)) return;
        // 먼 좌표의 청크 생성이 끝날 때까지 서버를 붙잡지 않고, 결과만 서버 스레드로 되돌린다.
        PlayerSpawnFinder.findSpawn(level, suggestion).whenComplete((position, error) ->
                server.execute(() -> finishPlacement(server, level, id, position, error)));
    }

    private static void finishPlacement(MinecraftServer server, ServerLevel level, UUID id,
                                        Vec3 position, Throwable error) {
        PENDING.remove(id);
        ServerPlayer player = server.getPlayerList().getPlayer(id);
        if (player == null) {
            // 좌표를 기다리는 사이에 나간 경우. 기록이 없으므로 다음 접속에서 다시 배정한다.
            HOLDS.remove(id);
            return;
        }
        release(player);
        if (error != null || position == null) {
            ServerUtilities.LOGGER.error("시작 위치를 찾지 못했습니다: " + player.getName().getString(), error);
            player.sendSystemMessage(FAILED_MESSAGE);
            return;
        }
        moveTo(player, level, position);
        assignPersonalSpawn(player, level, BlockPos.containing(position));
    }

    private static void assignPersonalSpawn(ServerPlayer player, ServerLevel level, BlockPos position) {
        ((PersonalSpawnAccess) player).serverutilities$setPersonalSpawn(position);
        setRespawnConfig(player, level, position);
        player.sendSystemMessage(Component.literal("시작 위치가 정해졌습니다. ("
                + position.getX() + ", " + position.getY() + ", " + position.getZ()
                + ") 사망하면 이 자리에서 다시 시작합니다."));
    }

    private static void setRespawnConfig(ServerPlayer player, ServerLevel level, BlockPos position) {
        LevelData.RespawnData data = LevelData.RespawnData.of(level.dimension(), position, player.getYRot(), 0.0F);
        // 침대 없이도 좌표가 유지되려면 forced가 참이어야 한다. 두 번째 인자는 바닐라 안내 문구 발송 여부다.
        player.setRespawnPosition(new ServerPlayer.RespawnConfig(data, true), false);
    }

    private static void moveTo(ServerPlayer player, ServerLevel level, Vec3 position) {
        player.teleportTo(level, position.x, position.y, position.z, Set.of(),
                player.getYRot(), player.getXRot(), false);
    }

    /** 개인 좌표가 막혔으면 가까운 청크부터 차례로 비어 있는 자리를 찾는다. */
    private static BlockPos findOpenSpot(ServerLevel level, BlockPos origin) {
        if (isOpen(level, origin)) return origin;
        int originChunkX = origin.getX() >> 4;
        int originChunkZ = origin.getZ() >> 4;
        for (int ring = 0; ring <= FALLBACK_CHUNK_RADIUS; ring++) {
            for (int offsetX = -ring; offsetX <= ring; offsetX++) {
                for (int offsetZ = -ring; offsetZ <= ring; offsetZ++) {
                    boolean onRing = Math.abs(offsetX) == ring || Math.abs(offsetZ) == ring;
                    if (!onRing) continue;
                    ChunkPos chunk = new ChunkPos(originChunkX + offsetX, originChunkZ + offsetZ);
                    BlockPos candidate = PlayerSpawnFinder.getSpawnPosInChunk(level, chunk);
                    if (candidate != null && isOpen(level, candidate)) return candidate;
                }
            }
        }
        return null;
    }

    /** 바닐라가 강제 리스폰을 허용하는 조건과 같게 맞춘다. */
    private static boolean isOpen(ServerLevel level, BlockPos position) {
        BlockState floor = level.getBlockState(position);
        BlockState head = level.getBlockState(position.above());
        boolean floorOpen = floor.getBlock().isPossibleToRespawnInThis(floor);
        boolean headOpen = head.getBlock().isPossibleToRespawnInThis(head);
        return floorOpen && headOpen;
    }

    private static void hold(ServerPlayer player) {
        HOLDS.computeIfAbsent(player.getUUID(),
                id -> new Hold(player.position(), player.isInvulnerable(), player.isNoGravity()));
        player.setInvulnerable(true);
        player.setNoGravity(true);
        player.setDeltaMovement(Vec3.ZERO);
    }

    private static void release(ServerPlayer player) {
        Hold hold = HOLDS.remove(player.getUUID());
        if (hold == null) return;
        player.setInvulnerable(hold.invulnerable());
        player.setNoGravity(hold.noGravity());
        player.setDeltaMovement(Vec3.ZERO);
        player.resetFallDistance();
    }

    private record Hold(Vec3 position, boolean invulnerable, boolean noGravity) { }
}
