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
import net.minecraft.resources.Identifier;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;

public class ItemCondition extends AbstractCondition {

    private final ItemStackTemplate itemTemplate;
    private final boolean checkComponents;

    public ItemCondition(boolean inverted, ItemStackTemplate itemTemplate, boolean checkComponents) {
        super(inverted);
        this.itemTemplate = itemTemplate;
        this.checkComponents = checkComponents;
    }

    @Override
    public Component getDescription() {
        return getDescription(getItemStack().getHoverName());
    }

    @Override
    public boolean isMet(ActionData actionData) {
        ItemStack expectedStack = getItemStack();
        Item item = actionData.getData(ActionDataType.ITEM);
        ItemStack itemStack = actionData.getData(ActionDataType.ITEM_STACK);
        boolean hasItem = item != null || itemStack != null;
        boolean passOnItem = item != null && item == expectedStack.getItem();
        boolean passOnItemStack = itemStack != null && testItemStack(expectedStack, itemStack);
        return hasItem && (passOnItem || passOnItemStack);
    }

    private boolean testItemStack(ItemStack expectedStack, ItemStack itemStack) {
        boolean sameItem = ItemStack.isSameItem(expectedStack, itemStack);
        boolean hasCount = expectedStack.getCount() > 1;
        boolean sameCount = itemStack.getCount() == expectedStack.getCount();
        boolean passOnCount = !hasCount || sameCount;
        boolean hasComponents = !checkComponents || !expectedStack.getComponents().isEmpty();
        boolean sameComponents = !checkComponents || ItemStack.isSameItemSameComponents(expectedStack, itemStack);
        boolean passOnComponents = !checkComponents || !hasComponents || sameComponents;
        return sameItem && passOnCount && passOnComponents;
    }

    @Override
    public IConditionType<?> getType() {
        return ConditionType.ITEM;
    }

    public ItemStack getItemStack() {
        return itemTemplate.create();
    }

    public boolean isCheckComponents() {
        return checkComponents;
    }

    public static class Serializer implements IConditionSerializer<ItemCondition> {

        @Override
        public ItemCondition fromJson(Identifier location, JsonObject jsonObject, boolean inverted) {
            return new ItemCondition(
                    inverted,
                    getItemStackTemplate(jsonObject.get("item")),
                    GsonHelper.getAsBoolean(jsonObject, "check_components", true));
        }

        @Override
        public ItemCondition fromNetwork(Identifier location, RegistryFriendlyByteBuf friendlyByteBuf, boolean inverted) {
            return new ItemCondition(
                    inverted,
                    ItemStackTemplate.STREAM_CODEC.decode(friendlyByteBuf),
                    friendlyByteBuf.readBoolean());
        }

        @Override
        public void toNetwork(RegistryFriendlyByteBuf friendlyByteBuf, ItemCondition type) {
            IConditionSerializer.super.toNetwork(friendlyByteBuf, type);
            ItemStackTemplate.STREAM_CODEC.encode(friendlyByteBuf, type.itemTemplate);
            friendlyByteBuf.writeBoolean(type.checkComponents);
        }
    }
}
