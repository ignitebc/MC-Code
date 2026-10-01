package com.daqem.jobsplus.player.job.powerup;

import com.daqem.jobsplus.player.JobsPlayer;
import com.daqem.jobsplus.player.JobsServerPlayer;
import com.daqem.jobsplus.player.job.Job;
import com.daqem.jobsplus.integration.arc.holder.holders.powerup.PowerupInstance;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class JobPowerupManager
{

    private final List<Powerup> powerups;

    public JobPowerupManager(@NotNull List<Powerup> powerups)
    {
        this.powerups = powerups;
    }

    public Optional<Powerup> getPowerup(PowerupInstance powerupInstance)
    {
        return powerups.stream().filter(powerup -> powerup.getPowerupLocation().equals(powerupInstance.getLocation())).findFirst();
    }

    public Optional<Powerup> getPowerup(Identifier powerupLocation)
    {
        return powerups.stream().filter(powerup -> powerup.getPowerupLocation().equals(powerupLocation)).findFirst();
    }

    public List<Powerup> getAllPowerups()
    {
        return powerups;
    }

    public boolean addPowerup(JobsPlayer player, Job job, PowerupInstance powerupInstance)
    {
        return addPowerup(player, job, powerupInstance, PowerupState.ACTIVE);
    }

    public boolean addPowerup(JobsPlayer player, Job job, PowerupInstance powerupInstance, PowerupState powerupState)
    {
        if (canAddPowerup(powerupInstance))
        {
            powerups.add(new Powerup(powerupInstance, powerupState));
            this.sendJobUpdatePacket(job, player);
            return true;
        }
        return false;
    }

    /** 검증을 모두 통과한 뒤 스킬을 한꺼번에 추가해 일부만 구매되는 상황을 막는다. */
    public boolean addPowerups(JobsPlayer player, Job job, List<PowerupInstance> powerupInstances)
    {
        if (powerupInstances == null || powerupInstances.isEmpty())
        {
            return false;
        }

        Set<Identifier> ownedOrValidatedLocations = new HashSet<>();
        for (Powerup powerup : this.powerups)
        {
            ownedOrValidatedLocations.add(powerup.getPowerupLocation());
        }

        for (PowerupInstance powerupInstance : powerupInstances)
        {
            if (powerupInstance == null
                    || ownedOrValidatedLocations.contains(powerupInstance.getLocation()))
            {
                return false;
            }

            Identifier parentLocation = powerupInstance.getParentLocation();
            if (parentLocation != null && !ownedOrValidatedLocations.contains(parentLocation))
            {
                return false;
            }
            ownedOrValidatedLocations.add(powerupInstance.getLocation());
        }

        for (PowerupInstance powerupInstance : powerupInstances)
        {
            this.powerups.add(new Powerup(powerupInstance, PowerupState.ACTIVE));
        }
        this.sendJobUpdatePacket(job, player);
        return true;
    }

    private void sendJobUpdatePacket(Job job, JobsPlayer player)
    {
        if (player instanceof JobsServerPlayer jobsServerPlayer)
        {
            jobsServerPlayer.jobsplus$updateJob(job);
        }
    }

    public boolean canAddPowerup(PowerupInstance powerupInstance)
    {
        if (powerups.stream().anyMatch(powerup -> powerup.getPowerupLocation().equals(powerupInstance.getLocation())))
            return false;
        if (powerupInstance.getParent() == null)
            return true;
        return getPowerup(powerupInstance.getParent()).isPresent();
    }

    public void forceAddPowerup(JobsPlayer player, Job job, PowerupInstance powerupInstance, PowerupState powerupState)
    {
        powerups.add(new Powerup(powerupInstance, powerupState));
        this.sendJobUpdatePacket(job, player);
    }

    public void clearPowerups()
    {
        powerups.clear();
    }

    public Optional<Powerup> getParent(PowerupInstance powerupInstance)
    {
        PowerupInstance parentPowerupInstance = powerupInstance.getParent();
        if (parentPowerupInstance == null)
            return Optional.empty();
        return getPowerup(parentPowerupInstance);
    }

    public Optional<Powerup> getParent(Powerup powerup)
    {
        PowerupInstance powerupInstance = powerup.getPowerupInstance();
        if (powerupInstance == null)
            return Optional.empty();
        PowerupInstance parentPowerupInstance = powerupInstance.getParent();
        if (parentPowerupInstance == null)
            return Optional.empty();
        return getPowerup(parentPowerupInstance);
    }

    public List<Powerup> getChildren(PowerupInstance powerupInstance)
    {
        return getChildren(powerupInstance, powerups);
    }

    public List<Powerup> getChildren(Powerup powerup)
    {
        return getChildren(powerup, powerups);
    }

    public static List<Powerup> getChildren(PowerupInstance powerupInstance, List<Powerup> powerups)
    {
        return powerups.stream().filter(powerup ->
        {
            PowerupInstance childPowerupInstance = powerup.getPowerupInstance();
            return childPowerupInstance != null
                    && childPowerupInstance.getParentLocation() != null
                    && childPowerupInstance.getParentLocation().equals(powerupInstance.getLocation());
        }).toList();
    }

    public static List<Powerup> getChildren(Powerup powerup, List<Powerup> powerups)
    {
        PowerupInstance powerupInstance = powerup.getPowerupInstance();
        return powerupInstance == null ? List.of() : getChildren(powerupInstance, powerups);
    }
}
