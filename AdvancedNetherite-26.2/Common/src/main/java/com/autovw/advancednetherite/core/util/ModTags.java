package com.autovw.advancednetherite.core.util;

import com.autovw.advancednetherite.AdvancedNetherite;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.block.Block;

/**
 * @since 1.9.0
 * @author Autovw
 */
public final class ModTags
{
    // BLOCK TAGS
    public static final TagKey<Block> NETHERITE_BLOCKS = modBlockTag("netherite_blocks");

    public static final TagKey<Block> INCORRECT_FOR_ASH_TOOL = modBlockTag("incorrect_for_ash_tool");
    public static final TagKey<Block> INCORRECT_FOR_SUNLIGHT_TOOL = modBlockTag("incorrect_for_sunlight_tool");
    public static final TagKey<Block> INCORRECT_FOR_SOUL_TOOL = modBlockTag("incorrect_for_soul_tool");
    public static final TagKey<Block> INCORRECT_FOR_FROST_TOOL = modBlockTag("incorrect_for_frost_tool");

    // ITEM TAGS
    public static final TagKey<Item> NETHERITE_BLOCKITEMS = modItemTag("netherite_blocks");

    public static final TagKey<Item> NETHERITE_INGOTS = modItemTag("netherite_ingots");
    public static final TagKey<Item> UPGRADE_TO_FROST = modItemTag("upgrade_to_frost");
    public static final TagKey<Item> UPGRADE_TO_SOUL = modItemTag("upgrade_to_soul");
    public static final TagKey<Item> UPGRADE_TO_SUNLIGHT = modItemTag("upgrade_to_sunlight");
    public static final TagKey<Item> UPGRADE_TO_ASH = modItemTag("upgrade_to_ash");

    public static final TagKey<Item> AXE_FROST = modItemTag("axe/frost");
    public static final TagKey<Item> AXE_SOUL = modItemTag("axe/soul");
    public static final TagKey<Item> AXE_SUNLIGHT = modItemTag("axe/sunlight");
    public static final TagKey<Item> AXE_ASH = modItemTag("axe/ash");
    public static final TagKey<Item> AXE_NETHERITE = modItemTag("axe/netherite");
    public static final TagKey<Item> AXE_UPGRADE_TO_FROST = modItemTag("axe/upgrade_to_frost");
    public static final TagKey<Item> AXE_UPGRADE_TO_SOUL = modItemTag("axe/upgrade_to_soul");
    public static final TagKey<Item> AXE_UPGRADE_TO_SUNLIGHT = modItemTag("axe/upgrade_to_sunlight");
    public static final TagKey<Item> AXE_UPGRADE_TO_ASH = modItemTag("axe/upgrade_to_ash");

    public static final TagKey<Item> BOOTS_FROST = modItemTag("boots/frost");
    public static final TagKey<Item> BOOTS_SOUL = modItemTag("boots/soul");
    public static final TagKey<Item> BOOTS_SUNLIGHT = modItemTag("boots/sunlight");
    public static final TagKey<Item> BOOTS_ASH = modItemTag("boots/ash");
    public static final TagKey<Item> BOOTS_NETHERITE = modItemTag("boots/netherite");
    public static final TagKey<Item> BOOTS_UPGRADE_TO_FROST = modItemTag("boots/upgrade_to_frost");
    public static final TagKey<Item> BOOTS_UPGRADE_TO_SOUL = modItemTag("boots/upgrade_to_soul");
    public static final TagKey<Item> BOOTS_UPGRADE_TO_SUNLIGHT = modItemTag("boots/upgrade_to_sunlight");
    public static final TagKey<Item> BOOTS_UPGRADE_TO_ASH = modItemTag("boots/upgrade_to_ash");

    public static final TagKey<Item> CHESTPLATE_FROST = modItemTag("chestplate/frost");
    public static final TagKey<Item> CHESTPLATE_SOUL = modItemTag("chestplate/soul");
    public static final TagKey<Item> CHESTPLATE_SUNLIGHT = modItemTag("chestplate/sunlight");
    public static final TagKey<Item> CHESTPLATE_ASH = modItemTag("chestplate/ash");
    public static final TagKey<Item> CHESTPLATE_NETHERITE = modItemTag("chestplate/netherite");
    public static final TagKey<Item> CHESTPLATE_UPGRADE_TO_FROST = modItemTag("chestplate/upgrade_to_frost");
    public static final TagKey<Item> CHESTPLATE_UPGRADE_TO_SOUL = modItemTag("chestplate/upgrade_to_soul");
    public static final TagKey<Item> CHESTPLATE_UPGRADE_TO_SUNLIGHT = modItemTag("chestplate/upgrade_to_sunlight");
    public static final TagKey<Item> CHESTPLATE_UPGRADE_TO_ASH = modItemTag("chestplate/upgrade_to_ash");

