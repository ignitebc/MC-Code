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

    /** 계열을 거슬러 오를 때 선행 관계가 잘못 순환해도 멈추게 하는 상한 */
    private static final int MAX_LINE_DEPTH = 64;

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
            Powerup addedPowerup = new Powerup(powerupInstance, powerupState);
            powerups.add(addedPowerup);
            // 새 단계가 계열의 가장 높은 단계가 되므로 계열 전체를 같은 상태로 맞춘다.
            setLineState(addedPowerup, powerupState);
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
            Powerup addedPowerup = new Powerup(powerupInstance, PowerupState.ACTIVE);
            this.powerups.add(addedPowerup);
            setLineState(addedPowerup, PowerupState.ACTIVE);
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
        Powerup addedPowerup = new Powerup(powerupInstance, powerupState);
        powerups.add(addedPowerup);
        setLineState(addedPowerup, powerupState);
        this.sendJobUpdatePacket(job, player);
    }

    /**
     * 같은 계열(선행 관계로 이어진 I~X 단계) 가운데 보유한 스킬.
     * <p>
     * 각 단계는 상위 단계가 꺼져 있을 때만 효과가 나서 한 단계만 켜고 끄면 의미가 없으므로, 계열 단위로 상태를 맞춘다.
     */
    public List<Powerup> getOwnedLine(Powerup powerup)
    {
        Identifier rootLocation = lineRootLocation(powerup.getPowerupInstance());
        if (rootLocation == null)
        {
            return List.of(powerup);
        }

        List<Powerup> line = new ArrayList<>();
        for (Powerup ownedPowerup : this.powerups)
        {
            if (rootLocation.equals(lineRootLocation(ownedPowerup.getPowerupInstance())))
            {
                line.add(ownedPowerup);
            }
        }
        return line;
    }

    /** 같은 계열의 보유 스킬을 모두 같은 상태로 바꾸고, 실제로 바뀐 스킬만 돌려준다. */
    public List<Powerup> setLineState(Powerup powerup, PowerupState state)
    {
        List<Powerup> changedPowerups = new ArrayList<>();
        // 관리자 명령은 NOT_OWNED·LOCKED도 지정할 수 있다. 그 상태가 계열 전체로 번지면 보유 단계까지 잃으므로 ON/OFF만 맞춘다.
        if (!isOnOffState(state))
        {
            return changedPowerups;
        }

        for (Powerup linePowerup : getOwnedLine(powerup))
        {
            boolean isChangeable = isOnOffState(linePowerup.getState()) && linePowerup.getState() != state;
            if (isChangeable)
            {
                linePowerup.setState(state);
                changedPowerups.add(linePowerup);
            }
        }
        return changedPowerups;
    }

    /**
     * 계열 안 단계마다 다른 상태가 저장되어 있으면 가장 높은 단계의 상태로 맞춘다.
     * <p>
     * 효과는 가장 높은 단계가 정하므로, 그 단계를 끈 플레이어는 계열 전체를 끈 것으로 본다.
     */
    public void alignLineStatesToHighestTier()
    {
        Map<Identifier, Powerup> highestByLine = new HashMap<>();
        Map<Identifier, Integer> depthByLine = new HashMap<>();
        for (Powerup ownedPowerup : this.powerups)
        {
            PowerupInstance powerupInstance = ownedPowerup.getPowerupInstance();
            Identifier rootLocation = lineRootLocation(powerupInstance);
            boolean isAlignable = rootLocation != null && isOnOffState(ownedPowerup.getState());
            if (!isAlignable)
            {
                continue;
            }

            int depth = lineDepth(powerupInstance);
            Integer highestDepth = depthByLine.get(rootLocation);
            if (highestDepth == null || depth > highestDepth)
            {
                depthByLine.put(rootLocation, depth);
                highestByLine.put(rootLocation, ownedPowerup);
            }
        }

        for (Powerup highestPowerup : highestByLine.values())
        {
            setLineState(highestPowerup, highestPowerup.getState());
        }
    }

    /** 구매한 뒤 켜고 끌 수 있는 상태인지 */
    private static boolean isOnOffState(PowerupState state)
    {
        return state == PowerupState.ACTIVE || state == PowerupState.INACTIVE;
    }

    /** 선행 관계를 따라 올라간 계열 첫 단계(I)의 ID. 스킬 정의를 찾지 못하면 null */
    private static @Nullable Identifier lineRootLocation(@Nullable PowerupInstance powerupInstance)
    {
        if (powerupInstance == null)
        {
            return null;
        }

        PowerupInstance current = powerupInstance;
        for (int depth = 0; depth < MAX_LINE_DEPTH; depth++)
        {
            PowerupInstance parent = current.getParent();
            if (parent == null)
            {
                break;
            }
            current = parent;
        }
        return current.getLocation();
    }

    /** 계열 첫 단계에서 몇 단계 아래인지. I은 0이다. */
    private static int lineDepth(PowerupInstance powerupInstance)
    {
        int depth = 0;
        PowerupInstance current = powerupInstance;
        while (depth < MAX_LINE_DEPTH && current.getParent() != null)
        {
            current = current.getParent();
            depth++;
        }
        return depth;
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
