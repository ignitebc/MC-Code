package com.daqem.jobsplus.networking.s2c;

import com.daqem.jobsplus.networking.JobsPlusNetworking;
import com.daqem.jobsplus.stock.StockMarketSnapshot;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.jetbrains.annotations.NotNull;

/**
 * 서버가 확정한 시세 스냅샷을 클라이언트에 내려보내는 패킷.
 * <p>
 * 스냅샷이 갱신될 때 접속 중인 전원에게, 그리고 직업 창을 열 때 해당 플레이어에게 전송된다.
 */
public class ClientboundStockSnapshotPacket implements CustomPacketPayload
{
    private final StockMarketSnapshot snapshot;

    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundStockSnapshotPacket> STREAM_CODEC =
            new StreamCodec<>()
            {
                @Override
                public @NotNull ClientboundStockSnapshotPacket decode(RegistryFriendlyByteBuf buffer)
                {
                    return new ClientboundStockSnapshotPacket(StockMarketSnapshot.fromNetwork(buffer));
                }

                @Override
                public void encode(RegistryFriendlyByteBuf buffer, ClientboundStockSnapshotPacket packet)
                {
                    packet.snapshot.toNetwork(buffer);
                }
            };

    public ClientboundStockSnapshotPacket(StockMarketSnapshot snapshot)
    {
        this.snapshot = snapshot;
    }

    public StockMarketSnapshot getSnapshot()
    {
        return snapshot;
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type()
    {
        return JobsPlusNetworking.CLIENTBOUND_STOCK_SNAPSHOT;
    }
}
