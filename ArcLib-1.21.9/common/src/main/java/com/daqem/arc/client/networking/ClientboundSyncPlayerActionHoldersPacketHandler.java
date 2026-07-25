package com.daqem.arc.client.networking;

import com.daqem.arc.api.player.ArcClientPlayer;
import com.daqem.arc.networking.ClientboundSyncPlayerActionHoldersPacket;
import dev.architectury.networking.NetworkManager;
import net.minecraft.client.Minecraft;

public class ClientboundSyncPlayerActionHoldersPacketHandler {

    public static void handleClientSide(ClientboundSyncPlayerActionHoldersPacket packet, NetworkManager.PacketContext context) {
        if (Minecraft.getInstance().player instanceof ArcClientPlayer arcClientPlayer) {
            arcClientPlayer.arc$clearActionHolders();
            arcClientPlayer.arc$addActionHolders(packet.getActionHolders());
        }
    }
}
