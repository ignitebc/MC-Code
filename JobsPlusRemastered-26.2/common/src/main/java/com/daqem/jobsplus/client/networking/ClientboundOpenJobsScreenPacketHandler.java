package com.daqem.jobsplus.client.networking;

import com.daqem.jobsplus.JobsPlus;
import com.daqem.jobsplus.client.gui.confimation.ConfirmationScreen;
import com.daqem.jobsplus.client.gui.confimation.ConfirmationScreenState;
import com.daqem.jobsplus.client.gui.confimation.PendingJobSelectionAlert;
import com.daqem.jobsplus.client.gui.jobs.JobsScreen;
import com.daqem.jobsplus.client.gui.jobs.JobsScreenState;
import com.daqem.jobsplus.client.gui.jobs.tab.RightTab;
import com.daqem.jobsplus.networking.s2c.ClientboundOpenJobsScreenPacket;
import com.daqem.jobsplus.player.job.Job;
import com.daqem.jobsplus.shop.ShopOffer;
import com.daqem.jobsplus.util.KoreanJosa;
import dev.architectury.networking.NetworkManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ClientboundOpenJobsScreenPacketHandler {
    public static void handleClientSide(ClientboundOpenJobsScreenPacket packet, NetworkManager.PacketContext context) {
        Minecraft mc = Minecraft.getInstance();

        List<Job> jobs = packet.getJobs();
        int coins = packet.getCoins();
        int maxJobs = packet.getMaxJobs();

        if (jobs == null || jobs.isEmpty()) {
            JobsPlus.LOGGER.error("Cannot open the jobs screen because the server sent no jobs.");
            return;
        }

        @Nullable
        Screen previousScreen = null;
        if (mc.gui.screen() instanceof JobsScreen jobsScreen) {
            previousScreen = jobsScreen.getPreviousScreen();
        }

        if (mc.gui.screen() instanceof JobsScreen jobsScreen) {
            JobsScreenState oldState = jobsScreen.getState();

            RightTab keepTab = oldState.getSelectedRightTab();
            Job keepJob = findSameJobOrFirst(jobs, oldState.getSelectedJob());
            @Nullable
            ShopOffer keepOffer = oldState.getSelectedShopOffer();
            String keepStockId = oldState.getSelectedStockId();
            String keepHoldingStockId = oldState.getSelectedHoldingStockId();
            var keepStockPanelMode = oldState.getStockPanelMode();
            var keepStockPositionSide = oldState.getSelectedStockPositionSide();
            int keepStockLeverage = oldState.getSelectedStockLeverage();

            JobsScreenState newState = new JobsScreenState(
                    jobs, coins, maxJobs, keepJob, keepTab, packet.getStockAccount());
            newState.setSelectedShopOffer(keepOffer);
            newState.setSelectedStockId(keepStockId);
            newState.setSelectedHoldingStockId(keepHoldingStockId);
            newState.setStockPanelMode(keepStockPanelMode);
            newState.setSelectedStockPositionSide(keepStockPositionSide);
            newState.setSelectedStockLeverage(keepStockLeverage);

            mc.gui.setScreen(new JobsScreen(newState, previousScreen));
            showPendingSelectionAlert(mc, jobs);
            return;
        }

        mc.gui.setScreen(new JobsScreen(
                new JobsScreenState(jobs, coins, maxJobs, null, RightTab.EXPERIENCE, packet.getStockAccount()),
                previousScreen));
        showPendingSelectionAlert(mc, jobs);
    }

    private static void showPendingSelectionAlert(Minecraft mc, List<Job> jobs) {
        String jobName = PendingJobSelectionAlert.consumeIfSelected(jobs);
        if (jobName == null) {
            return;
        }

        Component message = JobsPlus.translatable(
                "gui.confirmation.job_selected", jobName, KoreanJosa.eulReul(jobName));
        mc.gui.setScreen(new ConfirmationScreen(
                mc.gui.screen(),
                ConfirmationScreenState.alert(message, JobsPlus.translatable("gui.confirmation.ok"))));
    }

    private static Job findSameJobOrFirst(List<Job> newJobs, @Nullable Job oldSelected) {
        if (newJobs == null || newJobs.isEmpty()) {
            return null;
        }
        if (oldSelected == null) {
            return newJobs.getFirst();
        }

        Identifier oldId = oldSelected.getJobInstance().getLocation();
        for (Job j : newJobs) {
            if (j.getJobInstance().getLocation().equals(oldId)) {
                return j;
            }
        }
        return newJobs.getFirst();
    }
}
