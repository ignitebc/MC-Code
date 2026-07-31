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
 * 서버는 이 패킷으로 실제 시청자를 추적한다. 시청자가 없어도 미결제 포지션이나 예약 주문이 있으면
 * 체결 및 강제청산 감시를 위해 시세 갱신은 계속된다.
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

        // 시청 상태 토글을 반복 전송해 시세 세션 재시작을 유발하는 공격을 막는다.
        if (!StockViewRateLimiter.tryAcquire(jobsServerPlayer.jobsplus$getServerPlayer()))
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
