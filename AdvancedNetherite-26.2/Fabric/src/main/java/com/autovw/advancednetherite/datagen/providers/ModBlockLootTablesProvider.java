package com.autovw.advancednetherite.datagen.providers;

import com.autovw.advancednetherite.core.ModBlocks;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricBlockLootSubProvider;
import net.minecraft.core.HolderLookup;

import java.util.concurrent.CompletableFuture;

/**
 * @author Autovw
 */
public class ModBlockLootTablesProvider extends FabricBlockLootSubProvider
{
    public ModBlockLootTablesProvider(FabricPackOutput dataOutput, CompletableFuture<HolderLookup.Provider> registryLookup)
    {
        super(dataOutput, registryLookup);
    }

    @Override
    public void generate()
    {
        this.dropSelf(ModBlocks.ASH_BLOCK);
        this.dropSelf(ModBlocks.SOLAR_BLOCK);
        this.dropSelf(ModBlocks.SOUL_BLOCK);
        this.dropSelf(ModBlocks.FROST_BLOCK);
    }
}
