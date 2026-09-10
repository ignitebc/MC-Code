package com.autovw.advancednetherite.core;

import com.autovw.advancednetherite.AdvancedNetherite;
import com.autovw.advancednetherite.common.item.*;
import com.autovw.advancednetherite.core.util.ModArmorMaterials;
import com.autovw.advancednetherite.core.util.ModToolMaterials;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.equipment.ArmorType;

import java.util.List;

/**
 * @author Autovw
 */
public final class ModItems
{
    // Ingots
    public static final AdvancedItem ASH_INGOT = new AdvancedItem(new Item.Properties().setId(key("ash_ingot")));
    public static final AdvancedItem SOLAR_INGOT = new AdvancedItem(new Item.Properties().setId(key("solar_ingot")));
    public static final AdvancedItem SOUL_INGOT = new AdvancedItem(new Item.Properties().setId(key("soul_ingot")));
    public static final AdvancedItem FROST_INGOT = new AdvancedItem(new Item.Properties().setId(key("frost_ingot")));
    
    //bitCoin 등록
    public static final AdvancedItem BITCOIN = new AdvancedItem(new Item.Properties().setId(key("bitcoin")));
    
    //randomBox 등록
    public static final RandomBoxItem RANDOM_BOX_I = new RandomBoxItem(new Item.Properties().setId(key("random_box_i")),Identifier.fromNamespaceAndPath(AdvancedNetherite.MOD_ID, "randombox_i"));
    public static final RandomBoxItem RANDOM_BOX_II = new RandomBoxItem(new Item.Properties().setId(key("random_box_ii")),Identifier.fromNamespaceAndPath(AdvancedNetherite.MOD_ID, "randombox_ii"));
    public static final RandomBoxItem RANDOM_BOX_III = new RandomBoxItem(new Item.Properties().setId(key("random_box_iii")),Identifier.fromNamespaceAndPath(AdvancedNetherite.MOD_ID, "randombox_iii"));
    public static final RandomBoxItem RANDOM_BOX_IV = new RandomBoxItem(new Item.Properties().setId(key("random_box_iv")),Identifier.fromNamespaceAndPath(AdvancedNetherite.MOD_ID, "randombox_iv"));
    
    //rewardKey 등록
    public static final AdvancedItem REWARD_KEY_I = new AdvancedItem(new Item.Properties().setId(key("reward_key_i")));
    public static final AdvancedItem REWARD_KEY_II = new AdvancedItem(new Item.Properties().setId(key("reward_key_ii")));
    public static final AdvancedItem REWARD_KEY_III = new AdvancedItem(new Item.Properties().setId(key("reward_key_iii")));
    public static final AdvancedItem REWARD_KEY_IV = new AdvancedItem(new Item.Properties().setId(key("reward_key_iv")));

    // 강화조각
    public static final AdvancedItem ENHANCEMENT_SHARD = new AdvancedItem(new Item.Properties().setId(key("enhancement_shard")));
    public static final AdvancedItem ENHANCEMENT_GEM = new AdvancedItem(new Item.Properties().setId(key("enhancement_gem")));
    
    // 직업선택권 외 주문서
    public static final AdvancedItem JOB_SELECT_TICKET = new AdvancedItem(new Item.Properties().setId(key("job_select_ticket")));
    public static final AdvancedItem DEATH_ITEM_PROTECTION_SCROLL = new AdvancedItem(new Item.Properties().setId(key("death_item_protection_scroll")));
    public static final AdvancedItem ENHANCE_PROTECTION_SCROLL = new AdvancedItem(new Item.Properties().setId(key("enhance_protection_scroll")));
    public static final AdvancedItem LAND_PURCHASE_DOCUMENT = new AdvancedItem(new Item.Properties().setId(key("chunk_claim_map")));
    public static final AdvancedItem ENHANCE_SUCCESS_SCROLL_3 = new AdvancedItem(new Item.Properties().setId(key("enhance_success_scroll_3")));
    public static final AdvancedItem ENHANCE_SUCCESS_SCROLL_5 = new AdvancedItem(new Item.Properties().setId(key("enhance_success_scroll_5")));
    public static final AdvancedItem ENHANCE_SUCCESS_SCROLL_7 = new AdvancedItem(new Item.Properties().setId(key("enhance_success_scroll_7")));
    public static final AdvancedItem ENHANCE_SUCCESS_SCROLL_10 = new AdvancedItem(new Item.Properties().setId(key("enhance_success_scroll_10")));

