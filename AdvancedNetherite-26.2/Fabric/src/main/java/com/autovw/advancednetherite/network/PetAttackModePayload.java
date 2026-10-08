package com.autovw.advancednetherite.network;

import com.autovw.advancednetherite.AdvancedNetherite;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/**
 * 클라이언트 → 서버. 펫 공격 방식을 바꾸도록 요청한다. 보유한 모든 펫에 같이 적용한다.
 * 토글 대신 바꿀 방식을 보내서, 요청이 겹쳐도 화면에 보인 방식과 서버 값이 어긋나지 않게 한다.
 *
 * @param autoAttack true면 자동공격, false면 일반공격
 */
public record PetAttackModePayload(boolean autoAttack) implements CustomPacketPayload
{
    public static final Type<PetAttackModePayload> TYPE = new Type<>(
            Identifier.fromNamespaceAndPath(AdvancedNetherite.MOD_ID, "pet_attack_mode"));

    public static final StreamCodec<RegistryFriendlyByteBuf, PetAttackModePayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, PetAttackModePayload::autoAttack,
            PetAttackModePayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type()
    {
        return TYPE;
    }
}
