package com.daqem.jobsplus.player.job.powerup;

import com.daqem.jobsplus.integration.arc.holder.holders.powerup.PowerupInstance;
import com.daqem.jobsplus.player.job.Job;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

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

    /**
     * 현재 레벨에서 일괄 구매할 수 있는 모든 미습득 스킬을 선행 스킬부터 정렬해 반환한다.
     * 같은 일괄 구매에서 부모 스킬을 먼저 구매하면 해금되는 하위 스킬도 포함한다.
     */
    public static List<PowerupInstance> getBatchUnlockablePowerups(Job job)
    {
        if (job == null || job.getLevel() <= 0)
        {
            return List.of();
        }

        Identifier jobLocation = job.getJobInstance().getLocation();
        Set<Identifier> ownedOrPlannedLocations = new HashSet<>();
        for (Powerup ownedPowerup : job.getPowerupManager().getAllPowerups())
        {
            ownedOrPlannedLocations.add(ownedPowerup.getPowerupLocation());
        }

        List<PowerupInstance> remainingPowerups = new ArrayList<>();
        for (PowerupInstance powerupInstance : job.getJobInstance().getPowerups())
        {
            if (!powerupInstance.getJobLocation().equals(jobLocation))
            {
                continue;
            }
            if (ownedOrPlannedLocations.contains(powerupInstance.getLocation()))
            {
                continue;
            }
            if (job.getLevel() < powerupInstance.getRequiredLevel())
            {
                continue;
            }
            remainingPowerups.add(powerupInstance);
        }

        List<PowerupInstance> unlockablePowerups = new ArrayList<>();
        boolean foundUnlockablePowerup = true;
        while (foundUnlockablePowerup)
        {
            foundUnlockablePowerup = false;
            Iterator<PowerupInstance> iterator = remainingPowerups.iterator();
            while (iterator.hasNext())
            {
                PowerupInstance powerupInstance = iterator.next();
                Identifier parentLocation = powerupInstance.getParentLocation();
                boolean parentAvailable = parentLocation == null
                        || ownedOrPlannedLocations.contains(parentLocation);
                if (!parentAvailable)
                {
                    continue;
                }

                unlockablePowerups.add(powerupInstance);
                ownedOrPlannedLocations.add(powerupInstance.getLocation());
                iterator.remove();
                foundUnlockablePowerup = true;
            }
        }
        return List.copyOf(unlockablePowerups);
    }

    public static long getTotalPrice(List<PowerupInstance> powerupInstances)
    {
        long totalPrice = 0L;
        for (PowerupInstance powerupInstance : powerupInstances)
        {
            totalPrice += powerupInstance.getPrice();
        }
        return totalPrice;
    }
}
