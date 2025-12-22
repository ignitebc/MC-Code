package com.autovw.advancednetherite.common.randombox;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public final class RandomBoxConfigManager {

    private static final Gson GSON = new GsonBuilder().create();
    private static final Map<ResourceLocation, RandomBoxConfig> CACHE = new ConcurrentHashMap<>();

    private RandomBoxConfigManager() {
    }

    public static RandomBoxConfig get(MinecraftServer server, ResourceLocation configId) {
        RandomBoxConfig cached = CACHE.get(configId);
        if (cached != null)
            return cached;

        // data/<ns>/random_box/<path>.json
        ResourceLocation resLoc = ResourceLocation.fromNamespaceAndPath(configId.getNamespace(),"random_box/" + configId.getPath() + ".json");

        ResourceManager rm = server.getResourceManager();
        Optional<Resource> resOpt = rm.getResource(resLoc);
        if (resOpt.isEmpty())
            return null;

        Resource res = resOpt.get();

        try (InputStream is = res.open();
                BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {

            RandomBoxConfig config = GSON.fromJson(br, RandomBoxConfig.class);
            if (config == null)
                return null;

            CACHE.put(configId, config);
            return config;

        } catch (IOException | JsonSyntaxException e) {
            // 요구사항: 로그 불필요 -> 조용히 실패 처리
            return null;
        }
    }

    public static void clearCache() {
        CACHE.clear();
    }
}
