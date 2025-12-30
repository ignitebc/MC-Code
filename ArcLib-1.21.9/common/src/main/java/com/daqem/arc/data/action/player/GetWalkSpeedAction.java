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
import net.minecraft.resources.ResourceLocation;

import java.util.List;

/**
 * Fired when Minecraft queries the entity movement speed
 * (LivingEntity#getSpeed).
 */
public class GetWalkSpeedAction extends AbstractAction
{

    public GetWalkSpeedAction(ResourceLocation location, ResourceLocation actionHolderLocation, IActionHolderType<?> actionHolderType, boolean performOnClient, List<IReward> rewards, List<ICondition> conditions)
    {
        super(location, actionHolderLocation, actionHolderType, performOnClient, rewards, conditions);
    }

    @Override
    public IActionType<?> getType()
    {
        return ActionType.GET_WALK_SPEED;
    }

    public static class Serializer implements IActionSerializer<GetWalkSpeedAction>
    {

        @Override
        public GetWalkSpeedAction fromJson(ResourceLocation location, JsonObject jsonObject, ResourceLocation actionHolderLocation, IActionHolderType<?> actionHolderType, boolean performOnClient, List<IReward> rewards, List<ICondition> conditions)
        {
            return new GetWalkSpeedAction(location, actionHolderLocation, actionHolderType, true, rewards, conditions);
        }

        @Override
        public GetWalkSpeedAction fromNetwork(ResourceLocation location, RegistryFriendlyByteBuf friendlyByteBuf, ResourceLocation actionHolderLocation, IActionHolderType<?> actionHolderType, boolean performOnClient, List<IReward> rewards, List<ICondition> conditions)
        {
            return new GetWalkSpeedAction(location, actionHolderLocation, actionHolderType, true, rewards, conditions);
        }

        @Override
        public void toNetwork(RegistryFriendlyByteBuf friendlyByteBuf, GetWalkSpeedAction type)
        {
            IActionSerializer.super.toNetwork(friendlyByteBuf, type);
        }
    }
}
