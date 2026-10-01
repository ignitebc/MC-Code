package com.mcserver.serverutilities.lodestone;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.piston.PistonMovingBlockEntity;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** 앞으로 설치되는 자석석의 소유자와 위치를 기록하고 플레이어당 한 개로 제한한다. */
public final class LodestoneOwnership extends SavedData {
    private static final Identifier FILE_ID = Identifier.fromNamespaceAndPath(
            "serverutilities",
            "lodestone_ownership");

    private record OwnedLodestone(UUID ownerId, GlobalPos location) {
        private static final Codec<OwnedLodestone> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                UUIDUtil.STRING_CODEC.fieldOf("owner_id").forGetter(OwnedLodestone::ownerId),
                GlobalPos.CODEC.fieldOf("location").forGetter(OwnedLodestone::location)
        ).apply(instance, OwnedLodestone::new));
    }

    public static final Codec<LodestoneOwnership> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            OwnedLodestone.CODEC.listOf()
                    .optionalFieldOf("lodestones", List.of())
                    .forGetter(LodestoneOwnership::getOwnedLodestones)
    ).apply(instance, LodestoneOwnership::new));

    public static final SavedDataType<LodestoneOwnership> TYPE = new SavedDataType<>(
            FILE_ID,
            LodestoneOwnership::new,
            CODEC,
            DataFixTypes.SAVED_DATA_COMMAND_STORAGE);

    private final Map<UUID, GlobalPos> locationsByOwner = new LinkedHashMap<>();
    private final Map<GlobalPos, UUID> ownersByLocation = new LinkedHashMap<>();

    public LodestoneOwnership() {
    }

    private LodestoneOwnership(List<OwnedLodestone> lodestones) {
        for (OwnedLodestone lodestone : lodestones) {
            if (this.locationsByOwner.containsKey(lodestone.ownerId())
                    || this.ownersByLocation.containsKey(lodestone.location())) {
                continue;
            }
            this.locationsByOwner.put(lodestone.ownerId(), lodestone.location());
            this.ownersByLocation.put(lodestone.location(), lodestone.ownerId());
        }
    }

    public static void register() {
        PlayerBlockBreakEvents.AFTER.register((level, player, pos, state, blockEntity) -> {
            if (!(level instanceof ServerLevel serverLevel)) {
                return;
            }
            LodestoneOwnership ownership = get(serverLevel.getServer());
            ownership.release(GlobalPos.of(serverLevel.dimension(), pos));
        });
    }

    public static LodestoneOwnership get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(TYPE);
    }

    public static boolean canPlace(ServerPlayer player) {
        MinecraftServer server = player.level().getServer();
        LodestoneOwnership ownership = get(server);
        GlobalPos location = ownership.getLocation(player.getUUID());
        if (location == null) {
            return true;
        }

        ServerLevel level = server.getLevel(location.dimension());
        // 설치 시에만 해당 청크를 조회한다. 언로드된 청크를 빈 블록으로 간주하면 제한을 우회할 수 있다.
        if (level != null && isLodestonePresent(level, location.pos())) {
            return false;
        }

        // 폭발·명령어 제거로 남은 기록과 이전 버전에서 저장된 기록도 복구한다.
        ownership.release(location);
        return true;
    }

    public static void recordPlacement(ServerPlayer player, BlockPos pos) {
        LodestoneOwnership ownership = get(player.level().getServer());
        GlobalPos location = GlobalPos.of(player.level().dimension(), pos.immutable());
        // 설치가 성공한 위치의 이전 기록은 제거된 블록의 소유권이다.
        ownership.release(location);
        ownership.claim(player.getUUID(), location);
    }

    public static void moveLodestones(ServerLevel level, List<BlockPos> positions, Direction direction) {
        LodestoneOwnership ownership = get(level.getServer());
        Map<UUID, GlobalPos> movedLocations = new LinkedHashMap<>();
        for (BlockPos pos : positions) {
            UUID ownerId = ownership.getOwner(level.dimension(), pos);
            if (ownerId != null && level.getBlockState(pos).is(Blocks.LODESTONE)) {
                movedLocations.put(ownerId, GlobalPos.of(level.dimension(), pos.relative(direction)));
            }
        }
        // 연속된 자석석을 밀 때 목적지가 다른 소유자의 이전 위치일 수 있으므로 먼저 모두 해제한다.
        for (UUID ownerId : movedLocations.keySet()) {
            ownership.release(ownership.locationsByOwner.get(ownerId));
        }
        for (Map.Entry<UUID, GlobalPos> entry : movedLocations.entrySet()) {
            ownership.release(entry.getValue());
            ownership.claim(entry.getKey(), entry.getValue());
        }
    }

    private static boolean isLodestonePresent(ServerLevel level, BlockPos pos) {
        if (level.getBlockState(pos).is(Blocks.LODESTONE)) {
            return true;
        }
        // 피스톤 애니메이션 중에는 자석석 대신 이동 블록 엔티티가 목적지에 존재한다.
        return level.getBlockEntity(pos) instanceof PistonMovingBlockEntity movingBlock
                && movingBlock.getMovedState().is(Blocks.LODESTONE);
    }

    @Nullable
    public UUID getOwner(ResourceKey<Level> dimension, BlockPos pos) {
        return this.ownersByLocation.get(GlobalPos.of(dimension, pos));
    }

    @Nullable
    public GlobalPos getLocation(UUID ownerId) {
        return this.locationsByOwner.get(ownerId);
    }

    private void claim(UUID ownerId, GlobalPos location) {
        if (this.locationsByOwner.containsKey(ownerId) || this.ownersByLocation.containsKey(location)) {
            return;
        }
        this.locationsByOwner.put(ownerId, location);
        this.ownersByLocation.put(location, ownerId);
        this.setDirty();
    }

    private void release(GlobalPos location) {
        UUID ownerId = this.ownersByLocation.remove(location);
        if (ownerId == null) {
            return;
        }
        this.locationsByOwner.remove(ownerId);
        this.setDirty();
    }

    private List<OwnedLodestone> getOwnedLodestones() {
        List<OwnedLodestone> lodestones = new ArrayList<>(this.locationsByOwner.size());
        for (Map.Entry<UUID, GlobalPos> entry : this.locationsByOwner.entrySet()) {
            lodestones.add(new OwnedLodestone(entry.getKey(), entry.getValue()));
        }
        return List.copyOf(lodestones);
    }
}
