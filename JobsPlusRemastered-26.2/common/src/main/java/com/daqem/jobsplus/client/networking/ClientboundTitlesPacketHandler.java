package com.daqem.jobsplus.client.networking;

import com.daqem.jobsplus.client.title.ClientTitles;
import com.daqem.jobsplus.networking.s2c.ClientboundTitlesPacket;
import com.daqem.jobsplus.player.title.TitleType;
import dev.architectury.networking.NetworkManager;

import java.util.ArrayList;
import java.util.List;

public class ClientboundTitlesPacketHandler
{
    public static void handleClientSide(ClientboundTitlesPacket packet, NetworkManager.PacketContext context)
    {
        List<ClientTitles.Entry> entries = new ArrayList<>();
        for (ClientboundTitlesPacket.Entry entry : packet.getEntries())
        {
            TitleType.byId(entry.titleId()).ifPresent(type ->
                    entries.add(new ClientTitles.Entry(type, entry.holderName(), entry.mine())));
        }
        TitleType equipped = TitleType.byId(packet.getEquippedId()).orElse(null);
        ClientTitles.update(entries, equipped);
    }
}
