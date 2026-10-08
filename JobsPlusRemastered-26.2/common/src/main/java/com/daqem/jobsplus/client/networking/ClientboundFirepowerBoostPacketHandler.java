package com.daqem.jobsplus.client.networking;

import com.daqem.jobsplus.networking.s2c.ClientboundFirepowerBoostPacket;
import com.daqem.jobsplus.player.job.powerup.FirepowerBoost;
import dev.architectury.networking.NetworkManager;
import net.minecraft.world.entity.player.Player;

public class ClientboundFirepowerBoostPacketHandler
{
    public static void handleClientSide(ClientboundFirepowerBoostPacket packet, NetworkManager.PacketContext context)
    {
        Player player = context.getPlayer();
        if (player != null)
        {
            FirepowerBoost.setClientState(player.getUUID(), packet.getExtraRounds());
        }
    }
}
