package com.daqem.jobsplus.client.networking;

import com.daqem.jobsplus.client.stock.ClientStockMarket;
import com.daqem.jobsplus.networking.s2c.ClientboundStockSnapshotPacket;
import dev.architectury.networking.NetworkManager;

public class ClientboundStockSnapshotPacketHandler
{
    public static void handleClientSide(ClientboundStockSnapshotPacket packet, NetworkManager.PacketContext context)
    {
        ClientStockMarket.setSnapshot(packet.getSnapshot());
    }
}
