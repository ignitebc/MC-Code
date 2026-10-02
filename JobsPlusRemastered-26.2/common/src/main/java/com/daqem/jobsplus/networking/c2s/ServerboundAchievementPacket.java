package com.daqem.jobsplus.networking.c2s;

import com.daqem.jobsplus.achievement.AchievementManager;
import com.daqem.jobsplus.achievement.AchievementRewards;
import com.daqem.jobsplus.networking.JobsPlusNetworking;
import dev.architectury.networking.NetworkManager;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

/** 빈 ID는 조회, 등록된 ID는 수령 요청. 보상량·목표 달성값은 클라이언트에서 받지 않는다. */
public record ServerboundAchievementPacket(String season, String id) implements CustomPacketPayload
{
    public static final StreamCodec<RegistryFriendlyByteBuf, ServerboundAchievementPacket> STREAM_CODEC = new StreamCodec<>()
    {
        @Override
        public @NotNull ServerboundAchievementPacket decode(RegistryFriendlyByteBuf buffer)
        {
            return new ServerboundAchievementPacket(buffer.readUtf(32), buffer.readUtf(3));
        }

        @Override
        public void encode(RegistryFriendlyByteBuf buffer, ServerboundAchievementPacket packet)
        {
            buffer.writeUtf(packet.season, 32);
            buffer.writeUtf(packet.id, 3);
        }
    };

    public static void handleServerSide(ServerboundAchievementPacket packet, NetworkManager.PacketContext context)
    {
        if (!(context.getPlayer() instanceof ServerPlayer player))
        {
            return;
        }
        var server = player.level().getServer();
        server.execute(() -> {
            if (server.getPlayerList().getPlayer(player.getUUID()) != player
                    || !AchievementManager.requestAllowed(player, !packet.id.isEmpty()))
            {
                return;
            }
            if (packet.id.isEmpty())
            {
                AchievementManager.view(player);
            }
            else
            {
                AchievementRewards.claim(player, packet.season, packet.id);
            }
        });
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type()
    {
        return JobsPlusNetworking.SERVERBOUND_ACHIEVEMENTS;
    }
}
