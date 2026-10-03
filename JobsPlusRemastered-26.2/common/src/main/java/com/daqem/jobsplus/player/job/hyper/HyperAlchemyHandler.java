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

    public static boolean roll(ThrownSplashPotion potion, ItemStack item)
    {
        int level = item.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY)
                .copyTag().getIntOr(LEVEL_KEY, 0);
        if (level <= 0 || !(potion.getOwner() instanceof ServerPlayer)) return false;
        PotionContents contents = item.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY);
        boolean hasEffect = false;
        for (MobEffectInstance effect : contents.getAllEffects())
        {
            if (effect.getEffect().value().getCategory() != MobEffectCategory.BENEFICIAL) return false;
            hasEffect = true;
        }
        return hasEffect && potion.getRandom().nextDouble() * 100.0D < HyperSkillRules.getAlchemistChance(level);
    }

    public static boolean hasEffectLevels(MobEffectInstance effect)
    {
        var type = effect.getEffect();
        return !type.equals(MobEffects.FIRE_RESISTANCE) && !type.equals(MobEffects.WATER_BREATHING)
                && !type.equals(MobEffects.NIGHT_VISION) && !type.equals(MobEffects.INVISIBILITY)
                && !type.equals(MobEffects.SLOW_FALLING) && !type.equals(MobEffects.CONDUIT_POWER)
                && !type.equals(MobEffects.DOLPHINS_GRACE) && !type.equals(MobEffects.BREATH_OF_THE_NAUTILUS);
    }

    public static EffectAmplifierScope scope(ServerPlayer target, Entity owner, MobEffectInstance original)
    {
        boolean scalable = hasEffectLevels(original);
        int extra = scalable && owner != null && owner.getUUID().equals(target.getUUID()) ? 1 : 0;
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
