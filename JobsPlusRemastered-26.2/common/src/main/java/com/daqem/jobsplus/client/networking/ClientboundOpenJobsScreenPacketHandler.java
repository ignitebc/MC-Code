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

        // 알림 창이 떠 있는 동안에도 주식 정산 등으로 화면 갱신 패킷이 올 수 있다.
        // 갱신 대상은 알림에 가려진 직업 화면이므로 그 화면을 먼저 찾는다.
        @Nullable
        ConfirmationScreen openAlert = findOpenAlert(mc.gui.screen());
        @Nullable
        JobsScreen currentJobsScreen = findJobsScreen(mc.gui.screen());
        @Nullable
        Screen previousScreen = null;
        if (currentJobsScreen != null) {
            previousScreen = currentJobsScreen.getPreviousScreen();
        }

        JobsScreenState newState;
        if (currentJobsScreen != null) {
            newState = copyViewState(currentJobsScreen.getState(), jobs, coins, maxJobs, packet);
        } else {
            newState = new JobsScreenState(jobs, coins, maxJobs, null, RightTab.EXPERIENCE, packet.getStockAccount());
        }

        mc.gui.setScreen(new JobsScreen(newState, previousScreen));
        if (showPendingSelectionAlert(mc, jobs)) {
            return;
        }
        restoreOpenAlert(mc, openAlert);
    }

    private static JobsScreenState copyViewState(JobsScreenState oldState, List<Job> jobs, int coins, int maxJobs,
            ClientboundOpenJobsScreenPacket packet) {
        RightTab keepTab = oldState.getSelectedRightTab();
        Job keepJob = findSameJobOrFirst(jobs, oldState.getSelectedJob());
        @Nullable
        ShopOffer keepOffer = oldState.getSelectedShopOffer();

        JobsScreenState newState = new JobsScreenState(
                jobs, coins, maxJobs, keepJob, keepTab, packet.getStockAccount());
        newState.setSelectedShopOffer(keepOffer);
        newState.setSelectedStockId(oldState.getSelectedStockId());
        newState.setSelectedHoldingStockId(oldState.getSelectedHoldingStockId());
        newState.setStockPanelMode(oldState.getStockPanelMode());
        newState.setSelectedStockPositionSide(oldState.getSelectedStockPositionSide());
        newState.setSelectedStockLeverage(oldState.getSelectedStockLeverage());
        return newState;
    }

    /** @return 완료 알림을 띄웠으면 {@code true} */
    private static boolean showPendingSelectionAlert(Minecraft mc, List<Job> jobs) {
        String jobName = PendingJobSelectionAlert.consumeIfSelected(jobs);
        if (jobName == null) {
            return false;
        }

        Component message = JobsPlus.translatable(
                "gui.confirmation.job_selected", jobName, KoreanJosa.eulReul(jobName));
        mc.gui.setScreen(new ConfirmationScreen(
                mc.gui.screen(),
                ConfirmationScreenState.alert(message, JobsPlus.translatable("gui.confirmation.ok"))));
        return true;
    }

    /** 갱신 전에 떠 있던 알림은 갱신된 화면 위에 그대로 다시 올려 준다. */
    private static void restoreOpenAlert(Minecraft mc, @Nullable ConfirmationScreen openAlert) {
        if (openAlert == null) {
            return;
        }
        mc.gui.setScreen(new ConfirmationScreen(mc.gui.screen(), openAlert.getState()));
    }

    @Nullable
    private static ConfirmationScreen findOpenAlert(@Nullable Screen currentScreen) {
        if (currentScreen instanceof ConfirmationScreen confirmationScreen && confirmationScreen.isAlert()) {
            return confirmationScreen;
        }
        return null;
    }

    @Nullable
    private static JobsScreen findJobsScreen(@Nullable Screen currentScreen) {
        Screen screen = currentScreen;
        while (screen instanceof ConfirmationScreen confirmationScreen) {
            screen = confirmationScreen.getPreviousScreen();
        }
        if (screen instanceof JobsScreen jobsScreen) {
            return jobsScreen;
        }
        return null;
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
