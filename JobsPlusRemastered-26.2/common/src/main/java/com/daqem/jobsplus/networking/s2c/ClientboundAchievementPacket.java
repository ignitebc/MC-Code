package com.daqem.jobsplus.networking.s2c;

import com.daqem.jobsplus.achievement.AchievementCatalog;
import com.daqem.jobsplus.networking.JobsPlusNetworking;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** 표시용 목표값만 보낸다. 개체별 장비 원장과 다른 시즌 기록은 클라이언트로 보내지 않는다. */
public record ClientboundAchievementPacket(String season, Map<String, Long> values, Set<String> completed,
                                          Set<String> claimed, List<String> adventureBiomes, List<String> overworldBiomes) implements CustomPacketPayload
{
    /** 화면에 보내는 목표 진행도 수의 상한. 업적 200종의 서로 다른 목표 키에 여유를 둔 값이다. */
    private static final int MAX_VALUES = 512;
    public static final ClientboundAchievementPacket EMPTY = new ClientboundAchievementPacket("", Map.of(), Set.of(), Set.of(), List.of(), List.of());
    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundAchievementPacket> STREAM_CODEC = new StreamCodec<>()
    {
        @Override
        public @NotNull ClientboundAchievementPacket decode(RegistryFriendlyByteBuf buffer)
        {
            String season = buffer.readUtf(32);
            int count = readCount(buffer, MAX_VALUES);
            Map<String, Long> values = new HashMap<>();
            for (int index = 0; index < count; index++)
            {
                values.put(buffer.readUtf(128), Math.max(0L, buffer.readVarLong()));
            }
            Set<String> completed = readIds(buffer);
            Set<String> claimed = readIds(buffer);
            List<String> biomes = readBiomes(buffer);
            List<String> overworld = readBiomes(buffer);
            return new ClientboundAchievementPacket(season, values, completed, claimed, biomes, overworld);
        }

        @Override
        public void encode(RegistryFriendlyByteBuf buffer, ClientboundAchievementPacket packet)
        {
            buffer.writeUtf(packet.season, 32);
            buffer.writeVarInt(packet.values.size());
            for (Map.Entry<String, Long> entry : packet.values.entrySet())
            {
                buffer.writeUtf(entry.getKey(), 128);
                buffer.writeVarLong(entry.getValue());
            }
            writeIds(buffer, packet.completed);
            writeIds(buffer, packet.claimed);
            writeBiomes(buffer, packet.adventureBiomes);
            writeBiomes(buffer, packet.overworldBiomes);
        }
    };

    public ClientboundAchievementPacket
    {
        values = Map.copyOf(values);
        completed = Set.copyOf(completed);
        claimed = Set.copyOf(claimed);
        adventureBiomes = List.copyOf(adventureBiomes);
        overworldBiomes = List.copyOf(overworldBiomes);
    }

    private static List<String> readBiomes(RegistryFriendlyByteBuf buffer)
    {
        int count = readCount(buffer, 4096);
        List<String> biomes = new ArrayList<>();
        for (int index = 0; index < count; index++)
        {
            biomes.add(buffer.readUtf(256));
        }
        return biomes;
    }

    private static void writeBiomes(RegistryFriendlyByteBuf buffer, List<String> biomes)
    {
        buffer.writeVarInt(biomes.size());
        for (String biome : biomes)
        {
            buffer.writeUtf(biome, 256);
        }
    }

    private static int readCount(RegistryFriendlyByteBuf buffer, int maximum)
    {
        int count = buffer.readVarInt();
        if (count < 0 || count > maximum)
        {
            throw new IllegalArgumentException("Invalid achievement packet count: " + count);
        }
        return count;
    }

    private static Set<String> readIds(RegistryFriendlyByteBuf buffer)
    {
        // 완료·수령 목록은 업적 수를 넘을 수 없다. 업적을 늘리면 이 제한도 함께 늘어난다.
        int count = readCount(buffer, AchievementCatalog.all().size());
        Set<String> ids = new HashSet<>();
        for (int index = 0; index < count; index++)
        {
            ids.add(buffer.readUtf(3));
        }
        return ids;
    }

    private static void writeIds(RegistryFriendlyByteBuf buffer, Set<String> ids)
    {
        buffer.writeVarInt(ids.size());
        for (String id : ids)
        {
            buffer.writeUtf(id, 3);
        }
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type()
    {
        return JobsPlusNetworking.CLIENTBOUND_ACHIEVEMENTS;
    }
}
