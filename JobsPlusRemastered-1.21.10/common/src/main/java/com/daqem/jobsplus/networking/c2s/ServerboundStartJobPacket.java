package com.daqem.jobsplus.networking.c2s;

import com.daqem.jobsplus.JobsPlus;
import com.daqem.jobsplus.config.JobsPlusConfig;
import com.daqem.jobsplus.integration.arc.holder.holders.job.JobInstance;
import com.daqem.jobsplus.networking.JobsPlusNetworking;
import com.daqem.jobsplus.networking.s2c.ClientboundOpenJobsScreenPacket;
import com.daqem.jobsplus.player.JobsServerPlayer;
import dev.architectury.networking.NetworkManager;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.stream.Stream;

public class ServerboundStartJobPacket implements CustomPacketPayload {
    private final ResourceLocation jobLocation;

    public static final StreamCodec<RegistryFriendlyByteBuf, ServerboundStartJobPacket> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public @NotNull ServerboundStartJobPacket decode(RegistryFriendlyByteBuf buf) {
            return new ServerboundStartJobPacket(buf);
        }

        @Override
        public void encode(RegistryFriendlyByteBuf buf, ServerboundStartJobPacket packet) {
            buf.writeResourceLocation(packet.jobLocation);
        }
    };

    public ServerboundStartJobPacket(ResourceLocation jobLocation) {
        this.jobLocation = jobLocation;
    }

    public ServerboundStartJobPacket(RegistryFriendlyByteBuf friendlyByteBuf) {
        this.jobLocation = friendlyByteBuf.readResourceLocation();
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return JobsPlusNetworking.SERVERBOUND_START_JOB;
    }

    public static void handleServerSide(ServerboundStartJobPacket packet, NetworkManager.PacketContext context) {
        if (context.getPlayer() instanceof JobsServerPlayer serverPlayer) {
            JobInstance jobInstance = JobInstance.of(packet.jobLocation);

            if (jobInstance == null) {
                serverPlayer.jobsplus$getServerPlayer().sendSystemMessage(
                        JobsPlus.translatable("error.job_not_found", packet.jobLocation.toString()));
                return;
            }

            // 핵심: 전역 maxJobs가 아니라 "유효 최대 직업 수"로 비교
            if (serverPlayer.jobsplus$getJobs().size() >= serverPlayer.jobsplus$getEffectiveMaxJobs()) {
                serverPlayer.jobsplus$getServerPlayer()
                        .sendSystemMessage(JobsPlus.translatable("error.max_jobs_reached"));
                return;
            }

            if (serverPlayer.jobsplus$getJobs().size() >= JobsPlusConfig.amountOfFreeJobs.get()) {
                if (serverPlayer.jobsplus$getCoins() < jobInstance.getPrice()) {
                    serverPlayer.jobsplus$getServerPlayer()
                            .sendSystemMessage(JobsPlus.translatable("error.not_enough_coins"));
                    return;
                }
                serverPlayer.jobsplus$setCoins(serverPlayer.jobsplus$getCoins() - jobInstance.getPrice());
            }

            serverPlayer.jobsplus$addNewJob(jobInstance);

            // maxJobs(유효값) 포함해서 UI 갱신
            NetworkManager.sendToPlayer(
                    serverPlayer.jobsplus$getServerPlayer(),
                    new ClientboundOpenJobsScreenPacket(
                            Stream.concat(serverPlayer.jobsplus$getJobs().stream(),
                                    serverPlayer.jobsplus$getInactiveJobs().stream()).toList(),
                            serverPlayer.jobsplus$getCoins(),
                            serverPlayer.jobsplus$getEffectiveMaxJobs()));
        }
    }
}
