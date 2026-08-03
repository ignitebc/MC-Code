package com.daqem.arc.player;

import com.daqem.arc.api.action.data.ActionData;
import com.daqem.arc.api.action.data.ActionDataBuilder;
import com.daqem.arc.api.action.holder.IActionHolder;
import com.daqem.arc.api.action.type.ActionType;
import com.daqem.arc.api.player.ArcPlayer;
import com.daqem.arc.api.reward.IReward;
import com.daqem.arc.data.reward.player.OxygenBonusAttributeModifierReward;
import com.daqem.arc.data.reward.player.WaterMovementEfficiencyAttributeModifierReward;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 수중 관련 속성(산소 보너스, 수중 이동 효율)을 매 틱 동기화한다.
 * <p>
 * {@link MovementSpeedAttributeSync}와 같은 방식으로, 조건을 만족하는 보상을 모두 개별
 * 모디파이어로 등록해 합연산으로 더한다. 같은 스킬 줄에서 하위 단계가 중복으로 더해지는 것은
 * 스킬 데이터의 상위 단계 비활성 조건이 막아준다.
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
        Map<Identifier, Double> oxygenBonuses = new HashMap<>();
        Map<Identifier, Double> waterMovementEfficiencies = new HashMap<>();

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
                            && action.metConditions(actionData))
                    {
                        Identifier id = OxygenBonusAttributeModifierReward.computeModifierId(
                                holder.getLocation(),
                                action.getLocation());
                        oxygenBonuses.put(id, oxygenReward.getBonus());
                    }
                    else if (reward instanceof WaterMovementEfficiencyAttributeModifierReward efficiencyReward
                            && action.metConditions(actionData))
                    {
                        Identifier id = WaterMovementEfficiencyAttributeModifierReward.computeModifierId(
                                holder.getLocation(),
                                action.getLocation());
                        waterMovementEfficiencies.put(id, efficiencyReward.getEfficiency());
                    }
                }
            });
        }

        syncAttribute(serverPlayer, Attributes.OXYGEN_BONUS, "oxygen_bonus/", oxygenBonuses);
        syncAttribute(serverPlayer, Attributes.WATER_MOVEMENT_EFFICIENCY, "water_movement_efficiency/",
                waterMovementEfficiencies);
    }

    private static void syncAttribute(
            ServerPlayer serverPlayer,
            net.minecraft.core.Holder<net.minecraft.world.entity.ai.attributes.Attribute> attribute,
            String pathPrefix,
            Map<Identifier, Double> desiredModifiers)
    {
        AttributeInstance instance = serverPlayer.getAttribute(attribute);
        if (instance == null)
        {
            return;
        }

        List<Identifier> toRemove = new ArrayList<>();
        for (AttributeModifier existing : instance.getModifiers())
        {
            Identifier id = existing.id();
            if (id != null
                    && id.getNamespace().equals("arc")
                    && id.getPath().startsWith(pathPrefix)
                    && !desiredModifiers.containsKey(id))
            {
                toRemove.add(id);
            }
        }
        for (Identifier id : toRemove)
        {
            instance.removeModifier(id);
        }

        for (Map.Entry<Identifier, Double> entry : desiredModifiers.entrySet())
        {
            Identifier id = entry.getKey();
            double amount = entry.getValue();

            AttributeModifier current = instance.getModifier(id);
            if (current == null)
            {
                instance.addPermanentModifier(
                        new AttributeModifier(id, amount, AttributeModifier.Operation.ADD_VALUE));
            }
            else if (current.operation() != AttributeModifier.Operation.ADD_VALUE
                    || Double.compare(current.amount(), amount) != 0)
            {
                instance.removeModifier(id);
                instance.addPermanentModifier(
                        new AttributeModifier(id, amount, AttributeModifier.Operation.ADD_VALUE));
            }
        }
    }
}
