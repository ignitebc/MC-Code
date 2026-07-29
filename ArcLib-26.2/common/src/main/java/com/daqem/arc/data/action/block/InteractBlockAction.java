package com.daqem.arc.data.action.block;

import com.daqem.arc.api.action.AbstractAction;
import com.daqem.arc.api.action.holder.type.IActionHolderType;
import com.daqem.arc.api.action.serializer.IActionSerializer;
import com.daqem.arc.api.action.type.ActionType;
import com.daqem.arc.api.action.type.IActionType;
import com.daqem.arc.api.condition.ICondition;
import com.daqem.arc.api.reward.IReward;
import com.google.gson.*;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.Identifier;

import java.util.List;

public class InteractBlockAction extends AbstractAction {

    public InteractBlockAction(Identifier location, Identifier actionHolderLocation, IActionHolderType<?> actionHolderType, boolean performOnClient, List<IReward> rewards, List<ICondition> conditions) {
        super(location, actionHolderLocation, actionHolderType, performOnClient, rewards, conditions);
    }

    @Override
    public IActionType<?> getType() {
        return ActionType.INTERACT_BLOCK;
    }

    public static class Serializer implements IActionSerializer<InteractBlockAction> {

        @Override
        public InteractBlockAction fromJson(Identifier location, JsonObject jsonObject, Identifier actionHolderLocation, IActionHolderType<?> actionHolderType, boolean performOnClient, List<IReward> rewards, List<ICondition> conditions) {
            return new InteractBlockAction(location, actionHolderLocation, actionHolderType, performOnClient, rewards, conditions);
        }

        @Override
        public InteractBlockAction fromNetwork(Identifier location, RegistryFriendlyByteBuf friendlyByteBuf, Identifier actionHolderLocation, IActionHolderType<?> actionHolderType, boolean performOnClient, List<IReward> rewards, List<ICondition> conditions) {
            return new InteractBlockAction(location, actionHolderLocation, actionHolderType, performOnClient, rewards, conditions);
        }

        @Override
        public void toNetwork(RegistryFriendlyByteBuf friendlyByteBuf, InteractBlockAction type) {
            IActionSerializer.super.toNetwork(friendlyByteBuf, type);
        }
    }
}
