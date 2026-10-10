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
     * 클라이언트 설정에서 {@link com.autovw.advancednetherite.config.IClientConfig#matchingDurabilityBars()}가 켜져 있을 때 {@link net.minecraft.world.item.Item#getBarColor(ItemStack)}에서 호출된다.
     * 내구도 막대 색을 직접 정하려면 이 메서드를 {@link Override}한다.
     * @param originalColor 바꾸기 전 색의 정수값
     * @param stack 내구도가 있는 아이템의 ItemStack
     * @return Advanced Netherite 클라이언트 설정의 <code>matchingDurabilityBars</code>가 켜져 있을 때 내구도 막대에 표시할 색
     */
    default int durabilityBarColorModifier(int originalColor, ItemStack stack)
    {
        return originalColor;
    }
}
