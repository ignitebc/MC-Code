package com.daqem.arc.data.condition.item;

import com.daqem.arc.api.action.data.ActionData;
import com.daqem.arc.api.condition.AbstractCondition;
import com.daqem.arc.api.condition.serializer.IConditionSerializer;
import com.daqem.arc.api.condition.type.ConditionType;
import com.daqem.arc.api.condition.type.IConditionType;
import com.google.gson.*;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class ItemEquippedCondition extends AbstractCondition {

    private final ItemStack itemStack;

    public ItemEquippedCondition(boolean inverted, ItemStack itemStack) {
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
        List<ItemStack> armor = new ArrayList<>();
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (slot.isArmor()) {
                ItemStack stack = player.getItemBySlot(slot);
                if (!stack.isEmpty()) {
                    armor.add(player.getItemBySlot(slot));
                }
            }
        }
        return armor.stream().anyMatch(stack -> stack.getItem() == itemStack.getItem());
    }

    @Override
    public IConditionType<?> getType() {
        return ConditionType.ITEM_EQUIPPED;
    }

    public ItemStack getItemStack() {
        return itemStack;
    }

    public static class Serializer implements IConditionSerializer<ItemEquippedCondition> {

        @Override
        public ItemEquippedCondition fromJson(ResourceLocation location, JsonObject jsonObject, boolean inverted) {
            return new ItemEquippedCondition(
                    inverted,
                    getItemStack(jsonObject.get("item")));
        }

        @Override
        public ItemEquippedCondition fromNetwork(ResourceLocation location, RegistryFriendlyByteBuf friendlyByteBuf, boolean inverted) {
            return new ItemEquippedCondition(
                    inverted,
                    ItemStack.STREAM_CODEC.decode(friendlyByteBuf));
        }

        @Override
        public void toNetwork(RegistryFriendlyByteBuf friendlyByteBuf, ItemEquippedCondition type) {
            IConditionSerializer.super.toNetwork(friendlyByteBuf, type);
            ItemStack.STREAM_CODEC.encode(friendlyByteBuf, type.itemStack);
        }
    }
}
