package com.daqem.arc.data;

import com.daqem.arc.Arc;
import com.daqem.arc.ArcExpectPlatform;
import com.daqem.arc.api.action.IAction;
import com.daqem.arc.api.action.holder.ActionHolderManager;
import com.daqem.arc.api.action.type.IActionType;
import com.daqem.arc.config.ArcCommonConfig;
import com.daqem.arc.registry.ArcRegistry;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.gson.*;
import com.mojang.serialization.JsonOps;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeMap;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

public class ActionManager extends SimplePreparableReloadListener<List<IAction>> {

    @Override
    protected @NotNull List<IAction> prepare(ResourceManager resourceManager, ProfilerFiller profilerFiller) {
        Map<ResourceLocation, Resource> resourceMap = resourceManager.listResources("arc", (resourceLocation) ->
                        resourceLocation.getPath().endsWith(".json")).entrySet().stream()
                .collect(Collectors.toMap(entry ->
                                ResourceLocation.fromNamespaceAndPath(
                                        entry.getKey().getNamespace(),
                                        entry.getKey().getPath()
                                                .substring(0, entry.getKey().getPath().length() - ".json".length())
                                                .substring("arc/".length())),
                        Map.Entry::getValue));

        Map<ResourceLocation, JsonElement> map = new HashMap<>();
        for (Map.Entry<ResourceLocation, Resource> entry : resourceMap.entrySet()) {
            ResourceLocation location = entry.getKey();
            try {
                JsonElement jsonElement = GsonHelper.parse(entry.getValue().openAsReader());
                map.put(location, jsonElement);
            }
            catch (Exception runtimeException) {
                Arc.LOGGER.error("Parsing error loading action {}", location, runtimeException);
            }
        }
        List<IAction> actions = new ArrayList<>();

        if (!Arc.isDebugEnvironment()) {
            map.entrySet().removeIf(entry -> entry.getKey().getNamespace().equals("debug"));
        }

        for (Map.Entry<ResourceLocation, JsonElement> entry : map.entrySet()) {
            ResourceLocation location = entry.getKey();
            try {
                IAction action = fromJson(location, GsonHelper.convertToJsonObject(entry.getValue(), "top element"));
                actions.add(action);
            }
            catch (JsonParseException | IllegalArgumentException runtimeException) {
                Arc.LOGGER.error("Parsing error loading action {}", location, runtimeException);
            }
        }

        return actions;
    }

    @Override
    protected void apply(List<IAction> actions, ResourceManager resourceManager, ProfilerFiller profilerFiller) {
        ActionHolderManager actionHolderManager = ActionHolderManager.getInstance();
        actionHolderManager.clearAllActions();
        actionHolderManager.registerActions(actions);
        Arc.LOGGER.info("Loaded {} actions", actions.size());
    }

    /**
     * Parses a JSON object and returns the corresponding IAction instance.
     *
     * @param location the resource location of the IAction
     * @param jsonObject the JSON object representing the IAction
     * @return the parsed IAction instance
     * @throws JsonSyntaxException if the JSON object is invalid or if the action type is unsupported
     */
    public static IAction fromJson(ResourceLocation location, JsonObject jsonObject) {
        String type = GsonHelper.getAsString(jsonObject, "type");
        return ArcRegistry.ACTION.getOptional(ResourceLocation.parse(type))
                .orElseThrow(() -> new JsonSyntaxException("Invalid or unsupported action type '" + type + "'"))
                .getSerializer().fromJson(location, jsonObject);
    }
}
