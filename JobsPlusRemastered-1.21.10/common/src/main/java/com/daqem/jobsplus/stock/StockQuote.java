package com.daqem.jobsplus.stock;

/**
 * 특정 시각에 확정된 종목 시세.
 *
 * @param updatedAt 이 가격을 실제로 조회한 시각(epoch millis). 0이면 아직 한 번도 조회하지 못한 상태다.
 */
public record StockQuote(String id, String name, double priceKrw, double percentChange, boolean available,
                         long updatedAt)
{
    /**
     * 이 시간(밀리초)보다 오래된 가격은 거래에 사용하지 않는다.
     * 표시는 계속 하되, 갱신이 끊긴 가격으로 체결되는 것을 막기 위한 값이다.
     */
    public static final long STALE_THRESHOLD_MILLIS = 120_000L;

    public static StockQuote loading(String id, String name)
    {
        return new StockQuote(id, name, 0, 0, false, 0);
    }

    /**
     * 마지막 조회 이후 {@link #STALE_THRESHOLD_MILLIS}가 지났는지 여부.
     */
    public boolean isStale(long now)
    {
        return this.updatedAt <= 0 || now - this.updatedAt > STALE_THRESHOLD_MILLIS;
    }

    /**
     * 이 가격으로 매수·매도를 체결해도 되는지 여부.
     */
    public boolean isTradable(long now)
    {
        return this.available && this.priceKrw > 0 && !this.isStale(now);
    }
}
