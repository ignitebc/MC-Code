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
 * 낚시에서 찌를 던진 뒤 물고기가 접근하기 시작할 때까지의 대기시간에 곱하는 배율. 0.95면 5% 감소한다.
 * <p>
 * 바닐라가 미끼 마법 보정까지 끝낸 대기시간에 곱해지므로 미끼와는 곱연산으로 동작한다.
 * 접근 시간과 입질 반응 시간은 건드리지 않는다. 낚시 타이머는 서버에서만 계산되며,
 * 실제 반영은 FishingHook 믹스인이 처리하고 스킬 간 중첩 규칙은
 * {@link com.daqem.arc.player.FishingWaitTimeMultiplierResolver}가 담당한다.
 */
public class FishingWaitTimeMultiplierReward extends AbstractReward
{

    private final float multiplier;

    public FishingWaitTimeMultiplierReward(double chance, int priority, float multiplier)
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
        return RewardType.FISHING_WAIT_TIME_MULTIPLIER;
    }

    @Override
    public ActionResult apply(ActionData actionData)
    {
        return new ActionResult();
    }

    @Override
    public Component getName()
    {
        return Component.literal("낚시 대기시간 배율");
    }

    @Override
    public Component getDescription(Object... args)
    {
        return Component.literal("입질 대기시간이 " + Math.round((1.0F - multiplier) * 100) + "% 감소합니다");
    }

    public static class Serializer implements IRewardSerializer<FishingWaitTimeMultiplierReward>
    {

        @Override
        public FishingWaitTimeMultiplierReward fromJson(JsonObject jsonObject, double chance, int priority)
        {
            return new FishingWaitTimeMultiplierReward(
                    chance,
                    priority,
                    GsonHelper.getAsFloat(jsonObject, "multiplier"));
        }

        @Override
        public FishingWaitTimeMultiplierReward fromNetwork(RegistryFriendlyByteBuf friendlyByteBuf, double chance, int priority)
        {
            return new FishingWaitTimeMultiplierReward(chance, priority, friendlyByteBuf.readFloat());
        }

        @Override
        public void toNetwork(RegistryFriendlyByteBuf friendlyByteBuf, FishingWaitTimeMultiplierReward type)
        {
            IRewardSerializer.super.toNetwork(friendlyByteBuf, type);
            friendlyByteBuf.writeFloat(type.multiplier);
        }
    }
}
