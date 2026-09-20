package com.autovw.advancednetherite.network;

import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.UUID;

/**
 * 클라이언트에 보여줄 펫 한 마리의 상태.
 *
 * @param recordId              펫 저장소 기록 ID
 * @param petTypeId             펫 엔티티 타입의 레지스트리 ID
 * @param enabled               ON/OFF 상태
 * @param name                  플레이어가 붙인 이름. 없으면 빈 문자열
 * @param level                 펫 레벨
 * @param exp                   현재 레벨에서 쌓은 경험치
 * @param reviveRemainingMillis 보낸 시점 기준으로 부활까지 남은 시간. 살아 있으면 0.
 *                              서버와 클라이언트의 시계가 다를 수 있어 시각이 아니라 남은 시간으로 보낸다.
 */
public record PetStatusEntry(UUID recordId, String petTypeId, boolean enabled, String name,
                             int level, int exp, long reviveRemainingMillis)
{
    public static final StreamCodec<RegistryFriendlyByteBuf, PetStatusEntry> STREAM_CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC, PetStatusEntry::recordId,
            ByteBufCodecs.STRING_UTF8, PetStatusEntry::petTypeId,
            ByteBufCodecs.BOOL, PetStatusEntry::enabled,
            ByteBufCodecs.STRING_UTF8, PetStatusEntry::name,
            ByteBufCodecs.VAR_INT, PetStatusEntry::level,
            ByteBufCodecs.VAR_INT, PetStatusEntry::exp,
            ByteBufCodecs.VAR_LONG, PetStatusEntry::reviveRemainingMillis,
            PetStatusEntry::new);

    public PetStatusEntry withEnabled(boolean newEnabled)
    {
        return new PetStatusEntry(this.recordId, this.petTypeId, newEnabled, this.name,
                this.level, this.exp, this.reviveRemainingMillis);
    }

    public PetStatusEntry withName(String newName)
    {
        return new PetStatusEntry(this.recordId, this.petTypeId, this.enabled, newName,
                this.level, this.exp, this.reviveRemainingMillis);
    }
}
