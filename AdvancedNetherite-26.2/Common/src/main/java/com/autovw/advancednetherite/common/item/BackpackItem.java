package com.autovw.advancednetherite.common.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

import java.util.function.Consumer;

public final class BackpackItem extends AdvancedItem
{
    private final int capacity;

    public BackpackItem(Properties properties, int level)
    {
        super(properties.stacksTo(1));
        if (level < 1 || level > 3)
        {
            throw new IllegalArgumentException("Backpack level must be between 1 and 3");
        }
        this.capacity = level * 4;
    }

    public int getCapacity()
    {
        return this.capacity;
    }

    @Override
    public void addTooltips(ItemStack stack, TooltipContext context, TooltipDisplay display,
                            Consumer<Component> tooltip, TooltipFlag flag)
    {
        tooltip.accept(Component.literal("가방 슬롯에 장착하면 기본 인벤토리가 " + this.capacity + "칸 늘어납니다.")
                .withStyle(ChatFormatting.GRAY));
        tooltip.accept(Component.literal("가방을 해제하거나 교체하려면 추가 칸을 먼저 비워 주세요.")
                .withStyle(ChatFormatting.YELLOW));
    }
}
