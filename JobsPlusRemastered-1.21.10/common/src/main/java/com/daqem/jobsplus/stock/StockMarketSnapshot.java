package com.daqem.jobsplus.stock;

import net.minecraft.network.RegistryFriendlyByteBuf;

import java.util.List;

/**
 * 서버가 확정한 시세 스냅샷.
 * <p>
 * 같은 {@code version}을 가진 스냅샷은 어디서 읽어도 항상 같은 가격을 가리킨다.
 * 클라이언트 표와 서버 매수·매도가 이 스냅샷 하나만 사용해야 화면에 보이는 가격과 실제 체결 가격이 어긋나지 않는다.
 *
 * @param version   갱신될 때마다 1씩 증가하는 스냅샷 번호. 0은 아직 시세를 받지 못한 상태다.
 * @param updatedAt 이 스냅샷이 확정된 시각(epoch millis)
 */
public record StockMarketSnapshot(long version, long updatedAt, List<StockQuote> quotes)
{
    public static final StockMarketSnapshot EMPTY = new StockMarketSnapshot(0, 0, List.of());

    public StockQuote getQuote(String stockId)
    {
        return this.quotes.stream()
                .filter(quote -> quote.id().equals(stockId))
                .findFirst()
                .orElse(null);
    }

    public boolean isEmpty()
    {
        return this.version <= 0 || this.quotes.isEmpty();
    }

    public void toNetwork(RegistryFriendlyByteBuf buffer)
    {
        buffer.writeLong(this.version);
        buffer.writeLong(this.updatedAt);
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
        long updatedAt = buffer.readLong();
        List<StockQuote> quotes = buffer.readList(buf -> new StockQuote(
                buf.readUtf(),
                buf.readUtf(),
                buf.readDouble(),
                buf.readDouble(),
                buf.readBoolean(),
                buf.readLong()
        ));
        return new StockMarketSnapshot(version, updatedAt, List.copyOf(quotes));
    }
}
