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

    /**
     * 호환용 생성자(기존 코드 유지)
     * - maxJobs를 별도 전송하지 않는 경우를 위해 기본값을 넣는다.
     */
    public ClientboundOpenJobsScreenPacket(List<Job> jobs, int coins) {
        this(jobs, coins, 0);
    }

    /**
     * 신규 생성자: 서버에서 계산된 maxJobs(전역 maxJobs + 플레이어별 추가 슬롯)를 전달한다.
     */
    public ClientboundOpenJobsScreenPacket(List<Job> jobs, int coins, int maxJobs) {
        this.jobs = jobs;
        this.coins = coins;
        this.maxJobs = Math.max(0, maxJobs);
    }

    public ClientboundOpenJobsScreenPacket(RegistryFriendlyByteBuf friendlyByteBuf) {
        this.jobs = friendlyByteBuf.readList(friendlyByteBuf1 -> Job.Serializer.fromNetwork(friendlyByteBuf1, null));
        this.coins = friendlyByteBuf.readInt();
        // 구버전 패킷 호환: 남은 데이터가 없으면 0 처리
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
