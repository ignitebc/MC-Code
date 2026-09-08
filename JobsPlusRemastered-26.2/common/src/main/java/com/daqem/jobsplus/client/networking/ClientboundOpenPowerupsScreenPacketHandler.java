package com.daqem.jobsplus.client.networking;

import com.daqem.jobsplus.client.gui.jobs.JobsScreen;
import com.daqem.jobsplus.client.gui.confimation.ConfirmationScreen;
import com.daqem.jobsplus.client.gui.jobs.JobsScreenState;
import com.daqem.jobsplus.client.gui.jobs.tab.RightTab;
import com.daqem.jobsplus.client.gui.powerups.PowerupsScreen;
import com.daqem.jobsplus.client.gui.powerups.PowerupsScreenState;
import com.daqem.jobsplus.networking.s2c.ClientboundOpenPowerupsScreenPacket;
import com.daqem.jobsplus.player.job.Job;
import dev.architectury.networking.NetworkManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import org.jetbrains.annotations.Nullable;

public class ClientboundOpenPowerupsScreenPacketHandler {
    public static void handleClientSide(ClientboundOpenPowerupsScreenPacket packet,
            NetworkManager.PacketContext context) {
        Minecraft minecraft = Minecraft.getInstance();
        Screen current = minecraft.gui.screen();
        Screen underlying = current;
        while (underlying instanceof ConfirmationScreen confirmation) {
            underlying = confirmation.getPreviousScreen();
        }
        @Nullable
        Screen previousScreen = null;

        if (Minecraft.getInstance().gui.screen() instanceof JobsScreen jobsScreen) {
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
            if (current != powerupsScreen) minecraft.gui.setScreen(powerupsScreen);
            return;
        }

        JobsScreen jobsScreen = new JobsScreen(
                new JobsScreenState(packet.getJobs(), packet.getCoins(), packet.getMaxJobs(), job, RightTab.EXPERIENCE),
                previousScreen);

        minecraft.gui.setScreen(new PowerupsScreen(new PowerupsScreenState(job, packet.getCoins()), jobsScreen));
    }
}
