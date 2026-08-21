package com.autovw.advancednetherite.common.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

import java.util.function.Consumer;

/**
 * 직업 보상 2배 쿠폰 전용 아이템.
 * 실제 효과 처리는 JobsPlus가 담당하고, 이 클래스는 아이템 설명만 제공한다.
 */
public final class RewardCouponItem extends AdvancedItem
{
    public enum CouponType
    {
        EXPERIENCE,
        BITCOIN
    }

    private final CouponType couponType;

    public RewardCouponItem(Properties properties, CouponType couponType)
    {
        super(properties);
        this.couponType = couponType;
    }

    @Override
    public void addTooltips(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag)
    {
        switch (this.couponType)
        {
            case EXPERIENCE -> addExperienceTooltips(tooltip);
            case BITCOIN -> addBitcoinTooltips(tooltip);
        }
    }

    private static void addExperienceTooltips(Consumer<Component> tooltip)
    {
        tooltip.accept(Component.literal("사용 시 10분 동안 직업 경험치 획득량이 2배가 됩니다.")
                .withStyle(ChatFormatting.GRAY));
        tooltip.accept(Component.literal("보유 중인 직업의 직업 보상에만 적용됩니다.")
                .withStyle(ChatFormatting.DARK_GRAY));
        tooltip.accept(Component.literal("같은 쿠폰을 추가 사용하면 지속시간이 10분 연장됩니다.")
                .withStyle(ChatFormatting.YELLOW));
        tooltip.accept(Component.literal("사망·로그아웃 중에도 남은 시간은 계속 흐릅니다.")
                .withStyle(ChatFormatting.DARK_GRAY));
    }

    private static void addBitcoinTooltips(Consumer<Component> tooltip)
    {
        tooltip.accept(Component.literal("사용 시 10분 동안 직업 비트코인 지급량이 2배가 됩니다.")
                .withStyle(ChatFormatting.GRAY));
        tooltip.accept(Component.literal("비트코인 획득 확률은 증가하지 않습니다.")
                .withStyle(ChatFormatting.DARK_GRAY));
        tooltip.accept(Component.literal("주식 및 랜덤 상자 보상에는 적용되지 않습니다.")
                .withStyle(ChatFormatting.DARK_GRAY));
        tooltip.accept(Component.literal("같은 쿠폰을 추가 사용하면 지속시간이 10분 연장됩니다.")
                .withStyle(ChatFormatting.YELLOW));
        tooltip.accept(Component.literal("사망·로그아웃 중에도 남은 시간은 계속 흐릅니다.")
                .withStyle(ChatFormatting.DARK_GRAY));
    }
}
