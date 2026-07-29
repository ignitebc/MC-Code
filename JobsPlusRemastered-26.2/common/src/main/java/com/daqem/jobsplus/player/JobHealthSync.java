package com.daqem.jobsplus.player;

import com.daqem.jobsplus.JobsPlus;
import com.daqem.jobsplus.config.JobsPlusConfig;
import com.daqem.jobsplus.player.job.Job;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

import java.util.HashSet;
import java.util.Set;

public final class JobHealthSync
{

    private static final Identifier JOB_BASE_HEALTH_MODIFIER_ID = JobsPlus.getId("job_base_health");
    private static final int ALL_JOBS_COUNT = JobsPlusConfig.MAX_JOB_COUNT;
    private static final double ALL_JOBS_HEALTH = 20.0D;

    private JobHealthSync()
    {
    }

    public static void sync(JobsServerPlayer jobsServerPlayer)
    {
        ServerPlayer serverPlayer = jobsServerPlayer.jobsplus$getServerPlayer();
        AttributeInstance maxHealth = serverPlayer.getAttribute(Attributes.MAX_HEALTH);
        if (maxHealth == null)
        {
            return;
        }

        double totalHealth = 0.0D;
        int jobCount = 0;
        Set<Identifier> jobLocations = new HashSet<>();
        for (Job job : jobsServerPlayer.jobsplus$getJobs())
        {
            Identifier jobLocation = job.getJobInstance().getLocation();
            if (!jobLocations.add(jobLocation))
            {
                continue;
            }

            double jobHealth = getJobHealth(jobLocation);
            if (jobHealth <= 0.0D)
            {
                continue;
            }

            totalHealth += jobHealth;
            jobCount++;
        }

        if (jobCount == 0)
        {
            maxHealth.removeModifier(JOB_BASE_HEALTH_MODIFIER_ID);
            return;
        }

        double desiredHealth = jobCount == ALL_JOBS_COUNT
                ? ALL_JOBS_HEALTH
                : Math.round(totalHealth / jobCount);
        double modifierAmount = desiredHealth - maxHealth.getBaseValue();
        AttributeModifier currentModifier = maxHealth.getModifier(JOB_BASE_HEALTH_MODIFIER_ID);
        if (currentModifier == null
                || currentModifier.operation() != AttributeModifier.Operation.ADD_VALUE
                || Double.compare(currentModifier.amount(), modifierAmount) != 0)
        {
            maxHealth.removeModifier(JOB_BASE_HEALTH_MODIFIER_ID);
            maxHealth.addPermanentModifier(new AttributeModifier(
                    JOB_BASE_HEALTH_MODIFIER_ID,
                    modifierAmount,
                    AttributeModifier.Operation.ADD_VALUE));
        }

        if (serverPlayer.getHealth() > serverPlayer.getMaxHealth())
        {
            serverPlayer.setHealth(serverPlayer.getMaxHealth());
        }
    }

    private static double getJobHealth(Identifier jobLocation)
    {
        if (!JobsPlus.MOD_ID.equals(jobLocation.getNamespace()))
        {
            return 0.0D;
        }

        return switch (jobLocation.getPath())
        {
            case "smith", "hunter", "alchemist" -> 10.0D;
            case "farmer", "fisherman", "miner" -> 12.0D;
            case "digger", "adventurer" -> 14.0D;
            default -> 0.0D;
        };
    }
}
