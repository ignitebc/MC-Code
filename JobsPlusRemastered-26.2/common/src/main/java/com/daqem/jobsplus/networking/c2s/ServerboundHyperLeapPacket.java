package com.daqem.jobsplus.networking.c2s;

import com.daqem.jobsplus.networking.JobsPlusNetworking;
import com.daqem.jobsplus.player.job.hyper.HyperSkillHandler;
import dev.architectury.networking.NetworkManager;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

/** 클라이언트는 입력 전환만 전송한다. 충전 시간과 거리는 서버가 계산한다. */
public record ServerboundHyperLeapPacket(Action action, int sequence) implements CustomPacketPayload
{
    public enum Action { START, RELEASE, CANCEL }

    public static final StreamCodec<RegistryFriendlyByteBuf, ServerboundHyperLeapPacket> STREAM_CODEC = new StreamCodec<>()
    {
        @Override
        public @NotNull ServerboundHyperLeapPacket decode(RegistryFriendlyByteBuf buffer)
        {
            return new ServerboundHyperLeapPacket(buffer.readEnum(Action.class), buffer.readVarInt());
        }

        @Override
        public void encode(RegistryFriendlyByteBuf buffer, ServerboundHyperLeapPacket packet)
        {
            buffer.writeEnum(packet.action());
            buffer.writeVarInt(packet.sequence());
        }
    };

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type()
    {
        return JobsPlusNetworking.SERVERBOUND_HYPER_LEAP;
    }

    public static void handleServerSide(ServerboundHyperLeapPacket packet, NetworkManager.PacketContext context)
    {
        if (context.getPlayer() instanceof ServerPlayer player)
        {
            player.level().getServer().execute(() -> HyperSkillHandler.handleLeapInput(player, packet));
        }
    }
}
