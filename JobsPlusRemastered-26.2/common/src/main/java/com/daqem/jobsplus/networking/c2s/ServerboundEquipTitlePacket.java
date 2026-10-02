package com.daqem.jobsplus.networking.c2s;

import com.daqem.jobsplus.networking.JobsPlusNetworking;
import com.daqem.jobsplus.player.title.TitleManager;
import com.daqem.jobsplus.player.title.TitleType;
import dev.architectury.networking.NetworkManager;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

/**
 * 칭호 탭에서 장착하거나 해제할 칭호를 서버에 보낸다.
 * <p>
 * 보유 여부는 서버가 다시 확인한다. 빈 문자열은 장착 해제다.
 */
public class ServerboundEquipTitlePacket implements CustomPacketPayload
{
    private static final int MAX_ID_LENGTH = 64;

    private final String titleId;

    public static final StreamCodec<RegistryFriendlyByteBuf, ServerboundEquipTitlePacket> STREAM_CODEC = new StreamCodec<>()
    {
        @Override
        public @NotNull ServerboundEquipTitlePacket decode(RegistryFriendlyByteBuf buf)
        {
            return new ServerboundEquipTitlePacket(buf.readUtf(MAX_ID_LENGTH));
        }

        @Override
        public void encode(RegistryFriendlyByteBuf buf, ServerboundEquipTitlePacket packet)
        {
            buf.writeUtf(packet.titleId, MAX_ID_LENGTH);
        }
    };

    public ServerboundEquipTitlePacket(String titleId)
    {
        this.titleId = titleId;
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type()
    {
        return JobsPlusNetworking.SERVERBOUND_EQUIP_TITLE;
    }

    public static void handleServerSide(ServerboundEquipTitlePacket packet, NetworkManager.PacketContext context)
    {
        if (!(context.getPlayer() instanceof ServerPlayer player))
        {
            return;
        }
        boolean unequip = packet.titleId.isEmpty();
        Optional<TitleType> type = TitleType.byId(packet.titleId);
        if (!unequip && type.isEmpty())
        {
            return;
        }
        player.level().getServer().execute(() -> TitleManager.equip(player, type.orElse(null)));
    }
}
