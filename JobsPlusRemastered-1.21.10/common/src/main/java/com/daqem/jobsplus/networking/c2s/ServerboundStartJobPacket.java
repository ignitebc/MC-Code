package com.daqem.jobsplus.networking.c2s;

import com.daqem.jobsplus.JobsPlus;
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
        if (!(context.getPlayer() instanceof JobsServerPlayer serverPlayer)) {
            return;
        }

        JobInstance jobInstance = JobInstance.of(packet.jobLocation);
        if (jobInstance == null) {
            serverPlayer.jobsplus$getServerPlayer().sendSystemMessage(
                    JobsPlus.translatable("error.job_not_found", packet.jobLocation.toString())
            );
            return;
        }

        // 1) 최대 직업 수 제한: "유효 최대 직업 수"(무료 1 + 티켓 누적, 단 config max_jobs로 상한) 기준
        if (serverPlayer.jobsplus$getJobs().size() >= serverPlayer.jobsplus$getEffectiveMaxJobs()) {
            serverPlayer.jobsplus$getServerPlayer()
                    .sendSystemMessage(JobsPlus.translatable("error.max_jobs_reached"));
            return;
        }

        // 2) 코인 요구 여부:
        // - 기본 free_jobs(예: 1)만으로 판단하면 "티켓으로 늘린 슬롯"도 유료가 되어 버림
        // - 본 서버 정책: 티켓으로 확보한 슬롯은 "무료 선택 가능 슬롯"으로 취급
        // => 따라서 "유효 무료 직업 수"를 기준으로 초과 시에만 코인 차감
        if (serverPlayer.jobsplus$getJobs().size() >= serverPlayer.jobsplus$getEffectiveFreeJobs()) {
            if (serverPlayer.jobsplus$getCoins() < jobInstance.getPrice()) {
                serverPlayer.jobsplus$getServerPlayer()
                        .sendSystemMessage(JobsPlus.translatable("error.not_enough_coins"));
                return;
            }
            serverPlayer.jobsplus$setCoins(serverPlayer.jobsplus$getCoins() - jobInstance.getPrice());
        }

        serverPlayer.jobsplus$addNewJob(jobInstance);

        // 3) UI 갱신: maxJobs는 "유효 최대 직업 수"로 전송
        NetworkManager.sendToPlayer(
                serverPlayer.jobsplus$getServerPlayer(),
                new ClientboundOpenJobsScreenPacket(
                        Stream.concat(
                                serverPlayer.jobsplus$getJobs().stream(),
                                serverPlayer.jobsplus$getInactiveJobs().stream()
                        ).toList(),
                        serverPlayer.jobsplus$getCoins(),
                        serverPlayer.jobsplus$getEffectiveMaxJobs()
                )
        );
    }
}
