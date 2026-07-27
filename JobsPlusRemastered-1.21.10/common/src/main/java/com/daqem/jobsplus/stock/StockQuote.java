package com.daqem.jobsplus.stock;

/**
 * 특정 분에 확정된 종목 시세.
 * <p>
 * 이 가격이 어느 분의 것인지는 {@link StockMarketSnapshot#marketMinute()}가 관리한다.
 * 따라서 여기서는 경과 시간으로 유효기간을 따지지 않는다. 매분 새로 조회하므로
 * 조회하지 못한 종목은 그 분 동안 {@code available}이 false로 남아 거래할 수 없다.
 *
 * @param updatedAt 이 가격의 응답을 받은 시각(epoch millis). 화면 표시용이며 체결 판단에는 쓰지 않는다.
 */
public record StockQuote(String id, String name, double priceKrw, double percentChange, boolean available,
                         long updatedAt)
{
    /**
     * 아직 조회하지 못했거나 조회에 실패한 종목.
     */
    public static StockQuote unavailable(String id, String name)
    {
        return new StockQuote(id, name, 0, 0, false, 0);
    }

    public static StockQuote available(String id, String name, double priceKrw, double percentChange,
                                       long updatedAt)
    {
        return new StockQuote(id, name, priceKrw, percentChange, true, updatedAt);
    }

    /**
     * 체결에 쓸 수 있는 가격인지 여부.
     * <p>
     * 외부 API 응답에는 NaN이나 무한대가 섞일 수 있으므로 유한한 양수인지까지 확인한다.
     * 현재 분의 가격인지와 스냅샷 상태는 {@link StockMarketSnapshot}에서 따로 검사한다.
     */
    public boolean hasValidPrice()
    {
        return this.available
                && Double.isFinite(this.priceKrw)
                && this.priceKrw > 0
                && Double.isFinite(this.percentChange);
    }
}
