package com.autovw.advancednetherite.core;

import com.autovw.advancednetherite.AdvancedNetherite;
import com.autovw.advancednetherite.common.entity.DialgaPetEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

public final class ModEntityTypes
{
    public static final EntityType<DialgaPetEntity> DIALGA_PET = EntityType.Builder
            .of(DialgaPetEntity::new, MobCategory.CREATURE)
            .sized(0.9F, 1.0F)
            .clientTrackingRange(10)
            .noLootTable()
            .build(key("dialga_pet"));

    private ModEntityTypes()
    {
    }

    private static ResourceKey<EntityType<?>> key(String name)
    {
        return ResourceKey.create(Registries.ENTITY_TYPE, ResourceLocation.fromNamespaceAndPath(AdvancedNetherite.MOD_ID, name));
    }
}
