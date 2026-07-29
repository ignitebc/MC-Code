package com.daqem.jobsplus.networking;

import com.daqem.jobsplus.networking.s2c.ClientboundOpenJobsScreenPacket;
import com.daqem.jobsplus.player.JobsServerPlayer;
import dev.architectury.networking.NetworkManager;

import java.util.stream.Stream;

public final class StockScreenSync
{
    private StockScreenSync()
    {
    }

    public static void send(JobsServerPlayer player)
    {
        NetworkManager.sendToPlayer(
                player.jobsplus$getServerPlayer(),
                new ClientboundOpenJobsScreenPacket(
                        Stream.concat(player.jobsplus$getJobs().stream(), player.jobsplus$getInactiveJobs().stream())
                                .toList(),
                        player.jobsplus$getCoins(),
                        player.jobsplus$getEffectiveMaxJobs(),
                        player.jobsplus$getStockAccount()
                )
        );
    }
}
