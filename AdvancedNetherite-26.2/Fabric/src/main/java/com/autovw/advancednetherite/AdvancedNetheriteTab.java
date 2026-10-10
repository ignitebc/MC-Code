package com.autovw.advancednetherite;

import com.autovw.advancednetherite.api.annotation.Internal;
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

/**
 * @author Autovw
 */
public final class AdvancedNetheriteTab
{
    /**
     * Advanced Netherite 크리에이티브 탭
     */
    @Internal
    public static void registerTab()
    {
        ResourceKey<CreativeModeTab> tab = ResourceKey.create(Registries.CREATIVE_MODE_TAB, Identifier.fromNamespaceAndPath(AdvancedNetherite.MOD_ID, "tab"));
        Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, tab, FabricCreativeModeTab.builder()
                .icon(ModItems.SUNLIGHT_INGOT::getDefaultInstance)
                .title(Component.translatable("itemGroup." + AdvancedNetherite.MOD_ID + ".tab"))
                .displayItems((context, entries) ->
                {
                    // 주괴
                    entries.accept(ModItems.ASH_INGOT);
                    entries.accept(ModItems.SUNLIGHT_INGOT);
                    entries.accept(ModItems.SOUL_INGOT);
                    entries.accept(ModItems.FROST_INGOT);

                    // 갑옷
                    entries.accept(ModItems.ASH_HELMET);
                    entries.accept(ModItems.ASH_CHESTPLATE);
                    entries.accept(ModItems.ASH_LEGGINGS);
                    entries.accept(ModItems.ASH_BOOTS);

                    entries.accept(ModItems.SUNLIGHT_HELMET);
                    entries.accept(ModItems.SUNLIGHT_CHESTPLATE);
                    entries.accept(ModItems.SUNLIGHT_LEGGINGS);
                    entries.accept(ModItems.SUNLIGHT_BOOTS);

                    entries.accept(ModItems.SOUL_HELMET);
                    entries.accept(ModItems.SOUL_CHESTPLATE);
                    entries.accept(ModItems.SOUL_LEGGINGS);
                    entries.accept(ModItems.SOUL_BOOTS);

                    entries.accept(ModItems.FROST_HELMET);
                    entries.accept(ModItems.FROST_CHESTPLATE);
                    entries.accept(ModItems.FROST_LEGGINGS);
                    entries.accept(ModItems.FROST_BOOTS);

                    // 도끼
                    entries.accept(ModItems.ASH_AXE);
                    entries.accept(ModItems.SUNLIGHT_AXE);
                    entries.accept(ModItems.SOUL_AXE);
                    entries.accept(ModItems.FROST_AXE);

                    // 괭이
                    entries.accept(ModItems.ASH_HOE);
                    entries.accept(ModItems.SUNLIGHT_HOE);
                    entries.accept(ModItems.SOUL_HOE);
                    entries.accept(ModItems.FROST_HOE);

                    // 곡괭이
                    entries.accept(ModItems.ASH_PICKAXE);
                    entries.accept(ModItems.SUNLIGHT_PICKAXE);
                    entries.accept(ModItems.SOUL_PICKAXE);
                    entries.accept(ModItems.FROST_PICKAXE);

                    // 삽
                    entries.accept(ModItems.ASH_SHOVEL);
                    entries.accept(ModItems.SUNLIGHT_SHOVEL);
                    entries.accept(ModItems.SOUL_SHOVEL);
                    entries.accept(ModItems.FROST_SHOVEL);

                    // 검
                    entries.accept(ModItems.ASH_SWORD);
                    entries.accept(ModItems.SUNLIGHT_SWORD);
                    entries.accept(ModItems.SOUL_SWORD);
                    entries.accept(ModItems.FROST_SWORD);

                    // 창
                    entries.accept(ModItems.ASH_SPEAR);
                    entries.accept(ModItems.SUNLIGHT_SPEAR);
                    entries.accept(ModItems.SOUL_SPEAR);
                    entries.accept(ModItems.FROST_SPEAR);

                    // 블록
                    entries.accept(ModItems.ASH_BLOCK);
                    entries.accept(ModItems.SUNLIGHT_BLOCK);
                    entries.accept(ModItems.SOUL_BLOCK);
                    entries.accept(ModItems.FROST_BLOCK);

                    // 비트코인
                    entries.accept(ModItems.BITCOIN);

                    // 보상 쿠폰
                    entries.accept(ModRewardCouponItems.EXPERIENCE_DOUBLE_COUPON);
                    entries.accept(ModRewardCouponItems.EXPERIENCE_TRIPLE_COUPON);
                    entries.accept(ModRewardCouponItems.BITCOIN_DOUBLE_COUPON);
                    entries.accept(ModRewardCouponItems.BITCOIN_TRIPLE_COUPON);

                    entries.accept(ModBackpackItems.LEVEL_1);
                    entries.accept(ModBackpackItems.LEVEL_2);
                    entries.accept(ModBackpackItems.LEVEL_3);

                    // 랜덤 상자 1~4
                    entries.accept(ModItems.RANDOM_BOX_I);
                    entries.accept(ModItems.RANDOM_BOX_II);
                    entries.accept(ModItems.RANDOM_BOX_III);
                    entries.accept(ModItems.RANDOM_BOX_IV);

                    // 보상 열쇠 1~4
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
                    
                    // 펫 상자
                    entries.accept(ModItems.NOMAL_PETBOX);
                    entries.accept(ModItems.RARE_PETBOX);
                    entries.accept(ModItems.LEGEND_PETBOX);

                })
                .build()
        );
    }
}