    // petbox
    public static final PetBoxItem NOMAL_PETBOX = new PetBoxItem(List.of(() -> ModEntityTypes.DIALGA_PET, () -> ModEntityTypes.KIRBY_PET, () -> ModEntityTypes.GOMI_PET), 1.0, new Item.Properties().setId(key("nomal_petbox")));
    public static final PetBoxItem RARE_PETBOX = new PetBoxItem(List.of(() -> ModEntityTypes.UNICORN_PET, () -> ModEntityTypes.GAZELLE_PET), 3.0, new Item.Properties().setId(key("rare_petbox")));
    public static final PetBoxItem LEGEND_PETBOX = new PetBoxItem(List.of(() -> ModEntityTypes.FAIRLINS_PET, () -> ModEntityTypes.DARK_DRAGON_PET, () -> ModEntityTypes.SCULKEN_RAVEN_PET, () -> ModEntityTypes.SUPER_GOMI_PET), 5.0, new Item.Properties().setId(key("legend_petbox")));

    // ARMOR SETS
    // Netherite-Iron
    public static final AdvancedArmorItem ASH_HELMET = new AdvancedArmorItem(ModArmorMaterials.ASH, ArmorType.HELMET, new Item.Properties().setId(key("ash_helmet")));
    public static final AdvancedArmorItem ASH_CHESTPLATE = new AdvancedArmorItem(ModArmorMaterials.ASH, ArmorType.CHESTPLATE, new Item.Properties().setId(key("ash_chestplate")));
    public static final AdvancedArmorItem ASH_LEGGINGS = new AdvancedArmorItem(ModArmorMaterials.ASH, ArmorType.LEGGINGS, new Item.Properties().setId(key("ash_leggings")));
    public static final AdvancedArmorItem ASH_BOOTS = new AdvancedArmorItem(ModArmorMaterials.ASH, ArmorType.BOOTS, new Item.Properties().setId(key("ash_boots")));

    // Netherite-Gold
    public static final AdvancedArmorItem SOLAR_HELMET = new AdvancedArmorItem(ModArmorMaterials.SOLAR, ArmorType.HELMET, new Item.Properties().setId(key("solar_helmet")));
    public static final AdvancedArmorItem SOLAR_CHESTPLATE = new AdvancedArmorItem(ModArmorMaterials.SOLAR, ArmorType.CHESTPLATE, new Item.Properties().setId(key("solar_chestplate")));
    public static final AdvancedArmorItem SOLAR_LEGGINGS = new AdvancedArmorItem(ModArmorMaterials.SOLAR, ArmorType.LEGGINGS, new Item.Properties().setId(key("solar_leggings")));
    public static final AdvancedArmorItem SOLAR_BOOTS = new AdvancedArmorItem(ModArmorMaterials.SOLAR, ArmorType.BOOTS, new Item.Properties().setId(key("solar_boots")));

    // Netherite-Emerald
    public static final AdvancedArmorItem SOUL_HELMET = new AdvancedArmorItem(ModArmorMaterials.SOUL, ArmorType.HELMET, new Item.Properties().setId(key("soul_helmet")));
    public static final AdvancedArmorItem SOUL_CHESTPLATE = new AdvancedArmorItem(ModArmorMaterials.SOUL, ArmorType.CHESTPLATE, new Item.Properties().setId(key("soul_chestplate")));
    public static final AdvancedArmorItem SOUL_LEGGINGS = new AdvancedArmorItem(ModArmorMaterials.SOUL, ArmorType.LEGGINGS, new Item.Properties().setId(key("soul_leggings")));
    public static final AdvancedArmorItem SOUL_BOOTS = new AdvancedArmorItem(ModArmorMaterials.SOUL, ArmorType.BOOTS, new Item.Properties().setId(key("soul_boots")));

