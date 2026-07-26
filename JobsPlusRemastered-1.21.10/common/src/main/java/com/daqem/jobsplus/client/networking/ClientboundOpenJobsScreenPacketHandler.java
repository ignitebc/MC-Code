package com.daqem.jobsplus.client.networking;

import com.daqem.jobsplus.client.gui.jobs.JobsScreen;
import com.daqem.jobsplus.client.gui.jobs.JobsScreenState;
import com.daqem.jobsplus.client.gui.jobs.tab.RightTab;
import com.daqem.jobsplus.networking.s2c.ClientboundOpenJobsScreenPacket;
import com.daqem.jobsplus.player.job.Job;
import com.daqem.jobsplus.shop.ShopOffer;
import dev.architectury.networking.NetworkManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ClientboundOpenJobsScreenPacketHandler {
    public static void handleClientSide(ClientboundOpenJobsScreenPacket packet, NetworkManager.PacketContext context) {
        Minecraft mc = Minecraft.getInstance();

        List<Job> jobs = packet.getJobs();
        int coins = packet.getCoins();
        int maxJobs = packet.getMaxJobs();

        @Nullable
        Screen previousScreen = null;
        if (mc.screen instanceof JobsScreen jobsScreen) {
            previousScreen = jobsScreen.getPreviousScreen();
        }

        if (mc.screen instanceof JobsScreen jobsScreen) {
            JobsScreenState oldState = jobsScreen.getState();

            RightTab keepTab = oldState.getSelectedRightTab();
            Job keepJob = findSameJobOrFirst(jobs, oldState.getSelectedJob());
            @Nullable
            ShopOffer keepOffer = oldState.getSelectedShopOffer();
            String keepStockId = oldState.getSelectedStockId();
            String keepHoldingStockId = oldState.getSelectedHoldingStockId();
            var keepStockPanelMode = oldState.getStockPanelMode();

            JobsScreenState newState = new JobsScreenState(
                    jobs, coins, maxJobs, keepJob, keepTab, packet.getStockAccount());
            newState.setSelectedShopOffer(keepOffer);
            newState.setSelectedStockId(keepStockId);
            newState.setSelectedHoldingStockId(keepHoldingStockId);
            newState.setStockPanelMode(keepStockPanelMode);

            mc.setScreen(new JobsScreen(newState, previousScreen));
            return;
        }

        mc.setScreen(new JobsScreen(
                new JobsScreenState(jobs, coins, maxJobs, null, RightTab.EXPERIENCE, packet.getStockAccount()),
                previousScreen));
    }

    private static Job findSameJobOrFirst(List<Job> newJobs, @Nullable Job oldSelected) {
        if (newJobs == null || newJobs.isEmpty()) {
            return null;
        }
        if (oldSelected == null) {
            return newJobs.getFirst();
        }

        ResourceLocation oldId = oldSelected.getJobInstance().getLocation();
        for (Job j : newJobs) {
            if (j.getJobInstance().getLocation().equals(oldId)) {
                return j;
            }
        }
        return newJobs.getFirst();
    }
}
