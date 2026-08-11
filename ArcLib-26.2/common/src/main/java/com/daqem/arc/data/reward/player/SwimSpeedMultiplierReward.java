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
 * 물속 이동 속도를 배율로 올린다. 1.1이면 10% 빨라진다.
 * <p>
 * 헤엄칠 때와 물 바닥을 걸을 때 모두 적용되며, 수중 이동 효율(심층 잠수부)과는 별도로
 * 최종 가속에 곱해진다. 이동은 클라이언트가 계산하므로 실제 반영은
 * {@link com.daqem.arc.player.SwimSpeedMultiplierResolver}가 양쪽에서 같은 방식으로 처리한다.
 */
public class SwimSpeedMultiplierReward extends AbstractReward
{

    private final float multiplier;

    public SwimSpeedMultiplierReward(double chance, int priority, float multiplier)
    {
        super(chance, priority);
        this.multiplier = multiplier;
    }

    public float getMultiplier()
    {
        return multiplier;
    }

    @Override
    public IRewardType<?> getType()
    {
        return RewardType.SWIM_SPEED_MULTIPLIER;
    }

    @Override
    public ActionResult apply(ActionData actionData)
    {
        return new ActionResult();
    }

    @Override
    public Component getName()
    {
        return Component.literal("물속 이동 속도 배율");
    }

    @Override
    public Component getDescription(Object... args)
    {
        return Component.literal("물속 이동 속도가 " + Math.round((multiplier - 1.0F) * 100) + "% 빨라집니다");
    }

    public static class Serializer implements IRewardSerializer<SwimSpeedMultiplierReward>
    {

        @Override
        public SwimSpeedMultiplierReward fromJson(JsonObject jsonObject, double chance, int priority)
        {
            return new SwimSpeedMultiplierReward(
                    chance,
                    priority,
                    GsonHelper.getAsFloat(jsonObject, "multiplier"));
        }

        @Override
        public SwimSpeedMultiplierReward fromNetwork(RegistryFriendlyByteBuf friendlyByteBuf, double chance, int priority)
        {
            return new SwimSpeedMultiplierReward(chance, priority, friendlyByteBuf.readFloat());
        }

        @Override
        public void toNetwork(RegistryFriendlyByteBuf friendlyByteBuf, SwimSpeedMultiplierReward type)
        {
            IRewardSerializer.super.toNetwork(friendlyByteBuf, type);
            friendlyByteBuf.writeFloat(type.multiplier);
        }
    }
}