    // Netherite-Diamond
    public static final AdvancedArmorItem FROST_HELMET = new AdvancedArmorItem(ModArmorMaterials.FROST, ArmorType.HELMET, new Item.Properties().setId(key("frost_helmet")));
    public static final AdvancedArmorItem FROST_CHESTPLATE = new AdvancedArmorItem(ModArmorMaterials.FROST, ArmorType.CHESTPLATE, new Item.Properties().setId(key("frost_chestplate")));
    public static final AdvancedArmorItem FROST_LEGGINGS = new AdvancedArmorItem(ModArmorMaterials.FROST, ArmorType.LEGGINGS, new Item.Properties().setId(key("frost_leggings")));
    public static final AdvancedArmorItem FROST_BOOTS = new AdvancedArmorItem(ModArmorMaterials.FROST, ArmorType.BOOTS, new Item.Properties().setId(key("frost_boots")));

    // TOOLS
    // Axes
    public static final AdvancedAxeItem ASH_AXE = new AdvancedAxeItem(ModToolMaterials.ASH, 5, -3.0f, new Item.Properties().setId(key("ash_axe")));
    public static final AdvancedAxeItem SOLAR_AXE = new AdvancedAxeItem(ModToolMaterials.SOLAR, 6, -3.0f, new Item.Properties().setId(key("solar_axe")));
    public static final AdvancedAxeItem SOUL_AXE = new AdvancedAxeItem(ModToolMaterials.SOUL, 6, -3.0f, new Item.Properties().setId(key("soul_axe")));
    public static final AdvancedAxeItem FROST_AXE = new AdvancedAxeItem(ModToolMaterials.FROST, 7, -3.0f, new Item.Properties().setId(key("frost_axe")));

    // Hoes
    public static final AdvancedHoeItem ASH_HOE = new AdvancedHoeItem(ModToolMaterials.ASH, -4, 0.0F, new Item.Properties().setId(key("ash_hoe")));
    public static final AdvancedHoeItem SOLAR_HOE = new AdvancedHoeItem(ModToolMaterials.SOLAR, -4, 0.0F, new Item.Properties().setId(key("solar_hoe")));
    public static final AdvancedHoeItem SOUL_HOE = new AdvancedHoeItem(ModToolMaterials.SOUL, -5, 0.0F, new Item.Properties().setId(key("soul_hoe")));
    public static final AdvancedHoeItem FROST_HOE = new AdvancedHoeItem(ModToolMaterials.FROST, -5, 0.0F, new Item.Properties().setId(key("frost_hoe")));

    // Pickaxes
    public static final AdvancedPickaxeItem ASH_PICKAXE = new AdvancedPickaxeItem(ModToolMaterials.ASH, 1, -2.8F, new Item.Properties().setId(key("ash_pickaxe")));
    public static final AdvancedPickaxeItem SOLAR_PICKAXE = new AdvancedPickaxeItem(ModToolMaterials.SOLAR, 1, -2.8F, new Item.Properties().setId(key("solar_pickaxe")));
    public static final AdvancedPickaxeItem SOUL_PICKAXE = new AdvancedPickaxeItem(ModToolMaterials.SOUL, 1, -2.8F, new Item.Properties().setId(key("soul_pickaxe")));
    public static final AdvancedPickaxeItem FROST_PICKAXE = new AdvancedPickaxeItem(ModToolMaterials.FROST, 1, -2.8F, new Item.Properties().setId(key("frost_pickaxe")));

