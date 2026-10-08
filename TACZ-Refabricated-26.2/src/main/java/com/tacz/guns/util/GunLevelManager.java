package com.tacz.guns.util;

import com.tacz.guns.GunMod;
import com.tacz.guns.api.item.IGun;
import com.tacz.guns.api.item.nbt.GunItemDataAccessor;
import com.tacz.guns.network.NetworkHandler;
import com.tacz.guns.network.message.ServerMessageLevelUp;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.Container;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

import javax.annotation.Nullable;
import java.util.UUID;

/** 개별 총기의 누적 경험치, 레벨과 직접 사격·폭발 피해 배율을 관리한다. */
public final class GunLevelManager {
    public static final int MIN_LEVEL = 1;
    public static final int MAX_LEVEL = 500;
    public static final int EXP_PER_LEVEL = 100;
    public static final int MAX_EXP = (MAX_LEVEL - MIN_LEVEL) * EXP_PER_LEVEL;
    /** 레벨마다 직접 사격 피해 +1%. LV500에서 +499%다. */
    public static final double DAMAGE_BONUS_PER_LEVEL = 0.01;
    /** 레벨마다 폭발 피해 +0.8%. LV500에서 +399.2%다. 쏜 사람 자신과 다른 플레이어도 같은 배율로 맞는다. */
    public static final double EXPLOSION_DAMAGE_BONUS_PER_LEVEL = 0.008;

    public static final TagKey<EntityType<?>> EXP_TARGETS = TagKey.create(
            Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(GunMod.MOD_ID, "gun_level_exp_targets"));

    private static final String GUN_INSTANCE_ID_TAG = "GunInstanceId";

    private GunLevelManager() {
    }

    public static int clampExp(int exp) {
        return Math.clamp(exp, 0, MAX_EXP);
    }

    public static int getLevel(int exp) {
        int accumulatedExp = clampExp(exp);
        return MIN_LEVEL + accumulatedExp / EXP_PER_LEVEL;
    }

    /** 해당 레벨에 처음 도달하는 누적 경험치. LV1은 0, LV100은 9,900, LV500은 49,900이다. */
    public static int getExp(int level) {
        int validLevel = Math.clamp(level, MIN_LEVEL, MAX_LEVEL);
        return (validLevel - MIN_LEVEL) * EXP_PER_LEVEL;
    }

    public static double getDamageMultiplier(int level) {
        int validLevel = Math.clamp(level, MIN_LEVEL, MAX_LEVEL);
        return 1.0 + (validLevel - MIN_LEVEL) * DAMAGE_BONUS_PER_LEVEL;
    }

    public static double getDamageMultiplier(ItemStack gun) {
        IGun iGun = IGun.getIGunOrNull(gun);
        if (iGun == null) {
            return 1.0;
        }
        return getDamageMultiplier(iGun.getLevel(gun));
    }

    public static double getExplosionDamageMultiplier(int level) {
        int validLevel = Math.clamp(level, MIN_LEVEL, MAX_LEVEL);
        return 1.0 + (validLevel - MIN_LEVEL) * EXPLOSION_DAMAGE_BONUS_PER_LEVEL;
    }

    public static double getExplosionDamageMultiplier(ItemStack gun) {
        IGun iGun = IGun.getIGunOrNull(gun);
        if (iGun == null) {
            return 1.0;
        }
        return getExplosionDamageMultiplier(iGun.getLevel(gun));
    }

    public static double getDamageBonusPercent(int level) {
        int validLevel = Math.clamp(level, MIN_LEVEL, MAX_LEVEL);
        return (validLevel - MIN_LEVEL) * DAMAGE_BONUS_PER_LEVEL * 100.0;
    }

    /** 같은 탄약 한 발에서 생성된 모든 산탄이 이 발사 정보를 공유한다. */
    public static GunShotContext createShotContext(ItemStack gun, LivingEntity shooter, boolean consumedAmmo) {
        float damageMultiplier = (float) getDamageMultiplier(gun);
        IGun iGun = IGun.getIGunOrNull(gun);
        if (iGun == null || !consumedAmmo || !(shooter instanceof ServerPlayer player)) {
            return new GunShotContext(damageMultiplier);
        }
        if (player.isCreative() || player.isSpectator() || iGun.useDummyAmmo(gun) || iGun.getExp(gun) >= MAX_EXP) {
            return new GunShotContext(damageMultiplier);
        }
        UUID gunInstanceId = getGunInstanceId(gun);
        if (gunInstanceId == null) {
            gunInstanceId = UUID.randomUUID();
            String instanceId = gunInstanceId.toString();
            ItemNbtUtils.updateTag(gun, tag -> tag.putString(GUN_INSTANCE_ID_TAG, instanceId));
            player.getInventory().setChanged();
        }
        return new GunShotContext(damageMultiplier, player, gunInstanceId, iGun.getGunId(gun));
    }

    public static boolean isExperienceTarget(ServerPlayer player, LivingEntity target) {
        if (!(target instanceof Mob)) {
            return false;
        }
        if (target.isAlliedTo(player)) {
            return false;
        }
        if (!target.getType().builtInRegistryHolder().is(EXP_TARGETS)) {
            return false;
        }
        return true;
    }

