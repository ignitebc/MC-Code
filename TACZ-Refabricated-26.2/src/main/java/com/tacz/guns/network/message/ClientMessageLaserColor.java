package com.tacz.guns.network.message;

import com.tacz.guns.GunMod;
import com.tacz.guns.api.item.IAttachment;
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
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class ClientMessageLaserColor implements CustomPacketPayload {
    public static final Identifier PACKET_ID = Identifier.fromNamespaceAndPath(GunMod.MOD_ID, "c2s_laser_color");
    public static final CustomPacketPayload.Type<ClientMessageLaserColor> TYPE = new CustomPacketPayload.Type<>(PACKET_ID);
    public static final StreamCodec<FriendlyByteBuf, ClientMessageLaserColor> CODEC = StreamCodec.ofMember(ClientMessageLaserColor::write, ClientMessageLaserColor::new);

    private final Map<AttachmentType, Integer> colorMap = new HashMap<>();
    private boolean applyGunColor = false;
    private int gunColor = 0;

    private int gunSlotIndex = -1;

    private ClientMessageLaserColor() {

    }

    public ClientMessageLaserColor(FriendlyByteBuf buf) {
        this.colorMap.putAll(buf.readMap(buf1 -> buf1.readEnum(AttachmentType.class), FriendlyByteBuf::readInt));
        this.applyGunColor = buf.readBoolean();
        this.gunColor = buf.readInt();
        this.gunSlotIndex = buf.readInt();
    }

    public ClientMessageLaserColor(@NotNull ItemStack gun, int gunSlotIndex) {
        if (gun.getItem() instanceof IGun iGun) {
            for (AttachmentType type : AttachmentType.values()) {
                ItemStack attachment = iGun.getAttachment(gun, type);
                if (attachment.getItem() instanceof IAttachment iAttachment) {
                    if (iAttachment.hasCustomLaserColor(attachment)) {
                        colorMap.put(type, iAttachment.getLaserColor(attachment));
                    }
                }
            }
            if (iGun.hasCustomLaserColor(gun)) {
                this.gunColor = iGun.getLaserColor(gun);
                this.applyGunColor = true;
            }
            this.gunSlotIndex = gunSlotIndex;
        }
    }

        public void write(FriendlyByteBuf buf) {
        buf.writeMap(colorMap, FriendlyByteBuf::writeEnum, FriendlyByteBuf::writeInt);
        buf.writeBoolean(applyGunColor);
        buf.writeInt(gunColor);
        buf.writeInt(gunSlotIndex);
    }

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle(ServerPlayer player, PacketSender responseSender) {
        if (this.gunSlotIndex == -1) {
            return;
        }
        Inventory inventory = player.getInventory();
        ItemStack gunItem = inventory.getItem(gunSlotIndex);
        IGun iGun = IGun.getIGunOrNull(gunItem);
        if (iGun != null) {
            for (var entry : colorMap.entrySet()) {
                AttachmentType type = entry.getKey();
                int color = entry.getValue();
                // [getAttachment()가 돌려준 ItemStack이 아니라 "총에 있는 부착물 NBT"를 고쳐야 한다]
                //
                // getAttachment(gun, type) 내부는
                //     ItemNbtUtils.loadItemStack(nbt.getCompoundOrEmpty(key))
                // — 호출할 때마다 Codec으로 NBT에서 [완전히 새 ItemStack을 역직렬화]하므로,
                // 총에 저장된 데이터와 아무 참조 관계도 없다.
                //
                // 예전에는 여기를 이렇게 썼다
                //     ItemStack attachment = iGun.getAttachment(gunItem, type);
                //     iAttachment.setLaserColor(attachment, color);
                // 색을 [임시 사본]에 쓴 셈이며, 메서드가 돌아가면 그 사본은 버려져
                // 총의 부착물 NBT는 한 바이트도 바뀌지 않았다. 그래서 서버는 "저장 성공",
                // 클라이언트 화면도 바뀐 것처럼 보였지만(개조 화면은 로컬 미리보기 사본을 쓰므로),
                // 화면을 나갔다가 아이템 NBT에서 다시 읽으면 곧바로 기본 색으로 돌아갔다 —
                // 사용자가 실측한 "레이저 색을 바꿨는데 화면을 나가면 원래대로 돌아감"이 바로 이것이다.
                //
                // 원본은 tag를 그 자리에서 고쳐 다시 쓴다(1.21.1의 handle과 줄마다 대조):
                //     CompoundTag tag = iGun.getAttachmentTag(gunItem, type);
                //     if (tag != null) { AttachmentItemDataAccessor.setLaserColorToTag(tag, color); }
                //     iGun.setAttachmentTag(gunItem, type, tag);
                // getAttachmentTag/setAttachmentTag는 총 NBT 안의
                // "부착물 ItemStack의 components.custom_data" 층을 다루므로 변경이 실제로 저장된다.
                CompoundTag tag = iGun.getAttachmentTag(gunItem, type);
                if (tag != null) {
                    AttachmentItemDataAccessor.setLaserColorToTag(tag, color);
                    iGun.setAttachmentTag(gunItem, type, tag);
                }
            }
            if (applyGunColor) {
                // 총 자체의 레이저 색(내장 레이저)은 총 본체의 custom_data를 쓰고,
                // setLaserColor 내부가 바로 ItemNbtUtils.updateTag(gun, ...)라
                // gunItem에 직접 작용하므로 사본 문제가 없다.
                iGun.setLaserColor(gunItem, gunColor);
            }
        }
    }
}
