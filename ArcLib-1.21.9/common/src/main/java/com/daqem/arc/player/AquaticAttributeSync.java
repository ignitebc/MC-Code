package com.daqem.arc.player;

import com.daqem.arc.api.action.data.ActionData;
import com.daqem.arc.api.action.data.ActionDataBuilder;
import com.daqem.arc.api.action.holder.IActionHolder;
import com.daqem.arc.api.action.type.ActionType;
import com.daqem.arc.api.player.ArcPlayer;
import com.daqem.arc.api.reward.IReward;
import com.daqem.arc.data.reward.player.OxygenBonusAttributeModifierReward;
import com.daqem.arc.data.reward.player.WaterMovementEfficiencyAttributeModifierReward;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

import java.util.ArrayList;
import java.util.List;

/**
 * 수중 관련 속성(산소 보너스, 수중 이동 효율)을 매 틱 동기화한다.
 * <p>
 * {@link DefensiveAttributeSync}와 같은 방식으로, 조건을 만족하는 보상 중 가장 높은 값 하나만
 * 적용한다. 스킬 단계를 여러 개 보유해도 최상위 단계만 적용되도록 하기 위함이다.
 */
public final class AquaticAttributeSync
{

    private AquaticAttributeSync()
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

        ActionData actionData = new ActionDataBuilder(arcPlayer, ActionType.SWIM).build();
        AttributeRewardSelection oxygenBonus = new AttributeRewardSelection();
        AttributeRewardSelection waterMovementEfficiency = new AttributeRewardSelection();

        for (IActionHolder holder : arcPlayer.arc$getActionHolders())
        {
            if (holder == null)
            {
                continue;
            }

            holder.getActions().forEach(action -> {
                for (IReward reward : action.getRewards())
                {
                    if (reward instanceof OxygenBonusAttributeModifierReward oxygenReward
                            && action.metConditions(actionData)
                            && oxygenReward.getBonus() > oxygenBonus.value)
                    {
                        oxygenBonus.id = OxygenBonusAttributeModifierReward.computeModifierId(
                                holder.getLocation(),
                                action.getLocation());
                        oxygenBonus.value = oxygenReward.getBonus();
                    }
                    else if (reward instanceof WaterMovementEfficiencyAttributeModifierReward efficiencyReward
                            && action.metConditions(actionData)
                            && efficiencyReward.getEfficiency() > waterMovementEfficiency.value)
                    {
                        waterMovementEfficiency.id =
                                WaterMovementEfficiencyAttributeModifierReward.computeModifierId(
                                        holder.getLocation(),
                                        action.getLocation());
                        waterMovementEfficiency.value = efficiencyReward.getEfficiency();
                    }
                }
            });
        }

        syncAttribute(serverPlayer, Attributes.OXYGEN_BONUS, "oxygen_bonus/", oxygenBonus);
        syncAttribute(serverPlayer, Attributes.WATER_MOVEMENT_EFFICIENCY, "water_movement_efficiency/",
                waterMovementEfficiency);
    }

    private static void syncAttribute(
            ServerPlayer serverPlayer,
            net.minecraft.core.Holder<net.minecraft.world.entity.ai.attributes.Attribute> attribute,
            String pathPrefix,
            AttributeRewardSelection selection)
    {
        AttributeInstance instance = serverPlayer.getAttribute(attribute);
        if (instance == null)
        {
            return;
        }

        List<ResourceLocation> toRemove = new ArrayList<>();
        for (AttributeModifier existing : instance.getModifiers())
        {
            ResourceLocation id = existing.id();
            if (id != null
                    && id.getNamespace().equals("arc")
                    && id.getPath().startsWith(pathPrefix)
                    && !id.equals(selection.id))
            {
                toRemove.add(id);
            }
        }
        for (ResourceLocation id : toRemove)
        {
            instance.removeModifier(id);
        }

        if (selection.id == null)
        {
            return;
        }

        AttributeModifier current = instance.getModifier(selection.id);
        if (current == null)
        {
            instance.addPermanentModifier(
                    new AttributeModifier(selection.id, selection.value, AttributeModifier.Operation.ADD_VALUE));
        }
        else if (current.operation() != AttributeModifier.Operation.ADD_VALUE
                || Double.compare(current.amount(), selection.value) != 0)
        {
            instance.removeModifier(selection.id);
            instance.addPermanentModifier(
                    new AttributeModifier(selection.id, selection.value, AttributeModifier.Operation.ADD_VALUE));
        }
    }

    private static class AttributeRewardSelection
    {

        private ResourceLocation id;
        private double value;
    }
}
