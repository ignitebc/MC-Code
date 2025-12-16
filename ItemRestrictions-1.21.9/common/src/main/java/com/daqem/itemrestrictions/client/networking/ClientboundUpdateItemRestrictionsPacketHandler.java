package com.daqem.itemrestrictions.client.networking;

import com.daqem.itemrestrictions.data.ItemRestrictionManager;
import com.daqem.itemrestrictions.networking.clientbound.ClientboundUpdateItemRestrictionsPacket;
import dev.architectury.networking.NetworkManager;
import net.minecraft.client.Minecraft;

public class ClientboundUpdateItemRestrictionsPacketHandler {

    public static void handleClientSide(ClientboundUpdateItemRestrictionsPacket packet, NetworkManager.PacketContext context) {
        if (!Minecraft.getInstance().isLocalServer()) {
            ItemRestrictionManager.getInstance().setItemRestrictions(packet.getItemRestrictions());
        }
    }
}
