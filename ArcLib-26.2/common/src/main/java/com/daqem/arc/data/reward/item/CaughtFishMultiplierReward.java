package com.daqem.arc.data.reward.item;

import com.daqem.arc.api.action.data.ActionData;
import com.daqem.arc.api.action.data.type.ActionDataType;
import com.daqem.arc.api.action.result.ActionResult;
import com.daqem.arc.api.reward.AbstractReward;
import com.daqem.arc.api.reward.serializer.IRewardSerializer;
import com.daqem.arc.api.reward.type.IRewardType;
import com.daqem.arc.api.reward.type.RewardType;
import com.daqem.arc.player.SkillActivationNotifier;
import com.google.gson.JsonObject;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;

/**
 * 낚아 올린 물고기의 수량을 배율만큼 늘린다. 3이면 최종 3배가 된다.
 * <p>
 * 낚시 루트 결과가 월드에 소환되기 전(on_fished_up_item 시점)에 잡힌 ItemStack
 * 수량을 직접 곱하므로 루트 테이블을 다시 굴리지 않고, 낚기 관련 다른 보상이
 * 이중으로 발동하지도 않는다. minecraft:fishes 태그에 속한 아이템에만 적용되므로
 * 액션의 조건 구성과 무관하게 보물과 쓰레기는 절대 늘어나지 않는다.
 */
public class CaughtFishMultiplierReward extends AbstractReward {

    private final int multiplier;

    public CaughtFishMultiplierReward(double chance, int priority, int multiplier) {
        super(chance, priority);
        this.multiplier = multiplier;
    }

    @Override
    public ActionResult apply(ActionData actionData) {
        ActionResult actionResult = new ActionResult();
        if (multiplier <= 1) {
            return actionResult;
        }

        ItemStack caughtStack = actionData.getData(ActionDataType.ITEM_STACK);
        boolean isCaughtFish = caughtStack != null && !caughtStack.isEmpty() && caughtStack.is(ItemTags.FISHES);
        if (!isCaughtFish) {
            return actionResult;
        }

        int originalCount = caughtStack.getCount();
        int multipliedCount = Math.min(originalCount * multiplier, caughtStack.getMaxStackSize());
        if (multipliedCount <= originalCount) {
            return actionResult;
        }

        caughtStack.setCount(multipliedCount);
        SkillActivationNotifier.notifyExtraDrop(actionData, caughtStack.copyWithCount(multipliedCount - originalCount));
        return actionResult;
    }

    @Override
    public IRewardType<?> getType() {
        return RewardType.CAUGHT_FISH_MULTIPLIER;
    }

    public int getMultiplier() {
        return multiplier;
    }

    @Override
    public Component getName() {
        return Component.literal("낚은 물고기 수량 배율");
    }

    @Override
    public Component getDescription(Object... args) {
        return Component.literal("낚아 올린 물고기가 " + multiplier + "배가 됩니다");
    }

    public static class Serializer implements IRewardSerializer<CaughtFishMultiplierReward> {

        @Override
        public CaughtFishMultiplierReward fromJson(JsonObject jsonObject, double chance, int priority) {
            return new CaughtFishMultiplierReward(
                    chance,
                    priority,
                    GsonHelper.getAsInt(jsonObject, "multiplier"));
        }

        @Override
        public CaughtFishMultiplierReward fromNetwork(RegistryFriendlyByteBuf friendlyByteBuf, double chance, int priority) {
            return new CaughtFishMultiplierReward(
                    chance,
                    priority,
                    friendlyByteBuf.readInt());
        }

        @Override
        public void toNetwork(RegistryFriendlyByteBuf friendlyByteBuf, CaughtFishMultiplierReward type) {
            IRewardSerializer.super.toNetwork(friendlyByteBuf, type);
            friendlyByteBuf.writeInt(type.multiplier);
        }
    }
}
