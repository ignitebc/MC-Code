package com.daqem.jobsplus.client.networking;

import com.daqem.jobsplus.client.notification.ClientSkillNotifications;
import com.daqem.jobsplus.networking.s2c.ClientboundSkillNotificationsPacket;
import dev.architectury.networking.NetworkManager;

public class ClientboundSkillNotificationsPacketHandler
{
    public static void handleClientSide(ClientboundSkillNotificationsPacket packet, NetworkManager.PacketContext context)
    {
        ClientSkillNotifications.setEnabled(packet.isEnabled());
    }
}
