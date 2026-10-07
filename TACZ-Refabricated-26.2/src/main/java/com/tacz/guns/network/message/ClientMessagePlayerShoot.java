package com.tacz.guns.network.message;

import com.tacz.guns.GunMod;
import com.tacz.guns.api.entity.IGunOperator;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;

import java.util.function.Supplier;

public class ClientMessagePlayerShoot implements CustomPacketPayload {
    public static final Identifier PACKET_ID = Identifier.fromNamespaceAndPath(GunMod.MOD_ID, "c2s_player_shoot");
    public static final CustomPacketPayload.Type<ClientMessagePlayerShoot> TYPE = new CustomPacketPayload.Type<>(PACKET_ID);
    public static final StreamCodec<FriendlyByteBuf, ClientMessagePlayerShoot> CODEC = StreamCodec.ofMember(ClientMessagePlayerShoot::write, ClientMessagePlayerShoot::new);
    /**
     * 클라이언트가 보낸 조준과 서버가 알고 있는 회전의 허용 차이(도).
     * 서버 회전은 최대 한 틱 전 값이라 빠르게 돌리며 쏘면 차이가 난다. 이보다 크게 다르면 비정상 값으로 보고 서버 회전을 쓴다.
     */
    private static final float MAX_AIM_DIFFERENCE_DEGREES = 60f;

    /**
     * 这里的 timestamp 应该是基于 base timestamp 的相对值
     */
    private final long timestamp;
    private float chargeProgress;
    /** 클라이언트가 방아쇠를 당긴 순간의 조준(도) */
    private final float pitch;
    private final float yaw;

    public ClientMessagePlayerShoot(long timestamp, float chargeProgress, float pitch, float yaw) {
        this.timestamp = timestamp;
        this.chargeProgress = chargeProgress;
        this.pitch = pitch;
        this.yaw = yaw;
    }

    public ClientMessagePlayerShoot(FriendlyByteBuf buf) {
        this(buf.readLong(), buf.readFloat(), buf.readFloat(), buf.readFloat());
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeLong(timestamp);
        buf.writeFloat(chargeProgress);
        buf.writeFloat(pitch);
        buf.writeFloat(yaw);
    }

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    /**
     * 클라이언트가 쏜 순간의 조준으로 탄을 쏜다.
     * <p>
     * 클라이언트는 연사 간격을 맞추려고 틱 사이에 발사 패킷을 보내므로, 서버가 패킷을 받는 시점의 회전은
     * 마지막 이동 패킷의 회전(최대 한 틱 전)이다. 조준을 옮기며 쏘면 탄이 조준점 뒤쪽으로 나가므로 쏜 순간의 조준을 함께 받는다.
     */
    public void handle(ServerPlayer player, PacketSender responseSender) {
        float serverPitch = player.getXRot();
        float serverYaw = player.getYRot();
        if (!isAimReasonable(serverPitch, serverYaw)) {
            IGunOperator.fromLivingEntity(player).shoot(player::getXRot, player::getYRot, timestamp, chargeProgress);
            return;
        }
        // 점사의 뒷발은 패킷을 받은 뒤 서버가 받은 회전 변화(반동으로 들린 조준 등)를 더해 이어 간다.
        Supplier<Float> pitchSupplier = () -> Mth.clamp(pitch + (player.getXRot() - serverPitch), -90f, 90f);
        Supplier<Float> yawSupplier = () -> yaw + (player.getYRot() - serverYaw);
        IGunOperator.fromLivingEntity(player).shoot(pitchSupplier, yawSupplier, timestamp, chargeProgress);
    }

    private boolean isAimReasonable(float serverPitch, float serverYaw) {
        boolean finite = Float.isFinite(pitch) && Float.isFinite(yaw);
        if (!finite || Math.abs(pitch) > 90f) {
            return false;
        }
        float pitchDifference = Math.abs(pitch - serverPitch);
        float yawDifference = Math.abs(Mth.wrapDegrees(yaw - serverYaw));
        return pitchDifference <= MAX_AIM_DIFFERENCE_DEGREES && yawDifference <= MAX_AIM_DIFFERENCE_DEGREES;
    }
}
