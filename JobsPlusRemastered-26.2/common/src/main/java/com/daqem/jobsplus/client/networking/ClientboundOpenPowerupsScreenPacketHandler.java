package com.daqem.jobsplus.client.networking;

import com.daqem.jobsplus.client.gui.jobs.JobsScreen;
import com.daqem.jobsplus.client.gui.confimation.ConfirmationScreen;
import com.daqem.jobsplus.client.gui.jobs.JobsScreenState;
import com.daqem.jobsplus.client.gui.jobs.tab.RightTab;
import com.daqem.jobsplus.client.gui.powerups.PowerupsScreen;
import com.daqem.jobsplus.client.gui.powerups.PowerupsScreenState;
import com.daqem.jobsplus.networking.s2c.ClientboundOpenPowerupsScreenPacket;
import dev.architectury.networking.NetworkManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import com.daqem.jobsplus.player.job.Job;
import org.jetbrains.annotations.Nullable;

public class ClientboundOpenPowerupsScreenPacketHandler {
    public static void handleClientSide(ClientboundOpenPowerupsScreenPacket packet,
            NetworkManager.PacketContext context) {
        Minecraft minecraft = Minecraft.getInstance();
        Screen current = minecraft.gui.screen();
        @Nullable
        ConfirmationScreen openAlert = findOpenAlert(current);
        Screen underlying = current;
        while (underlying instanceof ConfirmationScreen confirmation) {
            underlying = confirmation.getPreviousScreen();
        }
        @Nullable
        Screen previousScreen = null;

        if (underlying instanceof JobsScreen jobsScreen) {
            previousScreen = jobsScreen.getPreviousScreen();
        }

        Job job = packet.getJobs().stream()
                .filter(j -> j.getJobInstance().getLocation().equals(packet.getJobLocation()))
                .findFirst()
                .orElse(null);

        if (job == null) return;
        if (underlying instanceof PowerupsScreen powerupsScreen
                && powerupsScreen.getState().getJob().getJobInstance().getLocation().equals(packet.getJobLocation())) {
            powerupsScreen.update(job, packet.getCoins());
            if (powerupsScreen.getPreviousScreen() instanceof JobsScreen parent) {
                parent.getState().setCoins(packet.getCoins());
            }
            if (current != powerupsScreen) {
                setScreenKeepingAlert(minecraft, powerupsScreen, openAlert);
            }
            return;
        }

        JobsScreen jobsScreen = new JobsScreen(
                new JobsScreenState(packet.getJobs(), packet.getCoins(), packet.getMaxJobs(), job, RightTab.EXPERIENCE),
                previousScreen);

        setScreenKeepingAlert(minecraft,
                new PowerupsScreen(new PowerupsScreenState(job, packet.getCoins()), jobsScreen), openAlert);
    }

    /**
     * 스킬 구매 결과 알림은 이 화면 갱신 패킷보다 먼저 도착한다.
     * 갱신하면서 그냥 덮으면 알림이 뜨자마자 사라지므로, 갱신된 화면 위에 같은 알림을 다시 올린다.
     */
    private static void setScreenKeepingAlert(Minecraft minecraft, Screen screen,
            @Nullable ConfirmationScreen openAlert) {
        if (openAlert == null) {
            minecraft.gui.setScreen(screen);
            return;
        }
        minecraft.gui.setScreen(new ConfirmationScreen(screen, openAlert.getState()));
    }

    @Nullable
    private static ConfirmationScreen findOpenAlert(@Nullable Screen currentScreen) {
        if (currentScreen instanceof ConfirmationScreen confirmationScreen && confirmationScreen.isAlert()) {
            return confirmationScreen;
        }
        return null;
    }
}
