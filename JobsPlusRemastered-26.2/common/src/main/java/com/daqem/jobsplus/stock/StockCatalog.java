package com.daqem.jobsplus.stock;

import java.util.List;

/**
 * 거래 가능한 종목 정의 목록.
 * <p>
 * 종목 구성은 시세와 달리 변하지 않으므로 네트워크로 주고받지 않고 양쪽 코드에 공통으로 둔다.
 * 덕분에 클라이언트는 서버 스냅샷이 아직 도착하지 않은 상태에서도 표 레이아웃과 종목명을 그릴 수 있다.
 */
public final class StockCatalog
{
    public enum Market
    {
        YAHOO,
        UPBIT
    }

    public record StockDefinition(String id, String name, String symbol, Market market)
    {
    }

    private static final List<StockDefinition> STOCKS = List.of(
            new StockDefinition("AAPL", "애플", "AAPL", Market.YAHOO),
            new StockDefinition("MSFT", "마이크로소프트", "MSFT", Market.YAHOO),
            new StockDefinition("005930", "삼성전자", "005930.KS", Market.YAHOO),
            new StockDefinition("000660", "SK하이닉스", "000660.KS", Market.YAHOO),
            new StockDefinition("009150", "삼성전기", "009150.KS", Market.YAHOO),
            new StockDefinition("006400", "삼성SDI", "006400.KS", Market.YAHOO),
            new StockDefinition("012450", "한화에어로스페이스", "012450.KS", Market.YAHOO),
            new StockDefinition("066570", "LG전자", "066570.KS", Market.YAHOO),
            new StockDefinition("BTC", "비트코인", "KRW-BTC", Market.UPBIT),
            new StockDefinition("ETH", "이더리움", "KRW-ETH", Market.UPBIT),
            new StockDefinition("389680", "유디엠텍", "389680.KQ", Market.YAHOO),
            new StockDefinition("035420", "네이버", "035420.KS", Market.YAHOO),
            new StockDefinition("005380", "현대차", "005380.KS", Market.YAHOO),
            new StockDefinition("NVDA", "엔비디아", "NVDA", Market.YAHOO),
            new StockDefinition("AVGO", "브로드컴", "AVGO", Market.YAHOO),
            new StockDefinition("INTC", "인텔", "INTC", Market.YAHOO),
            new StockDefinition("QCOM", "퀄컴", "QCOM", Market.YAHOO),
            new StockDefinition("GOOGL", "알파벳", "GOOGL", Market.YAHOO),
            new StockDefinition("TSLA", "테슬라", "TSLA", Market.YAHOO),
            new StockDefinition("AMZN", "아마존", "AMZN", Market.YAHOO),
            new StockDefinition("META", "메타", "META", Market.YAHOO),
            new StockDefinition("NFLX", "넷플릭스", "NFLX", Market.YAHOO),
            new StockDefinition("KO", "코카콜라", "KO", Market.YAHOO),
            new StockDefinition("DIS", "디즈니", "DIS", Market.YAHOO),
            new StockDefinition("TSM", "TSMC", "TSM", Market.YAHOO),
            new StockDefinition("035720", "카카오", "035720.KS", Market.YAHOO),
            new StockDefinition("DOGE", "도지코인", "KRW-DOGE", Market.UPBIT),
            new StockDefinition("XRP", "리플", "KRW-XRP", Market.UPBIT),
            new StockDefinition("SOL", "솔라나", "KRW-SOL", Market.UPBIT)
    );

    private StockCatalog()
    {
    }

    public static List<StockDefinition> getStocks()
    {
        return STOCKS;
    }

    public static StockDefinition getStock(String stockId)
    {
        return STOCKS.stream()
                .filter(stock -> stock.id().equals(stockId))
                .findFirst()
                .orElse(null);
    }

    public static StockDefinition getStockBySymbol(String symbol)
    {
        return STOCKS.stream()
                .filter(stock -> stock.symbol().equals(symbol))
                .findFirst()
                .orElse(null);
    }

    public static String getStockName(String stockId)
    {
        StockDefinition stock = getStock(stockId);
        return stock == null ? stockId : stock.name();
    }
}
