package com.daqem.arc.data.condition.item;

import com.daqem.arc.api.action.data.ActionData;
import com.daqem.arc.api.action.data.type.ActionDataType;
import com.daqem.arc.api.condition.AbstractCondition;
import com.daqem.arc.api.condition.serializer.IConditionSerializer;
import com.daqem.arc.api.condition.type.ConditionType;
import com.daqem.arc.api.condition.type.IConditionType;
import com.google.gson.*;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class ItemCondition extends AbstractCondition {

    private final ItemStack itemStack;
    private final boolean checkComponents;

    public ItemCondition(boolean inverted, ItemStack itemStack, boolean checkComponents) {
        super(inverted);
        this.itemStack = itemStack;
        this.checkComponents = checkComponents;
    }

    @Override
    public Component getDescription() {
        return getDescription(itemStack.getHoverName());
    }

    @Override
    public boolean isMet(ActionData actionData) {
        Item item = actionData.getData(ActionDataType.ITEM);
        ItemStack itemStack = actionData.getData(ActionDataType.ITEM_STACK);
        boolean hasItem = item != null || itemStack != null;
        boolean passOnItem = item != null && item == this.itemStack.getItem();
        boolean passOnItemStack = itemStack != null && testItemStack(itemStack);
        return hasItem && (passOnItem || passOnItemStack);
    }

    private boolean testItemStack(ItemStack itemStack) {
        boolean sameItem = ItemStack.isSameItem(this.itemStack, itemStack);
        boolean hasCount = this.itemStack.getCount() > 1;
        boolean sameCount = itemStack.getCount() == this.itemStack.getCount();
        boolean passOnCount = !hasCount || sameCount;
        boolean hasComponents = !checkComponents || !this.itemStack.getComponents().isEmpty();
        boolean sameComponents = !checkComponents || ItemStack.isSameItemSameComponents(this.itemStack, itemStack);
        boolean passOnComponents = !checkComponents || !hasComponents || sameComponents;
        return sameItem && passOnCount && passOnComponents;
    }

    @Override
    public IConditionType<?> getType() {
        return ConditionType.ITEM;
    }

    public ItemStack getItemStack() {
        return itemStack;
    }

    public boolean isCheckComponents() {
        return checkComponents;
    }

    public static class Serializer implements IConditionSerializer<ItemCondition> {

        @Override
        public ItemCondition fromJson(ResourceLocation location, JsonObject jsonObject, boolean inverted) {
            return new ItemCondition(
                    inverted,
                    getItemStack(jsonObject.get("item")),
                    GsonHelper.getAsBoolean(jsonObject, "check_components", true));
        }

        @Override
        public ItemCondition fromNetwork(ResourceLocation location, RegistryFriendlyByteBuf friendlyByteBuf, boolean inverted) {
            return new ItemCondition(
                    inverted,
                    ItemStack.STREAM_CODEC.decode(friendlyByteBuf),
                    friendlyByteBuf.readBoolean());
        }

        @Override
        public void toNetwork(RegistryFriendlyByteBuf friendlyByteBuf, ItemCondition type) {
            IConditionSerializer.super.toNetwork(friendlyByteBuf, type);
            ItemStack.STREAM_CODEC.encode(friendlyByteBuf, type.itemStack);
            friendlyByteBuf.writeBoolean(type.checkComponents);
        }
    }
}
