package com.daqem.jobsplus.event.stock;

import com.daqem.jobsplus.networking.StockScreenSync;
import com.daqem.jobsplus.networking.c2s.StockTransactionRateLimiter;
import com.daqem.jobsplus.networking.s2c.ClientboundStockSnapshotPacket;
import com.daqem.jobsplus.player.JobsServerPlayer;
import com.daqem.jobsplus.player.stock.StockAccount;
import com.daqem.jobsplus.player.stock.StockPosition;
import com.daqem.jobsplus.player.stock.StockPositionLedger;
import com.daqem.jobsplus.stock.SnapshotStatus;
import com.daqem.jobsplus.stock.StockCatalog;
import com.daqem.jobsplus.stock.StockMarketService;
import com.daqem.jobsplus.stock.StockMarketSnapshot;
import com.daqem.jobsplus.stock.StockPriceWindow;
import com.daqem.jobsplus.stock.StockQuote;
import dev.architectury.event.events.common.LifecycleEvent;
import dev.architectury.event.events.common.PlayerEvent;
import dev.architectury.event.events.common.TickEvent;
import dev.architectury.networking.NetworkManager;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * 주식 시세 갱신과 미결제 포지션 청산을 담당한다.
 * <p>
 * 주식 탭 시청자가 있거나 중앙 원장에 미결제 포지션이 하나라도 있으면 시세 세션을 유지한다.
 * 플레이어가 로그아웃해도 포지션은 월드 저장 데이터에 남으므로 분봉 청산 검사가 계속된다.
 */
public final class StockMarketTicker
{
    private static final Set<UUID> ACTIVE_VIEWERS = new LinkedHashSet<>();

    private static long lastRequestedMinute = Long.MIN_VALUE;
    private static long lastBroadcastVersion;
    private static boolean marketSessionActive;

    private StockMarketTicker()
    {
    }

    public static void registerEvent()
    {
        LifecycleEvent.SERVER_STARTED.register(StockMarketTicker::initializeServer);
        LifecycleEvent.SERVER_STOPPING.register(StockMarketTicker::shutdownServer);
        PlayerEvent.PLAYER_JOIN.register(StockMarketTicker::onPlayerJoin);
        PlayerEvent.PLAYER_QUIT.register(StockMarketTicker::onPlayerQuit);
        TickEvent.SERVER_POST.register(StockMarketTicker::onServerTick);
    }

    public static void enterStockView(ServerPlayer player)
    {
        ACTIVE_VIEWERS.add(player.getUUID());
        MinecraftServer server = player.level().getServer();
        if (server == null)
        {
            return;
        }

        ensureMarketSession(server);
        sendSnapshot(player);
    }

    public static void leaveStockView(ServerPlayer player)
    {
        ACTIVE_VIEWERS.remove(player.getUUID());
        MinecraftServer server = player.level().getServer();
        if (server != null)
        {
            ensureMarketSession(server);
        }
    }

    public static boolean isViewing(ServerPlayer player)
    {
        return ACTIVE_VIEWERS.contains(player.getUUID());
    }

    public static void sendSnapshot(ServerPlayer player)
    {
        NetworkManager.sendToPlayer(
                player,
                new ClientboundStockSnapshotPacket(StockMarketService.getInstance().getSnapshot())
        );
    }

    /**
     * 매수·매도로 계좌 포지션이 바뀐 직후 중앙 원장을 동기화한다.
     */
    public static void onStockAccountChanged(ServerPlayer player, StockAccount account)
    {
        MinecraftServer server = player.level().getServer();
        if (server == null)
        {
            return;
        }

        StockPositionLedger ledger = StockPositionLedger.get(server);
        ledger.syncPlayerPositions(
                player,
                account,
                StockMarketSnapshot.currentMarketMinute()
        );
        ensureMarketSession(server);
    }

    public static boolean isPositionCaughtUp(ServerPlayer player, String stockId, long currentMinute)
    {
        MinecraftServer server = player.level().getServer();
        if (server == null)
        {
            return false;
        }
        return StockPositionLedger.get(server).isCaughtUp(player.getUUID(), stockId, currentMinute);
    }