    public static final TagKey<Item> HELMET_FROST = modItemTag("helmet/frost");
    public static final TagKey<Item> HELMET_SOUL = modItemTag("helmet/soul");
    public static final TagKey<Item> HELMET_SUNLIGHT = modItemTag("helmet/sunlight");
    public static final TagKey<Item> HELMET_ASH = modItemTag("helmet/ash");
    public static final TagKey<Item> HELMET_NETHERITE = modItemTag("helmet/netherite");
    public static final TagKey<Item> HELMET_UPGRADE_TO_FROST = modItemTag("helmet/upgrade_to_frost");
    public static final TagKey<Item> HELMET_UPGRADE_TO_SOUL = modItemTag("helmet/upgrade_to_soul");
    public static final TagKey<Item> HELMET_UPGRADE_TO_SUNLIGHT = modItemTag("helmet/upgrade_to_sunlight");
    public static final TagKey<Item> HELMET_UPGRADE_TO_ASH = modItemTag("helmet/upgrade_to_ash");

    public static final TagKey<Item> HOE_FROST = modItemTag("hoe/frost");
    public static final TagKey<Item> HOE_SOUL = modItemTag("hoe/soul");
    public static final TagKey<Item> HOE_SUNLIGHT = modItemTag("hoe/sunlight");
    public static final TagKey<Item> HOE_ASH = modItemTag("hoe/ash");
    public static final TagKey<Item> HOE_NETHERITE = modItemTag("hoe/netherite");
    public static final TagKey<Item> HOE_UPGRADE_TO_FROST = modItemTag("hoe/upgrade_to_frost");
    public static final TagKey<Item> HOE_UPGRADE_TO_SOUL = modItemTag("hoe/upgrade_to_soul");
    public static final TagKey<Item> HOE_UPGRADE_TO_SUNLIGHT = modItemTag("hoe/upgrade_to_sunlight");
    public static final TagKey<Item> HOE_UPGRADE_TO_ASH = modItemTag("hoe/upgrade_to_ash");

    public static final TagKey<Item> INGOTS_NETHERITES_DIAMOND = modItemTag("ingot/netherites_diamond");
    public static final TagKey<Item> INGOTS_NETHERITES_EMERALD = modItemTag("ingot/netherites_emerald");
    public static final TagKey<Item> INGOTS_NETHERITES_GOLD = modItemTag("ingot/netherites_gold");
    public static final TagKey<Item> INGOTS_NETHERITES_IRON = modItemTag("ingot/netherites_iron");
    public static final TagKey<Item> INGOTS_NETHERITES = modItemTag("ingot/netherites");
    public static final TagKey<Item> INGOTS_UPGRADE_TO_FROST = modItemTag("ingot/upgrade_to_frost");
    public static final TagKey<Item> INGOTS_UPGRADE_TO_SOUL = modItemTag("ingot/upgrade_to_soul");
    public static final TagKey<Item> INGOTS_UPGRADE_TO_SUNLIGHT = modItemTag("ingot/upgrade_to_sunlight");
    public static final TagKey<Item> INGOTS_UPGRADE_TO_ASH = modItemTag("ingot/upgrade_to_ash");

    public static final TagKey<Item> LEGGINGS_FROST = modItemTag("leggings/frost");
    public static final TagKey<Item> LEGGINGS_SOUL = modItemTag("leggings/soul");
    public static final TagKey<Item> LEGGINGS_SUNLIGHT = modItemTag("leggings/sunlight");
    public static final TagKey<Item> LEGGINGS_ASH = modItemTag("leggings/ash");
    public static final TagKey<Item> LEGGINGS_NETHERITE = modItemTag("leggings/netherite");
    public static final TagKey<Item> LEGGINGS_UPGRADE_TO_FROST = modItemTag("leggings/upgrade_to_frost");
    public static final TagKey<Item> LEGGINGS_UPGRADE_TO_SOUL = modItemTag("leggings/upgrade_to_soul");
    public static final TagKey<Item> LEGGINGS_UPGRADE_TO_SUNLIGHT = modItemTag("leggings/upgrade_to_sunlight");
    public static final TagKey<Item> LEGGINGS_UPGRADE_TO_ASH = modItemTag("leggings/upgrade_to_ash");

