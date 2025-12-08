package com.daqem.arc.client.networking;

import com.daqem.arc.api.action.holder.ActionHolderManager;
import com.daqem.arc.client.gui.action.ActionScreen;
import com.daqem.arc.networking.ClientboundActionScreenPacket;
import dev.architectury.networking.NetworkManager;
import net.minecraft.client.Minecraft;

public class ClientboundActionScreenPacketHandler {

    public static void handleClientSide(ClientboundActionScreenPacket packet, NetworkManager.PacketContext context) {
        Minecraft.getInstance().setScreen(new ActionScreen(
                ActionHolderManager.getInstance().getActions(),
                ActionHolderManager.getInstance().getActions().stream()
                        .filter(a -> a.getLocation().equals(packet.getAction().getLocation()))
                        .findFirst()
                        .orElse(null)));
    }
}
