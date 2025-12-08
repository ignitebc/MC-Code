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
import net.minecraft.resources.ResourceLocation;

import java.util.List;

public class HurtItemAction extends AbstractAction {

    public HurtItemAction(ResourceLocation location, ResourceLocation actionHolderLocation, IActionHolderType<?> actionHolderType, boolean performOnClient, List<IReward> rewards, List<ICondition> conditions) {
        super(location, actionHolderLocation, actionHolderType, performOnClient, rewards, conditions);
    }

    @Override
    public IActionType<?> getType() {
        return ActionType.HURT_ITEM;
    }

    public static class Serializer implements IActionSerializer<HurtItemAction> {

        @Override
        public HurtItemAction fromJson(ResourceLocation location, JsonObject jsonObject, ResourceLocation actionHolderLocation, IActionHolderType<?> actionHolderType, boolean performOnClient, List<IReward> rewards, List<ICondition> conditions) {
            return new HurtItemAction(location, actionHolderLocation, actionHolderType, performOnClient, rewards, conditions);
        }

        @Override
        public HurtItemAction fromNetwork(ResourceLocation location, RegistryFriendlyByteBuf friendlyByteBuf, ResourceLocation actionHolderLocation, IActionHolderType<?> actionHolderType, boolean performOnClient, List<IReward> rewards, List<ICondition> conditions) {
            return new HurtItemAction(location, actionHolderLocation, actionHolderType, performOnClient, rewards, conditions);
        }

        @Override
        public void toNetwork(RegistryFriendlyByteBuf friendlyByteBuf, HurtItemAction type) {
            IActionSerializer.super.toNetwork(friendlyByteBuf, type);
        }
    }
}
