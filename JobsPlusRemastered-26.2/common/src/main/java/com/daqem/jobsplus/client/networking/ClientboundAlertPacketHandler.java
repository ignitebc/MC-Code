package com.daqem.jobsplus.client.networking;

import com.daqem.jobsplus.client.gui.confimation.ConfirmationScreen;
import com.daqem.jobsplus.client.gui.confimation.ConfirmationScreenState;
import com.daqem.jobsplus.networking.s2c.ClientboundAlertPacket;
import dev.architectury.networking.NetworkManager;
import net.minecraft.client.Minecraft;

public class ClientboundAlertPacketHandler
{
    public static void handleClientSide(ClientboundAlertPacket packet, NetworkManager.PacketContext context)
    {
        Minecraft minecraft = Minecraft.getInstance();
        minecraft.gui.setScreen(new ConfirmationScreen(
                minecraft.gui.screen(),
                ConfirmationScreenState.alert(
                        packet.getMessage(),
                        packet.getButtonMessage())
        ));
    }
}
