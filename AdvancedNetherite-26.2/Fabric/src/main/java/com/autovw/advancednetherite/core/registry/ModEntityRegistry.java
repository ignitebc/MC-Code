package com.autovw.advancednetherite.core.registry;

import com.autovw.advancednetherite.AdvancedNetherite;
import com.autovw.advancednetherite.common.entity.DialgaPetEntity;
import com.autovw.advancednetherite.core.ModEntityTypes;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;

public final class ModEntityRegistry
{
    private ModEntityRegistry()
    {
    }

    public static void registerEntityTypes()
    {
        Registry.register(BuiltInRegistries.ENTITY_TYPE, id("dialga_pet"), ModEntityTypes.DIALGA_PET);
        FabricDefaultAttributeRegistry.register(ModEntityTypes.DIALGA_PET, DialgaPetEntity.createAttributes());
        Registry.register(BuiltInRegistries.ENTITY_TYPE, id("dragoon_pet"), ModEntityTypes.DRAGOON_PET);
        FabricDefaultAttributeRegistry.register(ModEntityTypes.DRAGOON_PET, DialgaPetEntity.createAttributes());
        Registry.register(BuiltInRegistries.ENTITY_TYPE, id("fairlins_pet"), ModEntityTypes.FAIRLINS_PET);
        FabricDefaultAttributeRegistry.register(ModEntityTypes.FAIRLINS_PET, DialgaPetEntity.createAttributes());
        Registry.register(BuiltInRegistries.ENTITY_TYPE, id("dark_dragon_pet"), ModEntityTypes.DARK_DRAGON_PET);
        FabricDefaultAttributeRegistry.register(ModEntityTypes.DARK_DRAGON_PET, DialgaPetEntity.createAttributes());
    }

    private static Identifier id(String name)
    {
        return Identifier.fromNamespaceAndPath(AdvancedNetherite.MOD_ID, name);
    }
}
