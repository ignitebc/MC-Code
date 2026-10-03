package com.daqem.jobsplus.networking.s2c;

import com.daqem.jobsplus.networking.JobsPlusNetworking;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.jetbrains.annotations.NotNull;

public record ClientboundHyperStatusPacket(int smithLevel, int leapLevel, int shieldTicks,
                                           int shieldCooldown, int leapCooldown, boolean leaping)
        implements CustomPacketPayload
{
    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundHyperStatusPacket> STREAM_CODEC = new StreamCodec<>()
    {
        @Override
        public @NotNull ClientboundHyperStatusPacket decode(RegistryFriendlyByteBuf buffer)
        {
            return new ClientboundHyperStatusPacket(buffer.readVarInt(), buffer.readVarInt(), buffer.readVarInt(),
                    buffer.readVarInt(), buffer.readVarInt(), buffer.readBoolean());
        }

        @Override
        public void encode(RegistryFriendlyByteBuf buffer, ClientboundHyperStatusPacket packet)
        {
            buffer.writeVarInt(packet.smithLevel());
            buffer.writeVarInt(packet.leapLevel());
            buffer.writeVarInt(packet.shieldTicks());
            buffer.writeVarInt(packet.shieldCooldown());
            buffer.writeVarInt(packet.leapCooldown());
            buffer.writeBoolean(packet.leaping());
        }
    };

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type()
    {
        return JobsPlusNetworking.CLIENTBOUND_HYPER_STATUS;
    }
}
