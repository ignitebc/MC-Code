package com.daqem.jobsplus.networking.c2s;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 같은 플레이어가 한 서버 틱에 여러 주식 거래 패킷을 처리하지 못하게 막는다.
 */
public final class StockTransactionRateLimiter
{
    private static final Map<UUID, Integer> LAST_TRANSACTION_TICKS = new HashMap<>();

    private StockTransactionRateLimiter()
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
        Integer lastTransactionTick = LAST_TRANSACTION_TICKS.put(player.getUUID(), currentTick);
        if (lastTransactionTick == null)
        {
            return true;
        }
        if (lastTransactionTick == currentTick)
        {
            return false;
        }
        return true;
    }

    public static void forget(UUID playerId)
    {
        LAST_TRANSACTION_TICKS.remove(playerId);
    }

    public static void reset()
    {
        LAST_TRANSACTION_TICKS.clear();
    }
}
