package com.autovw.advancednetherite.core;

import com.autovw.advancednetherite.AdvancedNetherite;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;

/**
 * @author Autovw
 */
public final class ModBlocks
{
    public static final Block ASH_BLOCK = new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.NETHERITE_BLOCK).setId(key("ash_block")));
    public static final Block SOLAR_BLOCK = new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.NETHERITE_BLOCK).setId(key("solar_block")));
    public static final Block SOUL_BLOCK = new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.NETHERITE_BLOCK).setId(key("soul_block")));
    public static final Block FROST_BLOCK = new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.NETHERITE_BLOCK).setId(key("frost_block")));

    private static ResourceKey<Block> key(String name)
    {
        return ResourceKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath(AdvancedNetherite.MOD_ID, name));
    }
}
