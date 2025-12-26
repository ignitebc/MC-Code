package com.daqem.jobsplus.client.networking;

import com.daqem.arc.api.action.IAction;
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

public class ClientboundOpenJobsScreenPacketHandler
{
    public static void handleClientSide(ClientboundOpenJobsScreenPacket packet, NetworkManager.PacketContext context)
    {
        Minecraft mc = Minecraft.getInstance();

        // 패킷 데이터(프로젝트 실제 메서드명)
        List<Job> jobs = packet.getJobs();
        int coins = packet.getCoins();

        // 이전 화면 보존(기존 코드 유지)
        @Nullable Screen previousScreen = null;
        if (mc.screen instanceof JobsScreen jobsScreen)
        {
            previousScreen = jobsScreen.getPreviousScreen();
        }

        // 기존 JobsScreen이 열려 있으면 탭/선택 상태를 보존한다.
        if (mc.screen instanceof JobsScreen jobsScreen)
        {
            JobsScreenState oldState = jobsScreen.getState();

            RightTab keepTab = oldState.getSelectedRightTab();
            Job keepJob = findSameJobOrFirst(jobs, oldState.getSelectedJob());
            @Nullable ShopOffer keepOffer = oldState.getSelectedShopOffer();
            @Nullable IAction keepAction = oldState.getActiveAction();

            JobsScreenState newState = new JobsScreenState(jobs, coins, keepJob, keepTab);
            newState.setSelectedShopOffer(keepOffer);
            newState.setActiveAction(keepAction);

            mc.setScreen(new JobsScreen(newState, previousScreen));
            return;
        }

        // JobsScreen이 아니면 기본 동작
        mc.setScreen(new JobsScreen(new JobsScreenState(jobs, coins), previousScreen));
    }

    private static Job findSameJobOrFirst(List<Job> newJobs, @Nullable Job oldSelected)
    {
        if (newJobs == null || newJobs.isEmpty())
        {
            return null;
        }
        if (oldSelected == null)
        {
            return newJobs.getFirst();
        }

        ResourceLocation oldId = oldSelected.getJobInstance().getLocation();
        for (Job j : newJobs)
        {
            if (j.getJobInstance().getLocation().equals(oldId))
            {
                return j;
            }
        }
        return newJobs.getFirst();
    }
}
