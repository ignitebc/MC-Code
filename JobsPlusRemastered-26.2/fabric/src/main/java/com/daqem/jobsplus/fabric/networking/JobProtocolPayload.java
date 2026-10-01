package com.daqem.jobsplus.fabric.networking;

import com.daqem.jobsplus.JobsPlus;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerConfigurationConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerConfigurationNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/** 직업 패킷을 보내기 전에 클라이언트가 같은 직업 데이터 형식을 지원하는지 확인한다. */
public record JobProtocolPayload() implements CustomPacketPayload {
    public static final JobProtocolPayload INSTANCE = new JobProtocolPayload();
    public static final Type<JobProtocolPayload> TYPE =
            new Type<>(JobsPlus.getId("job_protocol_v2"));
    public static final StreamCodec<FriendlyByteBuf, JobProtocolPayload> STREAM_CODEC =
            StreamCodec.unit(INSTANCE);

    public static void register() {
        PayloadTypeRegistry.clientboundConfiguration().register(TYPE, STREAM_CODEC);
        ServerConfigurationConnectionEvents.CONFIGURE.register((listener, server) -> {
            // 등록된 수신 채널이 프로토콜 지원 표시다. 플레이 진입 전에 검사해야 화면 디코딩 오류를 막는다.
            if (!ServerConfigurationNetworking.canSend(listener, TYPE)) {
                listener.disconnect(Component.literal(
                        "JobsPlus 업데이트가 필요합니다. 서버에서 배포한 최신 JobsPlus 모드로 교체한 뒤 다시 접속해 주세요."));
            }
        });
    }

    @Override
    public Type<JobProtocolPayload> type() {
        return TYPE;
    }
}
