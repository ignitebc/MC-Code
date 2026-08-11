package com.daqem.jobsplus.stock;

import com.daqem.jobsplus.JobsPlus;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 시세 조회와 분 단위 스냅샷 확정을 담당한다.
 * <p>
 * <b>서버 전용이다.</b> 클라이언트는 API를 직접 조회하지 않고 서버가 보내 준
 * {@link StockMarketSnapshot}만 사용한다. 클라이언트가 따로 조회하면 표에 보이는 가격과
 * 서버가 체결에 쓰는 가격이 서로 달라질 수 있기 때문이다.
 * <p>
 * 갱신 시점은 {@link com.daqem.jobsplus.event.stock.StockMarketTicker}가 서버 시스템 시간의
 * 분 경계에서 {@link #refreshForMinute(long, Map)}을 호출해 결정한다. 이 클래스는 "이전 조회로부터
 * 몇 초가 지났는가"를 따지지 않는다.
 * <p>
 * 조회를 시작하는 즉시 모든 종목을 거래 불가로 만든 {@link SnapshotStatus#REFRESHING} 스냅샷을
 * 내보내므로, 응답이 도착하기 전까지는 이전 분 가격으로 거래할 수 없다.
 */
public final class StockMarketService
{
    /** 한 분의 조회에 허용하는 총 시간. 다음 분 경계를 넘기지 않도록 넉넉히 잡되 1분보다 짧아야 한다. */
    private static final long FETCH_DEADLINE_MILLIS = 45_000L;
    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(10);
    private static final String YAHOO_CHART_URL =
            "https://query1.finance.yahoo.com/v8/finance/chart/%s?interval=1m&range=1d";
    private static final String YAHOO_HISTORY_URL =
            "https://query1.finance.yahoo.com/v8/finance/chart/%s?period1=%d&period2=%d&interval=1m"
                    + "&includePrePost=false";
    private static final String YAHOO_EXCHANGE_RATE_SYMBOL = "KRW=X";
    private static final String UPBIT_TICKER_URL =
            "https://api.upbit.com/v1/ticker?markets=KRW-BTC,KRW-ETH,KRW-DOGE,KRW-XRP,KRW-SOL";
    private static final String UPBIT_MINUTE_CANDLE_URL =
            "https://api.upbit.com/v1/candles/minutes/1?market=%s&to=%s&count=200";

    /**
     * 한 번의 갱신에서 종목별로 복구할 최대 분 수.
     * Upbit 분봉 API의 요청당 최대 개수와 맞추며, 긴 공백은 거래를 막은 상태로 매분 순차 복구한다.
     */
    private static final long MAX_HISTORY_MINUTES_PER_REFRESH = 200L;

    private static final StockMarketService INSTANCE = new StockMarketService();

    private final HttpClient httpClient;
    private final ExecutorService refreshExecutor;
    private final AtomicLong requestedMinute = new AtomicLong(Long.MIN_VALUE);
    private final AtomicLong versionCounter = new AtomicLong();

    /**
     * 시세 세션 번호. 시청자·미결제 포지션·예약 주문이 모두 없어 세션이 끝나거나 새로 시작되면
     * 값이 올라간다.
     * 조회를 시작할 때의 번호와 끝났을 때의 번호가 다르면 그 결과는 버린다.
     */
    private final AtomicLong sessionGeneration = new AtomicLong();
    private volatile boolean sessionActive;

    /**
     * 스냅샷 교체 잠금.
     * <p>
     * 갱신 시작(서버 스레드)과 조회 완료(작업 스레드)가 동시에 스냅샷을 건드리면, 지난 분 결과가
     * 새 분의 갱신 중 스냅샷을 덮어써 표에 지난 가격이 남을 수 있다. 두 경로를 같은 잠금으로 묶어
     * 유효성 검사와 교체가 끊기지 않게 한다.
     */
    private final Object snapshotLock = new Object();
    private volatile StockMarketSnapshot snapshot = StockMarketSnapshot.EMPTY;
    private volatile Map<String, StockPriceWindow> priceWindows = Map.of();

    private StockMarketService()
    {
        // 분 경계 직전에 진입 조회가 시작되면 다음 분 조회와 겹칠 수 있다.
        // 단일 스레드면 새 분 조회가 이전 조회를 기다리게 되므로 여유분을 둔다.
        // 대기 큐는 1개로 제한한다. 실행 중 2개에 대기 1개를 넘게 쌓일 이유가 없고,
        // 큐가 가득 차면 가장 오래된 대기 조회를 버려 최신 분 조회가 밀리지 않게 한다.
        // 버려진 분은 다음 분 갱신에서 자연히 복구된다. 조회가 없는 동안 유휴 스레드는 회수한다.
        ThreadPoolExecutor boundedExecutor = new ThreadPoolExecutor(
                2, 2, 60L, TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(1),
                runnable -> {
                    Thread thread = new Thread(runnable, "JobsPlus-StockMarket");
                    thread.setDaemon(true);
                    return thread;
                },
                new ThreadPoolExecutor.DiscardOldestPolicy());
        boundedExecutor.allowCoreThreadTimeOut(true);
        this.refreshExecutor = boundedExecutor;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(8))
                .build();
    }

    public static StockMarketService getInstance()
    {
        return INSTANCE;
    }

    /**
     * 현재 확정된 스냅샷. 판매 체결과 구매 예약 검증은 반드시 이 값만 사용해야 한다.
     */
    public StockMarketSnapshot getSnapshot()
    {
        return this.snapshot;
    }

    public Map<String, StockPriceWindow> getPriceWindows()
    {
        return this.priceWindows;
    }

    /**
     * 첫 시청자가 들어오거나 첫 미결제 포지션·예약 주문이 생겼을 때 시세 조회를 시작한다.
     * <p>
     * 새 세션 번호를 부여해서, 이전 세션에서 돌고 있던 조회가 뒤늦게 끝나도 반영되지 않게 한다.
     */
    public void startSessionAndRefresh(long targetMinute, Map<String, Long> lastCheckedMinutes)
    {
        long generation;
        synchronized (this.snapshotLock)
        {
            generation = this.sessionGeneration.incrementAndGet();
            this.sessionActive = true;
            this.requestedMinute.set(targetMinute);
            this.priceWindows = Map.of();
            this.snapshot = StockMarketSnapshot.refreshing(this.versionCounter.incrementAndGet(), targetMinute);
        }
        Map<String, Long> monitoredPositions = Map.copyOf(lastCheckedMinutes);
        this.refreshExecutor.execute(() -> this.fetchAndPublish(targetMinute, generation, monitoredPositions));
    }

    /**
     * 시청자·미결제 포지션·예약 주문이 모두 없어졌거나 서버가 멈출 때 호출한다.
     * 진행 중인 조회 결과는 세션 번호가 어긋나므로 자동으로 폐기된다.
     */
    public void stopSession()
    {
        synchronized (this.snapshotLock)
        {
            this.sessionGeneration.incrementAndGet();
            this.sessionActive = false;
            this.requestedMinute.set(Long.MIN_VALUE);
            this.priceWindows = Map.of();
            this.snapshot = StockMarketSnapshot.EMPTY;
        }
    }

    /**
     * 지정한 분의 시세 조회를 시작한다.
     * <p>
     * 호출 즉시 해당 분의 {@link SnapshotStatus#REFRESHING} 스냅샷으로 교체하여 거래를 중지시키고,
     * 실제 조회는 별도 스레드에서 진행한다. 같은 분이나 지난 분에 대한 요청은 무시한다.
     * <p>
     * 세션 상태 확인과 스냅샷 교체를 같은 잠금 안에서 처리한다. 검사만 잠금 밖에서 하면
     * 그 사이에 세션이 끝났을 때 아무도 보지 않는 갱신 중 스냅샷이 남을 수 있다.
     */
    public void refreshForMinute(long targetMinute, Map<String, Long> lastCheckedMinutes)
    {
        long generation;
        synchronized (this.snapshotLock)
        {
            if (!this.sessionActive)
            {
                return;
            }
            if (targetMinute <= this.requestedMinute.get())
            {
                return;
            }

            this.requestedMinute.set(targetMinute);
            generation = this.sessionGeneration.get();
            this.priceWindows = Map.of();
            this.snapshot = StockMarketSnapshot.refreshing(this.versionCounter.incrementAndGet(), targetMinute);
        }
        Map<String, Long> monitoredPositions = Map.copyOf(lastCheckedMinutes);
        this.refreshExecutor.execute(() -> this.fetchAndPublish(targetMinute, generation, monitoredPositions));
    }

    private void fetchAndPublish(long targetMinute, long generation, Map<String, Long> lastCheckedMinutes)
    {
        try
        {
            this.publishResult(targetMinute, generation, this.fetchMarketData(targetMinute, lastCheckedMinutes));
        }
        catch (RuntimeException e)
        {
            JobsPlus.LOGGER.error("Stock market refresh failed for minute {}", targetMinute, e);
            this.publishResult(
                    targetMinute,
                    generation,
                    new MarketFetchResult(buildUnavailableQuotes(), Map.of())
            );
        }
    }

    /**
     * 모든 요청을 동시에 보내고 제한 시간 안에 도착한 응답만 사용한다.
     * <p>
     * 종목을 하나씩 순차 조회하면 최악의 경우 1분을 넘겨 그 분의 시세를 쓸 수 없게 된다.
     */
    private MarketFetchResult fetchMarketData(long targetMinute, Map<String, Long> lastCheckedMinutes)
    {
        Map<String, StockQuote> quotes = buildUnavailableQuotes();
        long deadline = System.currentTimeMillis() + FETCH_DEADLINE_MILLIS;

        CompletableFuture<HttpResponse<String>> exchangeRateRequest =
                this.requestAsync(YAHOO_CHART_URL.formatted(YAHOO_EXCHANGE_RATE_SYMBOL));
        Map<String, CompletableFuture<HttpResponse<String>>> yahooRequests = new LinkedHashMap<>();
        for (StockCatalog.StockDefinition stock : StockCatalog.getStocks())
        {
            if (stock.market() == StockCatalog.Market.YAHOO)
            {
                yahooRequests.put(stock.id(), this.requestAsync(YAHOO_CHART_URL.formatted(stock.symbol())));
            }
        }
        CompletableFuture<HttpResponse<String>> upbitRequest = this.requestAsync(UPBIT_TICKER_URL);

        double usdKrw = readExchangeRate(exchangeRateRequest, deadline);
        for (StockCatalog.StockDefinition stock : StockCatalog.getStocks())
        {
            CompletableFuture<HttpResponse<String>> request = yahooRequests.get(stock.id());
            if (request == null)
            {
                continue;
            }

            StockQuote quote = readYahooQuote(stock, request, usdKrw, deadline);
            if (quote != null)
            {
                quotes.put(stock.id(), quote);
            }
        }
        readUpbitQuotes(upbitRequest, quotes, deadline);
        Map<String, StockPriceWindow> fetchedPriceWindows =
                this.fetchPriceWindows(targetMinute, lastCheckedMinutes, usdKrw, deadline);

        JobsPlus.LOGGER.debug("Stock market minute {} fetched {} of {} symbols", targetMinute,
                quotes.values().stream().filter(StockQuote::available).count(), quotes.size());
        return new MarketFetchResult(Map.copyOf(quotes), fetchedPriceWindows);
    }

    /**
     * 마지막 검사 이후부터 직전 완료 분까지의 분봉 저가·고가를 조회한다.
     * 현재가 한 점이 아니라 이 범위로 청산을 판정해야, 가격이 청산선을 통과한 뒤 회복해도 포지션이
     * 살아남지 않는다. 아직 진행 중인 현재 분은 완료 처리하지 않고 현재가 검사만 별도로 수행한다.
     */
    private Map<String, StockPriceWindow> fetchPriceWindows(long targetMinute,
                                                            Map<String, Long> lastCheckedMinutes,
                                                            double usdKrw,
                                                            long deadline)
    {
        Map<String, CompletableFuture<HttpResponse<String>>> requests = new LinkedHashMap<>();
        Map<String, CompletableFuture<HttpResponse<String>>> exchangeRateRequests = new LinkedHashMap<>();
        Map<String, Long> checkedThroughMinutes = new LinkedHashMap<>();
        long lastCompletedMinute = targetMinute - 1;

        for (Map.Entry<String, Long> entry : lastCheckedMinutes.entrySet())
        {
            String stockId = entry.getKey();
            long lastCheckedMinute = entry.getValue();
            if (lastCheckedMinute >= lastCompletedMinute)
            {
                continue;
            }

            StockCatalog.StockDefinition stock = StockCatalog.getStock(stockId);
            if (stock == null)
            {
                continue;
            }

            long checkedThroughMinute = Math.min(
                    lastCompletedMinute,
                    lastCheckedMinute + MAX_HISTORY_MINUTES_PER_REFRESH
            );
            checkedThroughMinutes.put(stockId, checkedThroughMinute);
            requests.put(
                    stockId,
                    this.requestAsync(buildHistoryUrl(stock, lastCheckedMinute, checkedThroughMinute))
            );
            if (stock.market() == StockCatalog.Market.YAHOO && !isKoreanStock(stock))
            {
                exchangeRateRequests.put(
                        stockId,
                        this.requestAsync(buildYahooHistoryUrl(
                                YAHOO_EXCHANGE_RATE_SYMBOL,
                                lastCheckedMinute,
                                checkedThroughMinute
                        ))
                );
            }
        }

        Map<String, StockPriceWindow> priceWindows = new LinkedHashMap<>();
        for (Map.Entry<String, CompletableFuture<HttpResponse<String>>> requestEntry : requests.entrySet())
        {
            String stockId = requestEntry.getKey();
            StockCatalog.StockDefinition stock = StockCatalog.getStock(stockId);
            long fromExclusiveMinute = lastCheckedMinutes.get(stockId);
            long checkedThroughMinute = checkedThroughMinutes.get(stockId);

            String body = awaitBody(requestEntry.getValue(), deadline, stock.symbol() + " history");
            if (body == null)
            {
                continue;
            }

            StockPriceWindow priceWindow;
            if (stock.market() == StockCatalog.Market.YAHOO)
            {
                String exchangeRateBody = null;
                CompletableFuture<HttpResponse<String>> exchangeRateRequest = exchangeRateRequests.get(stockId);
                if (exchangeRateRequest != null)
                {
                    exchangeRateBody = awaitBody(
                            exchangeRateRequest,
                            deadline,
                            stock.symbol() + " exchange rate history"
                    );
                    if (exchangeRateBody == null)
                    {
                        continue;
                    }
                }
                priceWindow = parseYahooPriceWindow(
                        stockId,
                        body,
                        fromExclusiveMinute,
                        checkedThroughMinute,
                        exchangeRateBody,
                        usdKrw
                );
            }
            else
            {
                priceWindow = parseUpbitPriceWindow(
                        stockId,
                        body,
                        fromExclusiveMinute,
                        checkedThroughMinute
                );
            }

            if (priceWindow != null && priceWindow.hasCompleteCoverage())
            {
                priceWindows.put(stockId, priceWindow);
            }
        }
        return Map.copyOf(priceWindows);
    }

    private static String buildHistoryUrl(StockCatalog.StockDefinition stock, long fromExclusiveMinute,
                                          long checkedThroughMinute)
    {
        if (stock.market() == StockCatalog.Market.YAHOO)
        {
            return buildYahooHistoryUrl(stock.symbol(), fromExclusiveMinute, checkedThroughMinute);
        }

        long endExclusiveSeconds = Math.multiplyExact(checkedThroughMinute + 1, 60L);
        String endExclusiveTime = Instant.ofEpochSecond(endExclusiveSeconds).toString();
        String encodedEndTime = URLEncoder.encode(endExclusiveTime, StandardCharsets.UTF_8);
        return UPBIT_MINUTE_CANDLE_URL.formatted(stock.symbol(), encodedEndTime);
    }

    private static String buildYahooHistoryUrl(String symbol, long fromExclusiveMinute, long checkedThroughMinute)
    {
        long periodStartSeconds = Math.multiplyExact(fromExclusiveMinute + 1, 60L);
        long periodEndSeconds = Math.multiplyExact(checkedThroughMinute + 1, 60L);
        return YAHOO_HISTORY_URL.formatted(symbol, periodStartSeconds, periodEndSeconds);
    }

    private static boolean isKoreanStock(StockCatalog.StockDefinition stock)
    {
        if (stock.symbol().endsWith(".KS"))
        {
            return true;
        }
        if (stock.symbol().endsWith(".KQ"))
        {
            return true;
        }
        return false;
    }

    private static StockPriceWindow parseYahooPriceWindow(String stockId, String body,
                                                           long fromExclusiveMinute,
                                                           long checkedThroughMinute,
                                                           String exchangeRateBody,
                                                           double fallbackExchangeRate)
    {
        StockCatalog.StockDefinition stock = StockCatalog.getStock(stockId);
        if (stock == null)
        {
            return null;
        }

        try
        {
            List<StockPriceWindow.StockPriceCandle> nativeCandles =
                    parseYahooNativeCandles(body, fromExclusiveMinute, checkedThroughMinute);
            if (nativeCandles == null)
            {
                return null;
            }

            Map<Long, StockPriceWindow.StockPriceCandle> exchangeRateCandles = Map.of();
            if (exchangeRateBody != null)
            {
                List<StockPriceWindow.StockPriceCandle> parsedExchangeRateCandles =
                        parseYahooNativeCandles(exchangeRateBody, fromExclusiveMinute, checkedThroughMinute);
                if (parsedExchangeRateCandles == null)
                {
                    return null;
                }
                Map<Long, StockPriceWindow.StockPriceCandle> exchangeRateCandlesByMinute = new LinkedHashMap<>();
                for (StockPriceWindow.StockPriceCandle candle : parsedExchangeRateCandles)
                {
                    exchangeRateCandlesByMinute.put(candle.marketMinute(), candle);
                }
                exchangeRateCandles = Map.copyOf(exchangeRateCandlesByMinute);
            }

            List<StockPriceWindow.StockPriceCandle> convertedCandles = new ArrayList<>();
            for (StockPriceWindow.StockPriceCandle nativeCandle : nativeCandles)
            {
                if (isKoreanStock(stock))
                {
                    convertedCandles.add(nativeCandle);
                    continue;
                }

                double openExchangeRate = fallbackExchangeRate;
                double lowExchangeRate = fallbackExchangeRate;
                double highExchangeRate = fallbackExchangeRate;
                StockPriceWindow.StockPriceCandle exchangeRateCandle =
                        exchangeRateCandles.get(nativeCandle.marketMinute());
                if (exchangeRateBody != null && exchangeRateCandle == null)
                {
                    return null;
                }
                if (exchangeRateCandle != null)
                {
                    openExchangeRate = exchangeRateCandle.openPrice();
                    lowExchangeRate = exchangeRateCandle.lowPrice();
                    highExchangeRate = exchangeRateCandle.highPrice();
                }
                if (!Double.isFinite(openExchangeRate) || openExchangeRate <= 0
                        || !Double.isFinite(lowExchangeRate) || lowExchangeRate <= 0
                        || !Double.isFinite(highExchangeRate) || highExchangeRate <= 0)
                {
                    return null;
                }

                StockPriceWindow.StockPriceCandle convertedCandle = new StockPriceWindow.StockPriceCandle(
                        nativeCandle.marketMinute(),
                        nativeCandle.openPrice() * openExchangeRate,
                        nativeCandle.lowPrice() * lowExchangeRate,
                        nativeCandle.highPrice() * highExchangeRate
                );
                if (!convertedCandle.hasValidRange())
                {
                    return null;
                }
                convertedCandles.add(convertedCandle);
            }

            return new StockPriceWindow(
                    stockId,
                    fromExclusiveMinute,
                    checkedThroughMinute,
                    convertedCandles
            );
        }
        catch (RuntimeException e)
        {
            JobsPlus.LOGGER.warn("Failed to parse Yahoo price history for {}: {}", stockId, e.toString());
            return null;
        }
    }

    private static List<StockPriceWindow.StockPriceCandle> parseYahooNativeCandles(
            String body,
            long fromExclusiveMinute,
            long checkedThroughMinute)
    {
        JsonObject chart = JsonParser.parseString(body).getAsJsonObject().getAsJsonObject("chart");
        JsonArray results = chart.getAsJsonArray("result");
        if (results == null || results.isEmpty())
        {
            return null;
        }

        JsonObject result = results.get(0).getAsJsonObject();
        JsonArray timestamps = result.getAsJsonArray("timestamp");
        if (timestamps == null || timestamps.isEmpty())
        {
            return List.of();
        }

        JsonObject indicators = result.getAsJsonObject("indicators");
        if (indicators == null)
        {
            return null;
        }

        JsonArray quoteResults = indicators.getAsJsonArray("quote");
        if (quoteResults == null || quoteResults.isEmpty())
        {
            return null;
        }

        JsonObject quote = quoteResults.get(0).getAsJsonObject();
        JsonArray opens = quote.getAsJsonArray("open");
        JsonArray lows = quote.getAsJsonArray("low");
        JsonArray highs = quote.getAsJsonArray("high");
        if (opens == null || lows == null || highs == null)
        {
            return null;
        }
        if (timestamps.size() != opens.size()
                || timestamps.size() != lows.size()
                || timestamps.size() != highs.size())
        {
            return null;
        }

        List<StockPriceWindow.StockPriceCandle> candles = new ArrayList<>();
        for (int index = 0; index < timestamps.size(); index++)
        {
            JsonElement timestampElement = timestamps.get(index);
            JsonElement openElement = opens.get(index);
            JsonElement lowElement = lows.get(index);
            JsonElement highElement = highs.get(index);
            if (timestampElement.isJsonNull() || openElement.isJsonNull()
                    || lowElement.isJsonNull() || highElement.isJsonNull())
            {
                return null;
            }

            long candleMinute = Math.floorDiv(timestampElement.getAsLong(), 60L);
            if (candleMinute <= fromExclusiveMinute || candleMinute > checkedThroughMinute)
            {
                continue;
            }

            StockPriceWindow.StockPriceCandle candle = new StockPriceWindow.StockPriceCandle(
                    candleMinute,
                    openElement.getAsDouble(),
                    lowElement.getAsDouble(),
                    highElement.getAsDouble()
            );
            if (!candle.hasValidRange())
            {
                return null;
            }
            candles.add(candle);
        }
        return List.copyOf(candles);
    }

    private static StockPriceWindow parseUpbitPriceWindow(String stockId, String body,
                                                           long fromExclusiveMinute,
                                                           long checkedThroughMinute)
    {
        try
        {
            JsonArray candles = JsonParser.parseString(body).getAsJsonArray();
            List<StockPriceWindow.StockPriceCandle> priceCandles = new ArrayList<>();
            for (JsonElement candleElement : candles)
            {
                JsonObject candle = candleElement.getAsJsonObject();
                String candleTime = candle.get("candle_date_time_utc").getAsString();
                long candleMinute = LocalDateTime.parse(candleTime).toEpochSecond(ZoneOffset.UTC) / 60L;
                if (candleMinute <= fromExclusiveMinute || candleMinute > checkedThroughMinute)
                {
                    continue;
                }

                double candleOpen = candle.get("opening_price").getAsDouble();
                double candleLow = candle.get("low_price").getAsDouble();
                double candleHigh = candle.get("high_price").getAsDouble();
                StockPriceWindow.StockPriceCandle priceCandle =
                        new StockPriceWindow.StockPriceCandle(
                                candleMinute,
                                candleOpen,
                                candleLow,
                                candleHigh
                        );
                if (!priceCandle.hasValidRange())
                {
                    return null;
                }
                priceCandles.add(priceCandle);
            }

            return new StockPriceWindow(
                    stockId,
                    fromExclusiveMinute,
                    checkedThroughMinute,
                    priceCandles
            );
        }
        catch (RuntimeException e)
        {
            JobsPlus.LOGGER.warn("Failed to parse Upbit price history for {}: {}", stockId, e.toString());
            return null;
        }
    }

    private void publishResult(long targetMinute, long generation, MarketFetchResult result)
    {
        Map<String, StockQuote> quotes = result.quotes();
        List<StockQuote> orderedQuotes = new ArrayList<>(StockCatalog.getStocks().size());
        for (StockCatalog.StockDefinition stock : StockCatalog.getStocks())
        {
            StockQuote quote = quotes.get(stock.id());
            orderedQuotes.add(quote != null ? quote : StockQuote.unavailable(stock.id(), stock.name()));
        }

        // NaN이나 0 이하 가격은 available이어도 거래에 쓸 수 없으므로 성공으로 보지 않는다.
        boolean anySucceeded = orderedQuotes.stream().anyMatch(StockQuote::hasValidPrice);
        SnapshotStatus status = anySucceeded ? SnapshotStatus.READY : SnapshotStatus.FAILED;
        if (!anySucceeded)
        {
            JobsPlus.LOGGER.warn("Stock market refresh failed for every symbol at minute {}", targetMinute);
        }

        synchronized (this.snapshotLock)
        {
            if (!this.isResultStillWanted(targetMinute, generation))
            {
                return;
            }

            this.priceWindows = result.priceWindows();
            this.snapshot = new StockMarketSnapshot(
                    this.versionCounter.incrementAndGet(),
                    targetMinute,
                    System.currentTimeMillis(),
                    status,
                    List.copyOf(orderedQuotes)
            );
        }
    }

    /**
     * 조회를 시작한 뒤 세션이 끝났거나 다음 분이 시작됐다면 이 결과는 더 이상 쓸 수 없다.
     */
    private boolean isResultStillWanted(long targetMinute, long generation)
    {
        if (!this.sessionActive)
        {
            JobsPlus.LOGGER.debug("Discarding stock quotes for minute {}: market session stopped", targetMinute);
            return false;
        }
        if (generation != this.sessionGeneration.get())
        {
            JobsPlus.LOGGER.debug("Discarding stock quotes for minute {}: session restarted", targetMinute);
            return false;
        }
        if (targetMinute != this.requestedMinute.get())
        {
            JobsPlus.LOGGER.warn("Discarding stale stock quotes for minute {} (current: {})",
                    targetMinute, this.requestedMinute.get());
            return false;
        }
        return true;
    }

    private static Map<String, StockQuote> buildUnavailableQuotes()
    {
        Map<String, StockQuote> quotes = new LinkedHashMap<>();
        for (StockCatalog.StockDefinition stock : StockCatalog.getStocks())
        {
            quotes.put(stock.id(), StockQuote.unavailable(stock.id(), stock.name()));
        }
        return quotes;
    }

    private CompletableFuture<HttpResponse<String>> requestAsync(String url)
    {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(REQUEST_TIMEOUT)
                .header("Accept", "application/json")
                .header("User-Agent", "JobsPlus/1.0")
                .GET()
                .build();
        return this.httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString());
    }

    private static double readExchangeRate(CompletableFuture<HttpResponse<String>> request, long deadline)
    {
        String body = awaitBody(request, deadline, YAHOO_EXCHANGE_RATE_SYMBOL);
        if (body == null)
        {
            return 0;
        }

        try
        {
            return parseYahooQuote(body).price();
        }
        catch (RuntimeException e)
        {
            JobsPlus.LOGGER.warn("Failed to parse USD/KRW exchange rate: {}", e.toString());
            return 0;
        }
    }

    private static StockQuote readYahooQuote(StockCatalog.StockDefinition stock,
                                             CompletableFuture<HttpResponse<String>> request,
                                             double usdKrw, long deadline)
    {
        String body = awaitBody(request, deadline, stock.symbol());
        if (body == null)
        {
            return null;
        }

        boolean isKoreanStock = stock.symbol().endsWith(".KS") || stock.symbol().endsWith(".KQ");
        if (!isKoreanStock && usdKrw <= 0)
        {
            JobsPlus.LOGGER.warn("Skipping {} because the exchange rate is unavailable", stock.id());
            return null;
        }

        try
        {
            YahooQuote yahooQuote = parseYahooQuote(body);
            double priceKrw = isKoreanStock ? yahooQuote.price() : yahooQuote.price() * usdKrw;
            return StockQuote.available(stock.id(), stock.name(), priceKrw, yahooQuote.percentChange(),
                    System.currentTimeMillis());
        }
        catch (RuntimeException e)
        {
            JobsPlus.LOGGER.warn("Failed to parse Yahoo quote for {} ({}): {}", stock.id(), stock.symbol(),
                    e.toString());
            return null;
        }
    }

    private static void readUpbitQuotes(CompletableFuture<HttpResponse<String>> request,
                                        Map<String, StockQuote> quotes, long deadline)
    {
        String body = awaitBody(request, deadline, "Upbit");
        if (body == null)
        {
            return;
        }

        try
        {
            long updatedAt = System.currentTimeMillis();
            JsonArray tickers = JsonParser.parseString(body).getAsJsonArray();
            for (JsonElement tickerElement : tickers)
            {
                JsonObject ticker = tickerElement.getAsJsonObject();
                StockCatalog.StockDefinition stock =
                        StockCatalog.getStockBySymbol(ticker.get("market").getAsString());
                if (stock == null)
                {
                    continue;
                }

                quotes.put(stock.id(), StockQuote.available(
                        stock.id(),
                        stock.name(),
                        ticker.get("trade_price").getAsDouble(),
                        ticker.get("signed_change_rate").getAsDouble() * 100,
                        updatedAt
                ));
            }
        }
        catch (RuntimeException e)
        {
            JobsPlus.LOGGER.warn("Failed to parse Upbit tickers: {}", e.toString());
        }
    }

    /**
     * 제한 시간 안에 도착한 응답 본문. 실패하거나 늦으면 요청을 취소하고 {@code null}을 돌려준다.
     */
    private static String awaitBody(CompletableFuture<HttpResponse<String>> request, long deadline, String label)
    {
        HttpResponse<String> response = awaitResponse(request, deadline, label);
        if (response == null)
        {
            return null;
        }
        if (response.statusCode() != 200)
        {
            JobsPlus.LOGGER.warn("Stock quote response for {} returned status {}", label, response.statusCode());
            return null;
        }
        return response.body();
    }

    private static HttpResponse<String> awaitResponse(CompletableFuture<HttpResponse<String>> request,
                                                      long deadline, String label)
    {
        // 이미 도착한 응답은 남은 시간과 상관없이 그대로 쓴다.
        // 응답을 순서대로 읽기 때문에, 앞선 요청이 오래 걸렸다는 이유만으로
        // 이미 받아 둔 다른 종목의 시세까지 버리면 성공률이 크게 떨어진다.
        if (request.isDone())
        {
            if (request.isCancelled() || request.isCompletedExceptionally())
            {
                JobsPlus.LOGGER.warn("Stock quote request for {} did not complete normally", label);
                return null;
            }
            return request.getNow(null);
        }

        long remaining = deadline - System.currentTimeMillis();
        if (remaining <= 0)
        {
            request.cancel(true);
            JobsPlus.LOGGER.warn("Stock quote request for {} exceeded the fetch deadline", label);
            return null;
        }

        try
        {
            return request.get(remaining, TimeUnit.MILLISECONDS);
        }
        catch (TimeoutException e)
        {
            request.cancel(true);
            JobsPlus.LOGGER.warn("Stock quote request for {} timed out", label);
            return null;
        }
        catch (ExecutionException e)
        {
            JobsPlus.LOGGER.warn("Stock quote request for {} failed: {}", label, e.getCause());
            return null;
        }
        catch (InterruptedException e)
        {
            Thread.currentThread().interrupt();
            request.cancel(true);
            return null;
        }
    }

    private static YahooQuote parseYahooQuote(String body)
    {
        JsonObject chart = JsonParser.parseString(body).getAsJsonObject().getAsJsonObject("chart");
        JsonArray result = chart.getAsJsonArray("result");
        if (result == null || result.isEmpty())
        {
            throw new IllegalStateException("Stock quote result is empty");
        }

        JsonObject meta = result.get(0).getAsJsonObject().getAsJsonObject("meta");
        double price = getRequiredDouble(meta, "regularMarketPrice");
        double previousClose = getPreviousClose(meta);
        double percentChange = previousClose == 0 ? 0 : (price - previousClose) / previousClose * 100;
        return new YahooQuote(price, percentChange);
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

    private record MarketFetchResult(Map<String, StockQuote> quotes,
                                     Map<String, StockPriceWindow> priceWindows)
    {
    }
}
