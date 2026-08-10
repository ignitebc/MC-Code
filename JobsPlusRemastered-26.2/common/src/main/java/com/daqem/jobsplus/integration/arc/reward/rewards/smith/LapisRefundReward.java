package com.daqem.jobsplus.integration.arc.reward.rewards.smith;

import com.daqem.arc.api.action.data.ActionData;
import com.daqem.arc.api.action.data.type.ActionDataType;
import com.daqem.arc.api.action.result.ActionResult;
import com.daqem.arc.api.player.ArcPlayer;
import com.daqem.arc.api.reward.AbstractReward;
import com.daqem.arc.api.reward.serializer.IRewardSerializer;
import com.daqem.arc.api.reward.type.IRewardType;
import com.daqem.arc.player.SkillActivationNotifier;
import com.daqem.jobsplus.integration.arc.reward.type.JobsPlusRewardType;
import com.daqem.jobsplus.player.PlayerItemDelivery;
import com.google.gson.JsonObject;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * 마법 부여에 실제로 소모한 청금석을 그대로 돌려주는 보상.
 *
 * on_enchant_item 액션의 EXP_LEVEL 데이터는 마법 부여대에서 소모한
 * 청금석 개수와 항상 같기 때문에(1~3단계 선택지 = 청금석 1~3개),
 * 반환 개수를 JSON에 고정하지 않고 이 값을 그대로 사용한다.
 */
public class LapisRefundReward extends AbstractReward
{

    public LapisRefundReward(double chance, int priority)
    {
        super(chance, priority);
    }

    @Override
    public IRewardType<?> getType()
    {
        return JobsPlusRewardType.LAPIS_REFUND;
    }

    @Override
    public ActionResult apply(ActionData actionData)
    {
        Integer usedLapisCount = actionData.getData(ActionDataType.EXP_LEVEL);
        if (usedLapisCount == null || usedLapisCount <= 0)
        {
            return new ActionResult();
        }

        ArcPlayer arcPlayer = actionData.getPlayer();
        if (arcPlayer.arc$getPlayer() instanceof ServerPlayer serverPlayer)
        {
            ItemStack refund = new ItemStack(Items.LAPIS_LAZULI, usedLapisCount);
            ItemStack notificationStack = refund.copy();
            PlayerItemDelivery.giveOrDrop(serverPlayer, refund);
            SkillActivationNotifier.notifyExtraDrop(actionData, notificationStack);
        }

        return new ActionResult();
    }

    public static class Serializer implements IRewardSerializer<LapisRefundReward>
    {

        @Override
        public LapisRefundReward fromJson(JsonObject jsonObject, double chance, int priority)
        {
            return new LapisRefundReward(chance, priority);
        }

        @Override
        public LapisRefundReward fromNetwork(RegistryFriendlyByteBuf friendlyByteBuf, double chance, int priority)
        {
            return new LapisRefundReward(chance, priority);
        }

        @Override
        public void toNetwork(RegistryFriendlyByteBuf friendlyByteBuf, LapisRefundReward type)
        {
            IRewardSerializer.super.toNetwork(friendlyByteBuf, type);
        }
    }
}
