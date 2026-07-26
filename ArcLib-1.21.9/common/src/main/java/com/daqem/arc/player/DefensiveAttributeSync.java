package com.daqem.arc.player;

import com.daqem.arc.api.action.data.ActionData;
import com.daqem.arc.api.action.data.ActionDataBuilder;
import com.daqem.arc.api.action.holder.IActionHolder;
import com.daqem.arc.api.action.type.ActionType;
import com.daqem.arc.api.player.ArcPlayer;
import com.daqem.arc.api.reward.IReward;
import com.daqem.arc.data.reward.player.MaxHealthAttributeModifierReward;
import com.daqem.arc.data.reward.player.SafeFallDistanceAttributeModifierReward;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

import java.util.ArrayList;
import java.util.List;

public final class DefensiveAttributeSync
{

    private DefensiveAttributeSync()
    {
    }

    public static void sync(ArcPlayer arcPlayer)
    {
        if (arcPlayer == null)
        {
            return;
        }
        if (!(arcPlayer.arc$getPlayer() instanceof ServerPlayer serverPlayer))
        {
            return;
        }

        ActionData actionData = new ActionDataBuilder(arcPlayer, ActionType.WALK).build();
        AttributeRewardSelection maxHealth = new AttributeRewardSelection();
        AttributeRewardSelection safeFallDistance = new AttributeRewardSelection();

        for (IActionHolder holder : arcPlayer.arc$getActionHolders())
        {
            if (holder == null)
            {
                continue;
            }

            holder.getActions().forEach(action -> {
                for (IReward reward : action.getRewards())
                {
                    if (reward instanceof MaxHealthAttributeModifierReward maxHealthReward
                            && action.metConditions(actionData)
                            && maxHealthReward.getAmount() > maxHealth.value)
                    {
                        maxHealth.id = MaxHealthAttributeModifierReward.computeModifierId(
                                holder.getLocation(),
                                action.getLocation());
                        maxHealth.value = maxHealthReward.getAmount();
                    }
                    else if (reward instanceof SafeFallDistanceAttributeModifierReward safeFallDistanceReward
                            && action.metConditions(actionData)
                            && safeFallDistanceReward.getDistance() > safeFallDistance.value)
                    {
                        safeFallDistance.id = SafeFallDistanceAttributeModifierReward.computeModifierId(
                                holder.getLocation(),
                                action.getLocation());
                        safeFallDistance.value = safeFallDistanceReward.getDistance();
                    }
                }
            });
        }

        syncMaxHealth(serverPlayer, maxHealth);
        syncSafeFallDistance(serverPlayer, safeFallDistance);
    }

    private static void syncMaxHealth(ServerPlayer serverPlayer, AttributeRewardSelection selection)
    {
        AttributeInstance instance = serverPlayer.getAttribute(Attributes.MAX_HEALTH);
        if (instance == null)
        {
            return;
        }

        float currentHealth = serverPlayer.getHealth();
        syncModifier(instance, "max_health/", selection.id, selection.value);
        if (serverPlayer.getHealth() < currentHealth)
        {
            serverPlayer.setHealth(Math.min(currentHealth, serverPlayer.getMaxHealth()));
        }
    }

    private static void syncSafeFallDistance(
            ServerPlayer serverPlayer,
            AttributeRewardSelection selection)
    {
        AttributeInstance instance = serverPlayer.getAttribute(Attributes.SAFE_FALL_DISTANCE);
        if (instance == null)
        {
            return;
        }

        double modifierAmount = selection.id == null
                ? 0.0D
                : selection.value - instance.getBaseValue();
        syncModifier(instance, "safe_fall_distance/", selection.id, modifierAmount);
    }

    private static void syncModifier(
            AttributeInstance instance,
            String pathPrefix,
            ResourceLocation desiredId,
            double amount)
    {
        List<ResourceLocation> toRemove = new ArrayList<>();
        for (AttributeModifier existing : instance.getModifiers())
        {
            ResourceLocation id = existing.id();
            if (id != null
                    && id.getNamespace().equals("arc")
                    && id.getPath().startsWith(pathPrefix)
                    && !id.equals(desiredId))
            {
                toRemove.add(id);
            }
        }
        for (ResourceLocation id : toRemove)
        {
            instance.removeModifier(id);
        }

        if (desiredId == null)
        {
            return;
        }

        AttributeModifier current = instance.getModifier(desiredId);
        if (current == null)
        {
            instance.addPermanentModifier(
                    new AttributeModifier(desiredId, amount, AttributeModifier.Operation.ADD_VALUE));
        }
        else if (current.operation() != AttributeModifier.Operation.ADD_VALUE
                || Double.compare(current.amount(), amount) != 0)
        {
            instance.removeModifier(desiredId);
            instance.addPermanentModifier(
                    new AttributeModifier(desiredId, amount, AttributeModifier.Operation.ADD_VALUE));
        }
    }

    private static class AttributeRewardSelection
    {

        private ResourceLocation id;
        private double value;
    }
}
