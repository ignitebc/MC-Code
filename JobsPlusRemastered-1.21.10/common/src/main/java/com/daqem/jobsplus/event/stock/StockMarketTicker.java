package com.daqem.jobsplus.event.stock;

import com.daqem.jobsplus.networking.s2c.ClientboundStockSnapshotPacket;
import com.daqem.jobsplus.stock.StockMarketService;
import com.daqem.jobsplus.stock.StockMarketSnapshot;
import dev.architectury.event.events.common.LifecycleEvent;
import dev.architectury.event.events.common.PlayerEvent;
import dev.architectury.event.events.common.TickEvent;
import dev.architectury.networking.NetworkManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

/**
 * 주식 탭을 보고 있는 플레이어가 있을 때만, 서버 시스템 시간의 분 경계에 맞춰 시세를 갱신한다.
 * <p>
 * 시세 조회는 외부 API 호출이라 아무도 보지 않을 때까지 매분 돌릴 이유가 없다. 그래서
 * 시청자가 0명에서 1명이 되는 순간 즉시 한 번 조회하고, 그 뒤로는 매분 00초에 갱신하며,
 * 마지막 시청자가 나가면 갱신을 멈추고 진행 중이던 조회 결과도 버린다.
 * <p>
 * 시청자가 여러 명이어도 서버는 하나의 스냅샷만 유지한다. 두 번째 이후 진입자에게는 API를 다시
 * 호출하지 않고 현재 스냅샷만 보낸다.
 */
public final class StockMarketTicker
{
    private static final Set<UUID> ACTIVE_VIEWERS = new LinkedHashSet<>();

    private static long lastRequestedMinute = Long.MIN_VALUE;
    private static long lastBroadcastVersion;

    private StockMarketTicker()
    {
    }

    public static void registerEvent()
    {
        LifecycleEvent.SERVER_STARTED.register(server -> resetSession());
        LifecycleEvent.SERVER_STOPPING.register(server -> resetSession());
        PlayerEvent.PLAYER_QUIT.register(StockMarketTicker::leaveStockView);
        TickEvent.SERVER_POST.register(StockMarketTicker::onServerTick);
    }

    /**
     * 주식 탭에 들어왔을 때. 첫 시청자면 현재 분 시세를 즉시 조회한다.
     */
    public static void enterStockView(ServerPlayer player)
    {
        // 화면이 다시 만들어지는 등 같은 플레이어가 중복으로 보낼 수 있다.
        // 이때 다시 조회하면 API를 불필요하게 더 호출하게 되므로 스냅샷만 보낸다.
        if (!ACTIVE_VIEWERS.add(player.getUUID()))
        {
            sendSnapshot(player);
            return;
        }

        if (ACTIVE_VIEWERS.size() > 1)
        {
            sendSnapshot(player);
            return;
        }

        long currentMinute = StockMarketSnapshot.currentMarketMinute();
        lastRequestedMinute = currentMinute;
        lastBroadcastVersion = 0;
        StockMarketService.getInstance().startSessionAndRefresh(currentMinute);
        sendSnapshot(player);
    }

    /**
     * 주식 탭에서 나갔을 때. 마지막 시청자였다면 갱신을 멈춘다.
     */
    public static void leaveStockView(ServerPlayer player)
    {
        if (!ACTIVE_VIEWERS.remove(player.getUUID()))
        {
            return;
        }
        if (ACTIVE_VIEWERS.isEmpty())
        {
            resetSession();
        }
    }

    /**
     * 거래 요청을 보낸 플레이어가 실제로 주식 탭을 보고 있는지 여부.
     */
    public static boolean isViewing(ServerPlayer player)
    {
        return ACTIVE_VIEWERS.contains(player.getUUID());
    }

    public static void sendSnapshot(ServerPlayer player)
    {
        NetworkManager.sendToPlayer(player, new ClientboundStockSnapshotPacket(
                StockMarketService.getInstance().getSnapshot()));
    }

    private static void resetSession()
    {
        ACTIVE_VIEWERS.clear();
        lastRequestedMinute = Long.MIN_VALUE;
        lastBroadcastVersion = 0;
        StockMarketService.getInstance().stopSession();
    }

    private static void onServerTick(MinecraftServer server)
    {
        if (ACTIVE_VIEWERS.isEmpty())
        {
            return;
        }

        long currentMinute = StockMarketSnapshot.currentMarketMinute();
        if (currentMinute != lastRequestedMinute)
        {
            lastRequestedMinute = currentMinute;
            StockMarketService.getInstance().refreshForMinute(currentMinute);
        }

        broadcastSnapshotIfChanged(server);
    }

    private static void broadcastSnapshotIfChanged(MinecraftServer server)
    {
        StockMarketSnapshot snapshot = StockMarketService.getInstance().getSnapshot();
        if (snapshot.version() == lastBroadcastVersion)
        {
            return;
        }

        lastBroadcastVersion = snapshot.version();
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
