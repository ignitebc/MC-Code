package com.daqem.arc.data.reward.player;

import com.daqem.arc.api.action.data.ActionData;
import com.daqem.arc.api.action.result.ActionResult;
import com.daqem.arc.api.reward.AbstractReward;
import com.daqem.arc.api.reward.serializer.IRewardSerializer;
import com.daqem.arc.api.reward.type.IRewardType;
import com.daqem.arc.api.reward.type.RewardType;
import com.google.gson.JsonObject;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;

public class MaxHealthAttributeModifierReward extends AbstractReward
{

    private final double amount;

    public MaxHealthAttributeModifierReward(double chance, int priority, double amount)
    {
        super(chance, priority);
        this.amount = amount;
    }

    public double getAmount()
    {
        return amount;
    }

    public static ResourceLocation computeModifierId(ResourceLocation holderId, ResourceLocation actionId)
    {
        String holderKey = sanitizeForPath(holderId.getNamespace() + "_" + holderId.getPath());
        String actionKey = sanitizeForPath(actionId.getNamespace() + "_" + actionId.getPath());
        return ResourceLocation.fromNamespaceAndPath("arc", "max_health/" + holderKey + "/" + actionKey);
    }

    private static String sanitizeForPath(String value)
    {
        return value.toLowerCase()
                .replace(':', '_')
                .replace('/', '_')
                .replace('\\', '_')
                .replace(' ', '_');
    }

    @Override
    public IRewardType<?> getType()
    {
        return RewardType.MAX_HEALTH_ATTRIBUTE_MODIFIER;
    }

    @Override
    public ActionResult apply(ActionData actionData)
    {
        return new ActionResult();
    }

    @Override
    public Component getName()
    {
        return Component.literal("최대 체력 보정");
    }

    @Override
    public Component getDescription(Object... args)
    {
        return Component.literal("최대 체력을 " + amount + " 증가시킵니다");
    }

    public static class Serializer implements IRewardSerializer<MaxHealthAttributeModifierReward>
    {

        @Override
        public MaxHealthAttributeModifierReward fromJson(JsonObject jsonObject, double chance, int priority)
        {
            return new MaxHealthAttributeModifierReward(
                    chance,
                    priority,
                    GsonHelper.getAsDouble(jsonObject, "amount"));
        }

        @Override
        public MaxHealthAttributeModifierReward fromNetwork(
                RegistryFriendlyByteBuf friendlyByteBuf,
                double chance,
                int priority)
        {
            return new MaxHealthAttributeModifierReward(chance, priority, friendlyByteBuf.readDouble());
        }

        @Override
        public void toNetwork(
                RegistryFriendlyByteBuf friendlyByteBuf,
                MaxHealthAttributeModifierReward type)
        {
            IRewardSerializer.super.toNetwork(friendlyByteBuf, type);
            friendlyByteBuf.writeDouble(type.amount);
        }
    }
}
