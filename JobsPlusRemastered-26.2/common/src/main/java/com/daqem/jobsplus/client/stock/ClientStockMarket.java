package com.daqem.jobsplus.client.stock;

import com.daqem.jobsplus.stock.StockMarketSnapshot;
import com.daqem.jobsplus.stock.StockQuote;

/**
 * 서버가 보내 준 시세 스냅샷을 보관하는 클라이언트 전용 저장소.
 * <p>
 * 클라이언트는 절대 시세 API를 직접 조회하지 않는다. 화면에 그리는 가격과 서버가 거래 요청을
 * 검증하는 가격이 같아야 하므로, 표시용 가격도 서버 스냅샷에서만 가져온다.
 * <p>
 * 거래 가능 여부도 플레이어 PC 시간으로 판단하지 않는다. PC 시계가 서버와 다를 수 있어
 * 서버가 알려 준 {@link SnapshotStatus}와 종목별 조회 성공 여부만 사용한다.
 * 최종 판정은 언제나 서버가 현재 분을 기준으로 다시 수행한다.
 */
public final class ClientStockMarket
{
    private static volatile StockMarketSnapshot snapshot = StockMarketSnapshot.EMPTY;

    private ClientStockMarket()
    {
    }

    /**
     * 현재 스냅샷. 거래 한 번에는 이 값을 한 번만 읽어 가격과 번호를 함께 사용해야 한다.
     * 가격을 확인한 뒤 번호를 따로 읽으면 그 사이에 도착한 새 스냅샷과 뒤섞일 수 있다.
     */
    public static StockMarketSnapshot getSnapshot()
    {
        return snapshot;
    }

    public static void setSnapshot(StockMarketSnapshot newSnapshot)
    {
        snapshot = newSnapshot;
    }

    /**
     * 보관 중인 시세를 지운다.
     * 주식 탭에 다시 들어왔을 때 지난 세션의 가격이 잠깐 보이는 것을 막는다.
     */
    public static void clear()
    {
        snapshot = StockMarketSnapshot.EMPTY;
    }

    public static StockQuote getQuote(String stockId)
    {
        return snapshot.getQuote(stockId);
    }
}
