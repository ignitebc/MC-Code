package com.daqem.jobsplus.player.title;

import com.daqem.jobsplus.JobsPlus;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.UUIDUtil;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * 칭호 보유자와 플레이어별 장착 칭호를 월드에 저장한다.
 *
 * <p>칭호는 서버에 한 명만 가지므로 플레이어 NBT가 아니라 월드 저장 데이터에 둔다.
 * 그래야 보유자가 접속하지 않은 동안에도 다른 플레이어에게 보유자를 보여 줄 수 있다.
 */
public final class TitleLedger extends SavedData
{
    private static final Identifier FILE_ID = JobsPlus.getId("titles");

    public record Holder(UUID playerId, String playerName, long acquiredAt)
    {
        public static final Codec<Holder> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                UUIDUtil.STRING_CODEC.fieldOf("player_id").forGetter(Holder::playerId),
                Codec.STRING.fieldOf("player_name").forGetter(Holder::playerName),
                Codec.LONG.fieldOf("acquired_at").forGetter(Holder::acquiredAt)
        ).apply(instance, Holder::new));
    }

    public static final Codec<TitleLedger> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.unboundedMap(Codec.STRING, Holder.CODEC)
                    .optionalFieldOf("holders", Map.of())
                    .forGetter(TitleLedger::packHolders),
            Codec.unboundedMap(UUIDUtil.STRING_CODEC, Codec.STRING)
                    .optionalFieldOf("equipped", Map.of())
                    .forGetter(TitleLedger::packEquipped)
    ).apply(instance, TitleLedger::new));

    public static final SavedDataType<TitleLedger> TYPE = new SavedDataType<>(
            FILE_ID,
            TitleLedger::new,
            CODEC,
            DataFixTypes.SAVED_DATA_COMMAND_STORAGE
    );

    private final Map<TitleType, Holder> holders = new EnumMap<>(TitleType.class);
    private final Map<UUID, TitleType> equipped = new HashMap<>();

    public TitleLedger()
    {
    }

    private TitleLedger(Map<String, Holder> savedHolders, Map<UUID, String> savedEquipped)
    {
        // 칭호를 빼거나 ID를 바꾼 뒤에도 나머지 기록은 읽히도록 모르는 ID는 건너뛴다.
        savedHolders.forEach((id, holder) -> TitleType.byId(id).ifPresent(type -> this.holders.put(type, holder)));
        savedEquipped.forEach((playerId, id) -> TitleType.byId(id).ifPresent(type -> this.equipped.put(playerId, type)));
    }

    public static TitleLedger get(MinecraftServer server)
    {
        return server.overworld().getDataStorage().computeIfAbsent(TYPE);
    }

    public Optional<Holder> getHolder(TitleType type)
    {
        return Optional.ofNullable(this.holders.get(type));
    }

    public boolean isHolder(UUID playerId, TitleType type)
    {
        Holder holder = this.holders.get(type);
        return holder != null && holder.playerId().equals(playerId);
    }

    /**
     * 아직 주인이 없는 칭호만 준다.
     *
     * @return 이번 호출로 보유자가 되었으면 {@code true}
     */
    public boolean claim(TitleType type, UUID playerId, String playerName)
    {
        if (this.holders.containsKey(type))
        {
            return false;
        }
        this.holders.put(type, new Holder(playerId, playerName, System.currentTimeMillis()));
        this.setDirty();
        return true;
    }

    /** 운영자 지급. 기존 보유자가 있으면 빼앗고, 그 사람이 장착 중이었다면 장착도 푼다. */
    public void setHolder(TitleType type, UUID playerId, String playerName)
    {
        removeHolder(type);
        this.holders.put(type, new Holder(playerId, playerName, System.currentTimeMillis()));
        this.setDirty();
    }

    /** 보유자를 지운다. 장착 중이던 칭호면 장착도 함께 푼다. */
    public Optional<Holder> removeHolder(TitleType type)
    {
        Holder removed = this.holders.remove(type);
        if (removed == null)
        {
            return Optional.empty();
        }
        if (this.equipped.get(removed.playerId()) == type)
        {
            this.equipped.remove(removed.playerId());
        }
        this.setDirty();
        return Optional.of(removed);
    }

    public Optional<TitleType> getEquipped(UUID playerId)
    {
        return Optional.ofNullable(this.equipped.get(playerId));
    }

    /** @param type {@code null}이면 장착을 푼다. */
    public void setEquipped(UUID playerId, @Nullable TitleType type)
    {
        if (type == null)
        {
            this.equipped.remove(playerId);
        }
        else
        {
            this.equipped.put(playerId, type);
        }
        this.setDirty();
    }

    /**
     * 닉네임을 바꾼 보유자의 저장 이름을 새 이름으로 고친다.
     *
     * @return 바뀌기 전 이름. 바뀐 것이 없으면 비어 있다.
     */
    public Optional<String> updateHolderName(UUID playerId, String playerName)
    {
        String previousName = null;
        for (Map.Entry<TitleType, Holder> entry : this.holders.entrySet())
        {
            Holder holder = entry.getValue();
            boolean renamed = holder.playerId().equals(playerId) && !holder.playerName().equals(playerName);
            if (renamed)
            {
                previousName = holder.playerName();
                entry.setValue(new Holder(playerId, playerName, holder.acquiredAt()));
            }
        }
        if (previousName != null)
        {
            this.setDirty();
        }
        return Optional.ofNullable(previousName);
    }

    private Map<String, Holder> packHolders()
    {
        Map<String, Holder> packed = new LinkedHashMap<>();
        this.holders.forEach((type, holder) -> packed.put(type.getId(), holder));
        return packed;
    }

    private Map<UUID, String> packEquipped()
    {
        Map<UUID, String> packed = new LinkedHashMap<>();
        this.equipped.forEach((playerId, type) -> packed.put(playerId, type.getId()));
        return packed;
    }
}
