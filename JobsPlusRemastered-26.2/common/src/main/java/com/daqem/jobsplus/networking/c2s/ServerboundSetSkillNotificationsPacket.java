package com.daqem.jobsplus.networking.c2s;

import com.daqem.jobsplus.networking.JobsPlusNetworking;
import com.daqem.jobsplus.player.SkillNotificationSettings;
import dev.architectury.networking.NetworkManager;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

/**
 * 스킬 화면의 알림 버튼에서 확인을 누르면 원하는 알림 상태를 서버에 보낸다.
 * <p>
 * 뒤집기 요청이 아니라 바꿀 값을 보낸다. 확인을 연달아 누르거나 화면 값이 늦게 갱신되어도
 * 플레이어가 확인 창에서 고른 상태로만 저장된다.
 */
public class ServerboundSetSkillNotificationsPacket implements CustomPacketPayload {

    private final boolean enabled;

    public static final StreamCodec<RegistryFriendlyByteBuf, ServerboundSetSkillNotificationsPacket> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public @NotNull ServerboundSetSkillNotificationsPacket decode(RegistryFriendlyByteBuf buf) {
            return new ServerboundSetSkillNotificationsPacket(buf.readBoolean());
        }

        @Override
        public void encode(RegistryFriendlyByteBuf buf, ServerboundSetSkillNotificationsPacket packet) {
            buf.writeBoolean(packet.enabled);
        }
    };

    public ServerboundSetSkillNotificationsPacket(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return JobsPlusNetworking.SERVERBOUND_SET_SKILL_NOTIFICATIONS;
    }

    public static void handleServerSide(ServerboundSetSkillNotificationsPacket packet, NetworkManager.PacketContext context) {
        if (context.getPlayer() instanceof ServerPlayer serverPlayer) {
            SkillNotificationSettings.set(serverPlayer, packet.enabled);
        }
    }
}
