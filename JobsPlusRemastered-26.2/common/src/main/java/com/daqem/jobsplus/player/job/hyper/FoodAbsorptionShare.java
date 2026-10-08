package com.daqem.jobsplus.player.job.hyper;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.StringRepresentable;

import java.util.List;

/**
 * 음식 한 번이 흡수 중첩에 보탠 몫. 단계는 연금술사 스킬까지 적용된 값이다.
 * <p>
 * 남은 시간은 바닐라 효과처럼 플레이어가 틱을 받을 때만 줄어든다. 접속을 끊은 동안 흡수 효과가 멈춰 있으므로
 * 게임 시각으로 만료를 계산하면 재접속 때 효과와 몫이 어긋난다.
 */
public final class FoodAbsorptionShare
{
    public static final Codec<FoodAbsorptionShare> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Source.CODEC.fieldOf("source").forGetter(FoodAbsorptionShare::source),
            Codec.INT.fieldOf("amplifier").forGetter(FoodAbsorptionShare::amplifier),
            Codec.INT.fieldOf("remaining_ticks").forGetter(FoodAbsorptionShare::remainingTicks)
    ).apply(instance, FoodAbsorptionShare::new));
    public static final Codec<List<FoodAbsorptionShare>> LIST_CODEC = CODEC.listOf();

    private final Source source;
    private final int amplifier;
    private int remainingTicks;

    public FoodAbsorptionShare(Source source, int amplifier, int remainingTicks)
    {
        this.source = source;
        this.amplifier = amplifier;
        this.remainingTicks = remainingTicks;
    }

    public FoodAbsorptionShare copy()
    {
        return new FoodAbsorptionShare(this.source, this.amplifier, this.remainingTicks);
    }

    public Source source()
    {
        return this.source;
    }

    public int amplifier()
    {
        return this.amplifier;
    }

    public int remainingTicks()
    {
        return this.remainingTicks;
    }

    public void tickDown()
    {
        this.remainingTicks--;
    }

    public boolean isExpired()
    {
        return this.remainingTicks <= 0;
    }

    /** 같은 음식의 다른 몫이 단계와 남은 시간 모두에서 앞서면 이 몫은 효과에 영향을 주지 않는다. */
    public boolean isCoveredBy(FoodAbsorptionShare other)
    {
        boolean sameSource = this.source == other.source;
        boolean strongerOrEqual = other.amplifier >= this.amplifier;
        boolean lastsLongerOrEqual = other.remainingTicks >= this.remainingTicks;
        return sameSource && strongerOrEqual && lastsLongerOrEqual;
    }

    public enum Source implements StringRepresentable
    {
        GOLDEN_APPLE("golden_apple"),
        ENCHANTED_GOLDEN_APPLE("enchanted_golden_apple"),
        GOLDEN_CARROT("golden_carrot"),
        /** 중첩을 시작할 때 이미 있던 흡수 효과(불사의 토템, 하이퍼를 끈 채 먹은 사과 등). 교체로 잃지 않게 몫으로 넘겨받는다. */
        EXISTING("existing");

        public static final Codec<Source> CODEC = StringRepresentable.fromEnum(Source::values);

        private final String serializedName;

        Source(String serializedName)
        {
            this.serializedName = serializedName;
        }

        @Override
        public String getSerializedName()
        {
            return this.serializedName;
        }
    }
}
