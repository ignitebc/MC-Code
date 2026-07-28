package com.daqem.jobsplus.player.stock;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.List;

/**
 * 주식 보유 정보.
 * <p>
 * - investedAmount: 투자한 비트코인 원금 (구 세이브의 "quantity" 키를 그대로 사용)
 * - costBasis: 남아 있는 매수 원가 합계
 * - averageEntryPrice: 평균 매수 단가 (KRW)
 * - side: 가격 상승에 투자하는 롱 또는 가격 하락에 투자하는 숏
 * - leverage: 기초 종목 변동률에 적용할 배율 (기본 X1, X2, X3, X5, X10, X15, X20)
 * - units: 구버전 세이브 마이그레이션 전용 필드. averageEntryPrice가 없던 시절의
 *   세이브를 읽을 때 평단가를 복원하는 데만 쓰이며, 새 데이터에서는 항상 0이다.
 * <p>
 * 실제 보유 주식 수는 저장하지 않고 {@link #getUnits()}로 역산한다. 평가금액은
 * 투자 원금에 (기초 수익률 × 방향 × 배율)을 적용하며 0 아래로 내려가지 않는다.
 */
public record StockPosition(String stockId, double units, double costBasis, double investedAmount,
                            double averageEntryPrice, StockPositionSide side, int leverage)
{
    public static final int DEFAULT_LEVERAGE = 1;
    public static final List<Integer> ALLOWED_LEVERAGES = List.of(DEFAULT_LEVERAGE, 2, 3, 5, 10, 15, 20);

    public static final Codec<StockPosition> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("stock_id").forGetter(StockPosition::stockId),
            Codec.DOUBLE.fieldOf("units").forGetter(StockPosition::units),
            Codec.DOUBLE.fieldOf("cost_basis").forGetter(StockPosition::costBasis),
            // 세이브 호환을 위해 직렬화 키는 기존 "quantity"를 유지한다.
            Codec.DOUBLE.optionalFieldOf("quantity", -1D).forGetter(StockPosition::investedAmount),
            Codec.DOUBLE.optionalFieldOf("average_entry_price", -1D)
                    .forGetter(StockPosition::averageEntryPrice),
            Codec.STRING.xmap(StockPositionSide::fromSerializedName, StockPositionSide::getSerializedName)
                    .optionalFieldOf("side", StockPositionSide.LONG)
                    .forGetter(StockPosition::side),
            Codec.INT.optionalFieldOf("leverage", DEFAULT_LEVERAGE).forGetter(StockPosition::leverage)
    ).apply(instance, StockPosition::new));

    public StockPosition
    {
        if (investedAmount < 0)
        {
            investedAmount = costBasis;
        }
        if (averageEntryPrice < 0)
        {
            averageEntryPrice = units <= 0 ? 0 : costBasis / units;
        }
        if (side == null)
        {
            side = StockPositionSide.LONG;
        }
        leverage = normalizeLeverage(leverage);
    }

    public StockPosition(String stockId, double units, double costBasis, double investedAmount,
                         double averageEntryPrice)
    {
        this(stockId, units, costBasis, investedAmount, averageEntryPrice,
                StockPositionSide.LONG, DEFAULT_LEVERAGE);
    }

    public static boolean isAllowedLeverage(int leverage)
    {
        for (int allowedLeverage : ALLOWED_LEVERAGES)
        {
            if (allowedLeverage == leverage)
            {
                return true;
            }
        }
        return false;
    }

    public static int normalizeLeverage(int leverage)
    {
        if (isAllowedLeverage(leverage))
        {
            return leverage;
        }
        return DEFAULT_LEVERAGE;
    }

    public static String getLeverageDisplayName(int leverage)
    {
        if (leverage == DEFAULT_LEVERAGE)
        {
            return "기본";
        }
        return "X" + leverage;
    }

    public double getAverageEntryPrice()
    {
        return this.averageEntryPrice;
    }

    /**
     * 실제 보유 주식 수 (원가 / 평단가). 저장 필드가 아니라 항상 역산한다.
     */
    public double getUnits()
    {
        return this.averageEntryPrice <= 0 ? 0 : this.costBasis / this.averageEntryPrice;
    }

    public double getCurrentValue(double currentPrice)
    {
        if (this.costBasis <= 0 || this.averageEntryPrice <= 0 || !Double.isFinite(currentPrice)
                || currentPrice <= 0)
        {
            return 0;
        }

        double leveragedReturnRate = this.getReturnRate(currentPrice) / 100;
        double currentValue = this.costBasis * (1 + leveragedReturnRate);
        if (currentValue <= 0)
        {
            return 0;
        }
        return currentValue;
    }

    public double getReturnRate(double currentPrice)
    {
        if (this.averageEntryPrice <= 0 || !Double.isFinite(currentPrice) || currentPrice <= 0)
        {
            return 0;
        }

        double underlyingReturnRate = currentPrice / this.averageEntryPrice - 1;
        double positionReturnRate = underlyingReturnRate * this.side.getReturnDirection() * this.leverage;
        return positionReturnRate * 100;
    }

    public boolean isLiquidated(double currentPrice)
    {
        return this.getReturnRate(currentPrice) <= -100;
    }

    /**
     * 한 분 동안 기록된 저가·고가 중 청산선을 통과했는지 확인한다.
     * 롱은 저가, 숏은 고가를 사용해야 분봉 종가가 회복된 뒤에도 청산을 회피할 수 없다.
     */
    public boolean isLiquidatedBetween(double lowPrice, double highPrice)
    {
        if (this.side == StockPositionSide.LONG)
        {
            return this.isLiquidated(lowPrice);
        }
        return this.isLiquidated(highPrice);
    }

    public String getPositionName()
    {
        return this.side.getDisplayName() + " " + getLeverageDisplayName(this.leverage);
    }
}
