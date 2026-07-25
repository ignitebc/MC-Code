package com.daqem.itemrestrictions.client.networking;

import com.daqem.itemrestrictions.ItemRestrictions;
import com.daqem.itemrestrictions.client.screen.ItemRestrictionsScreen;
import com.daqem.itemrestrictions.data.ItemRestrictionManager;
import com.daqem.itemrestrictions.networking.clientbound.ClientboundRestrictionPacket;
import dev.architectury.networking.NetworkManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;

public class ClientboundRestrictionPacketHandler {

    public static void handleClientSide(ClientboundRestrictionPacket packet, NetworkManager.PacketContext context) {
        if (ItemRestrictions.isDebugEnvironment()) {
            ItemRestrictions.LOGGER.error("Received restriction packet from server! Restriction type: " + packet.getRestrictionType());
            ItemRestrictions.LOGGER.info("Amount of item restrictions on the client: " + ItemRestrictionManager.getInstance().getItemRestrictions().size());
        }
        if (context.getPlayer() instanceof LocalPlayer) {
            Screen currentScreen = Minecraft.getInstance().screen;
            if (currentScreen instanceof ItemRestrictionsScreen itemRestrictionsScreen) {
                itemRestrictionsScreen.itemrestrictions$cantCraft(packet.getRestrictionType());
            }
        }
    }
}
