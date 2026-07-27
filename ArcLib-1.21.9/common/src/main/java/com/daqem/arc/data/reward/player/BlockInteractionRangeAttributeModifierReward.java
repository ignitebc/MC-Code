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

public class BlockInteractionRangeAttributeModifierReward extends AbstractReward {

    private final double amount;

    public BlockInteractionRangeAttributeModifierReward(double chance, int priority, double amount) {
        super(chance, priority);
        this.amount = amount;
    }

    public double getAmount() {
        return amount;
    }

    public static ResourceLocation computeModifierId(ResourceLocation holderId, ResourceLocation actionId) {
        String holderKey = sanitizeForPath(holderId.getNamespace() + "_" + holderId.getPath());
        String actionKey = sanitizeForPath(actionId.getNamespace() + "_" + actionId.getPath());
        return ResourceLocation.fromNamespaceAndPath("arc", "block_interaction_range/" + holderKey + "/" + actionKey);
    }

    private static String sanitizeForPath(String value) {
        return value.toLowerCase()
                .replace(':', '_')
                .replace('/', '_')
                .replace('\\', '_')
                .replace(' ', '_');
    }

    @Override
    public IRewardType<?> getType() {
        return RewardType.BLOCK_INTERACTION_RANGE_ATTRIBUTE_MODIFIER;
    }

    @Override
    public ActionResult apply(ActionData actionData) {
        return new ActionResult();
    }

    @Override
    public Component getName() {
        return Component.literal("블록 상호작용 거리 보정");
    }

    @Override
    public Component getDescription(Object... args) {
        return Component.literal("블록 상호작용 거리를 " + amount + " 증가시킵니다");
    }

    public static class Serializer implements IRewardSerializer<BlockInteractionRangeAttributeModifierReward> {

        @Override
        public BlockInteractionRangeAttributeModifierReward fromJson(JsonObject jsonObject, double chance, int priority) {
            return new BlockInteractionRangeAttributeModifierReward(chance, priority, GsonHelper.getAsDouble(jsonObject, "amount"));
        }

        @Override
        public BlockInteractionRangeAttributeModifierReward fromNetwork(RegistryFriendlyByteBuf friendlyByteBuf, double chance, int priority) {
            return new BlockInteractionRangeAttributeModifierReward(chance, priority, friendlyByteBuf.readDouble());
        }

        @Override
        public void toNetwork(RegistryFriendlyByteBuf friendlyByteBuf, BlockInteractionRangeAttributeModifierReward type) {
            IRewardSerializer.super.toNetwork(friendlyByteBuf, type);
            friendlyByteBuf.writeDouble(type.amount);
        }
    }
}