    public static final TagKey<Item> PICKAXE_FROST = modItemTag("pickaxe/frost");
    public static final TagKey<Item> PICKAXE_SOUL = modItemTag("pickaxe/soul");
    public static final TagKey<Item> PICKAXE_SUNLIGHT = modItemTag("pickaxe/sunlight");
    public static final TagKey<Item> PICKAXE_ASH = modItemTag("pickaxe/ash");
    public static final TagKey<Item> PICKAXE_NETHERITE = modItemTag("pickaxe/netherite");
    public static final TagKey<Item> PICKAXE_UPGRADE_TO_FROST = modItemTag("pickaxe/upgrade_to_frost");
    public static final TagKey<Item> PICKAXE_UPGRADE_TO_SOUL = modItemTag("pickaxe/upgrade_to_soul");
    public static final TagKey<Item> PICKAXE_UPGRADE_TO_SUNLIGHT = modItemTag("pickaxe/upgrade_to_sunlight");
    public static final TagKey<Item> PICKAXE_UPGRADE_TO_ASH = modItemTag("pickaxe/upgrade_to_ash");

    public static final TagKey<Item> SHOVEL_FROST = modItemTag("shovel/frost");
    public static final TagKey<Item> SHOVEL_SOUL = modItemTag("shovel/soul");
    public static final TagKey<Item> SHOVEL_SUNLIGHT = modItemTag("shovel/sunlight");
    public static final TagKey<Item> SHOVEL_ASH = modItemTag("shovel/ash");
    public static final TagKey<Item> SHOVEL_NETHERITE = modItemTag("shovel/netherite");
    public static final TagKey<Item> SHOVEL_UPGRADE_TO_FROST = modItemTag("shovel/upgrade_to_frost");
    public static final TagKey<Item> SHOVEL_UPGRADE_TO_SOUL = modItemTag("shovel/upgrade_to_soul");
    public static final TagKey<Item> SHOVEL_UPGRADE_TO_SUNLIGHT = modItemTag("shovel/upgrade_to_sunlight");
    public static final TagKey<Item> SHOVEL_UPGRADE_TO_ASH = modItemTag("shovel/upgrade_to_ash");

    public static final TagKey<Item> SPEAR_FROST = modItemTag("spear/frost");
    public static final TagKey<Item> SPEAR_SOUL = modItemTag("spear/soul");
    public static final TagKey<Item> SPEAR_SUNLIGHT = modItemTag("spear/sunlight");
    public static final TagKey<Item> SPEAR_ASH = modItemTag("spear/ash");
    public static final TagKey<Item> SPEAR_NETHERITE = modItemTag("spear/netherite");
    public static final TagKey<Item> SPEAR_UPGRADE_TO_FROST = modItemTag("spear/upgrade_to_frost");
    public static final TagKey<Item> SPEAR_UPGRADE_TO_SOUL = modItemTag("spear/upgrade_to_soul");
    public static final TagKey<Item> SPEAR_UPGRADE_TO_SUNLIGHT = modItemTag("spear/upgrade_to_sunlight");
    public static final TagKey<Item> SPEAR_UPGRADE_TO_ASH = modItemTag("spear/upgrade_to_ash");

    public static final TagKey<Item> SWORD_FROST = modItemTag("sword/frost");
    public static final TagKey<Item> SWORD_SOUL = modItemTag("sword/soul");
    public static final TagKey<Item> SWORD_SUNLIGHT = modItemTag("sword/sunlight");
    public static final TagKey<Item> SWORD_ASH = modItemTag("sword/ash");
    public static final TagKey<Item> SWORD_NETHERITE = modItemTag("sword/netherite");
    public static final TagKey<Item> SWORD_UPGRADE_TO_FROST = modItemTag("sword/upgrade_to_frost");
    public static final TagKey<Item> SWORD_UPGRADE_TO_SOUL = modItemTag("sword/upgrade_to_soul");
    public static final TagKey<Item> SWORD_UPGRADE_TO_SUNLIGHT = modItemTag("sword/upgrade_to_sunlight");
    public static final TagKey<Item> SWORD_UPGRADE_TO_ASH = modItemTag("sword/upgrade_to_ash");

    // "tiers" tags
    public static final TagKey<Item> TIER_ARMOR = modItemTag("tier/armor");
    public static final TagKey<Item> TIER_TOOLS = modItemTag("tier/tools");

    public static final TagKey<Item> TIER_ARMOR_FROST = modItemTag("tier/armor/frost");
    public static final TagKey<Item> TIER_ARMOR_SOUL = modItemTag("tier/armor/soul");
    public static final TagKey<Item> TIER_ARMOR_SUNLIGHT = modItemTag("tier/armor/sunlight");
    public static final TagKey<Item> TIER_ARMOR_ASH = modItemTag("tier/armor/ash");

