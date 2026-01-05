package com.daqem.arc.data.condition.item;

import com.daqem.arc.api.action.data.ActionData;
import com.daqem.arc.api.action.data.type.ActionDataType;
import com.daqem.arc.api.condition.AbstractCondition;
import com.daqem.arc.api.condition.serializer.IConditionSerializer;
import com.daqem.arc.api.condition.type.ConditionType;
import com.daqem.arc.api.condition.type.IConditionType;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Checks whether the tool used for an action has the required enchantments.
 *
 * JSON format example:
 * {
 *   "type": "arc:enchantments",
 *   "enchantments": {
 *     "minecraft:silk_touch": { "min": 1 }
 *   }
 * }
 *
 * ArcLib-1.21.9 target notes:
 * - Do NOT use RegistryAccess#registry(...) / registryOrThrow(...): not available here.
 * - Use RegistryAccess#lookupOrThrow(...) (already used in BlocksCondition / ItemsCondition in ArcLib).
 */
public class EnchantmentsCondition extends AbstractCondition {

    /** key=enchantment id, value=range */
    private final Map<ResourceLocation, IntRange> enchantments;

    public EnchantmentsCondition(boolean inverted, Map<ResourceLocation, IntRange> enchantments) {
        super(inverted);
        this.enchantments = enchantments == null ? Collections.emptyMap() : Map.copyOf(enchantments);
    }

    @Override
    public boolean isMet(ActionData actionData) {
        if (enchantments.isEmpty()) {
            return true;
        }

        // Prefer explicit ITEM_STACK (trigger should provide it). Fallback to player's main hand.
        ItemStack stack = actionData.getData(ActionDataType.ITEM_STACK);
        if (stack == null) {
            stack = actionData.getPlayer().arc$getPlayer().getMainHandItem();
        }
        if (stack == null || stack.isEmpty()) {
            return false;
        }

        // World is stored in ActionData as ActionDataType.WORLD (there is no actionData.getWorld()).
        Level world = actionData.getData(ActionDataType.WORLD);
        if (world == null) {
            // Fallback: player's current world
            world = actionData.getPlayer().arc$getPlayer().level();
        }
        if (world == null) {
            return false;
        }

        RegistryAccess registryAccess = world.registryAccess();
        var enchantLookup = registryAccess.lookupOrThrow(Registries.ENCHANTMENT);

        for (Map.Entry<ResourceLocation, IntRange> entry : enchantments.entrySet()) {
            ResourceLocation enchId = entry.getKey();
            IntRange range = entry.getValue();

            ResourceKey<Enchantment> enchKey = ResourceKey.create(Registries.ENCHANTMENT, enchId);

            // HolderLookup#get(ResourceKey) -> Optional<Holder.Reference<Enchantment>> in this mapping
            Optional<Holder.Reference<Enchantment>> holderOpt = enchantLookup.get(enchKey);
            if (holderOpt.isEmpty()) {
                return false;
            }

            Holder<Enchantment> holder = holderOpt.get();
            int level = EnchantmentHelper.getItemEnchantmentLevel(holder, stack);

            if (!range.matches(level)) {
                return false;
            }
        }

        return true;
    }

    @Override
    public Component getDescription() {
        return getDescription(enchantments.size());
    }

    @Override
    public IConditionType<?> getType() {
        return ConditionType.ENCHANTMENTS;
    }

    public Map<ResourceLocation, IntRange> getEnchantments() {
        return enchantments;
    }

    public record IntRange(int min, Integer max) {
        public boolean matches(int value) {
            if (value < min) return false;
            return max == null || value <= max;
        }
    }

    public static class Serializer implements IConditionSerializer<EnchantmentsCondition> {

        @Override
        public EnchantmentsCondition fromJson(ResourceLocation location, JsonObject jsonObject, boolean inverted) {
            JsonObject enchObj = GsonHelper.getAsJsonObject(jsonObject, "enchantments", new JsonObject());
            Map<ResourceLocation, IntRange> map = new HashMap<>();

            for (Map.Entry<String, JsonElement> entry : enchObj.entrySet()) {
                ResourceLocation enchId = ResourceLocation.parse(entry.getKey());
                JsonObject rangeObj = entry.getValue().getAsJsonObject();
                int min = GsonHelper.getAsInt(rangeObj, "min", 0);
                Integer max = rangeObj.has("max") ? GsonHelper.getAsInt(rangeObj, "max") : null;
                map.put(enchId, new IntRange(min, max));
            }

            return new EnchantmentsCondition(inverted, map);
        }

        @Override
        public EnchantmentsCondition fromNetwork(ResourceLocation location, RegistryFriendlyByteBuf friendlyByteBuf, boolean inverted) {
            int size = friendlyByteBuf.readVarInt();
            Map<ResourceLocation, IntRange> map = new HashMap<>();

            for (int i = 0; i < size; i++) {
                ResourceLocation enchId = friendlyByteBuf.readResourceLocation();
                int min = friendlyByteBuf.readVarInt();
                boolean hasMax = friendlyByteBuf.readBoolean();
                Integer max = hasMax ? friendlyByteBuf.readVarInt() : null;
                map.put(enchId, new IntRange(min, max));
            }

            return new EnchantmentsCondition(inverted, map);
        }

        @Override
        public void toNetwork(RegistryFriendlyByteBuf friendlyByteBuf, EnchantmentsCondition type) {
            IConditionSerializer.super.toNetwork(friendlyByteBuf, type);
            friendlyByteBuf.writeVarInt(type.enchantments.size());
            type.enchantments.forEach((enchId, range) -> {
                friendlyByteBuf.writeResourceLocation(enchId);
                friendlyByteBuf.writeVarInt(range.min());
                friendlyByteBuf.writeBoolean(range.max() != null);
                if (range.max() != null) {
                    friendlyByteBuf.writeVarInt(range.max());
                }
            });
        }
    }
}
