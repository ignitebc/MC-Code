package com.autovw.advancednetherite.network;

import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.UUID;

/**
 * 클라이언트에 보여줄 펫 한 마리의 상태.
 *
 * @param recordId  펫 저장소 기록 ID
 * @param petTypeId 펫 엔티티 타입의 레지스트리 ID
 * @param enabled   ON/OFF 상태
 */
public record PetStatusEntry(UUID recordId, String petTypeId, boolean enabled)
{
    public static final StreamCodec<RegistryFriendlyByteBuf, PetStatusEntry> STREAM_CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC, PetStatusEntry::recordId,
            ByteBufCodecs.STRING_UTF8, PetStatusEntry::petTypeId,
            ByteBufCodecs.BOOL, PetStatusEntry::enabled,
            PetStatusEntry::new);
}
