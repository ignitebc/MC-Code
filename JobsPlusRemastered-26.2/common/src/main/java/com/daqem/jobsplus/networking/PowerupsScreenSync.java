package com.daqem.jobsplus.networking;

import com.daqem.jobsplus.networking.s2c.ClientboundOpenPowerupsScreenPacket;
import com.daqem.jobsplus.player.JobsServerPlayer;
import dev.architectury.networking.NetworkManager;
import net.minecraft.resources.Identifier;

import java.util.stream.Stream;

/** 서버가 확정한 직업·스킬·코인 상태를 스킬 화면에 전달한다. */
public final class PowerupsScreenSync
{
    private PowerupsScreenSync()
    {
    }

    public static void send(JobsServerPlayer player, Identifier jobLocation)
    {
        NetworkManager.sendToPlayer(
                player.jobsplus$getServerPlayer(),
                new ClientboundOpenPowerupsScreenPacket(
                        Stream.concat(
                                player.jobsplus$getJobs().stream(),
                                player.jobsplus$getInactiveJobs().stream()).toList(),
                        player.jobsplus$getCoins(),
                        player.jobsplus$getEffectiveMaxJobs(),
                        jobLocation));
    }
}
