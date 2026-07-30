package com.daqem.arc.data.condition.item;

import com.daqem.arc.api.action.data.ActionData;
import com.daqem.arc.api.condition.AbstractCondition;
import com.daqem.arc.api.condition.serializer.IConditionSerializer;
import com.daqem.arc.api.condition.type.ConditionType;
import com.daqem.arc.api.condition.type.IConditionType;
import com.google.gson.*;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;

public class ItemInHandCondition extends AbstractCondition {

    private final ItemStackTemplate itemTemplate;
    private final InteractionHand hand;

    public ItemInHandCondition(boolean inverted, ItemStackTemplate itemTemplate, InteractionHand hand) {
        super(inverted);
        this.itemTemplate = itemTemplate;
        this.hand = hand;
    }

    @Override
    public Component getDescription() {
        return getDescription(getItemStack().getHoverName(), hand.name().toLowerCase().replace("_", " "));
    }

    @Override
    public boolean isMet(ActionData actionData) {
        Player player = actionData.getPlayer().arc$getPlayer();
        Item targetItem = getItemStack().getItem();

        if (hand == null) {
            // Check both hands when no specific hand is defined
            return player.getMainHandItem().getItem() == targetItem
                    || player.getOffhandItem().getItem() == targetItem;
        }

        // Check only the specified hand
        return player.getItemInHand(hand).getItem() == targetItem;
    }


    @Override
    public IConditionType<?> getType() {
        return ConditionType.ITEM_IN_HAND;
    }

    public ItemStack getItemStack() {
        return itemTemplate.create();
    }

    public InteractionHand getHand() {
        return hand;
    }

    public static class Serializer implements IConditionSerializer<ItemInHandCondition> {

        @Override
        public ItemInHandCondition fromJson(Identifier location, JsonObject jsonObject, boolean inverted) {
            return new ItemInHandCondition(
                    inverted,
                    getItemStackTemplate(jsonObject.get("item")),
                    getOptionalHand(jsonObject, "hand")
            );
        }

        @Override
        public ItemInHandCondition fromNetwork(Identifier location, RegistryFriendlyByteBuf friendlyByteBuf, boolean inverted) {
            ItemStackTemplate itemTemplate = ItemStackTemplate.STREAM_CODEC.decode(friendlyByteBuf);
            InteractionHand hand = friendlyByteBuf.readBoolean() ? friendlyByteBuf.readEnum(InteractionHand.class) : null;
            return new ItemInHandCondition(
                    inverted,
                    itemTemplate,
                    hand
            );
        }

        @Override
        public void toNetwork(RegistryFriendlyByteBuf friendlyByteBuf, ItemInHandCondition type) {
            IConditionSerializer.super.toNetwork(friendlyByteBuf, type);
            ItemStackTemplate.STREAM_CODEC.encode(friendlyByteBuf, type.itemTemplate);
            friendlyByteBuf.writeBoolean(type.hand != null);
            if (type.hand != null) {
                friendlyByteBuf.writeEnum(type.hand);
            }
        }
    }
}
