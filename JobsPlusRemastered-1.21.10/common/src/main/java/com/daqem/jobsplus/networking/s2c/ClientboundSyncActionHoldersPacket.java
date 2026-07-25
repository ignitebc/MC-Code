package com.daqem.jobsplus.networking.s2c;

import com.daqem.jobsplus.networking.JobsPlusNetworking;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ClientboundSyncActionHoldersPacket implements CustomPacketPayload
{

    private final List<ResourceLocation> actionHolders;

    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundSyncActionHoldersPacket> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public @NotNull ClientboundSyncActionHoldersPacket decode(RegistryFriendlyByteBuf buf)
        {
            int size = buf.readVarInt();
            List<ResourceLocation> list = new ArrayList<>(Math.max(0, size));
            for (int i = 0; i < size; i++)
            {
                list.add(buf.readResourceLocation());
            }
            return new ClientboundSyncActionHoldersPacket(list);
        }

        @Override
        public void encode(RegistryFriendlyByteBuf buf, ClientboundSyncActionHoldersPacket packet)
        {
            List<ResourceLocation> list = packet.actionHolders == null ? Collections.emptyList() : packet.actionHolders;
            buf.writeVarInt(list.size());
            for (ResourceLocation id : list)
            {
                buf.writeResourceLocation(id);
            }
        }
    };

    public ClientboundSyncActionHoldersPacket(List<ResourceLocation> actionHolders)
    {
        this.actionHolders = actionHolders == null ? List.of() : actionHolders;
    }

    public List<ResourceLocation> getActionHolders()
    {
        return actionHolders;
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type()
    {
        return JobsPlusNetworking.CLIENTBOUND_SYNC_ACTION_HOLDERS;
    }
}
