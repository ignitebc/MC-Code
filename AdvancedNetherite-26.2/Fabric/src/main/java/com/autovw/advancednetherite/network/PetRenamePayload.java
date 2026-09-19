package com.autovw.advancednetherite.network;

import com.autovw.advancednetherite.AdvancedNetherite;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

import java.util.UUID;

/**
 * 클라이언트 → 서버. 펫 하나의 이름 변경을 요청한다.
 * 서버는 요청자가 소유한 기록인지 확인하고 이름을 다듬은 뒤 저장한다. 빈 이름은 붙인 이름을 지운다.
 */
public record PetRenamePayload(UUID recordId, String name) implements CustomPacketPayload
{
    /** 서버가 다듬기 전 원문의 상한. 정상 입력은 이보다 훨씬 짧다. */
    private static final int MAX_RAW_LENGTH = 64;

    public static final Type<PetRenamePayload> TYPE = new Type<>(
            Identifier.fromNamespaceAndPath(AdvancedNetherite.MOD_ID, "pet_rename"));

    public static final StreamCodec<RegistryFriendlyByteBuf, PetRenamePayload> STREAM_CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC, PetRenamePayload::recordId,
            ByteBufCodecs.stringUtf8(MAX_RAW_LENGTH), PetRenamePayload::name,
            PetRenamePayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type()
    {
        return TYPE;
    }
}
