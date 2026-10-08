package com.daqem.jobsplus.player.job.hyper;

import com.daqem.arc.player.EffectAmplifierScope;
import com.daqem.arc.player.SkillActivationNotifier;
import com.daqem.jobsplus.JobsPlus;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownSplashPotion;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.component.CustomData;

public final class HyperAlchemyHandler
{
    private static final String LEVEL_KEY = "jobsplus_hyper_alchemist_level";

    private HyperAlchemyHandler() {}

    public static void markThrown(Projectile projectile, Entity owner)
    {
        if (!(projectile instanceof ThrownSplashPotion potion) || !(owner instanceof ServerPlayer player)) return;
        int level = HyperSkillRules.getActiveLevel(player, HyperSkillRules.ALCHEMIST);
        // 투척 아이템의 복사본에 기록하여 비행 중 강화·토글·재접속으로 판정을 바꾸지 못하게 한다.
        ItemStack item = potion.getItem().copy();
        CustomData.update(DataComponents.CUSTOM_DATA, item, tag -> tag.putInt(LEVEL_KEY, level));
        potion.setItem(item);
    }

    /** 하이퍼를 켠 연금술사가 던진, 이로운 효과만 담긴 투척 물약인지. 본인 확정 강화와 아군 강화 판정의 공통 조건이다. */
    public static boolean isEligible(ThrownSplashPotion potion, ItemStack item)
    {
        if (thrownLevel(item) <= 0 || !(potion.getOwner() instanceof ServerPlayer)) return false;
        PotionContents contents = item.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY);
        boolean hasEffect = false;
        for (MobEffectInstance effect : contents.getAllEffects())
        {
            if (effect.getEffect().value().getCategory() != MobEffectCategory.BENEFICIAL) return false;
            hasEffect = true;
        }
        return hasEffect;
    }

    /** 한 병당 한 번 하는 강화 판정. 성공하면 범위 안 아군도 강화된다. */
    public static boolean roll(ThrownSplashPotion potion, ItemStack item)
    {
        double chance = HyperSkillRules.getAlchemistChance(thrownLevel(item));
        return potion.getRandom().nextDouble() * 100.0D < chance;
    }

    /** 본인은 판정과 관계없이 강화하고, 다른 플레이어는 판정에 성공했을 때만 강화한다. */
    public static boolean appliesTo(ServerPlayer target, Entity owner, boolean eligible, boolean enhanced)
    {
        if (enhanced)
        {
            return true;
        }
        return eligible && isOwner(target, owner);
    }

    private static boolean isOwner(ServerPlayer target, Entity owner)
    {
        return owner != null && owner.getUUID().equals(target.getUUID());
    }

    private static int thrownLevel(ItemStack item)
    {
        return item.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getIntOr(LEVEL_KEY, 0);
    }

    public static boolean hasEffectLevels(MobEffectInstance effect)
    {
        var type = effect.getEffect();
        return !type.equals(MobEffects.FIRE_RESISTANCE) && !type.equals(MobEffects.WATER_BREATHING)
                && !type.equals(MobEffects.NIGHT_VISION) && !type.equals(MobEffects.INVISIBILITY)
                && !type.equals(MobEffects.SLOW_FALLING) && !type.equals(MobEffects.CONDUIT_POWER)
                && !type.equals(MobEffects.DOLPHINS_GRACE) && !type.equals(MobEffects.BREATH_OF_THE_NAUTILUS);
    }

    public static EffectAmplifierScope scope(ServerPlayer target, Entity owner, MobEffectInstance original,
                                             boolean enhanced)
    {
        boolean scalable = hasEffectLevels(original);
        // 기본 +1은 본인 확정분과 판정 성공분이 같다. 본인이 판정에도 성공하면 기존처럼 +1을 더 받는다.
        boolean ownerBonus = scalable && enhanced && isOwner(target, owner);
        int extra = ownerBonus ? 1 : 0;
        // 저항 V의 완전 면역과 높은 단계의 회복 폭증을 막기 위해 하이퍼 강화 상한은 IV다.
        int cap = scalable ? 3 : original.getAmplifier();
        return new EffectAmplifierScope(target, original, scalable ? 1 : 0, extra, cap);
    }

    public static MobEffectInstance enhanced(ServerPlayer target, MobEffectInstance effect)
    {
        return new MobEffectInstance(effect.getEffect(), effect.getDuration(),
                EffectAmplifierScope.resolve(target, effect, 0), effect.isAmbient(), effect.isVisible(), effect.showIcon());
    }

    public static void notifyCaster(ThrownSplashPotion potion)
    {
        if (potion.getOwner() instanceof ServerPlayer player)
        {
            SkillActivationNotifier.notifySkillActivated(player, JobsPlus.translatable("hyper.alchemist.activated"));
        }
    }
}
