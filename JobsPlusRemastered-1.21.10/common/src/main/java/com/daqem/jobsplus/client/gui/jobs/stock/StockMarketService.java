package com.daqem.jobsplus.client.gui.jobs.stock;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

public final class StockMarketService
{
    private static final long REFRESH_INTERVAL_MILLIS = 60_000L;
    private static final String YAHOO_CHART_URL =
            "https://query1.finance.yahoo.com/v8/finance/chart/%s?interval=1m&range=1d";
    private static final String UPBIT_TICKER_URL =
            "https://api.upbit.com/v1/ticker?markets=KRW-BTC,KRW-ETH,KRW-DOGE,KRW-XRP,KRW-SOL";

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
            new StockDefinition("SPCX", "스페이스X", "SPCX", Market.YAHOO),
            new StockDefinition("DOGE", "도지코인", "KRW-DOGE", Market.UPBIT),
            new StockDefinition("XRP", "리플", "KRW-XRP", Market.UPBIT),
            new StockDefinition("SOL", "솔라나", "KRW-SOL", Market.UPBIT)
    );
    private static final StockMarketService INSTANCE = new StockMarketService();

    private final HttpClient httpClient;
    private final ExecutorService refreshExecutor;
    private final AtomicBoolean refreshing = new AtomicBoolean();
    private volatile List<StockQuote> quotes;
    private volatile long lastRefreshAttempt;
    private volatile long lastSuccessfulRefresh;

    private StockMarketService()
    {
        this.refreshExecutor = Executors.newSingleThreadExecutor(runnable -> {
            Thread thread = new Thread(runnable, "JobsPlus-StockMarket");
            thread.setDaemon(true);
            return thread;
        });
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(8))
                .build();
        this.quotes = STOCKS.stream()
                .map(stock -> StockQuote.loading(stock.id(), stock.name()))
                .toList();
    }

    public static StockMarketService getInstance()
    {
        return INSTANCE;
    }

    public List<StockQuote> getQuotes()
    {
        return this.quotes;
    }

    public StockQuote getQuote(String stockId)
    {
        return this.quotes.stream()
                .filter(quote -> quote.id().equals(stockId))
                .findFirst()
                .orElse(null);
    }

    public long getLastSuccessfulRefresh()
    {
        return this.lastSuccessfulRefresh;
    }

    public boolean isRefreshing()
    {
        return this.refreshing.get();
    }

    public void refreshIfNeeded()
    {
        long now = System.currentTimeMillis();
        if (now - this.lastRefreshAttempt < REFRESH_INTERVAL_MILLIS || !this.refreshing.compareAndSet(false, true))
        {
            return;
        }

        this.lastRefreshAttempt = now;
        this.refreshExecutor.execute(() -> {
            try
            {
                this.refreshQuotes();
            }
            finally
            {
                this.refreshing.set(false);
            }
        });
    }

    private void refreshQuotes()
    {
        Map<String, StockQuote> refreshedQuotes = new LinkedHashMap<>();
        this.quotes.forEach(quote -> refreshedQuotes.put(quote.id(), quote));

        double usdKrw = 0;
        try
        {
            usdKrw = this.fetchYahooPrice("KRW=X").price();
        }
        catch (IOException | InterruptedException | RuntimeException ignored)
        {
        }

        for (StockDefinition stock : STOCKS)
        {
            if (stock.market() != Market.YAHOO)
            {
                continue;
            }

            try
            {
                YahooQuote yahooQuote = this.fetchYahooPrice(stock.symbol());
                boolean isKoreanStock = stock.symbol().endsWith(".KS") || stock.symbol().endsWith(".KQ");
                if (!isKoreanStock && usdKrw == 0)
                {
                    continue;
                }
                double priceKrw = isKoreanStock ? yahooQuote.price() : yahooQuote.price() * usdKrw;
                refreshedQuotes.put(
                        stock.id(),
                        new StockQuote(stock.id(), stock.name(), priceKrw, yahooQuote.percentChange(), true)
                );
            }
            catch (IOException | InterruptedException | RuntimeException ignored)
            {
            }
        }

        try
        {
            this.fetchUpbitPrices(refreshedQuotes);
        }
        catch (IOException | InterruptedException | RuntimeException ignored)
        {
        }

        List<StockQuote> orderedQuotes = new ArrayList<>(STOCKS.size());
        for (StockDefinition stock : STOCKS)
        {
            orderedQuotes.add(refreshedQuotes.get(stock.id()));
        }
        this.quotes = List.copyOf(orderedQuotes);
        if (this.quotes.stream().anyMatch(StockQuote::available))
        {
            this.lastSuccessfulRefresh = System.currentTimeMillis();
        }
    }

    private YahooQuote fetchYahooPrice(String symbol) throws IOException, InterruptedException
    {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(YAHOO_CHART_URL.formatted(symbol)))
                .timeout(Duration.ofSeconds(10))
                .header("Accept", "application/json")
                .header("User-Agent", "JobsPlus/1.0")
                .GET()
                .build();
        HttpResponse<String> response = this.httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200)
        {
            throw new IOException("Stock quote response status: " + response.statusCode());
        }

        JsonObject chart = JsonParser.parseString(response.body()).getAsJsonObject().getAsJsonObject("chart");
        JsonArray result = chart.getAsJsonArray("result");
        if (result == null || result.isEmpty())
        {
            throw new IOException("Stock quote result is empty");
        }

        JsonObject meta = result.get(0).getAsJsonObject().getAsJsonObject("meta");
        double price = getRequiredDouble(meta, "regularMarketPrice");
        double previousClose = getPreviousClose(meta);
        double percentChange = previousClose == 0 ? 0 : (price - previousClose) / previousClose * 100;
        return new YahooQuote(price, percentChange);
    }

    private void fetchUpbitPrices(Map<String, StockQuote> refreshedQuotes) throws IOException, InterruptedException
    {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(UPBIT_TICKER_URL))
                .timeout(Duration.ofSeconds(10))
                .header("Accept", "application/json")
                .header("User-Agent", "JobsPlus/1.0")
                .GET()
                .build();
        HttpResponse<String> response = this.httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200)
        {
            throw new IOException("Upbit ticker response status: " + response.statusCode());
        }

        JsonArray tickers = JsonParser.parseString(response.body()).getAsJsonArray();
        for (JsonElement tickerElement : tickers)
        {
            JsonObject ticker = tickerElement.getAsJsonObject();
            String market = ticker.get("market").getAsString();
            StockDefinition stock = STOCKS.stream()
                    .filter(definition -> definition.symbol().equals(market))
                    .findFirst()
                    .orElse(null);
            if (stock == null)
            {
                continue;
            }

            refreshedQuotes.put(
                    stock.id(),
                    new StockQuote(
                            stock.id(),
                            stock.name(),
                            ticker.get("trade_price").getAsDouble(),
                            ticker.get("signed_change_rate").getAsDouble() * 100,
                            true
                    )
            );
        }
    }

    private static double getPreviousClose(JsonObject meta)
    {
        if (meta.has("chartPreviousClose") && !meta.get("chartPreviousClose").isJsonNull())
        {
            return meta.get("chartPreviousClose").getAsDouble();
        }
        return getRequiredDouble(meta, "previousClose");
    }

    private static double getRequiredDouble(JsonObject object, String key)
    {
        if (!object.has(key) || object.get(key).isJsonNull())
        {
            throw new IllegalStateException("Missing stock quote field: " + key);
        }
        return object.get(key).getAsDouble();
    }

    private enum Market
    {
        YAHOO,
        UPBIT
    }

    private record StockDefinition(String id, String name, String symbol, Market market)
    {
    }

    private record YahooQuote(double price, double percentChange)
    {
    }
}
