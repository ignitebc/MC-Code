package com.daqem.jobsplus.event.stock;

import com.daqem.jobsplus.networking.s2c.ClientboundStockSnapshotPacket;
import com.daqem.jobsplus.stock.StockMarketService;
import com.daqem.jobsplus.stock.StockMarketSnapshot;
import dev.architectury.event.events.common.TickEvent;
import dev.architectury.networking.NetworkManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

/**
 * 서버가 시세 갱신 주기를 직접 굴린다.
 * <p>
 * 예전에는 클라이언트 화면과 거래 패킷이 각자 갱신을 요청했기 때문에, 화면에 보이는 가격과
 * 실제 체결 가격이 서로 다른 스냅샷일 수 있었다. 이제는 서버 틱에서만 갱신하고,
 * 스냅샷 번호가 바뀐 순간 접속 중인 전원에게 새 가격을 내려보낸다.
 */
public final class StockMarketTicker
{
    private static long lastBroadcastVersion;

    private StockMarketTicker()
    {
    }

    public static void registerEvent()
    {
        TickEvent.SERVER_POST.register(StockMarketTicker::onServerTick);
    }

    /**
     * 직업 창을 여는 등 개별 플레이어에게 현재 가격을 즉시 알려야 할 때 사용한다.
     */
    public static void sendSnapshot(ServerPlayer player)
    {
        NetworkManager.sendToPlayer(player, new ClientboundStockSnapshotPacket(
                StockMarketService.getInstance().getSnapshot()));
    }

    private static void onServerTick(MinecraftServer server)
    {
        StockMarketService stockMarketService = StockMarketService.getInstance();
        stockMarketService.refreshIfNeeded();

        StockMarketSnapshot snapshot = stockMarketService.getSnapshot();
        if (snapshot.version() == lastBroadcastVersion)
        {
            return;
        }

        lastBroadcastVersion = snapshot.version();
        ClientboundStockSnapshotPacket packet = new ClientboundStockSnapshotPacket(snapshot);
        for (ServerPlayer player : server.getPlayerList().getPlayers())
        {
            NetworkManager.sendToPlayer(player, packet);
        }
    }
}
