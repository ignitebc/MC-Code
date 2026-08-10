package com.daqem.jobsplus.integration.arc.holder.holders.powerup;

import com.daqem.arc.api.action.data.ActionData;
import com.daqem.arc.api.action.holder.AbstractActionHolder;
import com.daqem.arc.api.action.holder.serializer.IActionHolderSerializer;
import com.daqem.arc.api.action.holder.type.IActionHolderType;
import com.daqem.arc.api.player.ArcPlayer;
import com.daqem.jobsplus.JobsPlus;
import com.daqem.jobsplus.integration.arc.holder.type.JobsPlusActionHolderType;
import com.daqem.jobsplus.player.JobsPlayer;
import com.daqem.jobsplus.player.job.Job;
import com.daqem.jobsplus.player.job.powerup.Powerup;
import com.daqem.jobsplus.player.job.powerup.PowerupState;
import com.daqem.jobsplus.player.job.powerup.PowerupType;
import com.google.gson.*;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Type;
import java.util.List;

public class PowerupInstance extends AbstractActionHolder
{

    private final Identifier jobLocation;
    private final @Nullable Identifier parentLocation;
    private final ItemStackTemplate iconTemplate;
    private final int price;
    private final int requiredLevel;
    private final PowerupType type;

    public PowerupInstance(Identifier location, Identifier jobLocation, @Nullable Identifier parentLocation, ItemStackTemplate iconTemplate, int price, int requiredLevel, PowerupType type)
    {
        super(location);
        this.jobLocation = jobLocation;
        this.parentLocation = parentLocation;
        this.iconTemplate = iconTemplate;
        this.price = price;
        this.requiredLevel = requiredLevel;
        this.type = type;
    }

    public MutableComponent getName()
    {
        return JobsPlus.translatable("powerup." + location.getNamespace() + "." + location.getPath().replace('/', '.') + ".name");
    }

    @Override
    public MutableComponent getDisplayName()
    {
        return getName();
    }

    public MutableComponent getDescription()
    {
        return JobsPlus.translatable("powerup." + location.getNamespace() + "." + location.getPath().replace('/', '.') + ".description");
    }

    public Identifier getJobLocation()
    {
        return jobLocation;
    }

    public @Nullable Identifier getParentLocation()
    {
        return parentLocation;
    }

    public ItemStack getIcon()
    {
        return iconTemplate.withCount(1).create();
    }

    public int getIconCount()
    {
        return iconTemplate.count();
    }

    public int getPrice()
    {
        return price;
    }

    public int getRequiredLevel()
    {
        return requiredLevel;
    }

    public PowerupType getPowerupType()
    {
        return this.type;
    }

    @Override
    public IActionHolderType<?> getType()
    {
        return JobsPlusActionHolderType.POWERUP_INSTANCE;
    }

    @Nullable
    public static PowerupInstance of(Identifier location)
    {
        return PowerupManager.getInstance().getAllPowerups().get(location);
    }

    @Override
    public boolean passedHolderCondition(ActionData actionData)
    {
        ArcPlayer arcPlayer = actionData.getPlayer();
        if (arcPlayer instanceof JobsPlayer jobsPlayer)
        {
            Job job = jobsPlayer.jobsplus$getJobs().stream().filter(job1 -> job1 != null && job1.getJobInstance() != null && job1.getJobInstance().getLocation().equals(this.getJobLocation())).findFirst().orElse(null);
            if (job != null)
            {
                Powerup powerup = job.getPowerupManager().getAllPowerups().stream().filter(powerup1 -> powerup1.getPowerupLocation().equals(this.getLocation())).findFirst().orElse(null);
                
                if (powerup != null)
                {
                    return powerup.getState() == PowerupState.ACTIVE;
                }
            }
        }
        return false;
    }

    public PowerupInstance getParent()
    {
        return parentLocation == null ? null : PowerupManager.getInstance().getAllPowerups().get(parentLocation);
    }

    public List<PowerupInstance> getChildren()
    {
        return PowerupManager.getInstance().getAllPowerups().values().stream().filter(powerupInstance -> powerupInstance.getParentLocation() != null && powerupInstance.getParentLocation().equals(this.getLocation())).toList();
    }

    public static class Serializer implements JsonDeserializer<PowerupInstance>, IActionHolderSerializer<PowerupInstance>
    {

        @Override
        public PowerupInstance deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
        {
            JsonObject jsonObject = json.getAsJsonObject();
            return fromJson(jsonObject, getResourceLocation(jsonObject, "location"));
        }

        @Override
        public PowerupInstance fromJson(JsonObject jsonObject, Identifier resourceLocation)
        {
            String parentLocation = GsonHelper.getAsString(jsonObject, "parent", null);
            return new PowerupInstance(resourceLocation, getResourceLocation(jsonObject, "job"), parentLocation == null ? null : Identifier.parse(parentLocation), getItemStackTemplate(GsonHelper.getAsJsonObject(jsonObject, "icon")), GsonHelper.getAsInt(jsonObject, "price"), GsonHelper.getAsInt(jsonObject, "required_level"), PowerupType.valueOf(GsonHelper.getAsString(jsonObject, "type", "basic").toUpperCase()));
        }

        @Override
        public PowerupInstance fromNetwork(RegistryFriendlyByteBuf friendlyByteBuf, Identifier resourceLocation)
        {
            return new PowerupInstance(friendlyByteBuf.readIdentifier(), friendlyByteBuf.readIdentifier(), friendlyByteBuf.readBoolean() ? friendlyByteBuf.readIdentifier() : null, ItemStackTemplate.STREAM_CODEC.decode(friendlyByteBuf), friendlyByteBuf.readInt(), friendlyByteBuf.readInt(), friendlyByteBuf.readEnum(PowerupType.class));
        }

        @Override
        public void toNetwork(RegistryFriendlyByteBuf friendlyByteBuf, PowerupInstance powerupInstance)
        {
            friendlyByteBuf.writeIdentifier(powerupInstance.getLocation());
            friendlyByteBuf.writeIdentifier(powerupInstance.getJobLocation());
            friendlyByteBuf.writeBoolean(powerupInstance.getParentLocation() != null);
            if (powerupInstance.getParentLocation() != null)
            {
                friendlyByteBuf.writeIdentifier(powerupInstance.getParentLocation());
            }
            ItemStackTemplate.STREAM_CODEC.encode(friendlyByteBuf, powerupInstance.iconTemplate);
            friendlyByteBuf.writeInt(powerupInstance.getPrice());
            friendlyByteBuf.writeInt(powerupInstance.getRequiredLevel());
            friendlyByteBuf.writeEnum(powerupInstance.getPowerupType());
            IActionHolderSerializer.super.toNetwork(friendlyByteBuf, powerupInstance);
        }
    }
}
