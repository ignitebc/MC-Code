package com.tacz.guns.network.message;

import com.tacz.guns.GunMod;
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

public class ClientMessageUnloadAttachment implements CustomPacketPayload {
    public static final Identifier PACKET_ID = Identifier.fromNamespaceAndPath(GunMod.MOD_ID, "c2s_unload_attachment");
    public static final CustomPacketPayload.Type<ClientMessageUnloadAttachment> TYPE = new CustomPacketPayload.Type<>(PACKET_ID);
    public static final StreamCodec<FriendlyByteBuf, ClientMessageUnloadAttachment> CODEC = StreamCodec.ofMember(ClientMessageUnloadAttachment::write, ClientMessageUnloadAttachment::new);

    private final int gunSlotIndex;
    private final AttachmentType attachmentType;

    public ClientMessageUnloadAttachment(FriendlyByteBuf buf) {
        this(buf.readInt(), buf.readEnum(AttachmentType.class));
    }

    public ClientMessageUnloadAttachment(int gunSlotIndex, AttachmentType attachmentType) {
        this.gunSlotIndex = gunSlotIndex;
        this.attachmentType = attachmentType;
    }

        public void write(FriendlyByteBuf buf) {
        buf.writeInt(gunSlotIndex);
        buf.writeEnum(attachmentType);
    }

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle(ServerPlayer player, PacketSender responseSender) {
        Inventory inventory = player.getInventory();
        ItemStack gunItem = inventory.getItem(gunSlotIndex);
        IGun iGun = IGun.getIGunOrNull(gunItem);
        if (iGun != null) {
            // 서버에서 부착물 잠금 검사
            if (iGun.hasAttachmentLock(gunItem)) {
                return;
            }
            ItemStack attachmentItem = iGun.getAttachment(gunItem, attachmentType);
            if (!attachmentItem.isEmpty()) {
                // 15차 수정: 순서는 반드시 "먼저 떼어 내고, 성공하면 아이템을 준다"여야 한다.
                //
                // 원래는 inventory.add(...) && unloadAttachment(...)였다. 즉 <b>부착물을 먼저 인벤토리에 넣고</b>
                // 그다음 총의 NBT를 지웠다. NBT 지우기가 실패하면(26.2에서 saveItemStack(ItemStack.EMPTY)는
                // count가 [1,99]를 벗어나 반드시 예외를 던짐) 플레이어는 이미 부착물을 받았는데 총에도 부착물이 남는다
                // — 이것이 "떼어 내기를 누르면 부착물이 무한 복제됨"의 원인이었다.
                //
                // 이제는 먼저 떼어 내고, 실제로 떼어졌는지 확인한 뒤, 마지막에 아이템을 준다.
                // 인벤토리에 넣지 못하면(인벤토리가 가득 참) 되돌려 부착물이 그냥 사라지지 않게 한다.
                iGun.unloadAttachment(gunItem, attachmentType);
                if (!iGun.getAttachment(gunItem, attachmentType).isEmpty()) {
                    // 떼어 내기가 적용되지 않았으면 바로 포기하고 아이템을 절대 주지 않는다
                    return;
                }
                if (!inventory.add(attachmentItem)) {
                    // 인벤토리에 못 넣으면 -> 땅에 떨어뜨린다. 부착물이 사라지게 두지 않는다
                    player.drop(attachmentItem, false);
                }
                // 부착물 데이터 새로 고침
                AttachmentPropertyManager.postChangeEvent(player, gunItem);
                // 떼어 낸 것이 확장 탄창이면 탄환을 모두 내놓는다
                if (attachmentType == AttachmentType.EXTENDED_MAG) {
                    iGun.dropAllAmmo(player, gunItem);
                }
                player.inventoryMenu.broadcastChanges();
                NetworkHandler.sendToClientPlayer(new ServerMessageRefreshRefitScreen(), player);
            }
        }
    }
}
