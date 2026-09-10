package com.autovw.advancednetherite.common;

import com.autovw.advancednetherite.api.annotation.Internal;
import com.autovw.advancednetherite.api.impl.IAdvancedHooks;
import com.autovw.advancednetherite.api.impl.IArmorMaterial;
import com.autovw.advancednetherite.api.impl.IToolMaterial;
import com.autovw.advancednetherite.common.item.AdvancedArmorItem;
import com.autovw.advancednetherite.config.ConfigHelper;
import com.autovw.advancednetherite.core.util.ModArmorMaterials;
import com.autovw.advancednetherite.core.util.ModToolMaterials;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Style;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Objects;

/**
 * @author Autovw
 */
public class AdvancedUtil
{
    /**
     * Helper method for getting the durability multiplier of an armor material
     * @param material The material to get the multiplier for
     * @return Durability multiplier for the appropriate armor material
     */
    public static int getArmorDurabilityMultiplier(ArmorMaterial material)
    {
        if (material == ModArmorMaterials.ASH)
            return 39;
        if (material == ModArmorMaterials.SOLAR)
            return 41;
        if (material == ModArmorMaterials.SOUL)
            return 43;
        if (material == ModArmorMaterials.FROST)
            return 47;
        return 0;
    }

    /**
     * Helper method for getting the appropriate durability bar color on tools
     * @param originalColor The original durability bar color
     * @param stack Tool stack
     * @return The appropriate bar color, based on tier and config settings
     */
    public static int getDurabilityBarColor(int originalColor, ItemStack stack)
    {
        int newColor = originalColor;

        if (ConfigHelper.get().getClient().matchingDurabilityBars())
        {
            // Tools
            if (stack.getItem() instanceof IToolMaterial material)
            {
                if (material.isMaterial(ModToolMaterials.ASH))
                    newColor = getColor(ChatFormatting.GRAY);
                if (material.isMaterial(ModToolMaterials.SOLAR))
                    newColor = getColor(ChatFormatting.GOLD);
                if (material.isMaterial(ModToolMaterials.SOUL))
                    newColor = getColor(ChatFormatting.DARK_GREEN);
                if (material.isMaterial(ModToolMaterials.FROST))
                    newColor = getColor(ChatFormatting.AQUA);
            }

            // Armor
            if (stack.getItem() instanceof IArmorMaterial material)
            {
                if (material.isMaterial(ModArmorMaterials.ASH))
                    newColor = getColor(ChatFormatting.GRAY);
                if (material.isMaterial(ModArmorMaterials.SOLAR))
                    newColor = getColor(ChatFormatting.GOLD);
                if (material.isMaterial(ModArmorMaterials.SOUL))
                    newColor = getColor(ChatFormatting.DARK_GREEN);
                if (material.isMaterial(ModArmorMaterials.FROST))
                    newColor = getColor(ChatFormatting.AQUA);
            }
        }

        return newColor;
    }

    @Internal
    private static int getColor(ChatFormatting color)
    {
        return Objects.requireNonNull(Style.EMPTY.withColor(color).getColor()).getValue();
    }

    /**
     * Helper method for applying the appropriate block destroy speed to tools
     * @param originalSpeed The original destroy speed
     * @param stack Tool stack
     * @param state State of block being broken
     * @return New destroy speed
     */
    public static float getDestroySpeed(float originalSpeed, ItemStack stack, BlockState state)
    {
        float newSpeed = originalSpeed;

        if (stack.getItem() instanceof IToolMaterial material)
        {
            if (stack.getItem().isCorrectToolForDrops(stack, state))
            {
                if (material.isMaterial(ModToolMaterials.ASH))
                    newSpeed *= ConfigHelper.get().getServer().getToolProperties().getIronBreakingSpeedMultiplier();
                if (material.isMaterial(ModToolMaterials.SOLAR))
                    newSpeed *= ConfigHelper.get().getServer().getToolProperties().getGoldBreakingSpeedMultiplier();
                if (material.isMaterial(ModToolMaterials.SOUL))
                    newSpeed *= ConfigHelper.get().getServer().getToolProperties().getEmeraldBreakingSpeedMultiplier();
                if (material.isMaterial(ModToolMaterials.FROST))
                    newSpeed *= ConfigHelper.get().getServer().getToolProperties().getDiamondBreakingSpeedMultiplier();
            }
        }

        return newSpeed;
    }

    /**
     * Determines if an enderman should behave passively towards the player, unless aggravated.
     * @param player Player wearing the armor
     * @return True if enderman should behave passively
     */
    public static boolean isWearingEndermanPassiveArmor(Player player)
    {
        for (EquipmentSlot slot : EquipmentSlotGroup.ARMOR)
        {
            ItemStack stack = player.getItemBySlot(slot);
            Item item = stack.getItem();
            if ((item instanceof AdvancedArmorItem && ((AdvancedArmorItem) item).pacifiesEndermen(stack)) || (item instanceof IAdvancedHooks && ((IAdvancedHooks) item).pacifyEndermen(stack)))
            {
                return true;
            }
        }
        return false;
    }

    /**
     * Determines if a phantom should behave passively towards the player, unless aggravated.
     * @param player Player wearing the armor
     * @return True if phantom should behave passively
     */
    public static boolean isWearingPhantomPassiveArmor(Player player)
    {
        for (EquipmentSlot slot : EquipmentSlotGroup.ARMOR)
        {
            ItemStack stack = player.getItemBySlot(slot);
            Item item = stack.getItem();
            if ((item instanceof AdvancedArmorItem && ((AdvancedArmorItem) item).pacifiesPhantoms(stack)) || (item instanceof IAdvancedHooks && ((IAdvancedHooks) item).pacifyPhantoms(stack)))
            {
                return true;
            }
        }

        return false;
    }
}
