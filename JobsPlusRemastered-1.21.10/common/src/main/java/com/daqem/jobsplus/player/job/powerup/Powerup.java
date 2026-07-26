package com.daqem.jobsplus.player.job.powerup;

import com.daqem.jobsplus.integration.arc.holder.holders.powerup.PowerupInstance;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

public class Powerup
{

    public static final Codec<Powerup> CODEC = RecordCodecBuilder.create(instance -> instance.group(ResourceLocation.CODEC.fieldOf("powerup").forGetter(Powerup::getPowerupLocation), PowerupState.CODEC.fieldOf("state").forGetter(Powerup::getState)

    ).apply(instance, Powerup::new));

    private final ResourceLocation powerupLocation;
    private @Nullable PowerupInstance powerupInstance;
    private PowerupState powerupState;

    public Powerup(ResourceLocation powerupLocation, PowerupState powerupState)
    {
        this.powerupLocation = powerupLocation;
        this.powerupInstance = PowerupInstance.of(powerupLocation);
        this.powerupState = powerupState;
    }

    public Powerup(PowerupInstance powerupInstance, PowerupState powerupState)
    {
        this.powerupLocation = powerupInstance.getLocation();
        this.powerupInstance = powerupInstance;
        this.powerupState = powerupState;
    }

    public ResourceLocation getPowerupLocation()
    {
        return powerupLocation;
    }

    public @Nullable PowerupInstance getPowerupInstance()
    {
        if (powerupInstance == null)
        {
            powerupInstance = PowerupInstance.of(powerupLocation);
        }
        return powerupInstance;
    }

    public PowerupState getState()
    {
        return powerupState;
    }

    public void setState(PowerupState powerupState)
    {
        this.powerupState = powerupState;
    }

    public void toggle()
    {
        if (powerupState == PowerupState.ACTIVE)
        {
            powerupState = PowerupState.INACTIVE;
        } else if (powerupState == PowerupState.INACTIVE)
        {
            powerupState = PowerupState.ACTIVE;
        }
    }
}
