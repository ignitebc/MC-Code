package com.autovw.advancednetherite.common.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

import java.util.function.Consumer;

/**
 * 직업 경험치 및 비트코인 획득 확률 쿠폰 전용 아이템.
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
    private final int multiplier;

    public RewardCouponItem(Properties properties, CouponType couponType)
    {
        this(properties, couponType, 2);
    }

    public RewardCouponItem(Properties properties, CouponType couponType, int multiplier)
    {
        super(properties);
        if (multiplier != 2 && multiplier != 3)
        {
            throw new IllegalArgumentException("Coupon multiplier must be 2 or 3");
        }
        this.couponType = couponType;
        this.multiplier = multiplier;
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

    private void addExperienceTooltips(Consumer<Component> tooltip)
    {
        tooltip.accept(Component.literal("사용 시 10분 동안 직업 경험치 획득량이 " + this.multiplier + "배가 됩니다.")
                .withStyle(ChatFormatting.GRAY));
        tooltip.accept(Component.literal("보유 중인 직업의 직업 보상에만 적용됩니다.")
                .withStyle(ChatFormatting.DARK_GRAY));
        tooltip.accept(Component.literal("같은 쿠폰을 추가 사용하면 지속시간이 10분 연장됩니다.")
                .withStyle(ChatFormatting.YELLOW));
        tooltip.accept(Component.literal("다른 배율의 경험치 쿠폰과 동시에 사용할 수 없습니다.")
                .withStyle(ChatFormatting.YELLOW));
        tooltip.accept(Component.literal("사망·로그아웃 중에도 남은 시간은 계속 흐릅니다.")
                .withStyle(ChatFormatting.DARK_GRAY));
    }

    private void addBitcoinTooltips(Consumer<Component> tooltip)
    {
        tooltip.accept(Component.literal("사용 시 10분 동안 직업 비트코인 획득 확률이 " + this.multiplier + "배가 됩니다.")
                .withStyle(ChatFormatting.GRAY));
        tooltip.accept(Component.literal("1회 지급량은 그대로이며, 획득 확률은 최대 100%입니다.")
                .withStyle(ChatFormatting.DARK_GRAY));
        tooltip.accept(Component.literal("주식 및 랜덤 상자 보상에는 적용되지 않습니다.")
                .withStyle(ChatFormatting.DARK_GRAY));
        tooltip.accept(Component.literal("같은 쿠폰을 추가 사용하면 지속시간이 10분 연장됩니다.")
                .withStyle(ChatFormatting.YELLOW));
        tooltip.accept(Component.literal("다른 배율의 비트코인 쿠폰과 동시에 사용할 수 없습니다.")
                .withStyle(ChatFormatting.YELLOW));
        tooltip.accept(Component.literal("사망·로그아웃 중에도 남은 시간은 계속 흐릅니다.")
                .withStyle(ChatFormatting.DARK_GRAY));
    }
}
