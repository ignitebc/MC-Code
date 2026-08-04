package com.daqem.itemrestrictions.chunk;

import com.daqem.itemrestrictions.ItemRestrictions;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 소유 청크에서 어떤 행동을 막을지 판정한다.
 * <p>
 * 소유자 본인은 제약이 없고, 다른 플레이어에게는 통행만 허용한다. 주체가 플레이어가 아닌
 * 월드 변화는 각 자동화 장치와 폭발 보호 로직에서 소유권 경계를 넘는 경우만 별도로 막는다.
 */
public final class ChunkProtection {

    /** 안내 문구를 반복해서 띄우지 않도록 두는 간격 */
    private static final long MESSAGE_COOLDOWN_MILLIS = 1500L;

    private static final Map<UUID, Long> LAST_MESSAGE_TIME = new ConcurrentHashMap<>();

    private ChunkProtection() {
    }

    /**
     * 플레이어가 해당 위치를 건드릴 수 있는지 확인한다. 주인이 없는 청크는 항상 허용한다.
     */
    public static boolean canModify(Level level, BlockPos blockPos, @Nullable Player player) {
        if (level.isClientSide()) {
            return true;
        }
        if (!ChunkOwnership.isAvailable()) {
            return false;
        }
        ChunkOwnership.Owner owner = ChunkOwnership.getOwner(level, blockPos);
        if (owner == null) {
            return true;
        }
        if (player == null) {
            return true;
        }
        return owner.uuid().equals(player.getUUID());
    }

    /**
     * 막았을 때 소유자 이름을 담은 안내를 띄운다. 연타로 도배되지 않도록 짧은 간격을 둔다.
     */
    public static void notifyBlocked(Level level, BlockPos blockPos, @Nullable Player player) {
        if (player == null || level.isClientSide()) {
            return;
        }

        Component message;
        if (!ChunkOwnership.isAvailable()) {
            message = ItemRestrictions.translatable("chunk.unavailable");
        } else {
            ChunkOwnership.Owner owner = ChunkOwnership.getOwner(level, blockPos);
            if (owner == null) {
                return;
            }
            message = ItemRestrictions.translatable("chunk.protected", getOwnerName(level, owner));
        }

        long now = System.currentTimeMillis();
        Long last = LAST_MESSAGE_TIME.get(player.getUUID());
        if (last != null && now - last < MESSAGE_COOLDOWN_MILLIS) {
            return;
        }
        LAST_MESSAGE_TIME.put(player.getUUID(), now);
        player.sendOverlayMessage(message);
    }

    /**
     * 구매 시점에 저장한 소유자 이름을 우선 쓰고, 이름이 저장되기 전의 데이터면
     * 접속 중인 플레이어에서 찾는다. 둘 다 없으면 대체 문구를 쓴다.
     */
    private static Component getOwnerName(Level level, ChunkOwnership.Owner owner) {
        if (owner.name() != null && !owner.name().isBlank()) {
            return Component.literal(owner.name());
        }

        MinecraftServer server = level.getServer();
        if (server != null) {
            ServerPlayer ownerPlayer = server.getPlayerList().getPlayer(owner.uuid());
            if (ownerPlayer != null) {
                return ownerPlayer.getName();
            }
        }
        return ItemRestrictions.translatable("chunk.owner.unknown");
    }

    /**
     * 위치를 건드릴 수 없으면 안내까지 띄우고 true를 돌려준다.
     */
    public static boolean denyAndNotify(Level level, BlockPos blockPos, @Nullable Player player) {
        if (canModify(level, blockPos, player)) {
            return false;
        }
        notifyBlocked(level, blockPos, player);
        return true;
    }

    /**
     * 클라이언트는 소유권 정보를 모른 채 설치·사용이 성공할 것으로 예측해 아이템 개수를 먼저 줄인다.
     * 서버가 취소하면 줄어든 것처럼 보이는 개수를 되돌리도록 인벤토리 전체를 다시 보낸다.
     * 아이템 소모가 있는 상호작용을 막았을 때만 호출해야 한다. (매 틱 호출되는 곳에서는 금지)
     */
    public static void resyncInventory(@Nullable Player player) {
        if (player instanceof ServerPlayer serverPlayer) {
            serverPlayer.containerMenu.sendAllDataToRemote();
        }
    }

