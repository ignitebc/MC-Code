package com.daqem.jobsplus.player.job.powerup;

import com.daqem.jobsplus.integration.arc.holder.holders.powerup.PowerupInstance;
import com.daqem.jobsplus.player.job.Job;
import net.minecraft.resources.Identifier;

/** 직업 레벨과 선행 스킬을 기준으로 현재 해방된 미습득 스킬을 계산한다. */
public final class PowerupAvailability
{
    private PowerupAvailability()
    {
    }

    public static int countAvailablePowerups(Job job)
    {
        if (job == null || job.getLevel() <= 0)
        {
            return 0;
        }

        int availablePowerupCount = 0;
        for (PowerupInstance powerupInstance : job.getJobInstance().getPowerups())
        {
            if (canUnlock(job, powerupInstance))
            {
                availablePowerupCount++;
            }
        }
        return availablePowerupCount;
    }

    public static boolean canUnlock(Job job, PowerupInstance powerupInstance)
    {
        if (job == null || powerupInstance == null || job.getLevel() <= 0)
        {
            return false;
        }

        Identifier jobLocation = job.getJobInstance().getLocation();
        if (!powerupInstance.getJobLocation().equals(jobLocation))
        {
            return false;
        }

        if (job.getPowerupManager().getPowerup(powerupInstance).isPresent())
        {
            return false;
        }

        if (job.getLevel() < powerupInstance.getRequiredLevel())
        {
            return false;
        }

        Identifier parentLocation = powerupInstance.getParentLocation();
        if (parentLocation == null)
        {
            return true;
        }

        if (job.getPowerupManager().getPowerup(parentLocation).isEmpty())
        {
            return false;
        }
        return true;
    }
}
