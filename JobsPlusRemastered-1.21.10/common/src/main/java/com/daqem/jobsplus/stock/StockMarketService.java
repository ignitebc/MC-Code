package com.daqem.jobsplus.stock;

import com.daqem.jobsplus.JobsPlus;
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

/**
 * 시세 조회 및 스냅샷 확정을 담당한다.
 * <p>
 * <b>서버 전용이다.</b> 클라이언트는 API를 직접 조회하지 않고 서버가 보내 준
 * {@link StockMarketSnapshot}만 사용한다. 클라이언트가 따로 조회하면 표에 보이는 가격과
 * 서버가 체결에 쓰는 가격이 서로 달라질 수 있기 때문이다.
 * <p>
 * 갱신은 {@link com.daqem.jobsplus.event.stock.StockMarketTicker}가 서버 틱마다 호출하는
 * {@link #refreshIfNeeded()}로만 이루어지며, 실제 HTTP 요청은 {@link #REFRESH_INTERVAL_MILLIS}에
 * 한 번으로 제한된다. 조회가 끝나면 스냅샷을 통째로 교체하므로 1분 동안은 모든 플레이어가
 * 동일한 가격을 본다.
 */
public final class StockMarketService
{
    private static final long REFRESH_INTERVAL_MILLIS = 60_000L;
    private static final String YAHOO_CHART_URL =
            "https://query1.finance.yahoo.com/v8/finance/chart/%s?interval=1m&range=1d";
    private static final String UPBIT_TICKER_URL =
            "https://api.upbit.com/v1/ticker?markets=KRW-BTC,KRW-ETH,KRW-DOGE,KRW-XRP,KRW-SOL";

    private static final StockMarketService INSTANCE = new StockMarketService();

    private final HttpClient httpClient;
    private final ExecutorService refreshExecutor;
    private final AtomicBoolean refreshing = new AtomicBoolean();
    private volatile StockMarketSnapshot snapshot;
    private volatile long lastRefreshAttempt;

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
        this.snapshot = new StockMarketSnapshot(0, 0, StockCatalog.getStocks().stream()
                .map(stock -> StockQuote.loading(stock.id(), stock.name()))
                .toList());
    }

    public static StockMarketService getInstance()
    {
        return INSTANCE;
    }

    /**
     * 현재 확정된 스냅샷. 매수·매도는 반드시 이 값만 사용해야 한다.
     */
    public StockMarketSnapshot getSnapshot()
    {
        return this.snapshot;
    }

    /**
     * 마지막 조회로부터 {@link #REFRESH_INTERVAL_MILLIS}가 지났으면 갱신을 시작한다.
     * <p>
     * 조회는 별도 스레드에서 진행되며, 성공해야만 스냅샷이 교체된다. 조회가 끝나기 전까지는
     * 이전 스냅샷이 그대로 유지되므로 이 메서드를 호출한 직후에 가격이 바뀌는 일은 없다.
     */
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
        StockMarketSnapshot previousSnapshot = this.snapshot;
        Map<String, StockQuote> refreshedQuotes = new LinkedHashMap<>();
        previousSnapshot.quotes().forEach(quote -> refreshedQuotes.put(quote.id(), quote));

        long fetchedAt = System.currentTimeMillis();
        int successCount = 0;

        double usdKrw = 0;
        try
        {
            usdKrw = this.fetchYahooPrice("KRW=X").price();
        }
        catch (IOException | InterruptedException | RuntimeException e)
        {
            JobsPlus.LOGGER.warn("Failed to fetch USD/KRW exchange rate: {}", e.toString());
            restoreInterruptFlag(e);
        }

        for (StockCatalog.StockDefinition stock : StockCatalog.getStocks())
        {
            if (stock.market() != StockCatalog.Market.YAHOO)
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
                        new StockQuote(stock.id(), stock.name(), priceKrw, yahooQuote.percentChange(), true, fetchedAt)
                );
                successCount++;
            }
            catch (IOException | InterruptedException | RuntimeException e)
            {
                JobsPlus.LOGGER.warn("Failed to fetch Yahoo quote for {} ({}): {}", stock.id(), stock.symbol(), e.toString());
                restoreInterruptFlag(e);
            }
        }

        try
        {
            successCount += this.fetchUpbitPrices(refreshedQuotes, fetchedAt);
        }
        catch (IOException | InterruptedException | RuntimeException e)
        {
            JobsPlus.LOGGER.warn("Failed to fetch Upbit tickers: {}", e.toString());
            restoreInterruptFlag(e);
        }

        // 한 종목도 못 받았으면 스냅샷을 교체하지 않는다.
        // 버전이 그대로 유지되므로 진행 중인 거래가 불필요하게 취소되지 않고,
        // 각 시세의 updatedAt이 그대로 늙어 일정 시간 후 자동으로 거래가 차단된다.
        if (successCount == 0)
        {
            JobsPlus.LOGGER.warn("Stock market refresh failed for every symbol. Keeping snapshot version {}.",
                    previousSnapshot.version());
            return;
        }

        List<StockQuote> orderedQuotes = new ArrayList<>(StockCatalog.getStocks().size());
        for (StockCatalog.StockDefinition stock : StockCatalog.getStocks())
        {
            StockQuote quote = refreshedQuotes.get(stock.id());
            orderedQuotes.add(quote != null ? quote : StockQuote.loading(stock.id(), stock.name()));
        }
        this.snapshot = new StockMarketSnapshot(
                previousSnapshot.version() + 1, fetchedAt, List.copyOf(orderedQuotes));
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

    private int fetchUpbitPrices(Map<String, StockQuote> refreshedQuotes, long fetchedAt)
            throws IOException, InterruptedException
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

        int successCount = 0;
        JsonArray tickers = JsonParser.parseString(response.body()).getAsJsonArray();
        for (JsonElement tickerElement : tickers)
        {
            JsonObject ticker = tickerElement.getAsJsonObject();
            StockCatalog.StockDefinition stock = StockCatalog.getStockBySymbol(ticker.get("market").getAsString());
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
                            true,
                            fetchedAt
                    )
            );
            successCount++;
        }
        return successCount;
    }

    private static void restoreInterruptFlag(Exception e)
    {
        if (e instanceof InterruptedException)
        {
            Thread.currentThread().interrupt();
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

    private record YahooQuote(double price, double percentChange)
    {
    }
}
