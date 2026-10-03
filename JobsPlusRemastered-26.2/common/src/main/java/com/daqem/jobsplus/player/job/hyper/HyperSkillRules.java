package com.daqem.jobsplus.player.job.hyper;

import com.daqem.jobsplus.JobsPlus;
import com.daqem.jobsplus.player.JobsServerPlayer;
import com.daqem.jobsplus.player.job.Job;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

import java.util.Set;
import java.util.List;
import java.util.Locale;

/** 서버 판정과 화면이 함께 사용하는 해금·강화 규칙. 비용 구간은 목표 LV 기준이다. */
public final class HyperSkillRules
{
    public static final int REQUIRED_JOB_LEVEL = 100;
    public static final int MAX_LEVEL = 10;
    public static final int OPEN_GEM_COST = 10;
    public static final int OPEN_COIN_COST = 300;
    public static final int UPGRADE_COIN_COST = 20;
    public static final int SHIELD_DURATION_TICKS = 60;
    public static final int LANDING_PROTECTION_TICKS = 20;
    public static final int LEECH_COOLDOWN_TICKS = 20;
    public static final float LEECH_HEALTH = 2.0F;
    public static final int LEAP_CHARGE_TICKS = 40;
    public static final int LEAP_MIN_CHARGE_TICKS = 6;
    public static final int LEAP_COOLDOWN_TICKS = 600;
    public static final Identifier MINER = JobsPlus.getId("miner");
    public static final Identifier DIGGER = JobsPlus.getId("digger");
    public static final Identifier FARMER = JobsPlus.getId("farmer");
    public static final Identifier FISHERMAN = JobsPlus.getId("fisherman");
    public static final Identifier HUNTER = JobsPlus.getId("hunter");
    public static final Identifier SMITH = JobsPlus.getId("smith");
    public static final Identifier ALCHEMIST = JobsPlus.getId("alchemist");
    public static final Identifier ADVENTURER = JobsPlus.getId("adventurer");
    public static final Identifier GEM = Identifier.parse("advancednetherite:enhancement_gem");
    public static final Identifier BITCOIN = Identifier.parse("advancednetherite:bitcoin");

    // 이미지 등록과 실제 해금 지원 여부는 별도로 관리한다.
    private static final Set<Identifier> ICON_JOBS = Set.of(
            MINER, DIGGER, FARMER, FISHERMAN, HUNTER, SMITH, ALCHEMIST, ADVENTURER);

    // 장비 강화 EnhancementHelper의 기본 확률과 동일하다. OPEN은 LV1을 확정 지급한다.
    private static final int[] SUCCESS_CHANCE = {90, 80, 70, 60, 50, 40, 30, 20, 10, 5};

    private HyperSkillRules() {}

    public static boolean supports(Identifier jobLocation)
    {
        return ICON_JOBS.contains(jobLocation) && !FISHERMAN.equals(jobLocation);
    }

    public static boolean hasIcon(Identifier jobLocation)
    {
        return ICON_JOBS.contains(jobLocation);
    }

    public static Component getName(Identifier jobLocation)
    {
        if (MINER.equals(jobLocation))
        {
            return JobsPlus.translatable("hyper.miner.name");
        }
        if (DIGGER.equals(jobLocation))
        {
            return JobsPlus.translatable("hyper.digger.name");
        }
        if (FARMER.equals(jobLocation))
        {
            return JobsPlus.translatable("hyper.farmer.name");
        }
        if (FISHERMAN.equals(jobLocation))
        {
            return JobsPlus.translatable("hyper.fisherman.name");
        }
        if (HUNTER.equals(jobLocation))
        {
            return JobsPlus.translatable("hyper.hunter.name");
        }
        if (SMITH.equals(jobLocation))
        {
            return JobsPlus.translatable("hyper.smith.name");
        }
        if (ALCHEMIST.equals(jobLocation))
        {
            return JobsPlus.translatable("hyper.alchemist.name");
        }
        if (ADVENTURER.equals(jobLocation))
        {
            return JobsPlus.translatable("hyper.adventurer.name");
        }
        return JobsPlus.translatable("hyper.not_available");
    }

    public static int getActivationChance(int level)
    {
        return Math.clamp(level, 0, MAX_LEVEL) * 10;
    }

    public static int getActiveLevel(ServerPlayer player, Identifier jobLocation)
    {
        if (!player.isAlive() || player.isSpectator() || player.isCreative()
                || !(player instanceof JobsServerPlayer jobsPlayer))
        {
            return 0;
        }
        Job job = jobsPlayer.jobsplus$getJob(jobLocation);
        if (job == null || job.getLevel() < REQUIRED_JOB_LEVEL || !job.getHyperSkill().active())
        {
            return 0;
        }
        return job.getHyperSkill().level();
    }

