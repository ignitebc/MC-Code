package com.mcserver.serverutilities.monster;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/** 서버 → 클라이언트. 몬스터 한 마리의 머리 위 레벨을 알린다. */
public record MonsterLevelPayload(int entityId, int level) implements CustomPacketPayload {
    public static final Type<MonsterLevelPayload> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath("serverutilities", "monster_level"));

    public static final StreamCodec<RegistryFriendlyByteBuf, MonsterLevelPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, MonsterLevelPayload::entityId,
            ByteBufCodecs.VAR_INT, MonsterLevelPayload::level,
            MonsterLevelPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
