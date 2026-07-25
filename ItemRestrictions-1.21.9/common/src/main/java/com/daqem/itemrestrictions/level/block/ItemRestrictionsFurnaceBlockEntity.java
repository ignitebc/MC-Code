package com.daqem.itemrestrictions.level.block;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;

import java.util.UUID;

public interface ItemRestrictionsFurnaceBlockEntity {

    ServerPlayer itemrestrictions$getPlayer();

    void itemrestrictions$setPlayer(ServerPlayer player);

    UUID itemrestrictions$getPlayerUUID();

    void itemrestrictions$setPlayerUUID(UUID playerUUID);

    int itemrestrictions$getLitTime();

    void itemrestrictions$setLitTime(int i);

    boolean itemrestrictions$isLit();

    RecipeManager.CachedCheck<SingleRecipeInput, ? extends AbstractCookingRecipe> itemrestrictions$getQuickCheck();

    AbstractFurnaceBlockEntity itemrestrictions$getAbstractFurnaceBlockEntity();

    RecipeHolder<?> itemrestrictions$getRecipe();

    boolean itemrestrictions$isRestricted();

    void itemrestrictions$setRestricted(boolean restricted);
}
