package com.daqem.arc.player;

import com.daqem.arc.api.action.data.ActionData;
import com.daqem.arc.api.action.data.ActionDataBuilder;
import com.daqem.arc.api.action.data.type.ActionDataType;
import com.daqem.arc.api.action.holder.IActionHolder;
import com.daqem.arc.api.action.type.ActionType;
import com.daqem.arc.api.player.ArcPlayer;
import com.daqem.arc.api.reward.IReward;
import com.daqem.arc.data.reward.player.EntityInteractionRangeAttributeModifierReward;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

import java.util.ArrayList;
import java.util.List;

public final class EntityInteractionRangeAttributeSync {

    private EntityInteractionRangeAttributeSync() {
    }

    public static void sync(ArcPlayer arcPlayer) {
        if (arcPlayer == null) return;
        if (!(arcPlayer.arc$getPlayer() instanceof ServerPlayer serverPlayer)) return;

        AttributeInstance instance = serverPlayer.getAttribute(Attributes.ENTITY_INTERACTION_RANGE);
        if (instance == null) return;

        ActionData actionData = new ActionDataBuilder(arcPlayer, ActionType.GET_DESTROY_SPEED)
                .withData(ActionDataType.ITEM_STACK, serverPlayer.getMainHandItem())
                .withData(ActionDataType.ITEM, serverPlayer.getMainHandItem().getItem())
                .build();
        ResourceLocation[] desiredId = new ResourceLocation[1];
        AttributeModifier[] desiredModifier = new AttributeModifier[1];
        double[] highestAmount = new double[]{0.0D};

        for (IActionHolder holder : arcPlayer.arc$getActionHolders()) {
            if (holder == null) continue;

            holder.getActions().forEach(action -> {
                for (IReward reward : action.getRewards()) {
                    if (!(reward instanceof EntityInteractionRangeAttributeModifierReward rangeReward)) continue;
                    if (!action.metConditions(actionData)) continue;
                    if (rangeReward.getAmount() > highestAmount[0]) {
                        ResourceLocation id = EntityInteractionRangeAttributeModifierReward.computeModifierId(holder.getLocation(), action.getLocation());
                        desiredId[0] = id;
                        desiredModifier[0] = new AttributeModifier(id, rangeReward.getAmount(), AttributeModifier.Operation.ADD_VALUE);
                        highestAmount[0] = rangeReward.getAmount();
                    }
                }
            });
        }

        List<ResourceLocation> toRemove = new ArrayList<>();
        for (AttributeModifier existing : instance.getModifiers()) {
            ResourceLocation id = existing.id();
            if (id != null && id.getNamespace().equals("arc") && id.getPath().startsWith("entity_interaction_range/") && !id.equals(desiredId[0])) {
                toRemove.add(id);
            }
        }
        for (ResourceLocation id : toRemove) {
            instance.removeModifier(id);
        }

        if (desiredId[0] != null && desiredModifier[0] != null) {
            ResourceLocation id = desiredId[0];
            AttributeModifier wanted = desiredModifier[0];
            AttributeModifier current = instance.getModifier(id);

            if (current == null) {
                instance.addPermanentModifier(wanted);
            } else if (current.operation() != wanted.operation() || Double.compare(current.amount(), wanted.amount()) != 0) {
                instance.removeModifier(id);
                instance.addPermanentModifier(wanted);
            }
        }
    }
}
