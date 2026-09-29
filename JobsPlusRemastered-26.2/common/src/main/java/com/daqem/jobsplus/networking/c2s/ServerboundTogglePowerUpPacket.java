package com.daqem.jobsplus.networking.c2s;

import com.daqem.jobsplus.metrics.MetricsEvent;
import com.daqem.jobsplus.networking.JobsPlusNetworking;
import com.daqem.jobsplus.player.JobsServerPlayer;
import com.daqem.jobsplus.player.job.Job;
import com.daqem.jobsplus.player.job.powerup.PowerupState;
import dev.architectury.networking.NetworkManager;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

public class ServerboundTogglePowerUpPacket implements CustomPacketPayload {

    private final Identifier jobLocation;
    private final Identifier powerupLocation;

    public static final StreamCodec<RegistryFriendlyByteBuf, ServerboundTogglePowerUpPacket> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public @NotNull ServerboundTogglePowerUpPacket decode(RegistryFriendlyByteBuf buf) {
            return new ServerboundTogglePowerUpPacket(buf);
        }

        @Override
        public void encode(RegistryFriendlyByteBuf buf, ServerboundTogglePowerUpPacket packet) {
            buf.writeIdentifier(packet.jobLocation);
            buf.writeIdentifier(packet.powerupLocation);
        }
    };

    public ServerboundTogglePowerUpPacket(Identifier jobLocation, Identifier powerupLocation) {
        this.jobLocation = jobLocation;
        this.powerupLocation = powerupLocation;
    }

    public ServerboundTogglePowerUpPacket(RegistryFriendlyByteBuf friendlyByteBuf) {
        this.jobLocation = friendlyByteBuf.readIdentifier();
        this.powerupLocation = friendlyByteBuf.readIdentifier();
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return JobsPlusNetworking.SERVERBOUND_TOGGLE_POWERUP;
    }

    public static void handleServerSide(ServerboundTogglePowerUpPacket packet, NetworkManager.PacketContext context) {
        if (context.getPlayer() instanceof JobsServerPlayer serverPlayer) {
            Job job = serverPlayer.jobsplus$getJob(packet.jobLocation);
            if (job != null) {
                job.getPowerupManager().getPowerup(packet.powerupLocation).ifPresent(powerup -> {
                    PowerupState stateBefore = powerup.getState();
                    powerup.toggle();
                    serverPlayer.jobsplus$updateJob(job);
                    // 꺼 둔 스킬은 효과가 없으므로 구간별 활성 스킬을 복원하려면 전환 시점이 필요하다.
                    MetricsEvent.of("POWERUP_TOGGLE")
                            .player(serverPlayer.jobsplus$getServerPlayer())
                            .job(packet.jobLocation)
                            .target(packet.powerupLocation)
                            .before(stateBefore)
                            .after(powerup.getState())
                            .jobLevel(job.getLevel())
                            .record();
                });
            }
        }
    }
}