    public static int getFarmerChance(int level)
    {
        return Math.clamp(level, 0, MAX_LEVEL) * 3;
    }

    public static int getHunterChance(int level)
    {
        return Math.clamp(level, 0, MAX_LEVEL) * 4;
    }

    public static double getAlchemistChance(int level)
    {
        return level <= 0 ? 0.0D : 10.0D + (Math.clamp(level, 1, MAX_LEVEL) - 1) * 40.0D / 9.0D;
    }

    public static int getLeapDistance(int level)
    {
        return Math.clamp(level, 0, MAX_LEVEL) * 8;
    }

    public static int getShieldCooldownTicks(int level)
    {
        return 1200 - (int) Math.round((Math.clamp(level, 1, MAX_LEVEL) - 1) * 600.0D / 9.0D);
    }

    public static Component getEffectSummary(Identifier jobLocation, int level)
    {
        if (FARMER.equals(jobLocation))
        {
            return JobsPlus.translatable("hyper.farmer.summary", getFarmerChance(level));
        }
        if (HUNTER.equals(jobLocation))
        {
            return JobsPlus.translatable("hyper.hunter.summary", getHunterChance(level));
        }
        if (ALCHEMIST.equals(jobLocation))
        {
            return JobsPlus.translatable("hyper.alchemist.summary", decimal(getAlchemistChance(level)));
        }
        if (ADVENTURER.equals(jobLocation))
        {
            return JobsPlus.translatable("hyper.adventurer.summary",
                    decimal(getLeapDistance(level) / 16.0D), getLeapDistance(level));
        }
        if (SMITH.equals(jobLocation))
        {
            return JobsPlus.translatable("hyper.smith.summary", decimal(getShieldCooldownTicks(level) / 20.0D));
        }
        return JobsPlus.translatable("hyper.mining.summary", getActivationChance(level));
    }

    public static List<Component> getDescriptionLines(Identifier jobLocation)
    {
        String key = MINER.equals(jobLocation) || DIGGER.equals(jobLocation) ? "mining" : jobLocation.getPath();
        return List.of(JobsPlus.translatable("hyper." + key + ".description"),
                JobsPlus.translatable("hyper." + key + ".rules"),
                JobsPlus.translatable("hyper." + key + ".details"),
                JobsPlus.translatable("hyper." + key + ".warning"));
    }

    private static String decimal(double value)
    {
        return String.format(Locale.ROOT, "%.1f", value);
    }

    public static int getSuccessChance(int targetLevel)
    {
        validateTargetLevel(targetLevel);
        return SUCCESS_CHANCE[targetLevel - 1];
    }

    public static int getGemCost(int targetLevel)
    {
        validateTargetLevel(targetLevel);
        if (targetLevel <= 4)
        {
            return 5;
        }
        if (targetLevel <= 7)
        {
            return 7;
        }
        return 10;
    }

    public static int getBitcoinCost(int targetLevel)
    {
        validateTargetLevel(targetLevel);
        if (targetLevel <= 4)
        {
            return 2;
        }
        if (targetLevel <= 7)
        {
            return 4;
        }
        return 6;
    }

    private static void validateTargetLevel(int targetLevel)
    {
        if (targetLevel < 2 || targetLevel > MAX_LEVEL)
        {
            throw new IllegalArgumentException("Hyper upgrade target must be LV2 through LV10");
        }
    }

    public static int countItems(Inventory inventory, Identifier itemLocation)
    {
        int count = 0;
        for (int slot = 0; slot < inventory.getContainerSize(); slot++)
        {
            ItemStack stack = inventory.getItem(slot);
            if (!stack.isEmpty() && itemLocation.equals(BuiltInRegistries.ITEM.getKey(stack.getItem())))
            {
                count += stack.getCount();
            }
        }
        return count;
    }

    /** 모든 재료의 보유량을 먼저 검증한 뒤 서버 스레드에서 차감한다. */
    public static void consumeItems(Inventory inventory, Identifier itemLocation, int amount)
    {
        int remaining = amount;
        for (int slot = 0; slot < inventory.getContainerSize() && remaining > 0; slot++)
        {
            ItemStack stack = inventory.getItem(slot);
            if (stack.isEmpty() || !itemLocation.equals(BuiltInRegistries.ITEM.getKey(stack.getItem())))
            {
                continue;
            }
            int removed = Math.min(remaining, stack.getCount());
            stack.shrink(removed);
            remaining -= removed;
            if (stack.isEmpty())
            {
                inventory.setItem(slot, ItemStack.EMPTY);
            }
        }
        inventory.setChanged();
    }
}
