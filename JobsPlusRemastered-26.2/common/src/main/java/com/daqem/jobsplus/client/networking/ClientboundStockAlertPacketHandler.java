package com.daqem.jobsplus.client.networking;

import com.daqem.jobsplus.client.gui.confimation.ConfirmationScreen;
import com.daqem.jobsplus.client.gui.confimation.ConfirmationScreenState;
import com.daqem.jobsplus.networking.s2c.ClientboundStockAlertPacket;
import dev.architectury.networking.NetworkManager;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

public class ClientboundStockAlertPacketHandler
{
    public static void handleClientSide(ClientboundStockAlertPacket packet, NetworkManager.PacketContext context)
    {
        Minecraft minecraft = Minecraft.getInstance();
        minecraft.gui.setScreen(new ConfirmationScreen(
                minecraft.gui.screen(),
                ConfirmationScreenState.alert(
                        Component.literal(packet.getMessage()),
                        Component.literal(packet.getButtonMessage()))
        ));
    }
}
