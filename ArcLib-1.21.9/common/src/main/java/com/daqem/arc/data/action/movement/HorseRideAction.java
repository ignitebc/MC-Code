package com.daqem.arc.data.action.movement;
import com.daqem.arc.api.action.AbstractAction;
import com.daqem.arc.api.action.holder.type.IActionHolderType;
import com.daqem.arc.api.action.serializer.IActionSerializer;
import com.daqem.arc.api.action.type.ActionType;
import com.daqem.arc.api.action.type.IActionType;
import com.daqem.arc.api.condition.ICondition;
import com.daqem.arc.api.reward.IReward;
import com.google.gson.JsonObject;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import java.util.List;

public class HorseRideAction extends AbstractAction {

    public HorseRideAction(ResourceLocation location, ResourceLocation actionHolderLocation, IActionHolderType<?> actionHolderType, boolean performOnClient, List<IReward> rewards, List<ICondition> conditions) {
        super(location, actionHolderLocation, actionHolderType, performOnClient, rewards, conditions);
    }

    @Override
    public IActionType<?> getType() {
        return ActionType.HORSE_RIDE;
    }

    public static class Serializer implements IActionSerializer<HorseRideAction> {

        @Override
        public HorseRideAction fromJson(ResourceLocation location, JsonObject jsonObject, ResourceLocation actionHolderLocation, IActionHolderType<?> actionHolderType, boolean performOnClient, List<IReward> rewards, List<ICondition> conditions) {
            return new HorseRideAction(location, actionHolderLocation, actionHolderType, performOnClient, rewards, conditions);
        }

        @Override
        public HorseRideAction fromNetwork(ResourceLocation location, RegistryFriendlyByteBuf friendlyByteBuf, ResourceLocation actionHolderLocation, IActionHolderType<?> actionHolderType, boolean performOnClient, List<IReward> rewards, List<ICondition> conditions) {
            return new HorseRideAction(location, actionHolderLocation, actionHolderType, performOnClient, rewards, conditions);
        }

        @Override
        public void toNetwork(RegistryFriendlyByteBuf friendlyByteBuf, HorseRideAction type) {
            IActionSerializer.super.toNetwork(friendlyByteBuf, type);
        }
    }
}