package com.tacz.guns.network.message.event;

import cn.sh1rocu.tacz.api.LogicalSide;
import com.tacz.guns.GunMod;
import com.tacz.guns.api.event.common.GunDrawEvent;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public class ServerMessageGunDraw implements CustomPacketPayload {
    public static final Identifier PACKET_ID = Identifier.fromNamespaceAndPath(GunMod.MOD_ID, "s2c_gundraw");
    public static final CustomPacketPayload.Type<ServerMessageGunDraw> TYPE = new CustomPacketPayload.Type<>(PACKET_ID);
    public static final StreamCodec<RegistryFriendlyByteBuf, ServerMessageGunDraw> CODEC = StreamCodec.ofMember(ServerMessageGunDraw::write, ServerMessageGunDraw::new);

    private final int entityId;
    private final ItemStack previousGunItem;
    private final ItemStack currentGunItem;

    public ServerMessageGunDraw(RegistryFriendlyByteBuf buf) {
        this(buf.readVarInt(), ItemStack.OPTIONAL_STREAM_CODEC.decode(buf), ItemStack.OPTIONAL_STREAM_CODEC.decode(buf));
    }

    public ServerMessageGunDraw(int entityId, ItemStack previousGunItem, ItemStack currentGunItem) {
        this.entityId = entityId;
        this.previousGunItem = previousGunItem;
        this.currentGunItem = currentGunItem;
    }

    /**
     * 두 ItemStack 필드는 {@code STREAM_CODEC}이 아니라 <b>반드시</b> {@code OPTIONAL_STREAM_CODEC}을 써야 한다.
     *
     * <h2>이유(멀티플레이 치명적 충돌의 근본 원인)</h2>
     * {@code ItemStack.STREAM_CODEC}은 {@link ItemStack#EMPTY}를 만나면 바로
     * {@code EncoderException("Empty ItemStack not allowed")}를 던진다(26.2 바이트코드
     * {@code ItemStack$2#encode} 183번째 줄). 그런데 이 메시지의
     * {@code previousGunItem} / {@code currentGunItem}은 <b>원래 빈 스택일 수 있다</b>:
     *
     * <ul>
     *   <li>{@code LivingEntityDrawGun#draw} 49번째 줄에 분명히
     *       {@code data.currentGunItem == null ? ItemStack.EMPTY : ...}라고 적혀 있다
     *       — 플레이어가 <b>처음</b> 총을 바꿀 때는 "이전 총"이 없으므로 반드시 빈 스택이다;</li>
     *   <li>{@code InventoryEvent#onPlayerChangeSelect}는
     *       {@code oldHotbarSelected == -1}이면 바로 {@code draw(ItemStack.EMPTY)}를 한다;</li>
     *   <li>플레이어가 손에 든 아이템을 <b>버리면</b> 그 칸이 비어, 전환/갱신 때도 빈 스택이 넘어간다.</li>
     * </ul>
     *
     * <h2>결과가 이렇게 심각한 이유</h2>
     * 인코딩 예외는 {@code Connection#doSendPacket}의 Netty 스레드에서 일어나
     * 그 연결을 바로 <b>끊는다</b>(로그: {@code lost connection: Internal Exception:
     * ... Failed to encode packet ... (tacz:s2c_gundraw)}).
     * 그리고 이 메시지는 {@code NetworkHandler#sendToTrackingEntity}로
     * <b>그 엔티티를 볼 수 있는 모든 플레이어</b>에게 보내지므로, 빈 스택 한 번이
     * 시야 안의 모든 사람(동작을 한 본인이 아니라)을 모두 내보낸다 —
     * 실측으로는 "서버장이 물건을 버리면 다른 사람이 모두 끊김", "어떤 플레이어가 들어오면 서버 전체가 멈춤"으로 나타났다.
     *
     * <h2>원본과 비교</h2>
     * 원본 1.21.1의 {@code ServerMessageGunDraw.STREAM_CODEC}은 이 두 필드에 바로
     * {@code ItemStack.OPTIONAL_STREAM_CODEC}을 썼다(글자 그대로 확인).
     * 이 프로젝트가 직접 쓴 {@code write}/read로 이식하면서 OPTIONAL이 아닌 판을 잘못 써서 <b>이식 회귀</b>가 되었다.
     *
     * <p>같은 디렉터리의 나머지 이벤트 메시지 5개(Fire/FireSelect/Melee/Reload/Shoot)는
     * 원본도 실제로 OPTIONAL이 아닌 {@code STREAM_CODEC}을 쓰며, 그것들이 싣는 것은
     * 반드시 실제 총 한 자루이므로 함께 고치면 <b>안 된다</b> — 하나씩 원본과 비교해 확인했다.
     */
    public void write(RegistryFriendlyByteBuf buf) {
        buf.writeVarInt(entityId);
        ItemStack.OPTIONAL_STREAM_CODEC.encode(buf, previousGunItem);
        ItemStack.OPTIONAL_STREAM_CODEC.encode(buf, currentGunItem);
    }

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    @Environment(EnvType.CLIENT)
    public void handle(LocalPlayer player, PacketSender responseSender) {
        doClientEvent(this);
    }

    @Environment(EnvType.CLIENT)
    private static void doClientEvent(ServerMessageGunDraw message) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) {
            return;
        }
        if (level.getEntity(message.entityId) instanceof LivingEntity livingEntity) {
            GunDrawEvent gunDrawEvent = new GunDrawEvent(livingEntity, message.previousGunItem, message.currentGunItem, LogicalSide.CLIENT);
            GunDrawEvent.CALLBACK.invoker().post(gunDrawEvent);
        }
    }
}
