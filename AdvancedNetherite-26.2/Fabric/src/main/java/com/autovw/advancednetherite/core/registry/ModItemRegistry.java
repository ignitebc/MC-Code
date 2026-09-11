package com.autovw.advancednetherite.core.registry;

import com.autovw.advancednetherite.AdvancedNetherite;
import com.autovw.advancednetherite.api.annotation.Internal;
import com.autovw.advancednetherite.core.ModItems;
import com.autovw.advancednetherite.core.ModBackpackItems;
import com.autovw.advancednetherite.core.ModRewardCouponItems;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;

/**
 * @author Autovw
 */
@Internal
public final class ModItemRegistry
{
    public static void registerItems()
    {
        Registry.register(BuiltInRegistries.ITEM, id("ash_ingot"), ModItems.ASH_INGOT);
        Registry.register(BuiltInRegistries.ITEM, id("sunlight_ingot"), ModItems.SUNLIGHT_INGOT);
        Registry.register(BuiltInRegistries.ITEM, id("soul_ingot"), ModItems.SOUL_INGOT);
        Registry.register(BuiltInRegistries.ITEM, id("frost_ingot"), ModItems.FROST_INGOT);

        Registry.register(BuiltInRegistries.ITEM, id("bitcoin"), ModItems.BITCOIN);
        Registry.register(BuiltInRegistries.ITEM, id("experience_double_coupon"), ModRewardCouponItems.EXPERIENCE_DOUBLE_COUPON);
        Registry.register(BuiltInRegistries.ITEM, id("experience_triple_coupon"), ModRewardCouponItems.EXPERIENCE_TRIPLE_COUPON);
        Registry.register(BuiltInRegistries.ITEM, id("bitcoin_double_coupon"), ModRewardCouponItems.BITCOIN_DOUBLE_COUPON);
        Registry.register(BuiltInRegistries.ITEM, id("bitcoin_triple_coupon"), ModRewardCouponItems.BITCOIN_TRIPLE_COUPON);
        Registry.register(BuiltInRegistries.ITEM, id("backpack_level_1"), ModBackpackItems.LEVEL_1);
        Registry.register(BuiltInRegistries.ITEM, id("backpack_level_2"), ModBackpackItems.LEVEL_2);
        Registry.register(BuiltInRegistries.ITEM, id("backpack_level_3"), ModBackpackItems.LEVEL_3);

        Registry.register(BuiltInRegistries.ITEM, id("random_box_i"), ModItems.RANDOM_BOX_I);
        Registry.register(BuiltInRegistries.ITEM, id("random_box_ii"), ModItems.RANDOM_BOX_II);
        Registry.register(BuiltInRegistries.ITEM, id("random_box_iii"), ModItems.RANDOM_BOX_III);
        Registry.register(BuiltInRegistries.ITEM, id("random_box_iv"), ModItems.RANDOM_BOX_IV);

        Registry.register(BuiltInRegistries.ITEM, id("reward_key_i"), ModItems.REWARD_KEY_I);
        Registry.register(BuiltInRegistries.ITEM, id("reward_key_ii"), ModItems.REWARD_KEY_II);
        Registry.register(BuiltInRegistries.ITEM, id("reward_key_iii"), ModItems.REWARD_KEY_III);
        Registry.register(BuiltInRegistries.ITEM, id("reward_key_iv"), ModItems.REWARD_KEY_IV);

        Registry.register(BuiltInRegistries.ITEM, id("enhancement_shard"), ModItems.ENHANCEMENT_SHARD);
        Registry.register(BuiltInRegistries.ITEM, id("enhancement_gem"), ModItems.ENHANCEMENT_GEM);

        Registry.register(BuiltInRegistries.ITEM, id("job_select_ticket"), ModItems.JOB_SELECT_TICKET);
        Registry.register(BuiltInRegistries.ITEM, id("death_item_protection_scroll"), ModItems.DEATH_ITEM_PROTECTION_SCROLL);
        Registry.register(BuiltInRegistries.ITEM, id("enhance_protection_scroll"), ModItems.ENHANCE_PROTECTION_SCROLL);
        Registry.register(BuiltInRegistries.ITEM, id("chunk_claim_map"), ModItems.LAND_PURCHASE_DOCUMENT);
        Registry.register(BuiltInRegistries.ITEM, id("enhance_success_scroll_3"), ModItems.ENHANCE_SUCCESS_SCROLL_3);
        Registry.register(BuiltInRegistries.ITEM, id("enhance_success_scroll_5"), ModItems.ENHANCE_SUCCESS_SCROLL_5);
        Registry.register(BuiltInRegistries.ITEM, id("enhance_success_scroll_7"), ModItems.ENHANCE_SUCCESS_SCROLL_7);
        Registry.register(BuiltInRegistries.ITEM, id("enhance_success_scroll_10"), ModItems.ENHANCE_SUCCESS_SCROLL_10);

        Registry.register(BuiltInRegistries.ITEM, id("nomal_petbox"), ModItems.NOMAL_PETBOX);
        Registry.register(BuiltInRegistries.ITEM, id("rare_petbox"), ModItems.RARE_PETBOX);
        Registry.register(BuiltInRegistries.ITEM, id("legend_petbox"), ModItems.LEGEND_PETBOX);

        Registry.register(BuiltInRegistries.ITEM, id("ash_helmet"), ModItems.ASH_HELMET);
        Registry.register(BuiltInRegistries.ITEM, id("ash_chestplate"), ModItems.ASH_CHESTPLATE);
        Registry.register(BuiltInRegistries.ITEM, id("ash_leggings"), ModItems.ASH_LEGGINGS);
        Registry.register(BuiltInRegistries.ITEM, id("ash_boots"), ModItems.ASH_BOOTS);
        Registry.register(BuiltInRegistries.ITEM, id("sunlight_helmet"), ModItems.SUNLIGHT_HELMET);
        Registry.register(BuiltInRegistries.ITEM, id("sunlight_chestplate"), ModItems.SUNLIGHT_CHESTPLATE);
        Registry.register(BuiltInRegistries.ITEM, id("sunlight_leggings"), ModItems.SUNLIGHT_LEGGINGS);
        Registry.register(BuiltInRegistries.ITEM, id("sunlight_boots"), ModItems.SUNLIGHT_BOOTS);
        Registry.register(BuiltInRegistries.ITEM, id("soul_helmet"), ModItems.SOUL_HELMET);
        Registry.register(BuiltInRegistries.ITEM, id("soul_chestplate"), ModItems.SOUL_CHESTPLATE);
        Registry.register(BuiltInRegistries.ITEM, id("soul_leggings"), ModItems.SOUL_LEGGINGS);
        Registry.register(BuiltInRegistries.ITEM, id("soul_boots"), ModItems.SOUL_BOOTS);
        Registry.register(BuiltInRegistries.ITEM, id("frost_helmet"), ModItems.FROST_HELMET);
        Registry.register(BuiltInRegistries.ITEM, id("frost_chestplate"), ModItems.FROST_CHESTPLATE);
        Registry.register(BuiltInRegistries.ITEM, id("frost_leggings"), ModItems.FROST_LEGGINGS);
        Registry.register(BuiltInRegistries.ITEM, id("frost_boots"), ModItems.FROST_BOOTS);

        Registry.register(BuiltInRegistries.ITEM, id("ash_axe"), ModItems.ASH_AXE);
        Registry.register(BuiltInRegistries.ITEM, id("sunlight_axe"), ModItems.SUNLIGHT_AXE);
        Registry.register(BuiltInRegistries.ITEM, id("soul_axe"), ModItems.SOUL_AXE);
        Registry.register(BuiltInRegistries.ITEM, id("frost_axe"), ModItems.FROST_AXE);

        Registry.register(BuiltInRegistries.ITEM, id("ash_hoe"), ModItems.ASH_HOE);
        Registry.register(BuiltInRegistries.ITEM, id("sunlight_hoe"), ModItems.SUNLIGHT_HOE);
        Registry.register(BuiltInRegistries.ITEM, id("soul_hoe"), ModItems.SOUL_HOE);
        Registry.register(BuiltInRegistries.ITEM, id("frost_hoe"), ModItems.FROST_HOE);

        Registry.register(BuiltInRegistries.ITEM, id("ash_pickaxe"), ModItems.ASH_PICKAXE);
        Registry.register(BuiltInRegistries.ITEM, id("sunlight_pickaxe"), ModItems.SUNLIGHT_PICKAXE);
        Registry.register(BuiltInRegistries.ITEM, id("soul_pickaxe"), ModItems.SOUL_PICKAXE);
        Registry.register(BuiltInRegistries.ITEM, id("frost_pickaxe"), ModItems.FROST_PICKAXE);

        Registry.register(BuiltInRegistries.ITEM, id("ash_shovel"), ModItems.ASH_SHOVEL);
        Registry.register(BuiltInRegistries.ITEM, id("sunlight_shovel"), ModItems.SUNLIGHT_SHOVEL);
        Registry.register(BuiltInRegistries.ITEM, id("soul_shovel"), ModItems.SOUL_SHOVEL);
        Registry.register(BuiltInRegistries.ITEM, id("frost_shovel"), ModItems.FROST_SHOVEL);

        Registry.register(BuiltInRegistries.ITEM, id("ash_sword"), ModItems.ASH_SWORD);
        Registry.register(BuiltInRegistries.ITEM, id("sunlight_sword"), ModItems.SUNLIGHT_SWORD);
        Registry.register(BuiltInRegistries.ITEM, id("soul_sword"), ModItems.SOUL_SWORD);
        Registry.register(BuiltInRegistries.ITEM, id("frost_sword"), ModItems.FROST_SWORD);

        Registry.register(BuiltInRegistries.ITEM, id("ash_spear"), ModItems.ASH_SPEAR);
        Registry.register(BuiltInRegistries.ITEM, id("sunlight_spear"), ModItems.SUNLIGHT_SPEAR);
        Registry.register(BuiltInRegistries.ITEM, id("soul_spear"), ModItems.SOUL_SPEAR);
        Registry.register(BuiltInRegistries.ITEM, id("frost_spear"), ModItems.FROST_SPEAR);

        Registry.register(BuiltInRegistries.ITEM, id("ash_block"), ModItems.ASH_BLOCK);
        Registry.register(BuiltInRegistries.ITEM, id("sunlight_block"), ModItems.SUNLIGHT_BLOCK);
        Registry.register(BuiltInRegistries.ITEM, id("soul_block"), ModItems.SOUL_BLOCK);
        Registry.register(BuiltInRegistries.ITEM, id("frost_block"), ModItems.FROST_BLOCK);
    }
    
    private static Identifier id(String name)
    {
        return Identifier.fromNamespaceAndPath(AdvancedNetherite.MOD_ID, name);
    }
}
