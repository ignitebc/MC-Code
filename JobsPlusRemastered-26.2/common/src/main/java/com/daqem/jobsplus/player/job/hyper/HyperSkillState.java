package com.daqem.jobsplus.player.job.hyper;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/** 일반 스킬 구매와 분리한 하이퍼 단계·활성 상태·요청 버전. */
public record HyperSkillState(int level, boolean active, int revision)
{
    public static final HyperSkillState EMPTY = new HyperSkillState(0, true, 0);
    public static final Codec<HyperSkillState> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.intRange(0, HyperSkillRules.MAX_LEVEL).fieldOf("level").forGetter(HyperSkillState::level),
            Codec.BOOL.optionalFieldOf("active", true).forGetter(HyperSkillState::active),
            Codec.INT.optionalFieldOf("revision", 0).forGetter(HyperSkillState::revision)
    ).apply(instance, HyperSkillState::new));

    public HyperSkillState
    {
        level = Math.clamp(level, 0, HyperSkillRules.MAX_LEVEL);
    }

    public HyperSkillState withLevel(int nextLevel)
    {
        return new HyperSkillState(nextLevel, this.active, this.revision + 1);
    }

    public HyperSkillState toggle()
    {
        return new HyperSkillState(this.level, !this.active, this.revision + 1);
    }
}
