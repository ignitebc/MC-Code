package com.autovw.advancednetherite.core.util;

import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.level.block.Block;

/**
 * @author Autovw
 */
public final class ModToolMaterials
{
    public static final ToolMaterial ASH = material(ModTags.INCORRECT_FOR_ASH_TOOL, 2200, 6.0F, 20, ModTags.REPAIRS_ASH_TOOLS);
    public static final ToolMaterial SUNLIGHT = material(ModTags.INCORRECT_FOR_SUNLIGHT_TOOL, 2300, 7.0F, 25, ModTags.REPAIRS_SUNLIGHT_TOOLS);
    public static final ToolMaterial SOUL = material(ModTags.INCORRECT_FOR_SOUL_TOOL, 2400, 8.0F, 30, ModTags.REPAIRS_SOUL_TOOLS);
    public static final ToolMaterial FROST = material(ModTags.INCORRECT_FOR_FROST_TOOL, 2500, 9.0F, 35, ModTags.REPAIRS_FROST_TOOLS);

    private static ToolMaterial material(TagKey<Block> incorrectBlocksForDrops, int durability, float attackDamageBonus, int enchantability, TagKey<Item> repairItems)
    {
        return new ToolMaterial(incorrectBlocksForDrops, durability, 1.0F, attackDamageBonus, enchantability, repairItems);
    }
}
