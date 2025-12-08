package com.daqem.arc.client.networking;

import com.daqem.arc.client.gui.holder.ActionHoldersScreen;
import com.daqem.arc.networking.ClientboundActionHoldersScreenPacket;
import dev.architectury.networking.NetworkManager;
import net.minecraft.client.Minecraft;

public class ClientboundActionHoldersScreenPacketHandler {

    public static void handleClientSide(ClientboundActionHoldersScreenPacket packet, NetworkManager.PacketContext context) {
        Minecraft.getInstance().setScreen(new ActionHoldersScreen(packet.getActionHolders()));
    }
}
