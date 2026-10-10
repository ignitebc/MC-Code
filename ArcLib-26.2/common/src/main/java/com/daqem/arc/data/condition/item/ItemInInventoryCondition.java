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
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
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
        Item expectedItem = getItemStack().getItem();
        Inventory inventory = player.getInventory();
        // 일반 36칸만 보면 배낭처럼 다른 모드가 인벤토리 뒤에 붙인 칸을 놓친다.
        // 컨테이너 전체를 훑되, 방어구·보조 손 같은 바닐라 장비 칸은 원래처럼 제외한다.
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            if (isVanillaEquipmentSlot(slot)) continue;
            if (inventory.getItem(slot).getItem() == expectedItem) return true;
        }
        return false;
    }

    private static boolean isVanillaEquipmentSlot(int slot) {
        int equipmentStart = Inventory.INVENTORY_SIZE;
        int equipmentEnd = equipmentStart + Inventory.EQUIPMENT_SLOT_MAPPING.size();
        return slot >= equipmentStart && slot < equipmentEnd;
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
