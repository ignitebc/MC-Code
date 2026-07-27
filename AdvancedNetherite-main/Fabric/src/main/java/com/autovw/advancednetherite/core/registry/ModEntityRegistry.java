package com.autovw.advancednetherite.core.registry;

import com.autovw.advancednetherite.AdvancedNetherite;
import com.autovw.advancednetherite.common.entity.DialgaPetEntity;
import com.autovw.advancednetherite.core.ModEntityTypes;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;

public final class ModEntityRegistry
{
    private ModEntityRegistry()
    {
    }

    public static void registerEntityTypes()
    {
        Registry.register(BuiltInRegistries.ENTITY_TYPE, id("dialga_pet"), ModEntityTypes.DIALGA_PET);
        FabricDefaultAttributeRegistry.register(ModEntityTypes.DIALGA_PET, DialgaPetEntity.createAttributes());
    }

    private static ResourceLocation id(String name)
    {
        return ResourceLocation.fromNamespaceAndPath(AdvancedNetherite.MOD_ID, name);
    }
}
