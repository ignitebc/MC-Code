package com.daqem.jobsplus.stock;

import net.minecraft.network.RegistryFriendlyByteBuf;

import java.util.List;

/**
 * 서버가 매분 확정하는 시세 스냅샷.
 * <p>
 * 클라이언트 표와 서버 거래 검증이 이 스냅샷 하나를 사용한다. 판매는 표시 가격으로 체결하고,
 * 구매는 표시 가격을 확인한 뒤 다음 분 시작가 예약을 생성한다. 어느 분의 가격인지를
 * {@link #marketMinute()}로 들고 다니므로, 서버는 거래 요청 직전에 스냅샷이 현재 분의 것인지 확인한다.
 *
 * @param version      갱신될 때마다 1씩 증가하는 스냅샷 번호. 0은 아직 시세를 받지 못한 상태다.
 * @param marketMinute 이 가격이 속한 분. epoch millis를 60000으로 나눈 값이다.
 * @param updatedAt    이 스냅샷을 실제로 확정한 시각(epoch millis)
 */
public record StockMarketSnapshot(long version, long marketMinute, long updatedAt, SnapshotStatus status,
                                  List<StockQuote> quotes)
{
    public static final StockMarketSnapshot EMPTY =
            new StockMarketSnapshot(0, Long.MIN_VALUE, 0, SnapshotStatus.REFRESHING, List.of());

    /**
     * 서버 시스템 시간 기준 현재 분. 갱신 주기와 체결 유효성의 기준값이다.
     */
    public static long currentMarketMinute()
    {
        return Math.floorDiv(System.currentTimeMillis(), 60_000L);
    }

    /**
     * 새 분의 조회를 시작할 때 즉시 내보낼 스냅샷.
     * <p>
     * 모든 종목을 조회 불가 상태로 두어, 응답이 도착하기 전까지 거래가 중지되도록 한다.
     */
    public static StockMarketSnapshot refreshing(long version, long marketMinute)
    {
        List<StockQuote> quotes = StockCatalog.getStocks().stream()
                .map(stock -> StockQuote.unavailable(stock.id(), stock.name()))
                .toList();
        return new StockMarketSnapshot(version, marketMinute, System.currentTimeMillis(),
                SnapshotStatus.REFRESHING, quotes);
    }

    public StockQuote getQuote(String stockId)
    {
        return this.quotes.stream()
                .filter(quote -> quote.id().equals(stockId))
                .findFirst()
                .orElse(null);
    }

    /**
     * 이 스냅샷으로 거래해도 되는지 여부. 종목별 조회 성공 여부는 별도로 확인해야 한다.
     */
    public boolean isTradable(long currentMinute)
    {
        return this.status == SnapshotStatus.READY && this.marketMinute == currentMinute;
    }

    public void toNetwork(RegistryFriendlyByteBuf buffer)
    {
        buffer.writeLong(this.version);
        buffer.writeLong(this.marketMinute);
        buffer.writeLong(this.updatedAt);
        buffer.writeEnum(this.status);
        buffer.writeCollection(this.quotes, (buf, quote) -> {
            buf.writeUtf(quote.id());
            buf.writeUtf(quote.name());
            buf.writeDouble(quote.priceKrw());
            buf.writeDouble(quote.percentChange());
            buf.writeBoolean(quote.available());
            buf.writeLong(quote.updatedAt());
        });
    }

    public static StockMarketSnapshot fromNetwork(RegistryFriendlyByteBuf buffer)
    {
        long version = buffer.readLong();
        long marketMinute = buffer.readLong();
        long updatedAt = buffer.readLong();
        SnapshotStatus status = buffer.readEnum(SnapshotStatus.class);
        List<StockQuote> quotes = buffer.readList(buf -> new StockQuote(
                buf.readUtf(),
                buf.readUtf(),
                buf.readDouble(),
                buf.readDouble(),
                buf.readBoolean(),
                buf.readLong()
        ));
        return new StockMarketSnapshot(version, marketMinute, updatedAt, status, List.copyOf(quotes));
    }
}
