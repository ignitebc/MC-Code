package com.daqem.arc.data.condition.item;

import com.daqem.arc.api.action.data.ActionData;
import com.daqem.arc.api.condition.AbstractCondition;
import com.daqem.arc.api.condition.serializer.IConditionSerializer;
import com.daqem.arc.api.condition.type.ConditionType;
import com.daqem.arc.api.condition.type.IConditionType;
import com.google.gson.JsonObject;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;

public class ItemInInventoryCondition extends AbstractCondition {

    private final ItemStackTemplate itemTemplate;

    public ItemInInventoryCondition(boolean inverted, ItemStackTemplate itemTemplate) {
        super(inverted);
        this.itemTemplate = itemTemplate;
    }

    @Override
    public Component getDescription() {
        return getDescription(getItemStack().getHoverName());
    }

    @Override
    public boolean isMet(ActionData actionData) {
        Player player = actionData.getPlayer().arc$getPlayer();
        ItemStack expectedStack = getItemStack();
        return player.getInventory().getNonEquipmentItems().stream().anyMatch(stack -> stack.getItem() == expectedStack.getItem());
    }

    @Override
    public IConditionType<?> getType() {
        return ConditionType.ITEM_IN_INVENTORY;
    }

    public ItemStack getItemStack() {
        return itemTemplate.create();
    }

    public static class Serializer implements IConditionSerializer<ItemInInventoryCondition> {

        @Override
        public ItemInInventoryCondition fromJson(Identifier location, JsonObject jsonObject, boolean inverted) {
            return new ItemInInventoryCondition(
                    inverted,
                    getItemStackTemplate(jsonObject.get("item")));
        }

        @Override
        public ItemInInventoryCondition fromNetwork(Identifier location, RegistryFriendlyByteBuf friendlyByteBuf, boolean inverted) {
            return new ItemInInventoryCondition(
                    inverted,
                    ItemStackTemplate.STREAM_CODEC.decode(friendlyByteBuf));
        }

        @Override
        public void toNetwork(RegistryFriendlyByteBuf friendlyByteBuf, ItemInInventoryCondition type) {
            IConditionSerializer.super.toNetwork(friendlyByteBuf, type);
            ItemStackTemplate.STREAM_CODEC.encode(friendlyByteBuf, type.itemTemplate);
        }
    }
}
