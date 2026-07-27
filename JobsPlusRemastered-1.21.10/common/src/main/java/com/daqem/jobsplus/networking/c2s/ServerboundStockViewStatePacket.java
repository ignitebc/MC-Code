package com.daqem.jobsplus.networking.c2s;

import com.daqem.jobsplus.event.stock.StockMarketTicker;
import com.daqem.jobsplus.networking.JobsPlusNetworking;
import com.daqem.jobsplus.player.JobsServerPlayer;
import dev.architectury.networking.NetworkManager;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.jetbrains.annotations.NotNull;

/**
 * 주식 탭을 보고 있는지 서버에 알린다.
 * <p>
 * 시세 조회는 외부 API를 호출하므로, 아무도 보고 있지 않을 때까지 매분 돌릴 이유가 없다.
 * 서버는 이 패킷으로 실제 시청자를 추적해서 한 명이라도 있을 때만 갱신한다.
 */
public class ServerboundStockViewStatePacket implements CustomPacketPayload
{
    private final boolean viewing;

    public static final StreamCodec<RegistryFriendlyByteBuf, ServerboundStockViewStatePacket> STREAM_CODEC =
            new StreamCodec<>()
            {
                @Override
                public @NotNull ServerboundStockViewStatePacket decode(RegistryFriendlyByteBuf buffer)
                {
                    return new ServerboundStockViewStatePacket(buffer.readBoolean());
                }

                @Override
                public void encode(RegistryFriendlyByteBuf buffer, ServerboundStockViewStatePacket packet)
                {
                    buffer.writeBoolean(packet.viewing);
                }
            };

    public ServerboundStockViewStatePacket(boolean viewing)
    {
        this.viewing = viewing;
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type()
    {
        return JobsPlusNetworking.SERVERBOUND_STOCK_VIEW_STATE;
    }

    public static void handleServerSide(ServerboundStockViewStatePacket packet, NetworkManager.PacketContext context)
    {
        if (!(context.getPlayer() instanceof JobsServerPlayer jobsServerPlayer))
        {
            return;
        }

        if (packet.viewing)
        {
            StockMarketTicker.enterStockView(jobsServerPlayer.jobsplus$getServerPlayer());
            return;
        }
        StockMarketTicker.leaveStockView(jobsServerPlayer.jobsplus$getServerPlayer());
    }
}