    public static final TagKey<Item> TIER_TOOL_FROST = modItemTag("tier/tool/frost");
    public static final TagKey<Item> TIER_TOOL_SOUL = modItemTag("tier/tool/soul");
    public static final TagKey<Item> TIER_TOOL_SUNLIGHT = modItemTag("tier/tool/sunlight");
    public static final TagKey<Item> TIER_TOOL_ASH = modItemTag("tier/tool/ash");

    // REPAIR TAGS
    public static final TagKey<Item> REPAIRS_ASH_ARMOR = modItemTag("repairs_ash_armor");
    public static final TagKey<Item> REPAIRS_SUNLIGHT_ARMOR = modItemTag("repairs_sunlight_armor");
    public static final TagKey<Item> REPAIRS_SOUL_ARMOR = modItemTag("repairs_soul_armor");
    public static final TagKey<Item> REPAIRS_FROST_ARMOR = modItemTag("repairs_frost_armor");

    public static final TagKey<Item> REPAIRS_ASH_TOOLS = modItemTag("repairs_ash_tools");
    public static final TagKey<Item> REPAIRS_SUNLIGHT_TOOLS = modItemTag("repairs_sunlight_tools");
    public static final TagKey<Item> REPAIRS_SOUL_TOOLS = modItemTag("repairs_soul_tools");
    public static final TagKey<Item> REPAIRS_FROST_TOOLS = modItemTag("repairs_frost_tools");

    // "pacify armor" tags
    public static final TagKey<Item> PACIFY_PHANTOMS_ARMOR = modItemTag("pacify_phantoms_armor");
    public static final TagKey<Item> PACIFY_PIGLINS_ARMOR = modItemTag("pacify_piglins_armor");
    public static final TagKey<Item> PACIFY_ENDERMEN_ARMOR = modItemTag("pacify_endermen_armor");

    // additional drop tags
    public static final TagKey<Item> DROPS_ADDITIONAL_CROPS = modItemTag("drops_additional_crops");

    public static final TagKey<Item> DROPS_ADDITIONAL_IRON = modItemTag("drops_additional_iron");
    public static final TagKey<Item> DROPS_ADDITIONAL_GOLD = modItemTag("drops_additional_gold");
    public static final TagKey<Item> DROPS_ADDITIONAL_EMERALD = modItemTag("drops_additional_emerald");
    public static final TagKey<Item> DROPS_ADDITIONAL_DIAMOND = modItemTag("drops_additional_diamond");

    public static final TagKey<Item> DROPS_ADDITIONAL_PHANTOM_LOOT = modItemTag("drops_additional_phantom_loot");
    public static final TagKey<Item> DROPS_ADDITIONAL_ZOMBIFIED_PIGLIN_LOOT = modItemTag("drops_additional_zombified_piglin_loot");
    public static final TagKey<Item> DROPS_ADDITIONAL_PIGLIN_LOOT = modItemTag("drops_additional_piglin_loot");
    public static final TagKey<Item> DROPS_ADDITIONAL_ENDERMAN_LOOT = modItemTag("drops_additional_enderman_loot");

    // ENCHANTMENT TAGS
    public static final TagKey<Enchantment> PREVENTS_ADDITIONAL_ORE_DROPS = modEnchantmentTag("prevents_additional_ore_drops");

    /**
     * @param tagLoc Resource location of the tag
     * @return Block tag
     */
    public static TagKey<Block> blockTag(Identifier tagLoc)
    {
        return TagKey.create(Registries.BLOCK, tagLoc);
    }

    /**
     * @param tagLoc Resource location of the tag
     * @return Item tag
     */
    public static TagKey<Item> itemTag(Identifier tagLoc)
    {
        return TagKey.create(Registries.ITEM, tagLoc);
    }

    /**
     * @param tagLoc Resource location of the tag
     * @return Enchantment tag
     */
    public static TagKey<Enchantment> enchantmentTag(Identifier tagLoc)
    {
        return TagKey.create(Registries.ENCHANTMENT, tagLoc);
    }

    private static TagKey<Block> modBlockTag(String tagName)
    {
        return ModTags.blockTag(Identifier.fromNamespaceAndPath(AdvancedNetherite.MOD_ID, tagName));
    }

    private static TagKey<Item> modItemTag(String tagName)
    {
        return ModTags.itemTag(Identifier.fromNamespaceAndPath(AdvancedNetherite.MOD_ID, tagName));
    }

    private static TagKey<Enchantment> modEnchantmentTag(String tagName)
    {
        return ModTags.enchantmentTag(Identifier.fromNamespaceAndPath(AdvancedNetherite.MOD_ID, tagName));
    }
}