    // Shovels
    public static final AdvancedShovelItem ASH_SHOVEL = new AdvancedShovelItem(ModToolMaterials.ASH, 1.5F, -3.0F, new Item.Properties().setId(key("ash_shovel")));
    public static final AdvancedShovelItem SOLAR_SHOVEL = new AdvancedShovelItem(ModToolMaterials.SOLAR, 1.5F, -3.0F, new Item.Properties().setId(key("solar_shovel")));
    public static final AdvancedShovelItem SOUL_SHOVEL = new AdvancedShovelItem(ModToolMaterials.SOUL, 1, -3.0F, new Item.Properties().setId(key("soul_shovel")));
    public static final AdvancedShovelItem FROST_SHOVEL = new AdvancedShovelItem(ModToolMaterials.FROST, 1, -3.0F, new Item.Properties().setId(key("frost_shovel")));

    // Swords
    public static final AdvancedSwordItem ASH_SWORD = new AdvancedSwordItem(ModToolMaterials.ASH, 3, -2.4F, new Item.Properties().setId(key("ash_sword")));
    public static final AdvancedSwordItem SOLAR_SWORD = new AdvancedSwordItem(ModToolMaterials.SOLAR, 4, -2.4F, new Item.Properties().setId(key("solar_sword")));
    public static final AdvancedSwordItem SOUL_SWORD = new AdvancedSwordItem(ModToolMaterials.SOUL, 4, -2.4F, new Item.Properties().setId(key("soul_sword")));
    public static final AdvancedSwordItem FROST_SWORD = new AdvancedSwordItem(ModToolMaterials.FROST, 5, -2.4F, new Item.Properties().setId(key("frost_sword")));

    // Spears
    public static final Item ASH_SPEAR = new AdvancedSpearItem(ModToolMaterials.ASH, 1.1F, 1.25F, 0.4F, 2.5F, 7.0F, 5.5F, 5.1F, 8.75F, 4.6F, new Item.Properties().setId(key("ash_spear")));
    public static final Item SOLAR_SPEAR = new AdvancedSpearItem(ModToolMaterials.SOLAR, 1.05F, 1.05F, 0.4F, 2.5F, 7.0F, 5.5F, 5.1F, 8.75F, 4.6F, new Item.Properties().setId(key("solar_spear")));
    public static final Item SOUL_SPEAR = new AdvancedSpearItem(ModToolMaterials.SOUL, 1.35F, 1.15F, 0.4F, 2.5F, 7.0F, 5.5F, 5.1F, 8.75F, 4.6F, new Item.Properties().setId(key("soul_spear")));
    public static final Item FROST_SPEAR = new AdvancedSpearItem(ModToolMaterials.FROST, 1.25F, 1.25F, 0.4F, 2.5F, 7.0F, 5.5F, 5.1F, 8.75F, 4.6F, new Item.Properties().setId(key("frost_spear")));

    // Blocks
    public static final AdvancedBlockItem ASH_BLOCK = new AdvancedBlockItem(ModBlocks.ASH_BLOCK, new Item.Properties().useBlockDescriptionPrefix().setId(key("ash_block")));
    public static final AdvancedBlockItem SOLAR_BLOCK = new AdvancedBlockItem(ModBlocks.SOLAR_BLOCK, new Item.Properties().useBlockDescriptionPrefix().setId(key("solar_block")));
    public static final AdvancedBlockItem SOUL_BLOCK = new AdvancedBlockItem(ModBlocks.SOUL_BLOCK, new Item.Properties().useBlockDescriptionPrefix().setId(key("soul_block")));
    public static final AdvancedBlockItem FROST_BLOCK = new AdvancedBlockItem(ModBlocks.FROST_BLOCK, new Item.Properties().useBlockDescriptionPrefix().setId(key("frost_block")));

    private static ResourceKey<Item> key(String name)
    {
        return ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(AdvancedNetherite.MOD_ID, name));
    }
}
