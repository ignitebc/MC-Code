package com.daqem.jobsplus.player.job.hyper;

import com.daqem.arc.api.player.ArcServerPlayer;
import com.daqem.arc.player.EffectRewardPreview;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * 농부 하이퍼를 켠 플레이어가 먹은 황금사과·마법이 부여된 황금사과·황금당근의 흡수량을 합쳐 흡수 효과 하나로 관리한다.
 * <p>
 * 바닐라 흡수 효과는 하나만 존재하고 가장 높은 단계만 적용되므로 여러 음식을 먹어도 흡수량이 합쳐지지 않는다.
 * 그래서 음식마다 몫(단계와 남은 시간)을 기록해 두고, 몫의 합으로 흡수 효과를 직접 교체한다.
 * <ul>
 *   <li>같은 음식은 한 몫만 차지한다. 다시 먹으면 바닐라 효과 규칙처럼 높은 단계가 우선하고, 그 몫이 끝나면 남은 낮은 단계가 이어진다.</li>
 *   <li>몫마다 시간이 따로 흐르고, 효과의 남은 시간은 가장 늦게 끝나는 몫을 따른다.</li>
 *   <li>먹을 때는 그 몫만큼만 흡수량을 채운다.</li>
 *   <li>연금술사 스킬은 몫을 등록할 때 한 번 적용한다. 효과는 forceAddEffect로 교체해 스킬이 다시 붙지 않는다.</li>
 * </ul>
 */
public final class FoodAbsorptionStack
{
    /** 바닐라 흡수 효과는 단계마다 최대 흡수량을 4씩 늘린다. */
    private static final float ABSORPTION_PER_LEVEL = 4.0F;
    /** 이 클래스와 바닐라 효과는 같은 틱의 다른 시점에 시간이 줄어든다. 그 차이를 넘으면 외부에서 효과가 바뀐 것으로 본다. */
    private static final int DURATION_TOLERANCE_TICKS = 20;

    private FoodAbsorptionStack() {}

    /**
     * 사과를 먹어 바닐라가 흡수 효과를 넣으려 할 때 호출된다.
     *
     * @return 중첩으로 처리했으면 true. false면 바닐라가 그대로 효과를 넣는다.
     */
    public static boolean addFromFood(LivingEntity entity, ItemStack food, MobEffectInstance effect)
    {
        if (!(entity instanceof ServerPlayer player) || !effect.is(MobEffects.ABSORPTION))
        {
            return false;
        }
        FoodAbsorptionShare.Source source = sourceOf(food);
        if (source == null || HyperSkillRules.getActiveLevel(player, HyperSkillRules.FARMER) == 0)
        {
            return false;
        }
        add(player, source, effect);
        return true;
    }

    /** 음식 한 번의 흡수 효과를 몫으로 등록하고 합친 효과를 적용한다. */
    public static void add(ServerPlayer player, FoodAbsorptionShare.Source source, MobEffectInstance effect)
    {
        List<FoodAbsorptionShare> shares = HyperSkillHandler.state(player).foodAbsorptionShares;
        if (isChangedOutside(player, shares))
        {
            shares.clear();
        }
        if (shares.isEmpty())
        {
            adoptExistingEffect(player, shares);
        }

        MobEffectInstance resolved = EffectRewardPreview.resolve((ArcServerPlayer) player, effect);
        if (resolved == null || resolved.getDuration() <= 0)
        {
            return;
        }
        // 효과를 바꾸면 최대 흡수량이 달라져 값이 잘릴 수 있으므로 바꾸기 전에 읽는다.
        float absorptionBefore = player.getAbsorptionAmount();
        FoodAbsorptionShare share = new FoodAbsorptionShare(source, resolved.getAmplifier(), resolved.getDuration());
        shares.add(share);
        removeCoveredShares(shares);
        applyEffect(player, shares);

        float refilled = absorptionBefore + absorptionOf(share.amplifier());
        player.setAbsorptionAmount(Math.min(player.getMaxAbsorption(), refilled));
    }

