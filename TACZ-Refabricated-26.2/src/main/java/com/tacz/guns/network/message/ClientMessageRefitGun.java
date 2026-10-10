package com.tacz.guns.network.message;

import com.tacz.guns.GunMod;
import com.tacz.guns.api.item.IAttachment;
import com.tacz.guns.api.item.IGun;
import com.tacz.guns.api.item.attachment.AttachmentType;
import com.tacz.guns.network.NetworkHandler;
import com.tacz.guns.resource.modifier.AttachmentPropertyManager;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

public class ClientMessageRefitGun implements CustomPacketPayload {
    public static final Identifier PACKET_ID = Identifier.fromNamespaceAndPath(GunMod.MOD_ID, "c2s_player_refit");
    public static final CustomPacketPayload.Type<ClientMessageRefitGun> TYPE = new CustomPacketPayload.Type<>(PACKET_ID);
    public static final StreamCodec<FriendlyByteBuf, ClientMessageRefitGun> CODEC = StreamCodec.ofMember(ClientMessageRefitGun::write, ClientMessageRefitGun::new);

    private final int attachmentSlotIndex;
    private final int gunSlotIndex;
    private final AttachmentType attachmentType;

    public ClientMessageRefitGun(FriendlyByteBuf buf) {
        this(buf.readInt(), buf.readInt(), buf.readEnum(AttachmentType.class));
    }

    public ClientMessageRefitGun(int attachmentSlotIndex, int gunSlotIndex, AttachmentType attachmentType) {
        this.attachmentSlotIndex = attachmentSlotIndex;
        this.gunSlotIndex = gunSlotIndex;
        this.attachmentType = attachmentType;
    }

        public void write(FriendlyByteBuf buf) {
        buf.writeInt(attachmentSlotIndex);
        buf.writeInt(gunSlotIndex);
        buf.writeEnum(attachmentType);
    }

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle(ServerPlayer player, PacketSender responseSender) {
        Inventory inventory = player.getInventory();
        ItemStack attachmentItem = inventory.getItem(attachmentSlotIndex);
        ItemStack gunItem = inventory.getItem(gunSlotIndex);
        IGun iGun = IGun.getIGunOrNull(gunItem);
        if (iGun != null) {
            // 서버에서 부착물 잠금 검사
            if (iGun.hasAttachmentLock(gunItem)) {
                return;
            }
            if (iGun.allowAttachment(gunItem, attachmentItem)) {
                // 클라이언트가 넘긴 attachmentType이 아니라 부착물 아이템 자체의 실제 종류를 쓴다
                IAttachment iAttachment = IAttachment.getIAttachmentOrNull(attachmentItem);
                if (iAttachment == null) {
                    return;
                }
                AttachmentType realType = iAttachment.getType(attachmentItem);
                ItemStack oldAttachmentItem = iGun.getAttachment(gunItem, realType);
                iGun.installAttachment(gunItem, attachmentItem);
                // 부착물 데이터 새로 고침
                AttachmentPropertyManager.postChangeEvent(player, gunItem);
                inventory.setItem(attachmentSlotIndex, oldAttachmentItem);
                // 떼어 낸 것이 확장 탄창이면 탄환을 모두 내놓는다
                if (realType == AttachmentType.EXTENDED_MAG) {
                    iGun.dropAllAmmo(player, gunItem);
                }
                player.inventoryMenu.broadcastChanges();
                NetworkHandler.sendToClientPlayer(new ServerMessageRefreshRefitScreen(), player);
            }
        }
    }
}
