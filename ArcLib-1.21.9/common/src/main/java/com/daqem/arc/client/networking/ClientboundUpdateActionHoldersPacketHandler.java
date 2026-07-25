package com.daqem.arc.client.networking;

import com.daqem.arc.api.action.holder.ActionHolderManager;
import com.daqem.arc.networking.ClientboundUpdateActionHoldersPacket;
import dev.architectury.networking.NetworkManager;
import net.minecraft.client.Minecraft;

public class ClientboundUpdateActionHoldersPacketHandler {

    public static void handleClientSide(ClientboundUpdateActionHoldersPacket packet, NetworkManager.PacketContext context) {
        if (!Minecraft.getInstance().isLocalServer()) {
            ActionHolderManager.getInstance().registerActionHolders(packet.getActionHolders());
        }
    }
}
