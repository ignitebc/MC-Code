package com.autovw.advancednetherite.network;

import com.autovw.advancednetherite.AdvancedNetherite;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

import java.util.UUID;

/**
 * 클라이언트 → 서버. 펫 하나의 ON/OFF 상태 전환을 요청한다.
 * 서버는 요청자가 소유한 기록인지 확인한 뒤 처리한다.
 */
public record PetTogglePayload(UUID recordId) implements CustomPacketPayload
{
    public static final Type<PetTogglePayload> TYPE = new Type<>(
            Identifier.fromNamespaceAndPath(AdvancedNetherite.MOD_ID, "pet_toggle"));

    public static final StreamCodec<RegistryFriendlyByteBuf, PetTogglePayload> STREAM_CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC, PetTogglePayload::recordId,
            PetTogglePayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type()
    {
        return TYPE;
    }
}
