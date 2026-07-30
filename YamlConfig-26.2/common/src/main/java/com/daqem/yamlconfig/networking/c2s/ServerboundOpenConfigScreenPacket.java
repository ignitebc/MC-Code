package com.daqem.yamlconfig.networking.c2s;

import com.daqem.yamlconfig.YamlConfig;
import com.daqem.yamlconfig.api.config.IConfig;
import com.daqem.yamlconfig.networking.YamlConfigNetworking;
import com.daqem.yamlconfig.networking.s2c.ClientboundOpenConfigScreenPacket;
import dev.architectury.networking.NetworkManager;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.Permissions;
import org.jetbrains.annotations.NotNull;

public class ServerboundOpenConfigScreenPacket implements CustomPacketPayload {

    private final String modId;
    private final String configName;

    // 패킷에는 문자열 ID만 담는다. 설정 조회는 권한 검사 후 서버 핸들러에서 수행해,
    // 조작된 ID로 디코딩 중 NPE가 발생하지 않게 한다.
    public static final StreamCodec<RegistryFriendlyByteBuf, ServerboundOpenConfigScreenPacket> STREAM_CODEC = StreamCodec.of(
            (buf, packet) -> {
                buf.writeUtf(packet.modId);
                buf.writeUtf(packet.configName);
            },
            buf -> new ServerboundOpenConfigScreenPacket(buf.readUtf(), buf.readUtf())
    );

    public ServerboundOpenConfigScreenPacket(String modId, String configName) {
        this.modId = modId;
        this.configName = configName;
    }

    public ServerboundOpenConfigScreenPacket(IConfig config) {
        this.modId = config.getModId();
        this.configName = config.getName();
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return YamlConfigNetworking.SERVERBOUND_OPEN_CONFIG_SCREEN_PACKET;
    }

    public void handleServerSide(NetworkManager.PacketContext packetContext) {
        if (!packetContext.getPlayer().permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER)) {
            return;
        }

        IConfig config = YamlConfig.CONFIG_MANAGER.getConfig(this.modId, this.configName);
        if (config == null) {
            YamlConfig.LOGGER.warn("Ignored open-config request for unknown config: {}:{}", this.modId, this.configName);
            return;
        }

        NetworkManager.sendToPlayer(
                (ServerPlayer) packetContext.getPlayer(),
                new ClientboundOpenConfigScreenPacket(config)
        );
    }
}
