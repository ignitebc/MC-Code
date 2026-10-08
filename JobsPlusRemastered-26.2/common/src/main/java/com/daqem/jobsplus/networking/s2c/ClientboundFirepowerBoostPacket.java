package com.daqem.jobsplus.networking.s2c;

import com.daqem.jobsplus.networking.JobsPlusNetworking;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.jetbrains.annotations.NotNull;

/**
 * 사냥꾼 화력 증강으로 늘어나는 권총·돌격소총 장탄 수를 본인 클라이언트에 알린다.
 * TACZ 클라이언트가 재장전 가능 여부와 탄약 표시를 이 값으로 서버와 맞춘다.
 */
public class ClientboundFirepowerBoostPacket implements CustomPacketPayload {

    private final int extraRounds;

    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundFirepowerBoostPacket> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public @NotNull ClientboundFirepowerBoostPacket decode(RegistryFriendlyByteBuf buf) {
            return new ClientboundFirepowerBoostPacket(buf.readVarInt());
        }

        @Override
        public void encode(RegistryFriendlyByteBuf buf, ClientboundFirepowerBoostPacket packet) {
            buf.writeVarInt(packet.extraRounds);
        }
    };

    public ClientboundFirepowerBoostPacket(int extraRounds) {
        this.extraRounds = extraRounds;
    }

    public int getExtraRounds() {
        return extraRounds;
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return JobsPlusNetworking.CLIENTBOUND_FIREPOWER_BOOST;
    }
}
