package com.daqem.itemrestrictions.level.block;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BrewingStandBlockEntity;

import java.util.UUID;

public interface ItemRestrictionsBrewingStandBlockEntity {

    ServerPlayer itemrestrictions$getPlayer();

    void itemrestrictions$setPlayer(ServerPlayer player);

    UUID itemrestrictions$getPlayerUUID();

    void itemrestrictions$setPlayerUUID(UUID playerUUID);

    void itemrestrictions$setBrewTime(int i);

    boolean[] itemrestrictions$getPotionBits();

    boolean[] itemrestrictions$getLastPotionCount();

    void itemrestrictions$setLastPotionCount(boolean[] bls);

    BrewingStandBlockEntity itemrestrictions$getBrewingStandBlockEntity();
}
