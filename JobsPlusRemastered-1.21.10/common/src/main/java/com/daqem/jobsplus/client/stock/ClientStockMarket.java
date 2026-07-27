package com.daqem.jobsplus.client.stock;

import com.daqem.jobsplus.stock.StockMarketSnapshot;
import com.daqem.jobsplus.stock.StockQuote;

/**
 * 서버가 보내 준 시세 스냅샷을 보관하는 클라이언트 전용 저장소.
 * <p>
 * 클라이언트는 절대 시세 API를 직접 조회하지 않는다. 화면에 그리는 가격과 서버가 체결에 사용하는
 * 가격이 반드시 같아야 하므로, 표시용 가격도 서버 스냅샷에서만 가져온다.
 */
public final class ClientStockMarket
{
    private static volatile StockMarketSnapshot snapshot = StockMarketSnapshot.EMPTY;

    private ClientStockMarket()
    {
    }

    public static StockMarketSnapshot getSnapshot()
    {
        return snapshot;
    }

    public static void setSnapshot(StockMarketSnapshot newSnapshot)
    {
        snapshot = newSnapshot;
    }

    public static StockQuote getQuote(String stockId)
    {
        return snapshot.getQuote(stockId);
    }

    /**
     * 화면에 표시된 가격의 스냅샷 번호. 매수·매도 패킷에 실어 보내 서버 가격과 대조한다.
     */
    public static long getSnapshotVersion()
    {
        return snapshot.version();
    }
}
