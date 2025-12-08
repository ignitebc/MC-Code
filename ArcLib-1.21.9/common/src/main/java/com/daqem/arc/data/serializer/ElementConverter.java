package com.daqem.arc.data.serializer;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ElementConverter<T> {

    private final Registry<T> registry;

    public ElementConverter(Registry<T> registry) {
        this.registry = registry;
    }

    public List<T> convertToElements(List<String> elements) {
        return elements.stream()
                .filter(elementLoc -> !elementLoc.startsWith("#"))
                .filter(elementLoc -> elementLoc.contains(":"))
                .map(convertToElement())
                .collect(Collectors.toList());
    }

    public List<TagKey<T>> convertToElementTags(List<String> elementTags) {
        return elementTags.stream()
                .filter(elementLoc -> elementLoc.startsWith("#"))
                .filter(elementLoc -> elementLoc.contains(":"))
                .map(replaceHashAndConvertToTag())
                .collect(Collectors.toList());
    }

    public T convertToElement(String element) {
        T type = registry.get(ResourceLocation.parse(element)).map(Holder.Reference::value).orElse(null);

        if (type instanceof Block && element.equals("minecraft:air")) {
            return type;
        }
        else if (type instanceof Item && element.equals("minecraft:air")) {
            return type;
        }
        else if (type instanceof MobEffect && element.equals("minecraft:luck")) {
            return type;
        }
        else if (type instanceof EntityType<?> && element.equals("minecraft:pig")) {
            return type;
        }

        // If not found in the registry, it will return the default value for the type.
        // This checks if the element is actually in the registry.
        else if (type == registry.get(ResourceLocation.parse("x")) || type == null) {
            throw new IllegalArgumentException(element + " could not be found in registry " + registry.key().location());
        }

        return type;
    }

    private Function<String, T> convertToElement() {
        return elementLoc -> registry.get(
                ResourceLocation.parse(elementLoc)).map(Holder.Reference::value).orElseThrow(
                () -> new IllegalArgumentException(elementLoc + " could not be found in registry " + registry.key().location()));
    }

    private Function<String, TagKey<T>> replaceHashAndConvertToTag() {
        return elementLoc -> TagKey.create(
                registry.key(),
                ResourceLocation.parse(
                        elementLoc.replace("#", "")));
    }
}
