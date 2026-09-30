package com.daqem.jobsplus.networking.s2c;

import com.daqem.jobsplus.networking.JobsPlusNetworking;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.jetbrains.annotations.NotNull;

/**
 * 서버에 저장된 스킬 알림 설정을 클라이언트에 알린다.
 * 스킬 화면의 버튼 문구와 확인 창 질문이 이 값으로 정해진다.
 */
public class ClientboundSkillNotificationsPacket implements CustomPacketPayload {

    private final boolean enabled;

    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundSkillNotificationsPacket> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public @NotNull ClientboundSkillNotificationsPacket decode(RegistryFriendlyByteBuf buf) {
            return new ClientboundSkillNotificationsPacket(buf.readBoolean());
        }

        @Override
        public void encode(RegistryFriendlyByteBuf buf, ClientboundSkillNotificationsPacket packet) {
            buf.writeBoolean(packet.enabled);
        }
    };

    public ClientboundSkillNotificationsPacket(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return JobsPlusNetworking.CLIENTBOUND_SKILL_NOTIFICATIONS;
    }
}
