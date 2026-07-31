package com.daqem.jobsplus.networking.c2s;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 같은 플레이어가 한 서버 틱에 주식 탭 시청 상태 패킷을 여러 번 처리하지 못하게 막는다.
 *
 * <p>거래 패킷과 별도의 기록을 쓰는 이유는, 탭을 여는 패킷이 거래 패킷의
 * 틱당 1회 예산을 소모해 정상 거래가 거부되는 일을 막기 위해서다.
 */
public final class StockViewRateLimiter
{
    private static final Map<UUID, Integer> LAST_VIEW_PACKET_TICKS = new HashMap<>();

    private StockViewRateLimiter()
    {
    }

    public static boolean tryAcquire(ServerPlayer player)
    {
        MinecraftServer server = player.level().getServer();
        if (server == null)
        {
            return false;
        }

        int currentTick = server.getTickCount();
        Integer lastPacketTick = LAST_VIEW_PACKET_TICKS.put(player.getUUID(), currentTick);
        if (lastPacketTick == null)
        {
            return true;
        }
        if (lastPacketTick == currentTick)
        {
            return false;
        }
        return true;
    }

    public static void forget(UUID playerId)
    {
        LAST_VIEW_PACKET_TICKS.remove(playerId);
    }

    public static void reset()
    {
        LAST_VIEW_PACKET_TICKS.clear();
    }
}
