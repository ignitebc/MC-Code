package com.daqem.jobsplus.achievement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/** 시즌별 원본 누적값과 달성 기록. 수령 기록은 인벤토리와 같은 플레이어 파일에 저장한다. */
public record AchievementProgress(Map<String, Long> counters, Set<String> completed, Map<String, GearProgress> equipment)
{
    private static final Codec<Set<String>> STRING_SET = Codec.STRING.listOf().xmap(HashSet::new, java.util.ArrayList::new);
    private static final Codec<Long> NON_NEGATIVE_LONG = Codec.LONG.validate(Codec.checkRange(0L, Long.MAX_VALUE));
    public static final Codec<AchievementProgress> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.unboundedMap(Codec.STRING, NON_NEGATIVE_LONG).fieldOf("counters").forGetter(AchievementProgress::counters),
            STRING_SET.fieldOf("completed").forGetter(AchievementProgress::completed),
            Codec.unboundedMap(Codec.STRING, GearProgress.CODEC).fieldOf("equipment").forGetter(AchievementProgress::equipment)
    ).apply(instance, AchievementProgress::new));

    public AchievementProgress()
    {
        this(Map.of(), Set.of(), Map.of());
    }

    public AchievementProgress
    {
        counters = new HashMap<>(counters);
        completed = new HashSet<>(completed);
        equipment = new HashMap<>(equipment);
    }

    public long value(String key)
    {
        return counters.getOrDefault(key, 0L);
    }

    public boolean set(String key, long value)
    {
        long next = Math.max(0L, value);
        if (value(key) == next)
        {
            return false;
        }
        counters.put(key, next);
        return true;
    }

    public boolean add(String key, long amount)
    {
        if (amount <= 0)
        {
            return false;
        }
        long current = value(key);
        long next = Long.MAX_VALUE;
        if (amount <= Long.MAX_VALUE - current)
        {
            next = current + amount;
        }
        return set(key, next);
    }

    public boolean maximum(String key, long value)
    {
        if (value > value(key))
        {
            return set(key, value);
        }
        return false;
    }

    public record GearProgress(String kind, String type, long contributedExperience, int reachedLevel)
    {
        public static final Codec<GearProgress> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.STRING.fieldOf("kind").forGetter(GearProgress::kind),
                Codec.STRING.fieldOf("type").forGetter(GearProgress::type),
                NON_NEGATIVE_LONG.fieldOf("experience").forGetter(GearProgress::contributedExperience),
                Codec.intRange(0, 100).fieldOf("reached_level").forGetter(GearProgress::reachedLevel)
        ).apply(instance, GearProgress::new));
    }
}
