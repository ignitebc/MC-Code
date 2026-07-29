package com.daqem.arc.data.action.player;

import com.daqem.arc.api.action.AbstractAction;
import com.daqem.arc.api.action.holder.type.IActionHolderType;
import com.daqem.arc.api.action.serializer.IActionSerializer;
import com.daqem.arc.api.action.type.ActionType;
import com.daqem.arc.api.action.type.IActionType;
import com.daqem.arc.api.condition.ICondition;
import com.daqem.arc.api.reward.IReward;
import com.google.gson.JsonObject;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.Identifier;

import java.util.List;

public class GetAttackSpeedAction extends AbstractAction {

    public GetAttackSpeedAction(Identifier location, Identifier actionHolderLocation, IActionHolderType<?> actionHolderType, boolean performOnClient, List<IReward> rewards, List<ICondition> conditions) {
        super(location, actionHolderLocation, actionHolderType, performOnClient, rewards, conditions);
    }

    @Override
    public IActionType<?> getType() {
        return ActionType.GET_ATTACK_SPEED;
    }

    public static class Serializer implements IActionSerializer<GetAttackSpeedAction> {

        @Override
        public GetAttackSpeedAction fromJson(Identifier location, JsonObject jsonObject, Identifier actionHolderLocation, IActionHolderType<?> actionHolderType, boolean performOnClient, List<IReward> rewards, List<ICondition> conditions) {
            return new GetAttackSpeedAction(location, actionHolderLocation, actionHolderType, true, rewards, conditions);
        }

        @Override
        public GetAttackSpeedAction fromNetwork(Identifier location, RegistryFriendlyByteBuf friendlyByteBuf, Identifier actionHolderLocation, IActionHolderType<?> actionHolderType, boolean performOnClient, List<IReward> rewards, List<ICondition> conditions) {
            return new GetAttackSpeedAction(location, actionHolderLocation, actionHolderType, true, rewards, conditions);
        }

        @Override
        public void toNetwork(RegistryFriendlyByteBuf friendlyByteBuf, GetAttackSpeedAction type) {
            IActionSerializer.super.toNetwork(friendlyByteBuf, type);
        }
    }
}
