package com.daqem.jobsplus.client.networking;

import com.daqem.arc.api.action.holder.IActionHolder;
import com.daqem.arc.api.player.ArcPlayer;
import com.daqem.jobsplus.integration.arc.holder.holders.job.JobInstance;
import com.daqem.jobsplus.integration.arc.holder.holders.powerup.PowerupInstance;
import com.daqem.jobsplus.networking.s2c.ClientboundSyncActionHoldersPacket;
import dev.architectury.networking.NetworkManager;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

public class ClientboundSyncActionHoldersPacketHandler
{

    public static void handleClientSide(ClientboundSyncActionHoldersPacket packet, NetworkManager.PacketContext context)
    {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null)
            return;

        if (!(mc.player instanceof ArcPlayer arcPlayer))
            return;

        // 1) 기존 홀더 초기화
        arcPlayer.arc$clearActionHolders();

        // 2) 서버가 준 활성 홀더 목록을 클라이언트에서도 동일하게 구성
        List<IActionHolder> holdersToAdd = new ArrayList<>();
        for (ResourceLocation id : packet.getActionHolders())
        {
            JobInstance job = JobInstance.of(id);
            if (job != null)
            {
                holdersToAdd.add(job);
                continue;
            }

            PowerupInstance powerup = PowerupInstance.of(id);
            if (powerup != null)
            {
                holdersToAdd.add(powerup);
            }
        }

        arcPlayer.arc$addActionHolders(holdersToAdd);
    }
}