    public static void addExperience(ServerPlayer player, UUID gunInstanceId, Identifier gunId) {
        GunLocation location = findGun(player, gunInstanceId, gunId);
        if (location == null) {
            return;
        }
        ItemStack gun = location.gun();
        IGun iGun = IGun.getIGunOrNull(gun);
        if (iGun == null) {
            return;
        }
        int exp = clampExp(iGun.getExp(gun));
        if (exp >= MAX_EXP) {
            return;
        }
        int oldLevel = getLevel(exp);
        int newExp = exp + 1;
        ItemNbtUtils.updateTag(gun, tag -> tag.putInt(GunItemDataAccessor.GUN_EXP_TAG, newExp));
        syncGun(location);

        int newLevel = getLevel(newExp);
        if (newLevel > oldLevel) {
            NetworkHandler.sendToClientPlayer(new ServerMessageLevelUp(gun.copy(), newLevel), player);
        }
    }

    @Nullable
    private static UUID getGunInstanceId(ItemStack gun) {
        CompoundTag tag = ItemNbtUtils.getTag(gun);
        String instanceId = tag.getStringOr(GUN_INSTANCE_ID_TAG, "");
        if (instanceId.isEmpty()) {
            return null;
        }
        try {
            return UUID.fromString(instanceId);
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }

    private static boolean matchesGun(ItemStack gun, UUID gunInstanceId, Identifier gunId) {
        if (gun.isEmpty()) {
            return false;
        }
        IGun iGun = IGun.getIGunOrNull(gun);
        if (iGun == null || !gunId.equals(iGun.getGunId(gun))) {
            return false;
        }
        if (!gunInstanceId.equals(getGunInstanceId(gun))) {
            return false;
        }
        return true;
    }

    @Nullable
    private static GunLocation findGun(ServerPlayer player, UUID gunInstanceId, Identifier gunId) {
        GunLocation location = findPlayerGun(player, gunInstanceId, gunId);
        if (location != null) {
            return location;
        }
        // 발사 후 거래하거나 떨어뜨린 총기도 같은 식별 정보로 찾는다.
        MinecraftServer server = player.level().getServer();
        for (ServerPlayer otherPlayer : server.getPlayerList().getPlayers()) {
            if (otherPlayer == player) {
                continue;
            }
            location = findPlayerGun(otherPlayer, gunInstanceId, gunId);
            if (location != null) {
                return location;
            }
        }
        for (ServerLevel level : server.getAllLevels()) {
            for (Entity entity : level.getAllEntities()) {
                if (entity instanceof ItemEntity itemEntity
                        && matchesGun(itemEntity.getItem(), gunInstanceId, gunId)) {
                    return new GunLocation(itemEntity.getItem(), null, null, itemEntity);
                }
                if (entity instanceof Container container) {
                    location = findContainerGun(container, null, gunInstanceId, gunId);
                    if (location != null) {
                        return location;
                    }
                }
            }
            location = findLoadedContainerGun(level, gunInstanceId, gunId);
            if (location != null) {
                return location;
            }
        }
        return null;
    }

    @Nullable
    private static GunLocation findPlayerGun(ServerPlayer player, UUID gunInstanceId, Identifier gunId) {
        Inventory inventory = player.getInventory();
        GunLocation location = findContainerGun(inventory, player, gunInstanceId, gunId);
        if (location != null) {
            return location;
        }
        ItemStack carried = player.containerMenu.getCarried();
        if (matchesGun(carried, gunInstanceId, gunId)) {
            return new GunLocation(carried, player, null, null);
        }
        for (Slot slot : player.containerMenu.slots) {
            ItemStack gun = slot.getItem();
            if (matchesGun(gun, gunInstanceId, gunId)) {
                return new GunLocation(gun, player, slot.container, null);
            }
        }
        return findContainerGun(player.getEnderChestInventory(), player, gunInstanceId, gunId);
    }

    @Nullable
    private static GunLocation findContainerGun(Container container, @Nullable ServerPlayer holder,
                                                UUID gunInstanceId, Identifier gunId) {
        for (int slotIndex = 0; slotIndex < container.getContainerSize(); slotIndex++) {
            ItemStack gun = container.getItem(slotIndex);
            if (matchesGun(gun, gunInstanceId, gunId)) {
                return new GunLocation(gun, holder, container, null);
            }
        }
        return null;
    }

    @Nullable
    private static GunLocation findLoadedContainerGun(ServerLevel level, UUID gunInstanceId, Identifier gunId) {
        GunLocation[] found = new GunLocation[1];
        // 소지품과 드롭 아이템에서 찾지 못한 경우에만 활성 청크의 보관함을 확인한다.
        // 경험치 지급을 위해 다른 청크를 강제로 로드하지 않는다.
        level.getChunkSource().chunkMap.forEachBlockTickingChunk(chunk -> {
            if (found[0] != null) {
                return;
            }
            for (BlockEntity blockEntity : chunk.getBlockEntities().values()) {
                if (!blockEntity.isRemoved() && blockEntity instanceof Container container) {
                    GunLocation location = findContainerGun(container, null, gunInstanceId, gunId);
                    if (location != null) {
                        found[0] = location;
                        return;
                    }
                }
            }
        });
        return found[0];
    }

    private static void syncGun(GunLocation location) {
        if (location.container() != null) {
            location.container().setChanged();
        }
        if (location.itemEntity() != null) {
            location.itemEntity().setItem(location.gun().copy());
        }
        ServerPlayer holder = location.holder();
        if (holder != null) {
            holder.getInventory().setChanged();
            holder.inventoryMenu.broadcastChanges();
            if (holder.containerMenu != holder.inventoryMenu) {
                holder.containerMenu.broadcastChanges();
            }
        }
    }

    private record GunLocation(ItemStack gun, @Nullable ServerPlayer holder,
                               @Nullable Container container, @Nullable ItemEntity itemEntity) {
    }
}
