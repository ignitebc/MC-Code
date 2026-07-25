package com.daqem.jobsplus.networking.s2c;

import com.daqem.jobsplus.networking.JobsPlusNetworking;
import com.daqem.jobsplus.player.job.Job;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ClientboundOpenPowerupsScreenPacket implements CustomPacketPayload {
    private final List<Job> jobs;
    private final int coins;
    private final int maxJobs;
    private final ResourceLocation jobLocation;

    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundOpenPowerupsScreenPacket> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public @NotNull ClientboundOpenPowerupsScreenPacket decode(RegistryFriendlyByteBuf buf) {
            return new ClientboundOpenPowerupsScreenPacket(buf);
        }

        @Override
        public void encode(RegistryFriendlyByteBuf buf, ClientboundOpenPowerupsScreenPacket packet) {
            buf.writeCollection(packet.jobs, Job.Serializer::toNetwork);
            buf.writeInt(packet.coins);
            buf.writeInt(packet.maxJobs);
            buf.writeResourceLocation(packet.jobLocation);
        }
    };

    // 호환용(기존 호출부용)
    public ClientboundOpenPowerupsScreenPacket(List<Job> jobs, int coins, ResourceLocation jobLocation) {
        this(jobs, coins, 0, jobLocation);
    }

    public ClientboundOpenPowerupsScreenPacket(List<Job> jobs, int coins, int maxJobs, ResourceLocation jobLocation) {
        this.jobs = jobs;
        this.coins = coins;
        this.maxJobs = Math.max(0, maxJobs);
        this.jobLocation = jobLocation;
    }

    public ClientboundOpenPowerupsScreenPacket(RegistryFriendlyByteBuf friendlyByteBuf) {
        this.jobs = friendlyByteBuf.readList(b -> Job.Serializer.fromNetwork(b, null));
        this.coins = friendlyByteBuf.readInt();
        this.maxJobs = friendlyByteBuf.readableBytes() > 0 ? Math.max(0, friendlyByteBuf.readInt()) : 0;
        this.jobLocation = friendlyByteBuf.readResourceLocation();
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return JobsPlusNetworking.CLIENTBOUND_OPEN_POWERUPS_SCREEN;
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

    public ResourceLocation getJobLocation() {
        return jobLocation;
    }
}
