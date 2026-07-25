package com.daqem.itemrestrictions.data;

import com.daqem.itemrestrictions.ItemRestrictions;
import com.daqem.itemrestrictions.ItemRestrictionsExpectPlatform;
import com.google.common.collect.ImmutableMap;
import com.google.gson.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ItemRestrictionManager extends SimplePreparableReloadListener<List<ItemRestriction>> {

    public static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping()
            .registerTypeHierarchyAdapter(ItemRestriction.class, new ItemRestriction.Serializer())
            .create();

    private ImmutableMap<ResourceLocation, ItemRestriction> itemRestrictions = ImmutableMap.of();

    private static ItemRestrictionManager instance;

    public ItemRestrictionManager() {
        instance = this;
    }

    @Override
    protected @NotNull List<ItemRestriction> prepare(ResourceManager resourceManager, ProfilerFiller profilerFiller) {
        Map<ResourceLocation, Resource> resourceMap = resourceManager.listResources("itemrestrictions/restrictions", (resourceLocation) ->
                        resourceLocation.getPath().endsWith(".json")).entrySet().stream()
                .collect(Collectors.toMap(entry ->
                                ResourceLocation.fromNamespaceAndPath(
                                        entry.getKey().getNamespace(),
                                        entry.getKey().getPath()
                                                .substring(0, entry.getKey().getPath().length() - ".json".length())
                                                .substring("itemrestrictions/restrictions/".length())),
                        Map.Entry::getValue));

        Map<ResourceLocation, JsonObject> map = new HashMap<>();
        for (Map.Entry<ResourceLocation, Resource> entry : resourceMap.entrySet()) {
            ResourceLocation location = entry.getKey();
            try {
                JsonObject jsonElement = GsonHelper.parse(entry.getValue().openAsReader());
                map.put(location, jsonElement);
            }
            catch (Exception runtimeException) {
                ItemRestrictions.LOGGER.error("Parsing error loading item restriction {}", location, runtimeException);
            }
        }
        List<ItemRestriction> itemRestrictions = new ArrayList<>();

        if (!ItemRestrictions.isDebugEnvironment()) {
            map.entrySet().removeIf(entry -> entry.getKey().getNamespace().equals("debug"));
        }

        for (Map.Entry<ResourceLocation, JsonObject> entry : map.entrySet()) {
            ResourceLocation location = entry.getKey();
            JsonObject jsonObject = entry.getValue();
            jsonObject.addProperty("location", location.toString());
            try {
                ItemRestriction itemRestriction = GSON.fromJson(entry.getValue(), ItemRestriction.class);
                itemRestrictions.add(itemRestriction);
            }
            catch (JsonParseException | IllegalArgumentException runtimeException) {
                ItemRestrictions.LOGGER.error("Parsing error loading item restriction {}", location, runtimeException);
            }
        }

        return itemRestrictions;
    }

    @Override
    protected void apply(List<ItemRestriction> object, ResourceManager resourceManager, ProfilerFiller profilerFiller) {
        ItemRestrictions.LOGGER.info("Loaded {} item restrictions", object.size());
        this.itemRestrictions = object.stream()
                .collect(ImmutableMap.toImmutableMap(
                        ItemRestriction::getLocation,
                        itemRestriction -> itemRestriction
                ));
    }

    public static ItemRestrictionManager getInstance() {
        return instance != null ? instance : ItemRestrictionsExpectPlatform.getItemRestrictionManager();
    }

    public List<ItemRestriction> getItemRestrictions() {
        return itemRestrictions.values().asList();
    }

    public ItemRestriction getItemRestriction(ResourceLocation location) {
        return itemRestrictions.get(location);
    }

    public void setItemRestrictions(List<ItemRestriction> itemRestrictions) {
        this.itemRestrictions = itemRestrictions.stream()
                .collect(ImmutableMap.toImmutableMap(
                        ItemRestriction::getLocation,
                        itemRestriction -> itemRestriction
                ));
    }
}