    /** 매 틱 몫의 시간을 줄이고, 몫이 끝나 합계가 바뀌면 효과를 다시 적용한다. */
    public static void tick(ServerPlayer player, HyperPlayerState state)
    {
        List<FoodAbsorptionShare> shares = state.foodAbsorptionShares;
        if (shares.isEmpty())
        {
            return;
        }
        // 우유·불사의 토템·흡수량 소진·명령어로 효과가 사라지거나 바뀌면 기록을 버리고 바닐라에 맡긴다.
        if (isChangedOutside(player, shares))
        {
            shares.clear();
            return;
        }

        int amplifierBefore = stackedAmplifier(shares);
        for (FoodAbsorptionShare share : shares)
        {
            share.tickDown();
        }
        shares.removeIf(FoodAbsorptionShare::isExpired);
        // 마지막 몫이 끝나는 틱에는 남은 시간을 맞춰 둔 바닐라 효과도 함께 끝난다.
        if (shares.isEmpty())
        {
            return;
        }
        if (stackedAmplifier(shares) != amplifierBefore)
        {
            applyEffect(player, shares);
            // 끝난 몫만큼 상한이 줄면 그 상한을 넘는 흡수량만 잘라 낸다.
            player.setAbsorptionAmount(Math.min(player.getAbsorptionAmount(), player.getMaxAbsorption()));
        }
    }

    private static FoodAbsorptionShare.Source sourceOf(ItemStack food)
    {
        if (food.is(Items.GOLDEN_APPLE))
        {
            return FoodAbsorptionShare.Source.GOLDEN_APPLE;
        }
        if (food.is(Items.ENCHANTED_GOLDEN_APPLE))
        {
            return FoodAbsorptionShare.Source.ENCHANTED_GOLDEN_APPLE;
        }
        return null;
    }

    private static boolean isChangedOutside(ServerPlayer player, List<FoodAbsorptionShare> shares)
    {
        if (shares.isEmpty())
        {
            return false;
        }
        MobEffectInstance current = player.getEffect(MobEffects.ABSORPTION);
        if (current == null)
        {
            return true;
        }
        boolean sameLevel = current.getAmplifier() == stackedAmplifier(shares);
        boolean sameDuration = Math.abs(current.getDuration() - longestRemaining(shares)) <= DURATION_TOLERANCE_TICKS;
        return !(sameLevel && sameDuration);
    }

    /** 중첩을 시작할 때 이미 있던 흡수 효과는 교체로 사라지지 않게 몫으로 넘겨받는다. 흡수량은 이미 채워져 있다. */
    private static void adoptExistingEffect(ServerPlayer player, List<FoodAbsorptionShare> shares)
    {
        MobEffectInstance current = player.getEffect(MobEffects.ABSORPTION);
        if (current == null || current.isInfiniteDuration() || current.getDuration() <= 0)
        {
            return;
        }
        shares.add(new FoodAbsorptionShare(FoodAbsorptionShare.Source.EXISTING,
                current.getAmplifier(), current.getDuration()));
    }

    private static void removeCoveredShares(List<FoodAbsorptionShare> shares)
    {
        List<FoodAbsorptionShare> kept = new ArrayList<>();
        for (FoodAbsorptionShare share : shares)
        {
            boolean covered = kept.stream().anyMatch(share::isCoveredBy);
            if (covered)
            {
                continue;
            }
            kept.removeIf(other -> other.isCoveredBy(share));
            kept.add(share);
        }
        shares.clear();
        shares.addAll(kept);
    }

    private static void applyEffect(ServerPlayer player, List<FoodAbsorptionShare> shares)
    {
        MobEffectInstance stacked = new MobEffectInstance(MobEffects.ABSORPTION,
                longestRemaining(shares), stackedAmplifier(shares));
        // addEffect는 바닐라 자동 재충전과 연금술사 스킬을 다시 거치므로, 계산한 값 그대로 교체한다.
        player.forceAddEffect(stacked, null);
    }

    /** 음식마다 가장 높은 단계 하나씩을 더한 값. 흡수 I은 단계 1로 센다. */
    private static int stackedAmplifier(List<FoodAbsorptionShare> shares)
    {
        Map<FoodAbsorptionShare.Source, Integer> strongest = new EnumMap<>(FoodAbsorptionShare.Source.class);
        for (FoodAbsorptionShare share : shares)
        {
            strongest.merge(share.source(), share.amplifier(), Math::max);
        }
        int levels = 0;
        for (int amplifier : strongest.values())
        {
            levels += amplifier + 1;
        }
        return levels - 1;
    }

    private static int longestRemaining(List<FoodAbsorptionShare> shares)
    {
        int longest = 0;
        for (FoodAbsorptionShare share : shares)
        {
            longest = Math.max(longest, share.remainingTicks());
        }
        return longest;
    }

    private static float absorptionOf(int amplifier)
    {
        return ABSORPTION_PER_LEVEL * (amplifier + 1);
    }
}
