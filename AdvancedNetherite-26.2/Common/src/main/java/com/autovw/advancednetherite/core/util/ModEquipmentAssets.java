package com.autovw.advancednetherite.core.util;

import net.minecraft.client.resources.model.EquipmentClientInfo;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.equipment.EquipmentAsset;

import java.util.function.BiConsumer;

import static com.autovw.advancednetherite.AdvancedNetherite.MOD_ID;

/**
 * @author Autovw
 */
public final class ModEquipmentAssets
{
    private static final ResourceKey<? extends Registry<EquipmentAsset>> ROOT_ID = ResourceKey.createRegistryKey(Identifier.withDefaultNamespace("equipment_asset"));

    public static final ResourceKey<EquipmentAsset> ASH = id("ash");
    public static final ResourceKey<EquipmentAsset> SUNLIGHT = id("sunlight");
    public static final ResourceKey<EquipmentAsset> SOUL = id("soul");
    public static final ResourceKey<EquipmentAsset> FROST = id("frost");

    private static ResourceKey<EquipmentAsset> id(String name)
    {
        return ResourceKey.create(ROOT_ID, Identifier.fromNamespaceAndPath(MOD_ID, name));
    }

    public static void bootstrap(BiConsumer<ResourceKey<EquipmentAsset>, EquipmentClientInfo> consumer)
    {
        consumer.accept(ModEquipmentAssets.ASH, onlyHumanoid("ash"));
        consumer.accept(ModEquipmentAssets.SUNLIGHT, onlyHumanoid("sunlight"));
        consumer.accept(ModEquipmentAssets.SOUL, onlyHumanoid("soul"));
        consumer.accept(ModEquipmentAssets.FROST, onlyHumanoid("frost"));
    }

    private static EquipmentClientInfo onlyHumanoid(String name)
    {
        return EquipmentClientInfo.builder().addHumanoidLayers(Identifier.fromNamespaceAndPath(MOD_ID, name)).build();
    }
}
