package com.daqem.jobsplus.networking.s2c;

import com.daqem.jobsplus.networking.JobsPlusNetworking;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.jetbrains.annotations.NotNull;

public record ClientboundHyperLeapPacket(double directionX, double directionZ, double distance)
        implements CustomPacketPayload
{
    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundHyperLeapPacket> STREAM_CODEC = new StreamCodec<>()
    {
        @Override
        public @NotNull ClientboundHyperLeapPacket decode(RegistryFriendlyByteBuf buffer)
        {
            return new ClientboundHyperLeapPacket(buffer.readDouble(), buffer.readDouble(), buffer.readDouble());
        }

        @Override
        public void encode(RegistryFriendlyByteBuf buffer, ClientboundHyperLeapPacket packet)
        {
            buffer.writeDouble(packet.directionX());
            buffer.writeDouble(packet.directionZ());
            buffer.writeDouble(packet.distance());
        }
    };

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type()
    {
        return JobsPlusNetworking.CLIENTBOUND_HYPER_LEAP;
    }
}
