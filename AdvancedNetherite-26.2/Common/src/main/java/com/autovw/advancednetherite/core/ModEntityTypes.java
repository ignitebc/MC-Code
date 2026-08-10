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
            .noSave()
            .build(key("dialga_pet"));

    public static final EntityType<DialgaPetEntity> UNICORN_PET = EntityType.Builder
            .of(DialgaPetEntity::new, MobCategory.CREATURE)
            .sized(1.1F, 1.5F)
            .clientTrackingRange(10)
            .fireImmune()
            .noLootTable()
            .noSave()
            .build(key("unicorn_pet"));

    public static final EntityType<DialgaPetEntity> FAIRLINS_PET = EntityType.Builder
            .of(DialgaPetEntity::new, MobCategory.CREATURE)
            .sized(1.0F, 0.8F)
            .clientTrackingRange(10)
            .fireImmune()
            .noLootTable()
            .noSave()
            .build(key("fairlins_pet"));

    public static final EntityType<DialgaPetEntity> DARK_DRAGON_PET = EntityType.Builder
            .of(DialgaPetEntity::new, MobCategory.CREATURE)
            .sized(1.3F, 1.1F)
            .clientTrackingRange(10)
            .fireImmune()
            .noLootTable()
            .noSave()
            .build(key("dark_dragon_pet"));

    public static final EntityType<DialgaPetEntity> KIRBY_PET = EntityType.Builder
            .of(DialgaPetEntity::new, MobCategory.CREATURE)
            .sized(0.6F, 0.7F)
            .clientTrackingRange(10)
            .fireImmune()
            .noLootTable()
            .noSave()
            .build(key("kirby_pet"));

    public static final EntityType<DialgaPetEntity> GAZELLE_PET = EntityType.Builder
            .of(DialgaPetEntity::new, MobCategory.CREATURE)
            .sized(0.8F, 1.1F)
            .clientTrackingRange(10)
            .fireImmune()
            .noLootTable()
            .noSave()
            .build(key("gazelle_pet"));

    public static final EntityType<DialgaPetEntity> SCULKEN_RAVEN_PET = EntityType.Builder
            .of(DialgaPetEntity::new, MobCategory.CREATURE)
            .sized(1.2F, 1.2F)
            .clientTrackingRange(10)
            .fireImmune()
            .noLootTable()
            .noSave()
            .build(key("sculken_raven_pet"));

    public static final EntityType<DialgaPetEntity> GOMI_PET = EntityType.Builder
            .of(DialgaPetEntity::new, MobCategory.CREATURE)
            .sized(0.8F, 1.0F)
            .clientTrackingRange(10)
            .fireImmune()
            .noLootTable()
            .noSave()
            .build(key("gomi_pet"));

    public static final EntityType<DialgaPetEntity> SUPER_GOMI_PET = EntityType.Builder
            .of(DialgaPetEntity::new, MobCategory.CREATURE)
            .sized(1.2F, 1.2F)
            .clientTrackingRange(10)
            .fireImmune()
            .noLootTable()
            .noSave()
            .build(key("super_gomi_pet"));

    private ModEntityTypes()
    {
    }

    private static ResourceKey<EntityType<?>> key(String name)
    {
        return ResourceKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(AdvancedNetherite.MOD_ID, name));
    }
}
