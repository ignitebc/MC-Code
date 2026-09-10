package com.autovw.advancednetherite;

import com.autovw.advancednetherite.api.annotation.Internal;
import com.autovw.advancednetherite.common.item.AdvancedItem;
import com.autovw.advancednetherite.core.ModItems;
import com.autovw.advancednetherite.core.ModRewardCouponItems;
import com.autovw.advancednetherite.core.ModBackpackItems;
import net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTab;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;

/**
 * @author Autovw
 */
public final class AdvancedNetheriteTab
{
    /**
     * Creative tab for Advanced Netherite
     */
    @Internal
    public static void registerTab()
    {
        ResourceKey<CreativeModeTab> tab = ResourceKey.create(Registries.CREATIVE_MODE_TAB, Identifier.fromNamespaceAndPath(AdvancedNetherite.MOD_ID, "tab"));
        Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, tab, FabricCreativeModeTab.builder()
                .icon(ModItems.SOLAR_INGOT::getDefaultInstance)
                .title(Component.translatable("itemGroup." + AdvancedNetherite.MOD_ID + ".tab"))
                .displayItems((context, entries) ->
                {
                    // Ingots
                    entries.accept(ModItems.ASH_INGOT);
                    entries.accept(ModItems.SOLAR_INGOT);
                    entries.accept(ModItems.SOUL_INGOT);
                    entries.accept(ModItems.FROST_INGOT);

                    // Armor
                    entries.accept(ModItems.ASH_HELMET);
                    entries.accept(ModItems.ASH_CHESTPLATE);
                    entries.accept(ModItems.ASH_LEGGINGS);
                    entries.accept(ModItems.ASH_BOOTS);

                    entries.accept(ModItems.SOLAR_HELMET);
                    entries.accept(ModItems.SOLAR_CHESTPLATE);
                    entries.accept(ModItems.SOLAR_LEGGINGS);
                    entries.accept(ModItems.SOLAR_BOOTS);

                    entries.accept(ModItems.SOUL_HELMET);
                    entries.accept(ModItems.SOUL_CHESTPLATE);
                    entries.accept(ModItems.SOUL_LEGGINGS);
                    entries.accept(ModItems.SOUL_BOOTS);

                    entries.accept(ModItems.FROST_HELMET);
                    entries.accept(ModItems.FROST_CHESTPLATE);
                    entries.accept(ModItems.FROST_LEGGINGS);
                    entries.accept(ModItems.FROST_BOOTS);

                    // Axes
                    entries.accept(ModItems.ASH_AXE);
                    entries.accept(ModItems.SOLAR_AXE);
                    entries.accept(ModItems.SOUL_AXE);
                    entries.accept(ModItems.FROST_AXE);

                    // Hoes
                    entries.accept(ModItems.ASH_HOE);
                    entries.accept(ModItems.SOLAR_HOE);
                    entries.accept(ModItems.SOUL_HOE);
                    entries.accept(ModItems.FROST_HOE);

                    // Pickaxes
                    entries.accept(ModItems.ASH_PICKAXE);
                    entries.accept(ModItems.SOLAR_PICKAXE);
                    entries.accept(ModItems.SOUL_PICKAXE);
                    entries.accept(ModItems.FROST_PICKAXE);

                    // Shovels
                    entries.accept(ModItems.ASH_SHOVEL);
                    entries.accept(ModItems.SOLAR_SHOVEL);
                    entries.accept(ModItems.SOUL_SHOVEL);
                    entries.accept(ModItems.FROST_SHOVEL);

                    // Swords
                    entries.accept(ModItems.ASH_SWORD);
                    entries.accept(ModItems.SOLAR_SWORD);
                    entries.accept(ModItems.SOUL_SWORD);
                    entries.accept(ModItems.FROST_SWORD);

                    // Spears
                    entries.accept(ModItems.ASH_SPEAR);
                    entries.accept(ModItems.SOLAR_SPEAR);
                    entries.accept(ModItems.SOUL_SPEAR);
                    entries.accept(ModItems.FROST_SPEAR);

                    // Blocks
                    entries.accept(ModItems.ASH_BLOCK);
                    entries.accept(ModItems.SOLAR_BLOCK);
                    entries.accept(ModItems.SOUL_BLOCK);
                    entries.accept(ModItems.FROST_BLOCK);

                    // BitCoin
                    entries.accept(ModItems.BITCOIN);

                    // Reward coupons
                    entries.accept(ModRewardCouponItems.EXPERIENCE_DOUBLE_COUPON);
                    entries.accept(ModRewardCouponItems.EXPERIENCE_TRIPLE_COUPON);
                    entries.accept(ModRewardCouponItems.BITCOIN_DOUBLE_COUPON);
                    entries.accept(ModRewardCouponItems.BITCOIN_TRIPLE_COUPON);

                    entries.accept(ModBackpackItems.LEVEL_1);
                    entries.accept(ModBackpackItems.LEVEL_2);
                    entries.accept(ModBackpackItems.LEVEL_3);

                    // randomBox 1~4
                    entries.accept(ModItems.RANDOM_BOX_I);
                    entries.accept(ModItems.RANDOM_BOX_II);
                    entries.accept(ModItems.RANDOM_BOX_III);
                    entries.accept(ModItems.RANDOM_BOX_IV);

                    // rewardKey 1~4
                    entries.accept(ModItems.REWARD_KEY_I);
                    entries.accept(ModItems.REWARD_KEY_II);
                    entries.accept(ModItems.REWARD_KEY_III);
                    entries.accept(ModItems.REWARD_KEY_IV);

                    // 강화조각 / 강화보석
                    entries.accept(ModItems.ENHANCEMENT_SHARD);
                    entries.accept(ModItems.ENHANCEMENT_GEM);

                    // 직업선택권 외 주문서
                    entries.accept(ModItems.JOB_SELECT_TICKET);
                    entries.accept(ModItems.DEATH_ITEM_PROTECTION_SCROLL);
                    entries.accept(ModItems.LAND_PURCHASE_DOCUMENT);
                    entries.accept(ModItems.ENHANCE_PROTECTION_SCROLL);
                    entries.accept(ModItems.ENHANCE_SUCCESS_SCROLL_3);
                    entries.accept(ModItems.ENHANCE_SUCCESS_SCROLL_5);
                    entries.accept(ModItems.ENHANCE_SUCCESS_SCROLL_7);
                    entries.accept(ModItems.ENHANCE_SUCCESS_SCROLL_10);
                    
                    // pet box
                    entries.accept(ModItems.NOMAL_PETBOX);
                    entries.accept(ModItems.RARE_PETBOX);
                    entries.accept(ModItems.LEGEND_PETBOX);

                })
                .build()
        );
    }
}
