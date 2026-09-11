package com.autovw.advancednetherite.core.util;

import net.minecraft.util.Util;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.item.equipment.EquipmentAsset;

import java.util.EnumMap;

/**
 * @author Autovw
 */
public final class ModArmorMaterials
{
    // 25.12.08 jjh, 수치 변경은 여기서 +1씩 더 증가하도록
    // durability : 내구도
    // enchantability : 인챈트 효율
    // toughness : 강한 공격에서 방어관통을 줄여주는 능력
    // knockbackResistance : 넉백 저항

    public static final ArmorMaterial ASH = register(37, Util.make(new EnumMap<>(ArmorType.class), (attribute) -> {
        attribute.put(ArmorType.BOOTS, 4);   
        attribute.put(ArmorType.LEGGINGS, 7);
        attribute.put(ArmorType.CHESTPLATE, 9);
        attribute.put(ArmorType.HELMET, 4);
        attribute.put(ArmorType.BODY, 11);
    }), 20, 3.5F, 0.06F, ModTags.REPAIRS_ASH_ARMOR, ModEquipmentAssets.ASH);
    public static final ArmorMaterial SUNLIGHT = register(38, Util.make(new EnumMap<>(ArmorType.class), (attribute) -> {
        attribute.put(ArmorType.BOOTS, 5);
        attribute.put(ArmorType.LEGGINGS, 8);
        attribute.put(ArmorType.CHESTPLATE, 10);
        attribute.put(ArmorType.HELMET, 5);
        attribute.put(ArmorType.BODY, 12);
    }), 25, 4.0F, 0.07F, ModTags.REPAIRS_SUNLIGHT_ARMOR, ModEquipmentAssets.SUNLIGHT);
    public static final ArmorMaterial SOUL = register(39, Util.make(new EnumMap<>(ArmorType.class), (attribute) -> {
        attribute.put(ArmorType.BOOTS, 6);
        attribute.put(ArmorType.LEGGINGS, 9);
        attribute.put(ArmorType.CHESTPLATE, 11);
        attribute.put(ArmorType.HELMET, 6);
        attribute.put(ArmorType.BODY, 13);
    }), 30, 4.5F, 0.08F, ModTags.REPAIRS_SOUL_ARMOR, ModEquipmentAssets.SOUL);
    public static final ArmorMaterial FROST = register(40, Util.make(new EnumMap<>(ArmorType.class), (attribute) -> {
        attribute.put(ArmorType.BOOTS, 7);
        attribute.put(ArmorType.LEGGINGS, 10);
        attribute.put(ArmorType.CHESTPLATE, 12);
        attribute.put(ArmorType.HELMET, 7);
        attribute.put(ArmorType.BODY, 14);
    }), 35, 5.0F, 0.09F, ModTags.REPAIRS_FROST_ARMOR, ModEquipmentAssets.FROST);

    /**
     * @param typeProtections       The amount of protection per slot
     * @param enchantability        The higher the number, the more likely better enchantments will be applied when using the enchanting table
     * @param toughness             Toughness for netherite armor
     * @param knockbackResistance   The knockback resistance for armor
     * @return Registered armor material
     */
    private static ArmorMaterial register(int durability, EnumMap<ArmorType, Integer> typeProtections, int enchantability, float toughness, float knockbackResistance, TagKey<Item> repairIngredient, ResourceKey<EquipmentAsset> equipmentAsset)
    {
        Holder<SoundEvent> equipSound = SoundEvents.ARMOR_EQUIP_NETHERITE;

        EnumMap<ArmorType, Integer> typeMap = new EnumMap<>(ArmorType.class);
        for (ArmorType type : ArmorType.values())
        {
            typeMap.put(type, typeProtections.get(type));
        }

        return new ArmorMaterial(durability, typeProtections, enchantability, equipSound, toughness, knockbackResistance, repairIngredient, equipmentAsset);
    }

    private ModArmorMaterials()
    {
    }
}
