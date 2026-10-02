package com.daqem.jobsplus.client.networking;

import com.daqem.jobsplus.client.achievement.ClientAchievements;
import com.daqem.jobsplus.networking.s2c.ClientboundAchievementPacket;
import dev.architectury.networking.NetworkManager;
import net.minecraft.client.Minecraft;

public final class ClientboundAchievementPacketHandler
{
    private ClientboundAchievementPacketHandler()
    {
    }

    public static void handleClientSide(ClientboundAchievementPacket packet, NetworkManager.PacketContext context)
    {
        Minecraft.getInstance().execute(() -> ClientAchievements.update(packet));
    }
}
