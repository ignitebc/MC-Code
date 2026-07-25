package com.daqem.arc.data.serializer;

import com.daqem.arc.api.action.holder.type.IActionHolderType;
import com.daqem.arc.registry.ArcRegistry;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public interface ArcSerializer {

    default ResourceLocation getResourceLocation(JsonObject jsonObject, String elementName){
        return ResourceLocation.parse(getString(jsonObject, elementName));
    }

    default ItemStack getItemStack(JsonElement element){
        return ItemStack.CODEC.decode(JsonOps.INSTANCE, element).result()
                .orElseThrow(() -> new JsonParseException("Invalid item stack")).getFirst();
    }

    default IActionHolderType<?> getHolderType(JsonObject jsonObject, String elementName){
        return new ElementConverter<>(ArcRegistry.ACTION_HOLDER).convertToElement(getString(jsonObject, elementName));
    }

    default Item getItem(JsonObject jsonObject, String elementName){
        return new ElementConverter<>(BuiltInRegistries.ITEM).convertToElement(getString(jsonObject, elementName));
    }

    default Block getBlock(JsonObject jsonObject, String elementName){
        return new ElementConverter<>(BuiltInRegistries.BLOCK).convertToElement(getString(jsonObject, elementName));
    }

    default MobEffect getMobEffect(JsonObject jsonObject, String elementName){
        return new ElementConverter<>(BuiltInRegistries.MOB_EFFECT).convertToElement(getString(jsonObject, elementName));
    }

    default EntityType<?> getEntityType(JsonObject jsonObject, String elementName){
        return new ElementConverter<>(BuiltInRegistries.ENTITY_TYPE).convertToElement(getString(jsonObject, elementName));
    }

    default String getString(JsonObject jsonObject, String elementName){
        return GsonHelper.getAsString(jsonObject, elementName);
    }

    default List<String> getStrings(JsonObject jsonObject, String elementName){
        return StreamSupport.stream(
                        GsonHelper.getAsJsonArray(jsonObject, elementName).spliterator(), false)
                .map(JsonElement::getAsString)
                .collect(Collectors.toList());
    }

    default <E> List<E> getElementsFromJson(Registry<E> registry, JsonObject jsonObject, String elementName){
        return new ElementConverter<>(registry).convertToElements(getStrings(jsonObject, elementName));
    }

    default <E> List<TagKey<E>> getTagKeysFromJson(Registry<E> registry, JsonObject jsonObject, String elementName){
        return new ElementConverter<>(registry).convertToElementTags(getStrings(jsonObject, elementName));
    }

    default List<Block> getBlocks(JsonObject jsonObject, String elementName){
        return getElementsFromJson(BuiltInRegistries.BLOCK, jsonObject, elementName);
    }

    default List<TagKey<Block>> getBlockTags(JsonObject jsonObject, String elementName){
        return getTagKeysFromJson(BuiltInRegistries.BLOCK, jsonObject, elementName);
    }

    default List<Item> getItems(JsonObject jsonObject, String elementName){
        return getElementsFromJson(BuiltInRegistries.ITEM, jsonObject, elementName);
    }

    default List<TagKey<Item>> getItemTags(JsonObject jsonObject, String elementName){
        return getTagKeysFromJson(BuiltInRegistries.ITEM, jsonObject, elementName);
    }

    default List<EntityType<?>> getEntityTypes(JsonObject jsonObject, String elementName){
        return getElementsFromJson(BuiltInRegistries.ENTITY_TYPE, jsonObject, elementName);
    }

    default InteractionHand getHand(JsonObject jsonObject, String elementName){
        String handName = getString(jsonObject, elementName);
        InteractionHand hand;
        try {
            hand = InteractionHand.valueOf(handName);
        } catch (IllegalArgumentException e) {
            throw new JsonParseException("Invalid hand, expected to find a valid hand ('MAIN_HAND' or 'OFF_HAND').");
        }
        return hand;
    }

    default @Nullable InteractionHand getOptionalHand(JsonObject jsonObject, String elementName){
        if (jsonObject.has(elementName)) {
            return getHand(jsonObject, elementName);
        }
        return null;
    }

    default MobEffectCategory getMobEffectCategory(JsonObject jsonObject, String elementName){
        String effectCategoryName = getString(jsonObject, elementName);
        MobEffectCategory effectCategory;
        try {
            effectCategory = MobEffectCategory.valueOf(effectCategoryName);
        } catch (IllegalArgumentException e) {
            throw new JsonParseException("Invalid effect_category, expected to find a valid effect category ('BENEFICIAL', 'HARMFUL' or 'NEUTRAL').");
        }
        return effectCategory;
    }

    default ResourceKey<Level> getDimension(JsonObject jsonObject, String elementName){
        String dimensionName = getString(jsonObject, elementName);
        ResourceKey<Level> dimension;
        try {
            dimension = ResourceKey.create(Registries.DIMENSION, ResourceLocation.parse(dimensionName));
        } catch (IllegalArgumentException e) {
            throw new JsonParseException("Invalid dimension, expected to find a valid dimension.");
        }
        return dimension;
    }
}