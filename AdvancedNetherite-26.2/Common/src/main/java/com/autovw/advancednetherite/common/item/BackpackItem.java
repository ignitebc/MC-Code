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
        tooltip.accept(Component.literal("가방 해제·교체 시 들어가지 않는 아이템은 바닥에 떨어집니다.")
                .withStyle(ChatFormatting.YELLOW));
        tooltip.accept(Component.literal("사망 손실로 장착한 가방이 삭제되면 내용물도 모두 삭제됩니다.")
                .withStyle(ChatFormatting.RED));
    }
}
