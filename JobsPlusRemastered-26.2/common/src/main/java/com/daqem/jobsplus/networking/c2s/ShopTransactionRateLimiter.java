package com.daqem.jobsplus.networking.c2s;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 같은 플레이어의 상점 거래 패킷 처리 빈도를 제한한다.
 *
 * - 마지막 요청 후 {@link #MIN_INTERVAL_TICKS}틱이 지나기 전의 요청은 거부한다.
 * - 최근 {@link #WINDOW_TICKS}틱(1초) 동안 {@link #MAX_REQUESTS_PER_WINDOW}회를 넘는 요청도 거부한다.
 * - 실패하는 요청도 인벤토리 순회와 메시지 전송 비용이 들기 때문에 성공 여부와 무관하게 집계한다.
 */
public final class ShopTransactionRateLimiter
{
    private static final int MIN_INTERVAL_TICKS = 3;
    private static final int WINDOW_TICKS = 20;
    private static final int MAX_REQUESTS_PER_WINDOW = 5;

    private static final Map<UUID, Deque<Integer>> RECENT_REQUEST_TICKS = new HashMap<>();

    private ShopTransactionRateLimiter()
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
        Deque<Integer> recentTicks = RECENT_REQUEST_TICKS.computeIfAbsent(player.getUUID(), id -> new ArrayDeque<>());

        // 이전 세션의 잔여 기록(현재 틱보다 미래 값)은 무효 처리한다.
        if (!recentTicks.isEmpty() && recentTicks.peekLast() > currentTick)
        {
            recentTicks.clear();
        }

        while (!recentTicks.isEmpty() && currentTick - recentTicks.peekFirst() >= WINDOW_TICKS)
        {
            recentTicks.pollFirst();
        }

        Integer lastRequestTick = recentTicks.peekLast();
        boolean tooSoon = lastRequestTick != null && currentTick - lastRequestTick < MIN_INTERVAL_TICKS;
        boolean windowFull = recentTicks.size() >= MAX_REQUESTS_PER_WINDOW;
        if (tooSoon || windowFull)
        {
            return false;
        }

        recentTicks.addLast(currentTick);
        return true;
    }

    public static void forget(UUID playerId)
    {
        RECENT_REQUEST_TICKS.remove(playerId);
    }

    public static void reset()
    {
        RECENT_REQUEST_TICKS.clear();
    }
}
