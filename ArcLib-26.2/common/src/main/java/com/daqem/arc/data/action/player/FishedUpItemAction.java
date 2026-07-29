package com.daqem.arc.data.action.player;

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

public class FishedUpItemAction extends AbstractAction {

    public FishedUpItemAction(Identifier location, Identifier actionHolderLocation, IActionHolderType<?> actionHolderType, boolean performOnClient, List<IReward> rewards, List<ICondition> conditions) {
        super(location, actionHolderLocation, actionHolderType, performOnClient, rewards, conditions);
    }

    @Override
    public IActionType<?> getType() {
        return ActionType.FISHED_UP_ITEM;
    }

    public static class Serializer implements IActionSerializer<FishedUpItemAction> {

        @Override
        public FishedUpItemAction fromJson(Identifier location, JsonObject jsonObject, Identifier actionHolderLocation, IActionHolderType<?> actionHolderType, boolean performOnClient, List<IReward> rewards, List<ICondition> conditions) {
            return new FishedUpItemAction(location, actionHolderLocation, actionHolderType, performOnClient, rewards, conditions);
        }

        @Override
        public FishedUpItemAction fromNetwork(Identifier location, RegistryFriendlyByteBuf friendlyByteBuf, Identifier actionHolderLocation, IActionHolderType<?> actionHolderType, boolean performOnClient, List<IReward> rewards, List<ICondition> conditions) {
            return new FishedUpItemAction(location, actionHolderLocation, actionHolderType, performOnClient, rewards, conditions);
        }

        @Override
        public void toNetwork(RegistryFriendlyByteBuf friendlyByteBuf, FishedUpItemAction type) {
            IActionSerializer.super.toNetwork(friendlyByteBuf, type);
        }
    }
}
