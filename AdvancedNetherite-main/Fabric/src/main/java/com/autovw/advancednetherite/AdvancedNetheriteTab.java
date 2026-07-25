package com.autovw.advancednetherite;

import com.autovw.advancednetherite.api.annotation.Internal;
import com.autovw.advancednetherite.common.item.AdvancedItem;
import com.autovw.advancednetherite.core.ModItems;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
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
        ResourceKey<CreativeModeTab> tab = ResourceKey.create(Registries.CREATIVE_MODE_TAB, ResourceLocation.fromNamespaceAndPath(AdvancedNetherite.MOD_ID, "tab"));
        Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, tab, FabricItemGroup.builder()
                .icon(ModItems.NETHERITE_GOLD_INGOT::getDefaultInstance)
                .title(Component.translatable("itemGroup." + AdvancedNetherite.MOD_ID + ".tab"))
                .displayItems((context, entries) ->
                {
                    // Ingots
                    entries.accept(ModItems.NETHERITE_IRON_INGOT);
                    entries.accept(ModItems.NETHERITE_GOLD_INGOT);
                    entries.accept(ModItems.NETHERITE_EMERALD_INGOT);
                    entries.accept(ModItems.NETHERITE_DIAMOND_INGOT);

                    // Armor
                    entries.accept(ModItems.NETHERITE_IRON_HELMET);
                    entries.accept(ModItems.NETHERITE_IRON_CHESTPLATE);
                    entries.accept(ModItems.NETHERITE_IRON_LEGGINGS);
                    entries.accept(ModItems.NETHERITE_IRON_BOOTS);

                    entries.accept(ModItems.NETHERITE_GOLD_HELMET);
                    entries.accept(ModItems.NETHERITE_GOLD_CHESTPLATE);
                    entries.accept(ModItems.NETHERITE_GOLD_LEGGINGS);
                    entries.accept(ModItems.NETHERITE_GOLD_BOOTS);

                    entries.accept(ModItems.NETHERITE_EMERALD_HELMET);
                    entries.accept(ModItems.NETHERITE_EMERALD_CHESTPLATE);
                    entries.accept(ModItems.NETHERITE_EMERALD_LEGGINGS);
                    entries.accept(ModItems.NETHERITE_EMERALD_BOOTS);

                    entries.accept(ModItems.NETHERITE_DIAMOND_HELMET);
                    entries.accept(ModItems.NETHERITE_DIAMOND_CHESTPLATE);
                    entries.accept(ModItems.NETHERITE_DIAMOND_LEGGINGS);
                    entries.accept(ModItems.NETHERITE_DIAMOND_BOOTS);

                    // Axes
                    entries.accept(ModItems.NETHERITE_IRON_AXE);
                    entries.accept(ModItems.NETHERITE_GOLD_AXE);
                    entries.accept(ModItems.NETHERITE_EMERALD_AXE);
                    entries.accept(ModItems.NETHERITE_DIAMOND_AXE);

                    // Hoes
                    entries.accept(ModItems.NETHERITE_IRON_HOE);
                    entries.accept(ModItems.NETHERITE_GOLD_HOE);
                    entries.accept(ModItems.NETHERITE_EMERALD_HOE);
                    entries.accept(ModItems.NETHERITE_DIAMOND_HOE);

                    // Pickaxes
                    entries.accept(ModItems.NETHERITE_IRON_PICKAXE);
                    entries.accept(ModItems.NETHERITE_GOLD_PICKAXE);
                    entries.accept(ModItems.NETHERITE_EMERALD_PICKAXE);
                    entries.accept(ModItems.NETHERITE_DIAMOND_PICKAXE);

                    // Shovels
                    entries.accept(ModItems.NETHERITE_IRON_SHOVEL);
                    entries.accept(ModItems.NETHERITE_GOLD_SHOVEL);
                    entries.accept(ModItems.NETHERITE_EMERALD_SHOVEL);
                    entries.accept(ModItems.NETHERITE_DIAMOND_SHOVEL);

                    // Swords
                    entries.accept(ModItems.NETHERITE_IRON_SWORD);
                    entries.accept(ModItems.NETHERITE_GOLD_SWORD);
                    entries.accept(ModItems.NETHERITE_EMERALD_SWORD);
                    entries.accept(ModItems.NETHERITE_DIAMOND_SWORD);

                    // Blocks
                    entries.accept(ModItems.NETHERITE_IRON_BLOCK);
                    entries.accept(ModItems.NETHERITE_GOLD_BLOCK);
                    entries.accept(ModItems.NETHERITE_EMERALD_BLOCK);
                    entries.accept(ModItems.NETHERITE_DIAMOND_BLOCK);

                    // BitCoin
                    entries.accept(ModItems.BITCOIN);

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
