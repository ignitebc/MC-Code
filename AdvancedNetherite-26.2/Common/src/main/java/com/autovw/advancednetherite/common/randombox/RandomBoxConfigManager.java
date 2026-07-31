package com.autovw.advancednetherite.common.randombox;

import com.google.gson.*;
import com.mojang.logging.LogUtils;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import org.slf4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public final class RandomBoxConfigManager {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Gson GSON = new GsonBuilder().setLenient().create();
    private static final Map<Identifier, RandomBoxConfig> CACHE = new ConcurrentHashMap<>();

    private RandomBoxConfigManager() {}

    public static RandomBoxConfig get(MinecraftServer server, Identifier configId) {
        RandomBoxConfig cached = CACHE.get(configId);
        if (cached != null) return cached;

        // data/<ns>/random_box/<path>.json
        Identifier resLoc = Identifier.fromNamespaceAndPath(
            configId.getNamespace(),
            "random_box/" + configId.getPath() + ".json"
        );

        ResourceManager rm = server.getResourceManager();
        Optional<Resource> resOpt = rm.getResource(resLoc);
        if (resOpt.isEmpty()) {
            LOGGER.warn("RandomBox config file not found: {}", resLoc);
            return null;
        }

        Resource res = resOpt.get();

        try (InputStream is = res.open();
             BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {

            JsonElement root = JsonParser.parseReader(br);
            if (root == null || !root.isJsonObject()) {
                LOGGER.error("RandomBox config {} is not a JSON object", resLoc);
                return null;
            }

            RandomBoxConfig config = parseConfig(resLoc, root.getAsJsonObject());
            if (config == null) return null;

            CACHE.put(configId, config);
            return config;

        } catch (IOException | JsonParseException e) {
            LOGGER.error("Failed to read RandomBox config {}", resLoc, e);
            return null;
        }
    }

    private static RandomBoxConfig parseConfig(Identifier source, JsonObject obj) {
        RandomBoxConfig cfg = new RandomBoxConfig();

        // optional (현재 RandomBoxItem에서는 사용 안 함)
        cfg.item = readResLoc(obj, "item");

        // required
        cfg.required_key = readResLoc(obj, "required_key");
        if (cfg.required_key == null) {
            LOGGER.error("RandomBox config {}: required_key is missing or invalid", source);
        }

        // consume (optional)
        if (obj.has("consume") && obj.get("consume").isJsonObject()) {
            JsonObject c = obj.getAsJsonObject("consume");
            RandomBoxConfig.Consume consume = new RandomBoxConfig.Consume();
            consume.box = readInt(source, c, "box", 1);
            consume.key = readInt(source, c, "key", 1);
            cfg.consume = consume;
        }

        // roll_mode (optional, 생략 시 SINGLE)
        cfg.roll_mode = readRollMode(source, obj, "roll_mode", RandomBoxConfig.RollMode.SINGLE);

        // rewards
        if (!obj.has("rewards") || !obj.get("rewards").isJsonArray()) {
            LOGGER.error("RandomBox config {}: rewards array is missing", source);
            cfg.rewards = null;
            return cfg;
        }

        JsonArray arr = obj.getAsJsonArray("rewards");
        java.util.List<RandomBoxConfig.Reward> list = new java.util.ArrayList<>();

        for (int index = 0; index < arr.size(); index++) {
            JsonElement el = arr.get(index);
            if (el == null || !el.isJsonObject()) {
                LOGGER.warn("RandomBox config {}: rewards[{}] is not an object, skipping", source, index);
                continue;
            }
            JsonObject rObj = el.getAsJsonObject();

            RandomBoxConfig.Reward r = new RandomBoxConfig.Reward();
            r.item = readResLoc(rObj, "item");
            r.count = readInt(source, rObj, "count", 1);
            r.chance = readDouble(source, rObj, "chance", 1.0);

            if (r.item == null) {
                LOGGER.warn("RandomBox config {}: rewards[{}] has missing or invalid item id, skipping", source, index);
                continue;
            }
            if (r.count <= 0) {
                LOGGER.warn("RandomBox config {}: rewards[{}] ({}) has invalid count {}, skipping",
                        source, index, r.item, r.count);
                continue;
            }
            if (Double.isNaN(r.chance) || Double.isInfinite(r.chance) || r.chance <= 0.0) {
                LOGGER.warn("RandomBox config {}: rewards[{}] ({}) has invalid chance {}, skipping",
                        source, index, r.item, r.chance);
                continue;
            }

            list.add(r);
        }

        if (list.isEmpty()) {
            LOGGER.error("RandomBox config {}: no valid reward entries", source);
        }

        cfg.rewards = list;
        return cfg;
    }

    private static Identifier readResLoc(JsonObject obj, String key) {
        if (!obj.has(key)) return null;

        JsonElement el = obj.get(key);
        if (el == null || el.isJsonNull()) return null;

        // 문자열 "minecraft:diamond" 형태 지원
        if (el.isJsonPrimitive() && el.getAsJsonPrimitive().isString()) {
            return Identifier.tryParse(el.getAsString());
        }

        // 혹시 객체 형태로 들어온 경우 {"namespace":"minecraft","path":"diamond"}도 지원
        if (el.isJsonObject()) {
            JsonObject o = el.getAsJsonObject();
            String ns = o.has("namespace") ? safeString(o.get("namespace")) : null;
            String path = o.has("path") ? safeString(o.get("path")) : null;
            if (ns != null && path != null) {
                return Identifier.fromNamespaceAndPath(ns, path);
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

    private static int readInt(Identifier source, JsonObject obj, String key, int def) {
        if (!obj.has(key)) return def;
        JsonElement el = obj.get(key);
        if (el == null || el.isJsonNull()) return def;

        try {
            if (el.isJsonPrimitive() && el.getAsJsonPrimitive().isNumber()) return el.getAsInt();
            if (el.isJsonPrimitive() && el.getAsJsonPrimitive().isString()) return Integer.parseInt(el.getAsString());
        } catch (Exception e) {
            LOGGER.warn("RandomBox config {}: field '{}' has unparseable value {}, using default {}",
                    source, key, el, def);
            return def;
        }

        LOGGER.warn("RandomBox config {}: field '{}' has unexpected type {}, using default {}",
                source, key, el, def);
        return def;
    }

    private static double readDouble(Identifier source, JsonObject obj, String key, double def) {
        if (!obj.has(key)) return def;
        JsonElement el = obj.get(key);
        if (el == null || el.isJsonNull()) return def;

        try {
            if (el.isJsonPrimitive() && el.getAsJsonPrimitive().isNumber()) return el.getAsDouble();
            if (el.isJsonPrimitive() && el.getAsJsonPrimitive().isString()) return Double.parseDouble(el.getAsString());
        } catch (Exception e) {
            LOGGER.warn("RandomBox config {}: field '{}' has unparseable value {}, using default {}",
                    source, key, el, def);
            return def;
        }

        LOGGER.warn("RandomBox config {}: field '{}' has unexpected type {}, using default {}",
                source, key, el, def);
        return def;
    }

    private static RandomBoxConfig.RollMode readRollMode(Identifier source, JsonObject obj, String key,
                                                         RandomBoxConfig.RollMode def) {
        if (!obj.has(key)) return def;
        JsonElement el = obj.get(key);
        if (el == null || el.isJsonNull()) return def;

        String s = null;
        if (el.isJsonPrimitive() && el.getAsJsonPrimitive().isString()) s = el.getAsString();
        if (s == null) {
            LOGGER.warn("RandomBox config {}: roll_mode has unexpected type {}, using default {}", source, el, def);
            return def;
        }

        s = s.trim().toUpperCase();
        if ("SINGLE".equals(s)) return RandomBoxConfig.RollMode.SINGLE;
        if ("INDEPENDENT".equals(s)) return RandomBoxConfig.RollMode.INDEPENDENT;

        LOGGER.warn("RandomBox config {}: unknown roll_mode '{}', using default {}", source, s, def);
        return def;
    }

    public static void clearCache() {
        CACHE.clear();
    }
}
