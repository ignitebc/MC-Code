package com.daqem.jobsplus.networking.c2s;

import com.daqem.jobsplus.JobsPlus;
import com.daqem.jobsplus.integration.arc.holder.holders.powerup.PowerupInstance;
import com.daqem.jobsplus.metrics.MetricsEvent;
import com.daqem.jobsplus.networking.JobsPlusNetworking;
import com.daqem.jobsplus.networking.PowerupsScreenSync;
import com.daqem.jobsplus.networking.s2c.ClientboundAlertPacket;
import com.daqem.jobsplus.player.JobsServerPlayer;
import com.daqem.jobsplus.player.job.Job;
import dev.architectury.networking.NetworkManager;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

public class ServerboundStartPowerupPacket implements CustomPacketPayload {

    private final Identifier jobLocation;
    private final Identifier powerupLocation;

    public static final StreamCodec<RegistryFriendlyByteBuf, ServerboundStartPowerupPacket> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public @NotNull ServerboundStartPowerupPacket decode(RegistryFriendlyByteBuf buf) {
            return new ServerboundStartPowerupPacket(buf);
        }

        @Override
        public void encode(RegistryFriendlyByteBuf buf, ServerboundStartPowerupPacket packet) {
            buf.writeIdentifier(packet.jobLocation);
            buf.writeIdentifier(packet.powerupLocation);
        }
    };

    public ServerboundStartPowerupPacket(Identifier jobLocation, Identifier powerupLocation) {
        this.jobLocation = jobLocation;
        this.powerupLocation = powerupLocation;
    }

    public ServerboundStartPowerupPacket(RegistryFriendlyByteBuf friendlyByteBuf) {
        this.jobLocation = friendlyByteBuf.readIdentifier();
        this.powerupLocation = friendlyByteBuf.readIdentifier();
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return JobsPlusNetworking.SERVERBOUND_START_POWERUP;
    }

    public static void handleServerSide(ServerboundStartPowerupPacket packet, NetworkManager.PacketContext context) {
        if (context.getPlayer() instanceof JobsServerPlayer serverPlayer) {
            Job job = serverPlayer.jobsplus$getJob(packet.jobLocation);
            PowerupInstance powerupInstance = PowerupInstance.of(packet.powerupLocation);

            if (job == null) {
                sendAlert(serverPlayer, JobsPlus.translatable("error.job_not_found", packet.jobLocation.toString()));
                return;
            }
            if (powerupInstance == null) {
                sendAlert(serverPlayer,
                        JobsPlus.translatable("error.powerup_not_found", packet.powerupLocation.toString()));
                return;
            }
            if (!powerupInstance.getJobLocation().equals(job.getJobInstance().getLocation())) {
                sendAlert(serverPlayer,
                        JobsPlus.translatable("error.could_not_add_powerup", powerupInstance.getName()));
                return;
            }
            if (serverPlayer.jobsplus$getCoins() < powerupInstance.getPrice()) {
                recordPurchaseFailure(serverPlayer, job, powerupInstance, "not_enough_coins");
                sendAlert(serverPlayer, JobsPlus.translatable("error.not_enough_coins"));
                return;
            }
            if (job.getLevel() < powerupInstance.getRequiredLevel()) {
                recordPurchaseFailure(serverPlayer, job, powerupInstance, "level_too_low");
                sendAlert(serverPlayer, JobsPlus.translatable("error.not_high_enough_level"));
                return;
            }
            if (job.getPowerupManager().getPowerup(powerupInstance).isPresent()) {
                sendAlert(serverPlayer,
                        JobsPlus.translatable("error.powerup_already_owned", powerupInstance.getName()));
                return;
            }
            if (powerupInstance.getParent() != null && job.getPowerupManager().getPowerup(powerupInstance.getParent()).isEmpty()) {
                recordPurchaseFailure(serverPlayer, job, powerupInstance, "parent_missing");
                sendAlert(serverPlayer,
                        JobsPlus.translatable("error.could_not_add_powerup", powerupInstance.getName()));
                return;
            }

            if (job.getPowerupManager().addPowerup(serverPlayer, job, powerupInstance)) {
                int coinsBefore = serverPlayer.jobsplus$getCoins();
                serverPlayer.jobsplus$setCoins(coinsBefore - powerupInstance.getPrice());
                // 스킬 구매 순서·시각·재원은 최종 NBT로 알 수 없으므로 구매 시점에 남긴다.
                MetricsEvent.of("POWERUP_BUY")
                        .player(serverPlayer.jobsplus$getServerPlayer())
                        .job(job.getJobInstance().getLocation())
                        .target(powerupInstance.getLocation())
                        .value(powerupInstance.getPrice())
                        .coins(coinsBefore, serverPlayer.jobsplus$getCoins())
                        .jobLevel(job.getLevel())
                        .detail("required_level", powerupInstance.getRequiredLevel())
                        .detail("parent", powerupInstance.getParent() == null ? "" : powerupInstance.getParent().getLocation())
                        .detail("owned_after", job.getPowerupManager().getAllPowerups().size())
                        .record();
                sendAlert(serverPlayer,
                        JobsPlus.translatable("gui.confirmation.powerup_purchased", powerupInstance.getName()));
                PowerupsScreenSync.send(serverPlayer, packet.jobLocation);
            } else {
                sendAlert(serverPlayer,
                        JobsPlus.translatable("error.could_not_add_powerup", powerupInstance.getName()));
            }
        }
    }

    /** 코인·레벨·선행 스킬 중 무엇이 구매를 막았는지 남겨 성장 병목을 구분한다. */
    private static void recordPurchaseFailure(JobsServerPlayer serverPlayer, Job job, PowerupInstance powerupInstance, String reason) {
        MetricsEvent.of("POWERUP_BUY_FAILED")
                .player(serverPlayer.jobsplus$getServerPlayer())
                .job(job.getJobInstance().getLocation())
                .target(powerupInstance.getLocation())
                .value(powerupInstance.getPrice())
                .coins(serverPlayer.jobsplus$getCoins(), serverPlayer.jobsplus$getCoins())
                .jobLevel(job.getLevel())
                .detail("reason", reason)
                .detail("required_level", powerupInstance.getRequiredLevel())
                .record();
    }

    /** 스킬 구매 결과는 화면을 열어 둔 채로 확인하므로 채팅이 아니라 모달 알림으로 알린다. */
    private static void sendAlert(JobsServerPlayer serverPlayer, Component message)
    {
        NetworkManager.sendToPlayer(
                serverPlayer.jobsplus$getServerPlayer(), new ClientboundAlertPacket(message));
    }
}
