package com.autovw.advancednetherite.core.util;

import com.autovw.advancednetherite.AdvancedNetherite;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

/**
 * Fabric 전용 태그 모음
 * @author Autovw
 */
public final class FabricModTags
{
    // 설정 태그
    public static final TagKey<Item> CONFIG_PACIFY_PHANTOMS = ModTags.itemTag(Identifier.fromNamespaceAndPath(AdvancedNetherite.MOD_ID, "config/pacify_phantoms"));
    public static final TagKey<Item> CONFIG_PACIFY_PIGLINS = ModTags.itemTag(Identifier.fromNamespaceAndPath(AdvancedNetherite.MOD_ID, "config/pacify_piglins"));
    public static final TagKey<Item> CONFIG_PACIFY_ENDERMEN = ModTags.itemTag(Identifier.fromNamespaceAndPath(AdvancedNetherite.MOD_ID, "config/pacify_endermen"));

    // "c"(공통) 태그
    public static final TagKey<Item> COMMON_HELMETS = commonItemTag("helmets");
    public static final TagKey<Item> COMMON_CHESTPLATES = commonItemTag("chestplates");
    public static final TagKey<Item> COMMON_LEGGINGS = commonItemTag("leggings");
    public static final TagKey<Item> COMMON_BOOTS = commonItemTag("boots");
    public static final TagKey<Item> COMMON_NETHERITE_INGOTS = commonItemTag("netherite_ingots");
    public static final TagKey<Block> COMMON_NETHERITE_BLOCKS = commonBlockTag("netherite_blocks");

    // 툴팁 희귀도 태그
    public static final TagKey<Item> TOOLTIP_RARENESS_EPIC_ITEM = ModTags.itemTag(Identifier.fromNamespaceAndPath("tooltiprareness", "epic_item"));
    public static final TagKey<Item> TOOLTIP_RARENESS_LEGENDARY_ITEM = ModTags.itemTag(Identifier.fromNamespaceAndPath("tooltiprareness", "legendary_item"));

    private static TagKey<Item> commonItemTag(String key)
    {
        return ModTags.itemTag(Identifier.fromNamespaceAndPath("c", key));
    }

    private static TagKey<Block> commonBlockTag(String key)
    {
        return ModTags.blockTag(Identifier.fromNamespaceAndPath("c", key));
    }
}
