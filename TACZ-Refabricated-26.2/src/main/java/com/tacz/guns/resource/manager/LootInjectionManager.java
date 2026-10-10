package com.tacz.guns.resource.manager;

import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.tacz.guns.GunMod;
import com.tacz.guns.resource.CommonAssetsManager;
import com.tacz.guns.resource.pojo.data.loot.LootTableInjection;
import com.tacz.guns.util.ResourceScanner;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class LootInjectionManager extends SimplePreparableReloadListener<Map<Identifier, List<JsonElement>>> implements IdentifiableResourceReloadListener {
    private final Map<Identifier, List<LootTableInjection>> injections = Maps.newHashMap();
    private final Gson gson = CommonAssetsManager.GSON;
    private final Marker marker = MarkerFactory.getMarker("LootInjection");
    private final FileToIdConverter fileToIdConverter = FileToIdConverter.json("tacz_loot_injectors");

    @Override
    protected @NotNull Map<Identifier, List<JsonElement>> prepare(ResourceManager resourceManager, ProfilerFiller profiler) {
        return ResourceScanner.scanDirectoryAll(resourceManager, fileToIdConverter, gson);
    }

    @Override
    protected void apply(Map<Identifier, List<JsonElement>> object, ResourceManager resourceManager, ProfilerFiller profiler) {
        injections.clear();
        for (Map.Entry<Identifier, List<JsonElement>> entry : object.entrySet()) {
            Identifier id = entry.getKey();
            for (JsonElement element : entry.getValue()) {
                try {
                    LootTableInjection injection = LootTableInjection.fromJson(id, element);
                    for (Identifier lootTable : injection.lootTables()) {
                        injections.computeIfAbsent(lootTable, key -> new java.util.ArrayList<>()).add(injection);
                    }
                } catch (JsonParseException | IllegalArgumentException e) {
                    GunMod.LOGGER.error(marker, "Failed to load loot injection {}", id, e);
                }
            }
        }
    }

    public List<LootTableInjection> getInjections(Identifier lootTable) {
        return injections.getOrDefault(lootTable, Collections.emptyList());
    }

    /**
     * 주입 대상으로 선언된 모든 전리품 표 ID.
     *
     * <p>{@code injections}가 원래 대상 표 ID를 키로 하므로 키 집합을 그대로 내보내면 되며,
     * 상태를 따로 관리할 필요가 없다. 호출하는 쪽이 잘못 고치지 않도록 바꿀 수 없는 보기를 돌려준다.</p>
     */
    public Set<Identifier> getInjectionTargets() {
        return Collections.unmodifiableSet(injections.keySet());
    }

    public static final Identifier ID = Identifier.fromNamespaceAndPath(GunMod.MOD_ID, "loot_injection_loader");

    @Override
    public Identifier getFabricId() {
        return ID;
    }
}