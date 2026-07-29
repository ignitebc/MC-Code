package com.autovw.advancednetherite.api.impl;

import net.minecraft.world.item.ItemStack;

/**
 * @since Minecraft 26.2 - Advanced Netherite 2.4.2
 * @see com.autovw.advancednetherite.config.IClientConfig#matchingDurabilityBars()
 * @see com.autovw.advancednetherite.common.AdvancedUtil#getDurabilityBarColor(int, ItemStack)
 * @author Autovw
 */
public interface IDurabilityBarColorModifier
{
    /**
     * Called in {@link net.minecraft.world.item.Item#getBarColor(ItemStack)} when {@link com.autovw.advancednetherite.config.IClientConfig#matchingDurabilityBars()} is enabled in the client configuration.
     * {@link Override} this method to apply your own custom durability bar color.
     * @param originalColor The integer value of the color before modification
     * @param stack The ItemStack of the durability item
     * @return The color displayed on the durability bar when <code>matchingDurabilityBars</code> is enabled in the client config of Advanced Netherite
     */
    default int durabilityBarColorModifier(int originalColor, ItemStack stack)
    {
        return originalColor;
    }
}
