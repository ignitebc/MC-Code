package com.daqem.arc.data.reward.item;

import com.daqem.arc.api.action.data.ActionData;
import com.daqem.arc.api.action.result.ActionResult;
import com.daqem.arc.api.player.ArcPlayer;
import com.daqem.arc.api.reward.AbstractReward;
import com.daqem.arc.api.reward.serializer.IRewardSerializer;
import com.daqem.arc.api.reward.type.IRewardType;
import com.daqem.arc.api.reward.type.RewardType;
import com.daqem.arc.player.PlayerItemDelivery;
import com.daqem.arc.player.SkillActivationNotifier;
import com.google.gson.JsonObject;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;

public class ItemReward extends AbstractReward {

    private final ItemStackTemplate itemTemplate;
    private final int amount;

    public ItemReward(double chance, int priority, ItemStackTemplate itemTemplate, int amount) {
        super(chance, priority);
        this.itemTemplate = itemTemplate;
        this.amount = amount;
    }

    @Override
    public Component getDescription() {
        return getDescription(amount, getItemStack().getHoverName());
    }

    @Override
    public ActionResult apply(ActionData actionData) {
        ArcPlayer arcPlayer = actionData.getPlayer();
        if (arcPlayer.arc$getPlayer() instanceof ServerPlayer serverPlayer) {
            ItemStack reward = itemTemplate.create();
            ItemStack notificationStack = reward.copy();
            PlayerItemDelivery.giveOrDrop(serverPlayer, reward);
            SkillActivationNotifier.notifyExtraDrop(serverPlayer, notificationStack);
        }
        return new ActionResult();
    }

    @Override
    public IRewardType<?> getType() {
        return RewardType.ITEM;
    }

    public ItemStack getItemStack() {
        return itemTemplate.create();
    }

    public int getAmount() {
        return amount;
    }

    public static class Serializer implements IRewardSerializer<ItemReward> {

        @Override
        public ItemReward fromJson(JsonObject jsonObject, double chance, int priority) {
            int amount = GsonHelper.getAsInt(jsonObject, "amount", 1);
            ItemStackTemplate itemTemplate = getItemStackTemplate(jsonObject.get("item")).withCount(amount);
            return new ItemReward(chance, priority, itemTemplate, amount);
        }

        @Override
        public ItemReward fromNetwork(RegistryFriendlyByteBuf friendlyByteBuf, double chance, int priority) {
            return new ItemReward(chance, priority, ItemStackTemplate.STREAM_CODEC.decode(friendlyByteBuf), friendlyByteBuf.readInt());
        }

        @Override
        public void toNetwork(RegistryFriendlyByteBuf friendlyByteBuf, ItemReward type) {
            IRewardSerializer.super.toNetwork(friendlyByteBuf, type);
            ItemStackTemplate.STREAM_CODEC.encode(friendlyByteBuf, type.itemTemplate);
            friendlyByteBuf.writeInt(type.amount);
        }
    }
}
