package com.daqem.arc.data;

import com.daqem.arc.Arc;
import com.daqem.arc.api.action.IAction;
import com.daqem.arc.api.action.holder.ActionHolderManager;
import com.daqem.arc.registry.ArcRegistry;
import com.google.gson.*;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

public class ActionManager extends SimplePreparableReloadListener<List<IAction>> {

    @Override
    protected @NotNull List<IAction> prepare(ResourceManager resourceManager, ProfilerFiller profilerFiller) {
        Map<Identifier, Resource> resourceMap = resourceManager.listResources("arc", (resourceLocation) ->
                        resourceLocation.getPath().endsWith(".json")).entrySet().stream()
                .collect(Collectors.toMap(entry ->
                                Identifier.fromNamespaceAndPath(
                                        entry.getKey().getNamespace(),
                                        entry.getKey().getPath()
                                                .substring(0, entry.getKey().getPath().length() - ".json".length())
                                                .substring("arc/".length())),
                        Map.Entry::getValue));

        Map<Identifier, JsonElement> map = new HashMap<>();
        for (Map.Entry<Identifier, Resource> entry : resourceMap.entrySet()) {
            Identifier location = entry.getKey();
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

        for (Map.Entry<Identifier, JsonElement> entry : map.entrySet()) {
            Identifier location = entry.getKey();
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
     * JSON 객체를 해석해 해당하는 IAction 인스턴스를 돌려준다.
     *
     * @param location IAction의 리소스 위치
     * @param jsonObject IAction을 나타내는 JSON 객체
     * @return 해석한 IAction 인스턴스
     * @throws JsonSyntaxException JSON 객체가 잘못되었거나 지원하지 않는 액션 종류일 때
     */
    public static IAction fromJson(Identifier location, JsonObject jsonObject) {
        String type = GsonHelper.getAsString(jsonObject, "type");
        return ArcRegistry.ACTION.getOptional(Identifier.parse(type))
                .orElseThrow(() -> new JsonSyntaxException("Invalid or unsupported action type '" + type + "'"))
                .getSerializer().fromJson(location, jsonObject);
    }
}
