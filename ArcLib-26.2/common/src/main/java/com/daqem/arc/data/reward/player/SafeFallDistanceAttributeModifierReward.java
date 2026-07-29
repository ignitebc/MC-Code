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
import net.minecraft.resources.Identifier;
import net.minecraft.util.GsonHelper;

public class SafeFallDistanceAttributeModifierReward extends AbstractReward
{

    private final double distance;

    public SafeFallDistanceAttributeModifierReward(double chance, int priority, double distance)
    {
        super(chance, priority);
        this.distance = distance;
    }

    public double getDistance()
    {
        return distance;
    }

    public static Identifier computeModifierId(Identifier holderId, Identifier actionId)
    {
        String holderKey = sanitizeForPath(holderId.getNamespace() + "_" + holderId.getPath());
        String actionKey = sanitizeForPath(actionId.getNamespace() + "_" + actionId.getPath());
        return Identifier.fromNamespaceAndPath("arc", "safe_fall_distance/" + holderKey + "/" + actionKey);
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
        return RewardType.SAFE_FALL_DISTANCE_ATTRIBUTE_MODIFIER;
    }

    @Override
    public ActionResult apply(ActionData actionData)
    {
        return new ActionResult();
    }

    @Override
    public Component getName()
    {
        return Component.literal("안전 낙하 거리 보정");
    }

    @Override
    public Component getDescription(Object... args)
    {
        return Component.literal("안전 낙하 거리를 " + distance + "(으)로 설정합니다");
    }

    public static class Serializer implements IRewardSerializer<SafeFallDistanceAttributeModifierReward>
    {

        @Override
        public SafeFallDistanceAttributeModifierReward fromJson(
                JsonObject jsonObject,
                double chance,
                int priority)
        {
            return new SafeFallDistanceAttributeModifierReward(
                    chance,
                    priority,
                    GsonHelper.getAsDouble(jsonObject, "distance"));
        }

        @Override
        public SafeFallDistanceAttributeModifierReward fromNetwork(
                RegistryFriendlyByteBuf friendlyByteBuf,
                double chance,
                int priority)
        {
            return new SafeFallDistanceAttributeModifierReward(
                    chance,
                    priority,
                    friendlyByteBuf.readDouble());
        }

        @Override
        public void toNetwork(
                RegistryFriendlyByteBuf friendlyByteBuf,
                SafeFallDistanceAttributeModifierReward type)
        {
            IRewardSerializer.super.toNetwork(friendlyByteBuf, type);
            friendlyByteBuf.writeDouble(type.distance);
        }
    }
}
