package com.autovw.advancednetherite.datagen.providers;

import com.autovw.advancednetherite.core.ModBlocks;
import com.autovw.advancednetherite.core.util.FabricModTags;
import com.autovw.advancednetherite.core.util.ModTags;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagsProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.tags.BlockTags;

import java.util.concurrent.CompletableFuture;

/**
 * @author Autovw
 */
public class ModBlockTagsProvider extends FabricTagsProvider.BlockTagsProvider
{
    public ModBlockTagsProvider(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture)
    {
        super(output, registriesFuture);
    }

    @Override
    protected void addTags(HolderLookup.Provider arg)
    {
        // Mod block tags
        tag(ModTags.NETHERITE_BLOCKS)
                .add(ModBlocks.NETHERITE_IRON_BLOCK.properties().blockId())
                .add(ModBlocks.NETHERITE_GOLD_BLOCK.properties().blockId())
                .add(ModBlocks.NETHERITE_EMERALD_BLOCK.properties().blockId())
                .add(ModBlocks.NETHERITE_DIAMOND_BLOCK.properties().blockId());

        tag(ModTags.INCORRECT_FOR_NETHERITE_IRON_TOOL)
                //.addTag(BlockTags.INCORRECT_FOR_NETHERITE_TOOL)
        ;
        tag(ModTags.INCORRECT_FOR_NETHERITE_GOLD_TOOL)
                //.addTag(BlockTags.INCORRECT_FOR_NETHERITE_TOOL)
        ;
        tag(ModTags.INCORRECT_FOR_NETHERITE_EMERALD_TOOL)
                //.addTag(BlockTags.INCORRECT_FOR_NETHERITE_TOOL)
        ;
        tag(ModTags.INCORRECT_FOR_NETHERITE_DIAMOND_TOOL)
                //.addTag(BlockTags.INCORRECT_FOR_NETHERITE_TOOL)
        ;


        // Vanilla block tags
        tag(BlockTags.MINEABLE_WITH_PICKAXE)
                .addTag(ModTags.NETHERITE_BLOCKS);
        tag(BlockTags.BEACON_BASE_BLOCKS)
                .addTag(ModTags.NETHERITE_BLOCKS);
        tag(BlockTags.GUARDED_BY_PIGLINS)
                .add(ModBlocks.NETHERITE_GOLD_BLOCK.properties().blockId());
        tag(BlockTags.NEEDS_DIAMOND_TOOL)
                .addTag(ModTags.NETHERITE_BLOCKS);


        // Common block tags
        tag(FabricModTags.COMMON_NETHERITE_BLOCKS)
                .addTag(ModTags.NETHERITE_BLOCKS);
    }
}
