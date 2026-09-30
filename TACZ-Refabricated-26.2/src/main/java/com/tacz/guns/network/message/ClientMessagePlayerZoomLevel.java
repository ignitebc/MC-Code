package com.tacz.guns.network.message;

import com.tacz.guns.GunMod;
import com.tacz.guns.api.DefaultAssets;
import com.tacz.guns.api.item.IGun;
import com.tacz.guns.api.item.attachment.AttachmentType;
import com.tacz.guns.api.item.nbt.AttachmentItemDataAccessor;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

/**
 * 마우스 휠로 고른 조준경 배율 단계를 주 손 총의 부착 조준경에 저장한다.
 *
 * <p>배율 단계 목록은 클라이언트 표시 데이터에만 있으므로 단계 번호는 클라이언트가 정한다. 배율은 시야와
 * 감도에만 쓰여 판정에 영향이 없으므로 서버는 범위만 확인한다. 일체형 조준경은 배율을 바꾸지 않는다.
 */
public class ClientMessagePlayerZoomLevel implements CustomPacketPayload {
    public static final Identifier PACKET_ID = Identifier.fromNamespaceAndPath(GunMod.MOD_ID, "c2s_player_zoom_level");
    public static final CustomPacketPayload.Type<ClientMessagePlayerZoomLevel> TYPE = new CustomPacketPayload.Type<>(PACKET_ID);
    public static final StreamCodec<FriendlyByteBuf, ClientMessagePlayerZoomLevel> CODEC =
            StreamCodec.ofMember(ClientMessagePlayerZoomLevel::write, ClientMessagePlayerZoomLevel::new);
    /** 받을 수 있는 배율 단계 수의 상한. 조준경 데이터는 이보다 많은 단계를 두지 않는다. */
    private static final int MAX_ZOOM_LEVELS = 16;

    private final int zoomNumber;

    public ClientMessagePlayerZoomLevel(int zoomNumber) {
        this.zoomNumber = zoomNumber;
    }

    public ClientMessagePlayerZoomLevel(FriendlyByteBuf buf) {
        this(buf.readVarInt());
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeVarInt(zoomNumber);
    }

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle(ServerPlayer player, PacketSender responseSender) {
        boolean validLevel = zoomNumber >= 0 && zoomNumber < MAX_ZOOM_LEVELS;
        if (!validLevel) {
            return;
        }
        ItemStack gun = player.getMainHandItem();
        IGun iGun = IGun.getIGunOrNull(gun);
        if (iGun == null) {
            return;
        }
        Identifier scopeId = iGun.getAttachmentId(gun, AttachmentType.SCOPE);
        CompoundTag scopeTag = iGun.getAttachmentTag(gun, AttachmentType.SCOPE);
        if (DefaultAssets.isEmptyAttachmentId(scopeId) || scopeTag == null) {
            return;
        }
        AttachmentItemDataAccessor.setZoomNumberToTag(scopeTag, zoomNumber);
        // getAttachmentTag 는 사본을 돌려주므로 총 NBT 에 다시 써야 반영된다.
        iGun.setAttachmentTag(gun, AttachmentType.SCOPE, scopeTag);
    }
}
