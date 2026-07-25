package com.daqem.arc.data.condition.item;

import com.daqem.arc.api.action.data.ActionData;
import com.daqem.arc.api.action.data.type.ActionDataType;
import com.daqem.arc.api.condition.AbstractCondition;
import com.daqem.arc.api.condition.serializer.IConditionSerializer;
import com.daqem.arc.api.condition.type.ConditionType;
import com.daqem.arc.api.condition.type.IConditionType;
import com.google.gson.*;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class ItemsCondition extends AbstractCondition {

    private final List<Item> items;
    private final List<TagKey<Item>> itemTags;


    public ItemsCondition(boolean inverted, List<Item> items, List<TagKey<Item>> itemTags) {
        super(inverted);
        this.items = items;
        this.itemTags = itemTags;
    }

    @Override
    public Component getDescription() {
        return getDescription(items.stream().map(Item::getName).toArray(Component[]::new), itemTags.stream().map(TagKey::location).toArray(ResourceLocation[]::new));
    }

    @Override
    public boolean isMet(ActionData actionData) {
        ItemStack itemStack = actionData.getData(ActionDataType.ITEM_STACK);
        if (itemStack == null) {
            Item item = actionData.getData(ActionDataType.ITEM);
            if (item != null) {
                itemStack = item.getDefaultInstance();
            }
        }
        return itemStack != null && (isItem(itemStack) || isItemByTag(itemStack));
    }

    @Override
    public IConditionType<?> getType() {
        return ConditionType.ITEMS;
    }

    private boolean isItem(ItemStack itemStack) {
        return this.items.contains(itemStack.getItem());
    }

    private boolean isItemByTag(ItemStack itemStack) {
        return this.itemTags.stream().anyMatch(itemStack::is);
    }

    public List<Item> getItems() {
        return items;
    }

    public List<TagKey<Item>> getItemTags() {
        return itemTags;
    }

    public List<ItemStack> getItemStacks(RegistryAccess registryAccess) {
        List<ItemStack> itemStacks = new ArrayList<>();
        for (Item item : items) {
            itemStacks.add(new ItemStack(item));
        }
        for (TagKey<Item> itemTag : itemTags) {
            registryAccess.lookupOrThrow(Registries.ITEM).get(itemTag).ifPresent(holders -> {
                for (var holder : holders) {
                    itemStacks.add(new ItemStack(holder.value()));
                }
            });
        }
        return itemStacks;
    }

    public static class Serializer implements IConditionSerializer<ItemsCondition> {

        @Override
        public ItemsCondition fromJson(ResourceLocation location, JsonObject jsonObject, boolean inverted) {
            return new ItemsCondition(
                    inverted,
                    getItems(jsonObject, "items"),
                    getItemTags(jsonObject, "items"));
        }

        @Override
        public ItemsCondition fromNetwork(ResourceLocation location, RegistryFriendlyByteBuf friendlyByteBuf, boolean inverted) {
            int itemCount = friendlyByteBuf.readVarInt();
            int tagCount = friendlyByteBuf.readVarInt();

            List<Item> items = new ArrayList<>();
            List<TagKey<Item>> itemTags = new ArrayList<>();

            for (int i = 0; i < itemCount; i++) {

                items.add(ByteBufCodecs.registry(Registries.ITEM).decode(friendlyByteBuf));
            }

            for (int i = 0; i < tagCount; i++) {
                itemTags.add(TagKey.create(BuiltInRegistries.ITEM.key(), friendlyByteBuf.readResourceLocation()));
            }


            return new ItemsCondition(
                    inverted,
                    items,
                    itemTags);
        }

        @Override
        public void toNetwork(RegistryFriendlyByteBuf friendlyByteBuf, ItemsCondition type) {
            IConditionSerializer.super.toNetwork(friendlyByteBuf, type);
            friendlyByteBuf.writeVarInt(type.items.size());
            friendlyByteBuf.writeVarInt(type.itemTags.size());
            type.items.forEach(item -> ByteBufCodecs.registry(Registries.ITEM).encode(friendlyByteBuf, item));
            type.itemTags.forEach(tag -> friendlyByteBuf.writeResourceLocation(tag.location()));
        }
    }
}
