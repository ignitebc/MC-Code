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
import net.minecraft.resources.Identifier;
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
 * 액션에 쓴 도구에 필요한 마법 부여가 있는지 확인한다.
 *
 * JSON 형식 예:
 * {
 *   "type": "arc:enchantments",
 *   "enchantments": {
 *     "minecraft:silk_touch": { "min": 1 }
 *   }
 * }
 *
 * ArcLib 26.2 대상 참고:
 * - RegistryAccess#registry(...) / registryOrThrow(...)는 여기서 쓸 수 없으므로 쓰지 않는다.
 * - RegistryAccess#lookupOrThrow(...)를 쓴다(ArcLib의 BlocksCondition / ItemsCondition도 이미 쓰고 있다).
 */
public class EnchantmentsCondition extends AbstractCondition {

    /** 키=마법 부여 ID, 값=범위 */
    private final Map<Identifier, IntRange> enchantments;

    public EnchantmentsCondition(boolean inverted, Map<Identifier, IntRange> enchantments) {
        super(inverted);
        this.enchantments = enchantments == null ? Collections.emptyMap() : Map.copyOf(enchantments);
    }

    @Override
    public boolean isMet(ActionData actionData) {
        if (enchantments.isEmpty()) {
            return true;
        }

        // 명시된 ITEM_STACK을 우선한다(트리거가 넘겨줘야 한다). 없으면 플레이어의 주 손을 쓴다.
        ItemStack stack = actionData.getData(ActionDataType.ITEM_STACK);
        if (stack == null) {
            stack = actionData.getPlayer().arc$getPlayer().getMainHandItem();
        }
        if (stack == null || stack.isEmpty()) {
            return false;
        }

        // 월드는 ActionData에 ActionDataType.WORLD로 들어 있다(actionData.getWorld()는 없다).
        Level world = actionData.getData(ActionDataType.WORLD);
        if (world == null) {
            // 없으면 플레이어가 있는 월드를 쓴다
            world = actionData.getPlayer().arc$getPlayer().level();
        }
        if (world == null) {
            return false;
        }

        RegistryAccess registryAccess = world.registryAccess();
        var enchantLookup = registryAccess.lookupOrThrow(Registries.ENCHANTMENT);

        for (Map.Entry<Identifier, IntRange> entry : enchantments.entrySet()) {
            Identifier enchId = entry.getKey();
            IntRange range = entry.getValue();

            ResourceKey<Enchantment> enchKey = ResourceKey.create(Registries.ENCHANTMENT, enchId);

            // 이 매핑에서 HolderLookup#get(ResourceKey)는 Optional<Holder.Reference<Enchantment>>를 돌려준다
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

    public Map<Identifier, IntRange> getEnchantments() {
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
        public EnchantmentsCondition fromJson(Identifier location, JsonObject jsonObject, boolean inverted) {
            JsonObject enchObj = GsonHelper.getAsJsonObject(jsonObject, "enchantments", new JsonObject());
            Map<Identifier, IntRange> map = new HashMap<>();

            for (Map.Entry<String, JsonElement> entry : enchObj.entrySet()) {
                Identifier enchId = Identifier.parse(entry.getKey());
                JsonObject rangeObj = entry.getValue().getAsJsonObject();
                int min = GsonHelper.getAsInt(rangeObj, "min", 0);
                Integer max = rangeObj.has("max") ? GsonHelper.getAsInt(rangeObj, "max") : null;
                map.put(enchId, new IntRange(min, max));
            }

            return new EnchantmentsCondition(inverted, map);
        }

        @Override
        public EnchantmentsCondition fromNetwork(Identifier location, RegistryFriendlyByteBuf friendlyByteBuf, boolean inverted) {
            int size = friendlyByteBuf.readVarInt();
            Map<Identifier, IntRange> map = new HashMap<>();

            for (int i = 0; i < size; i++) {
                Identifier enchId = friendlyByteBuf.readIdentifier();
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
                friendlyByteBuf.writeIdentifier(enchId);
                friendlyByteBuf.writeVarInt(range.min());
                friendlyByteBuf.writeBoolean(range.max() != null);
                if (range.max() != null) {
                    friendlyByteBuf.writeVarInt(range.max());
                }
            });
        }
    }
}
