package com.daqem.jobsplus.player.job.powerup;

import com.daqem.arc.api.action.IAction;
import com.daqem.arc.api.reward.IReward;
import com.daqem.arc.data.reward.effect.EffectAmplifierAdditionReward;
import com.daqem.arc.data.reward.effect.EffectDurationMultiplierReward;
import com.daqem.jobsplus.JobsPlus;
import com.daqem.jobsplus.integration.arc.holder.holders.powerup.PowerupInstance;
import com.daqem.jobsplus.player.JobsServerPlayer;
import com.daqem.jobsplus.player.job.Job;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * 연금술사가 물약 계열(투척 물약, 잔류 물약 구름, 화살촉 화살)로 몹에게 건 해로운 효과를 자기 일반스킬만큼 키운다.
 * <p>
 * 내가 받는 효과는 ArcLib 스킬 데이터('오래 끓인 약'·'농축 정제')가 처리하고, 이 클래스는 몹에게 주는 쪽만 맡는다.
 * 수치는 켜 둔 가장 높은 단계의 스킬 데이터 보상 값을 그대로 읽어 데이터와 어긋나지 않게 한다.
 * 플레이어에게 건 효과는 키우지 않는다. 받는 쪽 스킬과 겹쳐 PvP에서 4배가 되지 않게 하기 위해서다.
 */
public final class AlchemistPotionBoost
{
    public static final AlchemistPotionBoost NONE = new AlchemistPotionBoost(1.0D, 0, 0.0D);

    private static final Identifier ALCHEMIST = JobsPlus.getId("alchemist");
    private static final String LONGER_POTIONS = "alchemist/longer_potions_";
    private static final String STRONGER_POTIONS = "alchemist/stronger_potions_";
    private static final List<String> TIERS = List.of("i", "ii", "iii", "iv", "v", "vi", "vii", "viii", "ix", "x");
    /** ArcLib 보상 확률은 백분율이다. */
    private static final double PERCENT = 100.0D;

    private final double durationMultiplier;
    private final int amplifierAddition;
    private final double amplifierChance;

    private AlchemistPotionBoost(double durationMultiplier, int amplifierAddition, double amplifierChance)
    {
        this.durationMultiplier = durationMultiplier;
        this.amplifierAddition = amplifierAddition;
        this.amplifierChance = amplifierChance;
    }

    /** 물약을 던지거나 쏜 사람의 현재 스킬 상태. 연금술사 플레이어가 아니거나 켠 스킬이 없으면 NONE */
    public static AlchemistPotionBoost of(@Nullable Entity thrower)
    {
        if (!(thrower instanceof JobsServerPlayer player))
        {
            return NONE;
        }
        Job alchemist = player.jobsplus$getJob(ALCHEMIST);
        if (alchemist == null)
        {
            return NONE;
        }

        double multiplier = 1.0D;
        EffectDurationMultiplierReward durationReward =
                findReward(highestActive(alchemist, LONGER_POTIONS), EffectDurationMultiplierReward.class);
        if (durationReward != null)
        {
            multiplier = durationReward.getMultiplier();
        }

        int addition = 0;
        double chance = 0.0D;
        EffectAmplifierAdditionReward amplifierReward =
                findReward(highestActive(alchemist, STRONGER_POTIONS), EffectAmplifierAdditionReward.class);
        if (amplifierReward != null)
        {
            addition = amplifierReward.getAddition();
            chance = amplifierReward.getChance();
        }

        boolean hasBoost = multiplier > 1.0D || addition > 0;
        if (!hasBoost)
        {
            return NONE;
        }
        return new AlchemistPotionBoost(multiplier, addition, chance);
    }

    /** 물약 하나(투척 물약·잔류 구름·화살)마다 한 번 굴리는 '농축 정제' 판정. 같은 물약에 맞은 몹은 같은 결과를 받는다. */
    public boolean rollAmplifier(RandomSource random)
    {
        if (this.amplifierAddition <= 0)
        {
            return false;
        }
        return random.nextDouble() * PERCENT <= this.amplifierChance;
    }

    /** 몹에게 거는 해로운 효과를 키운 효과. 플레이어, 이로운 효과, 키울 것이 없을 때는 받은 효과를 그대로 돌려준다. */
    public MobEffectInstance applyToMob(MobEffectInstance effect, LivingEntity target, boolean amplify)
    {
        boolean beneficial = effect.getEffect().value().getCategory() == MobEffectCategory.BENEFICIAL;
        if (this == NONE || target instanceof Player || beneficial)
        {
            return effect;
        }
        int duration = effect.getDuration();
        if (!effect.isInfiniteDuration())
        {
            duration = Mth.floor(duration * this.durationMultiplier);
        }
        int amplifier = effect.getAmplifier();
        if (amplify)
        {
            amplifier += this.amplifierAddition;
        }
        return new MobEffectInstance(effect.getEffect(), duration, amplifier,
                effect.isAmbient(), effect.isVisible(), effect.showIcon());
    }

    /** 이 계열에서 켜 둔 가장 높은 단계. 켠 단계가 없으면 null */
    private static @Nullable PowerupInstance highestActive(Job job, String linePrefix)
    {
        PowerupInstance highest = null;
        int highestTier = 0;
        for (Powerup powerup : job.getPowerupManager().getAllPowerups())
        {
            Identifier location = powerup.getPowerupLocation();
            boolean inLine = JobsPlus.MOD_ID.equals(location.getNamespace()) && location.getPath().startsWith(linePrefix);
            if (!inLine || powerup.getState() != PowerupState.ACTIVE)
            {
                continue;
            }
            int tier = TIERS.indexOf(location.getPath().substring(linePrefix.length())) + 1;
            if (tier > highestTier)
            {
                highestTier = tier;
                highest = powerup.getPowerupInstance();
            }
        }
        return highest;
    }

    @Nullable
    private static <T extends IReward> T findReward(@Nullable PowerupInstance powerup, Class<T> rewardType)
    {
        if (powerup == null)
        {
            return null;
        }
        for (IAction action : powerup.getActions())
        {
            for (IReward reward : action.getRewards())
            {
                if (rewardType.isInstance(reward))
                {
                    return rewardType.cast(reward);
                }
            }
        }
        return null;
    }
}
