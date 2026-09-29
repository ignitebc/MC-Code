package com.daqem.jobsplus.metrics;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.Mob;

import java.util.List;
import java.util.Locale;

/**
 * 몹이 처음 생성될 때의 스폰 원인을 엔티티 태그로 남긴다.
 * 엔티티 태그는 몹 저장 데이터에 함께 저장되므로 청크가 다시 로드되어도 유지된다.
 * 스포너·몬스터 공장 처치를 자연 스폰 사냥과 나누어 분석하기 위해 쓴다.
 */
public final class SpawnReasonTag
{
    private static final String PREFIX = "jobsplus.spawn.";
    /** 이 기능 이전에 생긴 몹처럼 원인 태그가 없는 몹 */
    static final String UNKNOWN = "unknown";

    private SpawnReasonTag()
    {
    }

    public static void tag(Mob mob, EntitySpawnReason reason)
    {
        if (reason == null || reason == EntitySpawnReason.LOAD || reason == EntitySpawnReason.DIMENSION_TRAVEL)
        {
            // 저장된 몹을 다시 불러오거나 차원을 옮기는 경우는 새 스폰이 아니므로 처음 원인을 유지한다.
            return;
        }
        List<String> previousTags = mob.entityTags().stream().filter(tag -> tag.startsWith(PREFIX)).toList();
        previousTags.forEach(mob::removeTag);
        mob.addTag(PREFIX + reason.name().toLowerCase(Locale.ROOT));
    }

    /** 몹이면 스폰 원인(없으면 unknown), 몹이 아니거나 없으면 빈 문자열을 돌려준다. */
    static String read(Entity entity)
    {
        if (!(entity instanceof Mob))
        {
            return "";
        }
        for (String tag : entity.entityTags())
        {
            if (tag.startsWith(PREFIX))
            {
                return tag.substring(PREFIX.length());
            }
        }
        return UNKNOWN;
    }
}
