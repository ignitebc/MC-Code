package com.autovw.advancednetherite.core.registry;

import com.autovw.advancednetherite.AdvancedNetherite;
import com.autovw.advancednetherite.api.annotation.Internal;
import com.autovw.advancednetherite.core.ModBlocks;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;

/**
 * @author Autovw
 */
@Internal
public final class ModBlockRegistry
{
    public static void registerBlocks()
    {
        Registry.register(BuiltInRegistries.BLOCK, id("ash_block"), ModBlocks.ASH_BLOCK);
        Registry.register(BuiltInRegistries.BLOCK, id("sunlight_block"), ModBlocks.SUNLIGHT_BLOCK);
        Registry.register(BuiltInRegistries.BLOCK, id("soul_block"), ModBlocks.SOUL_BLOCK);
        Registry.register(BuiltInRegistries.BLOCK, id("frost_block"), ModBlocks.FROST_BLOCK);
    }

    private static Identifier id(String name)
    {
        return Identifier.fromNamespaceAndPath(AdvancedNetherite.MOD_ID, name);
    }
}
