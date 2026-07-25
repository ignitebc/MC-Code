package com.daqem.arc.data.condition.item;

import com.daqem.arc.api.action.data.ActionData;
import com.daqem.arc.api.condition.AbstractCondition;
import com.daqem.arc.api.condition.serializer.IConditionSerializer;
import com.daqem.arc.api.condition.type.ConditionType;
import com.daqem.arc.api.condition.type.IConditionType;
import com.google.gson.JsonObject;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class ItemInInventoryCondition extends AbstractCondition {

    private final ItemStack itemStack;

    public ItemInInventoryCondition(boolean inverted, ItemStack itemStack) {
        super(inverted);
        this.itemStack = itemStack;
    }

    @Override
    public Component getDescription() {
        return getDescription(itemStack.getHoverName());
    }

    @Override
    public boolean isMet(ActionData actionData) {
        Player player = actionData.getPlayer().arc$getPlayer();
        return player.getInventory().getNonEquipmentItems().stream().anyMatch(stack -> stack.getItem() == itemStack.getItem());
    }

    @Override
    public IConditionType<?> getType() {
        return ConditionType.ITEM_IN_INVENTORY;
    }

    public ItemStack getItemStack() {
        return itemStack;
    }

    public static class Serializer implements IConditionSerializer<ItemInInventoryCondition> {

        @Override
        public ItemInInventoryCondition fromJson(ResourceLocation location, JsonObject jsonObject, boolean inverted) {
            return new ItemInInventoryCondition(
                    inverted,
                    getItemStack(jsonObject.get("item")));
        }

        @Override
        public ItemInInventoryCondition fromNetwork(ResourceLocation location, RegistryFriendlyByteBuf friendlyByteBuf, boolean inverted) {
            return new ItemInInventoryCondition(
                    inverted,
                    ItemStack.STREAM_CODEC.decode(friendlyByteBuf));
        }

        @Override
        public void toNetwork(RegistryFriendlyByteBuf friendlyByteBuf, ItemInInventoryCondition type) {
            IConditionSerializer.super.toNetwork(friendlyByteBuf, type);
            ItemStack.STREAM_CODEC.encode(friendlyByteBuf, type.itemStack);
        }
    }
}
