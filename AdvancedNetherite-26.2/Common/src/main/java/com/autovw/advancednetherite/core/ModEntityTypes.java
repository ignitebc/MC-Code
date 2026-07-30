package com.autovw.advancednetherite.core;

import com.autovw.advancednetherite.AdvancedNetherite;
import com.autovw.advancednetherite.common.entity.DialgaPetEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

public final class ModEntityTypes
{
    public static final EntityType<DialgaPetEntity> DIALGA_PET = EntityType.Builder
            .of(DialgaPetEntity::new, MobCategory.CREATURE)
            .sized(0.9F, 1.0F)
            .clientTrackingRange(10)
            .fireImmune()
            .noLootTable()
            .build(key("dialga_pet"));

    public static final EntityType<DialgaPetEntity> DRAGOON_PET = EntityType.Builder
            .of(DialgaPetEntity::new, MobCategory.CREATURE)
            .sized(1.2F, 0.8F)
            .clientTrackingRange(10)
            .fireImmune()
            .noLootTable()
            .build(key("dragoon_pet"));

    public static final EntityType<DialgaPetEntity> FAIRLINS_PET = EntityType.Builder
            .of(DialgaPetEntity::new, MobCategory.CREATURE)
            .sized(1.0F, 0.8F)
            .clientTrackingRange(10)
            .fireImmune()
            .noLootTable()
            .build(key("fairlins_pet"));

    public static final EntityType<DialgaPetEntity> DARK_DRAGON_PET = EntityType.Builder
            .of(DialgaPetEntity::new, MobCategory.CREATURE)
            .sized(1.3F, 1.1F)
            .clientTrackingRange(10)
            .fireImmune()
            .noLootTable()
            .build(key("dark_dragon_pet"));

    private ModEntityTypes()
    {
    }

    private static ResourceKey<EntityType<?>> key(String name)
    {
        return ResourceKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(AdvancedNetherite.MOD_ID, name));
    }
}
