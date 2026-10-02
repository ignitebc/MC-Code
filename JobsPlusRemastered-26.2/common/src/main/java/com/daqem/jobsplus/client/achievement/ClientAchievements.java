package com.daqem.jobsplus.client.achievement;

import com.daqem.jobsplus.networking.c2s.ServerboundAchievementPacket;
import com.daqem.jobsplus.networking.s2c.ClientboundAchievementPacket;
import dev.architectury.networking.NetworkManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;

/** 서버 응답만 표시한다. 서버를 옮길 때 다른 플레이어의 진행 화면을 재사용하지 않는다. */
public final class ClientAchievements
{
    private static ClientboundAchievementPacket snapshot = ClientboundAchievementPacket.EMPTY;
    private static ClientPacketListener connection;
    private static long nextPoll;
    public static String category = "A";
    public static String selectedId = "A01";

    private ClientAchievements()
    {
    }

    public static ClientboundAchievementPacket getSnapshot()
    {
        checkConnection();
        return snapshot;
    }

    private static void checkConnection()
    {
        ClientPacketListener current = Minecraft.getInstance().getConnection();
        if (current != connection)
        {
            connection = current;
            snapshot = ClientboundAchievementPacket.EMPTY;
            nextPoll = 0;
            category = "A";
            selectedId = "A01";
        }
    }

    public static void update(ClientboundAchievementPacket packet)
    {
        checkConnection();
        snapshot = packet;
    }

    public static void poll()
    {
        checkConnection();
        long now = System.nanoTime();
        if (connection != null && now >= nextPoll)
        {
            nextPoll = now + 1_000_000_000L;
            NetworkManager.sendToServer(new ServerboundAchievementPacket(snapshot.season(), ""));
        }
    }

    public static void claim(String id)
    {
        checkConnection();
        if (connection != null && !snapshot.season().isEmpty())
        {
            NetworkManager.sendToServer(new ServerboundAchievementPacket(snapshot.season(), id));
        }
    }
}
