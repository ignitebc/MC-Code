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
import net.minecraft.util.GsonHelper;

/**
 * JSON multiplier는 % 값(0.5 ~ 5)을 받는다. 내부에서는 factor(1.005 ~ 1.05)로 변환해서
 * ActionResult에 전달한다.
 */
public class WalkSpeedMultiplierReward extends AbstractReward
{

    private final float multiplier; // factor (ex: 1.05)

    public WalkSpeedMultiplierReward(double chance, int priority, float multiplier)
    {
        super(chance, priority);
        this.multiplier = multiplier;
    }

    @Override
    public Component getDescription()
    {
        float percent = (multiplier - 1.0F) * 100.0F;
        return Component.literal("이동 속도 +" + percent + "%");
    }

    @Override
    public ActionResult apply(ActionData actionData)
    {
        return new ActionResult().withWalkSpeedModifier(multiplier);
    }

    @Override
    public IRewardType<?> getType()
    {
        return RewardType.WALK_SPEED_MULTIPLIER;
    }

    public float getMultiplier()
    {
        return multiplier;
    }

    public static class Serializer implements IRewardSerializer<WalkSpeedMultiplierReward>
    {

        @Override
        public WalkSpeedMultiplierReward fromJson(JsonObject jsonObject, double chance, int priority)
        {
            // JSON: 0.5 ~ 5 (percent)
            float percent = GsonHelper.getAsFloat(jsonObject, "multiplier");
            float factor = 1.0F + (percent / 100.0F);
            return new WalkSpeedMultiplierReward(chance, priority, factor);
        }

        @Override
        public WalkSpeedMultiplierReward fromNetwork(RegistryFriendlyByteBuf friendlyByteBuf, double chance, int priority)
        {
            return new WalkSpeedMultiplierReward(chance, priority, friendlyByteBuf.readFloat());
        }

        @Override
        public void toNetwork(RegistryFriendlyByteBuf friendlyByteBuf, WalkSpeedMultiplierReward type)
        {
            IRewardSerializer.super.toNetwork(friendlyByteBuf, type);
            friendlyByteBuf.writeFloat(type.multiplier);
        }
    }
}
