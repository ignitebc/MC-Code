package com.daqem.jobsplus.networking.s2c;

import com.daqem.jobsplus.networking.JobsPlusNetworking;
import com.daqem.jobsplus.player.job.Job;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ClientboundOpenJobsScreenPacket implements CustomPacketPayload {
    private final List<Job> jobs;
    private final int coins;
    private final int maxJobs;

    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundOpenJobsScreenPacket> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public @NotNull ClientboundOpenJobsScreenPacket decode(RegistryFriendlyByteBuf buf) {
            return new ClientboundOpenJobsScreenPacket(buf);
        }

        @Override
        public void encode(RegistryFriendlyByteBuf buf, ClientboundOpenJobsScreenPacket packet) {
            buf.writeCollection(packet.jobs, Job.Serializer::toNetwork);
            buf.writeInt(packet.coins);
            buf.writeInt(packet.maxJobs);
        }
    };

    // 호환용(기존 호출부용)
    public ClientboundOpenJobsScreenPacket(List<Job> jobs, int coins) {
        this(jobs, coins, 0);
    }

    public ClientboundOpenJobsScreenPacket(List<Job> jobs, int coins, int maxJobs) {
        this.jobs = jobs;
        this.coins = coins;
        this.maxJobs = Math.max(0, maxJobs);
    }

    public ClientboundOpenJobsScreenPacket(RegistryFriendlyByteBuf friendlyByteBuf) {
        this.jobs = friendlyByteBuf.readList(b -> Job.Serializer.fromNetwork(b, null));
        this.coins = friendlyByteBuf.readInt();
        // 구버전 호환: 남은 데이터가 없으면 0
        this.maxJobs = friendlyByteBuf.readableBytes() > 0 ? Math.max(0, friendlyByteBuf.readInt()) : 0;
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return JobsPlusNetworking.CLIENTBOUND_OPEN_JOBS_SCREEN;
    }

    public List<Job> getJobs() {
        return jobs;
    }

    public int getCoins() {
        return coins;
    }

    public int getMaxJobs() {
        return maxJobs;
    }
}
