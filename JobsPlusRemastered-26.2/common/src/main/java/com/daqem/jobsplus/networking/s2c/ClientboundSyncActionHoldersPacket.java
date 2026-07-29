package com.daqem.jobsplus.networking.s2c;

import com.daqem.jobsplus.networking.JobsPlusNetworking;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ClientboundSyncActionHoldersPacket implements CustomPacketPayload
{

    private final List<Identifier> actionHolders;

    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundSyncActionHoldersPacket> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public @NotNull ClientboundSyncActionHoldersPacket decode(RegistryFriendlyByteBuf buf)
        {
            int size = buf.readVarInt();
            List<Identifier> list = new ArrayList<>(Math.max(0, size));
            for (int i = 0; i < size; i++)
            {
                list.add(buf.readIdentifier());
            }
            return new ClientboundSyncActionHoldersPacket(list);
        }

        @Override
        public void encode(RegistryFriendlyByteBuf buf, ClientboundSyncActionHoldersPacket packet)
        {
            List<Identifier> list = packet.actionHolders == null ? Collections.emptyList() : packet.actionHolders;
            buf.writeVarInt(list.size());
            for (Identifier id : list)
            {
                buf.writeIdentifier(id);
            }
        }
    };

    public ClientboundSyncActionHoldersPacket(List<Identifier> actionHolders)
    {
        this.actionHolders = actionHolders == null ? List.of() : actionHolders;
    }

    public List<Identifier> getActionHolders()
    {
        return actionHolders;
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type()
    {
        return JobsPlusNetworking.CLIENTBOUND_SYNC_ACTION_HOLDERS;
    }
}
