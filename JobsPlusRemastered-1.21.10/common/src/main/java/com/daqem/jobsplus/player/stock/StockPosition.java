package com.daqem.jobsplus.player.stock;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/**
 * 주식 보유 정보.
 * <p>
 * - investedAmount: 투자한 비트코인 원금 (구 세이브의 "quantity" 키를 그대로 사용)
 * - costBasis: 남아 있는 매수 원가 합계
 * - averageEntryPrice: 평균 매수 단가 (KRW)
 * - units: 구버전 세이브 마이그레이션 전용 필드. averageEntryPrice가 없던 시절의
 *   세이브를 읽을 때 평단가를 복원하는 데만 쓰이며, 새 데이터에서는 항상 0이다.
 * <p>
 * 실제 보유 주식 수는 저장하지 않고 {@link #getUnits()}로 역산한다.
 */
public record StockPosition(String stockId, double units, double costBasis, double investedAmount,
                            double averageEntryPrice)
{
    public static final Codec<StockPosition> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("stock_id").forGetter(StockPosition::stockId),
            Codec.DOUBLE.fieldOf("units").forGetter(StockPosition::units),
            Codec.DOUBLE.fieldOf("cost_basis").forGetter(StockPosition::costBasis),
            // 세이브 호환을 위해 직렬화 키는 기존 "quantity"를 유지한다.
            Codec.DOUBLE.optionalFieldOf("quantity", -1D).forGetter(StockPosition::investedAmount),
            Codec.DOUBLE.optionalFieldOf("average_entry_price", -1D)
                    .forGetter(StockPosition::averageEntryPrice)
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
        return this.getUnits() * currentPrice;
    }
}