    /**
     * 거래 패킷과 시세 확정 틱이 같은 틱에 겹친 경우에도 청산 대상 포지션을 거래하지 못하게 한다.
     */
    public static boolean liquidatePositionIfNeeded(JobsServerPlayer jobsServerPlayer, StockQuote quote)
    {
        ServerPlayer player = jobsServerPlayer.jobsplus$getServerPlayer();
        StockPosition position = jobsServerPlayer.jobsplus$getStockAccount().getPosition(quote.id());
        if (position == null || !position.isLiquidated(quote.priceKrw()))
        {
            return false;
        }

        MinecraftServer server = player.level().getServer();
        if (server == null)
        {
            return false;
        }

        StockPositionLedger ledger = StockPositionLedger.get(server);
        StockPositionLedger.TrackedPosition trackedPosition =
                ledger.getTrackedPosition(player.getUUID(), position.stockId());
        if (trackedPosition == null)
        {
            ledger.syncPlayerPositions(
                    player,
                    jobsServerPlayer.jobsplus$getStockAccount(),
                    StockMarketSnapshot.currentMarketMinute()
            );
            trackedPosition = ledger.getTrackedPosition(player.getUUID(), position.stockId());
        }
        if (trackedPosition == null)
        {
            return false;
        }

        liquidatePosition(server, ledger, trackedPosition, System.currentTimeMillis());
        return true;
    }

    private static void initializeServer(MinecraftServer server)
    {
        ACTIVE_VIEWERS.clear();
        lastRequestedMinute = Long.MIN_VALUE;
        lastBroadcastVersion = 0;
        marketSessionActive = false;
        StockTransactionRateLimiter.reset();
        StockMarketService.getInstance().stopSession();
        ensureMarketSession(server);
    }

    private static void shutdownServer(MinecraftServer server)
    {
        ACTIVE_VIEWERS.clear();
        lastRequestedMinute = Long.MIN_VALUE;
        lastBroadcastVersion = 0;
        marketSessionActive = false;
        StockTransactionRateLimiter.reset();
        StockMarketService.getInstance().stopSession();
    }

    private static void onPlayerJoin(ServerPlayer player)
    {
        if (!(player instanceof JobsServerPlayer jobsServerPlayer))
        {
            return;
        }

        MinecraftServer server = player.level().getServer();
        if (server == null)
        {
            return;
        }

        StockPositionLedger ledger = StockPositionLedger.get(server);
        StockAccount account = ledger.applyPendingLiquidations(
                player.getUUID(),
                jobsServerPlayer.jobsplus$getStockAccount()
        );
        jobsServerPlayer.jobsplus$setStockAccount(account);
        ledger.syncPlayerPositions(
                player,
                account,
                StockMarketSnapshot.currentMarketMinute()
        );
        ensureMarketSession(server);
    }

    private static void onPlayerQuit(ServerPlayer player)
    {
        ACTIVE_VIEWERS.remove(player.getUUID());
        StockTransactionRateLimiter.forget(player.getUUID());

        MinecraftServer server = player.level().getServer();
        if (server != null)
        {
            ensureMarketSession(server);
        }
    }

    private static void ensureMarketSession(MinecraftServer server)
    {
        StockPositionLedger ledger = StockPositionLedger.get(server);
        boolean sessionRequired = !ACTIVE_VIEWERS.isEmpty() || ledger.hasOpenPositions();
        if (sessionRequired && !marketSessionActive)
        {
            long currentMinute = StockMarketSnapshot.currentMarketMinute();
            lastRequestedMinute = currentMinute;
            lastBroadcastVersion = 0;
            marketSessionActive = true;
            StockMarketService.getInstance().startSessionAndRefresh(
                    currentMinute,
                    ledger.getEarliestCheckedMinutes()
            );
            return;
        }
        if (!sessionRequired && marketSessionActive)
        {
            marketSessionActive = false;
            lastRequestedMinute = Long.MIN_VALUE;
            lastBroadcastVersion = 0;
            StockMarketService.getInstance().stopSession();
        }
    }

    private static void onServerTick(MinecraftServer server)
    {
        ensureMarketSession(server);
        if (!marketSessionActive)
        {
            return;
        }

        long currentMinute = StockMarketSnapshot.currentMarketMinute();
        if (currentMinute != lastRequestedMinute)
        {
            lastRequestedMinute = currentMinute;
            StockPositionLedger ledger = StockPositionLedger.get(server);
            StockMarketService.getInstance().refreshForMinute(
                    currentMinute,
                    ledger.getEarliestCheckedMinutes()
            );
        }

        processSnapshotIfChanged(server);
    }

