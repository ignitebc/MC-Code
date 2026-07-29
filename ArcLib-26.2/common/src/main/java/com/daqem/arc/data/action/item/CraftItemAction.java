package com.daqem.arc.data.action.item;

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

public class CraftItemAction extends AbstractAction {

    public CraftItemAction(Identifier location, Identifier actionHolderLocation, IActionHolderType<?> actionHolderType, boolean performOnClient, List<IReward> rewards, List<ICondition> conditions) {
        super(location, actionHolderLocation, actionHolderType, performOnClient, rewards, conditions);
    }

    @Override
    public IActionType<?> getType() {
        return ActionType.CRAFT_ITEM;
    }

    public static class Serializer implements IActionSerializer<CraftItemAction> {

        @Override
        public CraftItemAction fromJson(Identifier location, JsonObject jsonObject, Identifier actionHolderLocation, IActionHolderType<?> actionHolderType, boolean performOnClient, List<IReward> rewards, List<ICondition> conditions) {
            return new CraftItemAction(location, actionHolderLocation, actionHolderType, performOnClient, rewards, conditions);
        }

        @Override
        public CraftItemAction fromNetwork(Identifier location, RegistryFriendlyByteBuf friendlyByteBuf, Identifier actionHolderLocation, IActionHolderType<?> actionHolderType, boolean performOnClient, List<IReward> rewards, List<ICondition> conditions) {
            return new CraftItemAction(location, actionHolderLocation, actionHolderType, performOnClient, rewards, conditions);
        }

        @Override
        public void toNetwork(RegistryFriendlyByteBuf friendlyByteBuf, CraftItemAction type) {
            IActionSerializer.super.toNetwork(friendlyByteBuf, type);
        }
    }
}