    @Nullable
    public static Player findResponsiblePlayer(Explosion explosion) {
        if (explosion.getIndirectSourceEntity() instanceof Player player) {
            return player;
        }
        return findIgniter(explosion.getDirectSourceEntity());
    }

    @Nullable
    private static Player findIgniter(@Nullable Entity source) {
        if (source instanceof Player player) {
            return player;
        }
        if (source instanceof PrimedTnt primedTnt && primedTnt.getOwner() instanceof Player player) {
            return player;
        }
        return null;
    }

    /**
     * 청크 경계를 넘는 동작인지 확인한다. 피스톤과 유체가 밖에서 안으로 영향을 주는 경우를 막는 데 쓴다.
     */
    public static boolean crossesIntoProtectedChunk(Level level, BlockPos fromPos, BlockPos toPos) {
        if (level.isClientSide()) {
            return false;
        }
        if (!ChunkOwnership.isAvailable()) {
            return true;
        }
        ChunkOwnership.Owner target = ChunkOwnership.getOwner(level, toPos);
        if (target == null) {
            return false;
        }
        ChunkOwnership.Owner origin = ChunkOwnership.getOwner(level, fromPos);
        if (origin == null) {
            return true;
        }
        return !origin.uuid().equals(target.uuid());
    }

    /**
     * 두 위치의 소유자가 다른지 확인한다. 한쪽만 소유된 경우도 소유권 경계를 넘는 것으로 본다.
     */
    public static boolean crossesOwnershipBoundary(Level level, BlockPos firstPos, BlockPos secondPos) {
        if (level.isClientSide()) {
            return false;
        }
        if (!ChunkOwnership.isAvailable()) {
            return true;
        }
        ChunkOwnership.Owner firstOwner = ChunkOwnership.getOwner(level, firstPos);
        ChunkOwnership.Owner secondOwner = ChunkOwnership.getOwner(level, secondPos);
        if (firstOwner == null && secondOwner == null) {
            return false;
        }
        if (firstOwner == null || secondOwner == null) {
            return true;
        }
        return !firstOwner.uuid().equals(secondOwner.uuid());
    }

    /**
     * 기준 위치에서 반경 안에 남의 소유 청크가 있는지 확인한다.
     * <p>
     * 소유권은 청크 기둥 단위라 높이는 볼 필요가 없다. 가로 범위가 걸치는 청크만 훑으면 되므로
     * 실제 검사 대상은 몇 개에 그친다. TNT 폭발이나 유체 확산이 남의 땅에 닿을지 미리 재는 데 쓴다.
     */
    public static boolean wouldReachOthersChunk(Level level, BlockPos origin, int radius, @Nullable Player player) {
        if (level.isClientSide()) {
            return false;
        }
        if (!ChunkOwnership.isAvailable()) {
            return true;
        }
        ChunkPos min = ChunkPos.containing(origin.offset(-radius, 0, -radius));
        ChunkPos max = ChunkPos.containing(origin.offset(radius, 0, radius));
        for (int chunkX = min.x(); chunkX <= max.x(); chunkX++) {
            for (int chunkZ = min.z(); chunkZ <= max.z(); chunkZ++) {
                ChunkOwnership.Owner owner = ChunkOwnership.getOwner(level, new ChunkPos(chunkX, chunkZ));
                if (owner == null) {
                    continue;
                }
                if (player == null || !owner.uuid().equals(player.getUUID())) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 남의 청크에 영향이 갈 설치라면 안내를 띄우고 true를 돌려준다.
     */
    public static boolean denyReachAndNotify(Level level, BlockPos origin, int radius, @Nullable Player player,
                                             String translationKey) {
        if (!wouldReachOthersChunk(level, origin, radius, player)) {
            return false;
        }
        if (player != null && !level.isClientSide()) {
            resyncInventory(player);
            long now = System.currentTimeMillis();
            Long last = LAST_MESSAGE_TIME.get(player.getUUID());
            if (last == null || now - last >= MESSAGE_COOLDOWN_MILLIS) {
                LAST_MESSAGE_TIME.put(player.getUUID(), now);
                player.sendOverlayMessage(ItemRestrictions.translatable(translationKey));
            }
        }
        return true;
    }

    public static void clearNotificationCooldown(Player player) {
        LAST_MESSAGE_TIME.remove(player.getUUID());
    }
}
