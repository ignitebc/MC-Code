package com.daqem.jobsplus.networking.s2c;

import com.daqem.jobsplus.networking.JobsPlusNetworking;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * 칭호 탭에 표시할 보유자 목록과 받는 사람의 장착 칭호를 보낸다.
 *
 * <p>칭호 ID는 문자열로 보낸다. 서버와 클라이언트의 칭호 목록이 다른 버전이어도
 * 모르는 칭호만 빠지고 나머지는 읽힌다.
 */
public class ClientboundTitlesPacket implements CustomPacketPayload
{
    /**
     * @param holderName 보유자가 없으면 빈 문자열
     * @param mine       받는 사람이 보유자인지
     */
    public record Entry(String titleId, String holderName, boolean mine)
    {
    }

    private final List<Entry> entries;
    /** 장착하지 않았으면 빈 문자열 */
    private final String equippedId;

    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundTitlesPacket> STREAM_CODEC = new StreamCodec<>()
    {
        @Override
        public @NotNull ClientboundTitlesPacket decode(RegistryFriendlyByteBuf buf)
        {
            int size = buf.readVarInt();
            List<Entry> entries = new ArrayList<>(size);
            for (int index = 0; index < size; index++)
            {
                entries.add(new Entry(buf.readUtf(), buf.readUtf(), buf.readBoolean()));
            }
            return new ClientboundTitlesPacket(entries, buf.readUtf());
        }

        @Override
        public void encode(RegistryFriendlyByteBuf buf, ClientboundTitlesPacket packet)
        {
            buf.writeVarInt(packet.entries.size());
            for (Entry entry : packet.entries)
            {
                buf.writeUtf(entry.titleId());
                buf.writeUtf(entry.holderName());
                buf.writeBoolean(entry.mine());
            }
            buf.writeUtf(packet.equippedId);
        }
    };

    public ClientboundTitlesPacket(List<Entry> entries, String equippedId)
    {
        this.entries = List.copyOf(entries);
        this.equippedId = equippedId;
    }

    public List<Entry> getEntries()
    {
        return entries;
    }

    public String getEquippedId()
    {
        return equippedId;
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type()
    {
        return JobsPlusNetworking.CLIENTBOUND_TITLES;
    }
}
