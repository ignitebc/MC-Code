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
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;

import java.util.ArrayList;
import java.util.List;

public class ItemEquippedCondition extends AbstractCondition {

    private final ItemStackTemplate itemTemplate;

    public ItemEquippedCondition(boolean inverted, ItemStackTemplate itemTemplate) {
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
        List<ItemStack> armor = new ArrayList<>();
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (slot.isArmor()) {
                ItemStack stack = player.getItemBySlot(slot);
                if (!stack.isEmpty()) {
                    armor.add(player.getItemBySlot(slot));
                }
            }
        }
        return armor.stream().anyMatch(stack -> stack.getItem() == expectedStack.getItem());
    }

    @Override
    public IConditionType<?> getType() {
        return ConditionType.ITEM_EQUIPPED;
    }

    public ItemStack getItemStack() {
        return itemTemplate.create();
    }

    public static class Serializer implements IConditionSerializer<ItemEquippedCondition> {

        @Override
        public ItemEquippedCondition fromJson(Identifier location, JsonObject jsonObject, boolean inverted) {
            return new ItemEquippedCondition(
                    inverted,
                    getItemStackTemplate(jsonObject.get("item")));
        }

        @Override
        public ItemEquippedCondition fromNetwork(Identifier location, RegistryFriendlyByteBuf friendlyByteBuf, boolean inverted) {
            return new ItemEquippedCondition(
                    inverted,
                    ItemStackTemplate.STREAM_CODEC.decode(friendlyByteBuf));
        }

        @Override
        public void toNetwork(RegistryFriendlyByteBuf friendlyByteBuf, ItemEquippedCondition type) {
            IConditionSerializer.super.toNetwork(friendlyByteBuf, type);
            ItemStackTemplate.STREAM_CODEC.encode(friendlyByteBuf, type.itemTemplate);
        }
    }
}
