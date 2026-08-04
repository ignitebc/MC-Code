package com.autovw.advancednetherite.network;

import com.autovw.advancednetherite.AdvancedNetherite;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

import java.util.List;

/**
 * 서버 → 클라이언트. 플레이어가 소유한 펫 목록과 ON/OFF 상태를 동기화한다.
 * 접속 직후와 펫 목록이 바뀔 때마다 전송된다.
 */
public record PetListSyncPayload(List<PetStatusEntry> pets) implements CustomPacketPayload
{
    public static final Type<PetListSyncPayload> TYPE = new Type<>(
            Identifier.fromNamespaceAndPath(AdvancedNetherite.MOD_ID, "pet_list_sync"));

    public static final StreamCodec<RegistryFriendlyByteBuf, PetListSyncPayload> STREAM_CODEC = StreamCodec.composite(
            PetStatusEntry.STREAM_CODEC.apply(ByteBufCodecs.list()), PetListSyncPayload::pets,
            PetListSyncPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type()
    {
        return TYPE;
    }
}
