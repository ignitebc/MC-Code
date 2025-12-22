package com.autovw.advancednetherite.common.randombox;

import com.google.gson.*;
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

    private static final Gson GSON = new GsonBuilder().setLenient().create();
    private static final Map<ResourceLocation, RandomBoxConfig> CACHE = new ConcurrentHashMap<>();

    private RandomBoxConfigManager() {}

    public static RandomBoxConfig get(MinecraftServer server, ResourceLocation configId) {
        RandomBoxConfig cached = CACHE.get(configId);
        if (cached != null) return cached;

        // data/<ns>/random_box/<path>.json
        ResourceLocation resLoc = ResourceLocation.fromNamespaceAndPath(
            configId.getNamespace(),
            "random_box/" + configId.getPath() + ".json"
        );

        ResourceManager rm = server.getResourceManager();
        Optional<Resource> resOpt = rm.getResource(resLoc);
        if (resOpt.isEmpty()) {
            return null;
        }

        Resource res = resOpt.get();

        try (InputStream is = res.open();
             BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {

            JsonElement root = JsonParser.parseReader(br);
            if (root == null || !root.isJsonObject()) return null;

            RandomBoxConfig config = parseConfig(root.getAsJsonObject());
            if (config == null) return null;

            CACHE.put(configId, config);
            return config;

        } catch (IOException | JsonParseException e) {
            // 요구사항대로 조용히 실패
            return null;
        }
    }

    private static RandomBoxConfig parseConfig(JsonObject obj) {
        RandomBoxConfig cfg = new RandomBoxConfig();

        // optional (현재 RandomBoxItem에서는 사용 안 함)
        cfg.item = readResLoc(obj, "item");

        // required
        cfg.required_key = readResLoc(obj, "required_key");

        // consume (optional)
        if (obj.has("consume") && obj.get("consume").isJsonObject()) {
            JsonObject c = obj.getAsJsonObject("consume");
            RandomBoxConfig.Consume consume = new RandomBoxConfig.Consume();
            consume.box = readInt(c, "box", 1);
            consume.key = readInt(c, "key", 1);
            cfg.consume = consume;
        }

        // roll_mode (optional, default INDEPENDENT)
        cfg.roll_mode = readRollMode(obj, "roll_mode", RandomBoxConfig.RollMode.INDEPENDENT);

        // rewards
        if (!obj.has("rewards") || !obj.get("rewards").isJsonArray()) {
            cfg.rewards = null;
            return cfg;
        }

        JsonArray arr = obj.getAsJsonArray("rewards");
        java.util.List<RandomBoxConfig.Reward> list = new java.util.ArrayList<>();

        for (JsonElement el : arr) {
            if (el == null || !el.isJsonObject()) continue;
            JsonObject rObj = el.getAsJsonObject();

            RandomBoxConfig.Reward r = new RandomBoxConfig.Reward();
            r.item = readResLoc(rObj, "item");
            r.count = readInt(rObj, "count", 1);
            r.chance = readDouble(rObj, "chance", 1.0);

            // item이 없으면 스킵
            if (r.item == null) continue;

            list.add(r);
        }

        cfg.rewards = list;
        return cfg;
    }

    private static ResourceLocation readResLoc(JsonObject obj, String key) {
        if (!obj.has(key)) return null;

        JsonElement el = obj.get(key);
        if (el == null || el.isJsonNull()) return null;

        // 문자열 "minecraft:diamond" 형태 지원
        if (el.isJsonPrimitive() && el.getAsJsonPrimitive().isString()) {
            return ResourceLocation.tryParse(el.getAsString());
        }

        // 혹시 객체 형태로 들어온 경우 {"namespace":"minecraft","path":"diamond"}도 지원
        if (el.isJsonObject()) {
            JsonObject o = el.getAsJsonObject();
            String ns = o.has("namespace") ? safeString(o.get("namespace")) : null;
            String path = o.has("path") ? safeString(o.get("path")) : null;
            if (ns != null && path != null) {
                return ResourceLocation.fromNamespaceAndPath(ns, path);
            }
        }

        return null;
    }

    private static String safeString(JsonElement el) {
        if (el == null || el.isJsonNull()) return null;
        if (!el.isJsonPrimitive()) return null;
        JsonPrimitive p = el.getAsJsonPrimitive();
        if (!p.isString()) return null;
        return p.getAsString();
    }

    private static int readInt(JsonObject obj, String key, int def) {
        try {
            if (!obj.has(key)) return def;
            JsonElement el = obj.get(key);
            if (el == null || el.isJsonNull()) return def;
            if (el.isJsonPrimitive() && el.getAsJsonPrimitive().isNumber()) return el.getAsInt();
            if (el.isJsonPrimitive() && el.getAsJsonPrimitive().isString()) return Integer.parseInt(el.getAsString());
            return def;
        } catch (Exception ignored) {
            return def;
        }
    }

    private static double readDouble(JsonObject obj, String key, double def) {
        try {
            if (!obj.has(key)) return def;
            JsonElement el = obj.get(key);
            if (el == null || el.isJsonNull()) return def;
            if (el.isJsonPrimitive() && el.getAsJsonPrimitive().isNumber()) return el.getAsDouble();
            if (el.isJsonPrimitive() && el.getAsJsonPrimitive().isString()) return Double.parseDouble(el.getAsString());
            return def;
        } catch (Exception ignored) {
            return def;
        }
    }

    private static RandomBoxConfig.RollMode readRollMode(JsonObject obj, String key, RandomBoxConfig.RollMode def) {
        if (!obj.has(key)) return def;
        JsonElement el = obj.get(key);
        if (el == null || el.isJsonNull()) return def;

        String s = null;
        if (el.isJsonPrimitive() && el.getAsJsonPrimitive().isString()) s = el.getAsString();
        if (s == null) return def;

        s = s.trim().toUpperCase();
        if ("SINGLE".equals(s)) return RandomBoxConfig.RollMode.SINGLE;
        if ("INDEPENDENT".equals(s)) return RandomBoxConfig.RollMode.INDEPENDENT;

        return def;
    }

    public static void clearCache() {
        CACHE.clear();
    }
}
