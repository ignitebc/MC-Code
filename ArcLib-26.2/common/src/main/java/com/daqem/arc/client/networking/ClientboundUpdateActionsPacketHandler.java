package com.daqem.arc.client.networking;

import com.daqem.arc.api.action.holder.ActionHolderManager;
import com.daqem.arc.networking.ClientboundUpdateActionsPacket;
import dev.architectury.networking.NetworkManager;
import net.minecraft.client.Minecraft;

public class ClientboundUpdateActionsPacketHandler {

    public static void handleClientSide(ClientboundUpdateActionsPacket packet, NetworkManager.PacketContext context) {
        if (!Minecraft.getInstance().isLocalServer()) {
            ActionHolderManager.getInstance().registerActions(packet.getActions());
        }
    }
}
