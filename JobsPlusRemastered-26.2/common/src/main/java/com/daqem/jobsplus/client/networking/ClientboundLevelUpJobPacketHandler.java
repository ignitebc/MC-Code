package com.daqem.jobsplus.client.networking;

import com.daqem.jobsplus.client.toast.LevelUpJobToast;
import com.daqem.jobsplus.client.gui.confimation.ConfirmationScreen;
import com.daqem.jobsplus.client.gui.powerups.PowerupsScreen;
import com.daqem.jobsplus.integration.arc.holder.holders.job.JobInstance;
import com.daqem.jobsplus.networking.c2s.ServerboundOpenPowerupsScreenPacket;
import com.daqem.jobsplus.networking.s2c.ClientboundLevelUpJobPacket;
import dev.architectury.networking.NetworkManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

public class ClientboundLevelUpJobPacketHandler
{

    public static void handleClientSide(ClientboundLevelUpJobPacket packet, NetworkManager.PacketContext context)
    {
        LevelUpJobToast.addOrUpdate(Minecraft.getInstance().gui.toastManager(), JobInstance.of(packet.getJobLocation()), packet.getLevel());
        Screen screen = Minecraft.getInstance().gui.screen();
        while (screen instanceof ConfirmationScreen confirmationScreen)
        {
            screen = confirmationScreen.getPreviousScreen();
        }
        // 구매 처리 중에는 구매 응답이 전체 직업과 잔액을 갱신한다.
        if (screen instanceof PowerupsScreen powerupsScreen
                && !powerupsScreen.getState().isHyperRequestPending())
        {
            NetworkManager.sendToServer(new ServerboundOpenPowerupsScreenPacket(
                    powerupsScreen.getState().getJob().getJobInstance().getLocation()));
        }
    }
}
