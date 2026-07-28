package com.daqem.jobsplus.stock;

import com.daqem.jobsplus.player.stock.StockPosition;

import java.util.List;

/**
 * 미결제 포지션 감시를 위해 조회한 분봉 가격 범위.
 *
 * @param fromExclusiveMinute 마지막으로 검사를 마친 분
 * @param checkedThroughMinute 이번 조회로 검사를 마친 마지막 분
 * @param candles 거래가 발생해 시가·저가·고가가 존재하는 분봉 목록
 */
public record StockPriceWindow(String stockId, long fromExclusiveMinute, long checkedThroughMinute,
                               List<StockPriceCandle> candles)
{
    public StockPriceWindow
    {
        candles = List.copyOf(candles);
    }

    public boolean hasCompleteCoverage()
    {
        return this.checkedThroughMinute > this.fromExclusiveMinute;
    }

    public boolean crossesLiquidationPrice(StockPosition position, long positionLastCheckedMinute)
    {
        for (StockPriceCandle candle : this.candles)
        {
            if (candle.marketMinute() <= positionLastCheckedMinute)
            {
                continue;
            }
            if (position.isLiquidatedBetween(candle.lowPrice(), candle.highPrice()))
            {
                return true;
            }
        }
        return false;
    }

    public StockPriceCandle getCandle(long marketMinute)
    {
        for (StockPriceCandle candle : this.candles)
        {
            if (candle.marketMinute() == marketMinute)
            {
                return candle;
            }
        }
        return null;
    }

    public record StockPriceCandle(long marketMinute, double openPrice, double lowPrice, double highPrice)
    {
        public boolean hasValidRange()
        {
            return Double.isFinite(this.openPrice)
                    && Double.isFinite(this.lowPrice)
                    && Double.isFinite(this.highPrice)
                    && this.openPrice > 0
                    && this.lowPrice > 0
                    && this.openPrice >= this.lowPrice
                    && this.openPrice <= this.highPrice
                    && this.highPrice >= this.lowPrice;
        }
    }
}
