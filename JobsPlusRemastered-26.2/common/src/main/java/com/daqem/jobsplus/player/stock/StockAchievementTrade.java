package com.daqem.jobsplus.player.stock;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** 시즌 매수 원금과 아직 매도하지 않은 분량. 이전 보유분은 FIFO로 먼저 제외한다. */
public record StockAchievementTrade(double untrackedAmount, double boughtAmount, double remainingAmount,
                                    boolean completed, double closedAmount)
{
    public static final Codec<StockAchievementTrade> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.DOUBLE.fieldOf("untracked").forGetter(StockAchievementTrade::untrackedAmount),
            Codec.DOUBLE.fieldOf("bought").forGetter(StockAchievementTrade::boughtAmount),
            Codec.DOUBLE.fieldOf("remaining").forGetter(StockAchievementTrade::remainingAmount),
            Codec.BOOL.fieldOf("completed").forGetter(StockAchievementTrade::completed),
            Codec.DOUBLE.optionalFieldOf("closed", 0D).forGetter(StockAchievementTrade::closedAmount)
    ).apply(instance, StockAchievementTrade::new));

    public StockAchievementTrade
    {
        if (!Double.isFinite(untrackedAmount) || !Double.isFinite(boughtAmount) || !Double.isFinite(remainingAmount)
                || !Double.isFinite(closedAmount) || untrackedAmount < 0 || boughtAmount < 0
                || remainingAmount < 0 || closedAmount < 0 || closedAmount > boughtAmount)
        {
            throw new IllegalArgumentException("Invalid stock achievement trade");
        }
        untrackedAmount = quantity(untrackedAmount).doubleValue();
        boughtAmount = quantity(boughtAmount).doubleValue();
        remainingAmount = quantity(remainingAmount).doubleValue();
        closedAmount = quantity(closedAmount).doubleValue();
    }

    public StockAchievementTrade(double untrackedAmount, double boughtAmount, double remainingAmount, boolean completed)
    {
        this(untrackedAmount, boughtAmount, remainingAmount, completed, initialClosedAmount(boughtAmount, remainingAmount));
    }

    private static double initialClosedAmount(double boughtAmount, double remainingAmount)
    {
        if (remainingAmount <= 0.00000001)
        {
            return boughtAmount;
        }
        return 0;
    }

    public StockAchievementTrade buy(double amount)
    {
        if (!Double.isFinite(amount) || amount <= 0)
        {
            return this;
        }
        if (completed)
        {
            return this;
        }
        double bought = quantity(boughtAmount).add(quantity(amount)).doubleValue();
        double remaining = quantity(remainingAmount).add(quantity(amount)).doubleValue();
        return new StockAchievementTrade(untrackedAmount, bought, remaining, false, closedAmount);
    }

    public StockAchievementTrade sell(double amount)
    {
        if (!Double.isFinite(amount) || amount <= 0)
        {
            return this;
        }
        if (completed)
        {
            return this;
        }
        BigDecimal sold = quantity(amount);
        BigDecimal excluded = quantity(untrackedAmount).min(sold);
        double untracked = quantity(untrackedAmount).subtract(excluded).doubleValue();
        double remaining = quantity(remainingAmount).subtract(sold.subtract(excluded)).max(BigDecimal.ZERO).doubleValue();
        double closed = closedAmount;
        boolean complete = false;
        if (remaining <= 0.00000001)
        {
            remaining = 0;
            closed = boughtAmount;
            if (boughtAmount >= 1)
            {
                complete = true;
            }
        }
        return new StockAchievementTrade(untracked, boughtAmount, remaining, complete, closed);
    }

    public StockAchievementTrade liquidate()
    {
        if (completed)
        {
            return this;
        }
        // 현재 미완료 사이클은 제외하되, 전에 전량 매도를 끝낸 원금 합계는 유지한다.
        return new StockAchievementTrade(0, closedAmount, 0, false, closedAmount);
    }

    private static BigDecimal quantity(double amount)
    {
        return BigDecimal.valueOf(amount).setScale(8, RoundingMode.DOWN);
    }
}
