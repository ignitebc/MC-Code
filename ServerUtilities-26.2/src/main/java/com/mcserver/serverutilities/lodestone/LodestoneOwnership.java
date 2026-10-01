package com.mcserver.serverutilities.lodestone;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.Level;
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
        return !get(player.level().getServer()).locationsByOwner.containsKey(player.getUUID());
    }

    public static void recordPlacement(ServerPlayer player, BlockPos pos) {
        LodestoneOwnership ownership = get(player.level().getServer());
        GlobalPos location = GlobalPos.of(player.level().dimension(), pos.immutable());
        ownership.claim(player.getUUID(), location);
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