    private static void processSnapshotIfChanged(MinecraftServer server)
    {
        StockMarketService marketService = StockMarketService.getInstance();
        StockMarketSnapshot snapshot = marketService.getSnapshot();
        if (snapshot.version() == lastBroadcastVersion)
        {
            return;
        }

        lastBroadcastVersion = snapshot.version();
        processCurrentQuotes(server, snapshot);
        processPriceWindows(server, marketService.getPriceWindows(), snapshot.updatedAt());
        broadcastSnapshot(snapshot, server);
        ensureMarketSession(server);
    }

    private static void processCurrentQuotes(MinecraftServer server, StockMarketSnapshot snapshot)
    {
        if (snapshot.status() != SnapshotStatus.READY)
        {
            return;
        }

        StockPositionLedger ledger = StockPositionLedger.get(server);
        for (StockPositionLedger.TrackedPosition trackedPosition : ledger.getTrackedPositions())
        {
            StockQuote quote = snapshot.getQuote(trackedPosition.position().stockId());
            if (quote == null || !quote.hasValidPrice())
            {
                continue;
            }
            if (trackedPosition.position().isLiquidated(quote.priceKrw()))
            {
                liquidatePosition(server, ledger, trackedPosition, snapshot.updatedAt());
            }
        }
    }

    private static void processPriceWindows(MinecraftServer server,
                                            Map<String, StockPriceWindow> priceWindows,
                                            long liquidatedAt)
    {
        if (priceWindows.isEmpty())
        {
            return;
        }

        StockPositionLedger ledger = StockPositionLedger.get(server);
        for (StockPriceWindow priceWindow : priceWindows.values())
        {
            for (StockPositionLedger.TrackedPosition trackedPosition : ledger.getTrackedPositions())
            {
                if (!trackedPosition.position().stockId().equals(priceWindow.stockId()))
                {
                    continue;
                }
                if (trackedPosition.lastCheckedMinute() >= priceWindow.checkedThroughMinute())
                {
                    continue;
                }
                if (priceWindow.crossesLiquidationPrice(
                        trackedPosition.position(),
                        trackedPosition.lastCheckedMinute()
                ))
                {
                    liquidatePosition(server, ledger, trackedPosition, liquidatedAt);
                }
            }
            ledger.markCheckedThrough(priceWindow.stockId(), priceWindow.checkedThroughMinute());
        }
    }

    /**
     * 중앙 원장에 청산을 먼저 기록하고, 접속 중인 플레이어면 즉시 개인 계좌에도 반영한다.
     */
    private static void liquidatePosition(MinecraftServer server, StockPositionLedger ledger,
                                          StockPositionLedger.TrackedPosition trackedPosition,
                                          long liquidatedAt)
    {
        ledger.markLiquidated(trackedPosition, liquidatedAt);

        ServerPlayer player = server.getPlayerList().getPlayer(trackedPosition.playerId());
        if (player instanceof JobsServerPlayer jobsServerPlayer)
        {
            StockAccount account = ledger.applyPendingLiquidations(
                    trackedPosition.playerId(),
                    jobsServerPlayer.jobsplus$getStockAccount()
            );
            jobsServerPlayer.jobsplus$setStockAccount(account);
            if (isViewing(player))
            {
                StockScreenSync.send(jobsServerPlayer);
            }
        }

        broadcastLiquidation(server, trackedPosition);
    }

    private static void broadcastLiquidation(MinecraftServer server,
                                             StockPositionLedger.TrackedPosition trackedPosition)
    {
        StockPosition position = trackedPosition.position();
        String stockName = StockCatalog.getStockName(position.stockId());
        String message = "[주식] " + trackedPosition.playerName() + "님의 " + stockName + " "
                + position.getPositionName() + " 포지션이 수익률 -100%에 도달하여 청산되었습니다.";
        server.getPlayerList().broadcastSystemMessage(Component.literal(message), false);
    }

    private static void broadcastSnapshot(StockMarketSnapshot snapshot, MinecraftServer server)
    {
        ClientboundStockSnapshotPacket packet = new ClientboundStockSnapshotPacket(snapshot);
        for (UUID viewerId : ACTIVE_VIEWERS)
        {
            ServerPlayer viewer = server.getPlayerList().getPlayer(viewerId);
            if (viewer != null)
            {
                NetworkManager.sendToPlayer(viewer, packet);
            }
        }
    }
}
