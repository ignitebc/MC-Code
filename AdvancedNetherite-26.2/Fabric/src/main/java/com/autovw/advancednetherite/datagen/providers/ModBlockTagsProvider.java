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
        // 모드 블록 태그
        tag(ModTags.NETHERITE_BLOCKS)
                .add(ModBlocks.ASH_BLOCK.properties().blockId())
                .add(ModBlocks.SUNLIGHT_BLOCK.properties().blockId())
                .add(ModBlocks.SOUL_BLOCK.properties().blockId())
                .add(ModBlocks.FROST_BLOCK.properties().blockId());

        tag(ModTags.INCORRECT_FOR_ASH_TOOL)
        ;
        tag(ModTags.INCORRECT_FOR_SUNLIGHT_TOOL)
        ;
        tag(ModTags.INCORRECT_FOR_SOUL_TOOL)
        ;
        tag(ModTags.INCORRECT_FOR_FROST_TOOL)
        ;


        // 바닐라 블록 태그
        tag(BlockTags.MINEABLE_WITH_PICKAXE)
                .addTag(ModTags.NETHERITE_BLOCKS);
        tag(BlockTags.BEACON_BASE_BLOCKS)
                .addTag(ModTags.NETHERITE_BLOCKS);
        tag(BlockTags.GUARDED_BY_PIGLINS)
                .add(ModBlocks.SUNLIGHT_BLOCK.properties().blockId());
        tag(BlockTags.NEEDS_DIAMOND_TOOL)
                .addTag(ModTags.NETHERITE_BLOCKS);


        // 공통 블록 태그
        tag(FabricModTags.COMMON_NETHERITE_BLOCKS)
                .addTag(ModTags.NETHERITE_BLOCKS);
    }
}
