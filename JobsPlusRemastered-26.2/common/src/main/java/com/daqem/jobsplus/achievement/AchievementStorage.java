package com.daqem.jobsplus.achievement;

import com.daqem.jobsplus.JobsPlus;
import com.daqem.jobsplus.config.JobsPlusConfig;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelResource;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** 월드별 시즌 원장. 오프라인 중 완성되는 수동 양조도 UUID에 기록한다. 손상 파일은 초기화하지 않는다. */
public final class AchievementStorage
{
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Codec<Map<String, Map<String, AchievementProgress>>> CODEC = Codec.unboundedMap(
            Codec.STRING, Codec.unboundedMap(Codec.STRING, AchievementProgress.CODEC));
    private static final Map<String, Map<String, AchievementProgress>> SEASONS = new HashMap<>();
    private static Path path;
    private static MinecraftServer activeServer;
    private static boolean dirty;

    private AchievementStorage()
    {
    }

    public static void load(MinecraftServer server)
    {
        SEASONS.clear();
        activeServer = server;
        path = server.getWorldPath(LevelResource.ROOT).resolve("jobsplus-achievements.json");
        dirty = false;
        if (!Files.exists(path))
        {
            if (Files.exists(backupPath()))
            {
                loadBackup(new IOException("Achievement file is missing but a backup exists"));
            }
            seasonStartedAt();
            save();
            return;
        }
        try
        {
            SEASONS.putAll(read(path));
        }
        catch (Exception exception)
        {
            loadBackup(exception);
        }
        seasonStartedAt();
        save();
    }

    public static boolean isLoaded()
    {
        if (activeServer != null && path != null)
        {
            return true;
        }
        return false;
    }

    public static long seasonStartedAt()
    {
        AchievementProgress metadata = get(activeServer, new UUID(0L, 0L));
        long startedAt = metadata.value("season_started_at");
        if (startedAt == 0)
        {
            startedAt = System.currentTimeMillis();
            metadata.set("season_started_at", startedAt);
            dirty = true;
        }
        return startedAt;
    }

    private static void loadBackup(Exception originalFailure)
    {
        try
        {
            Map<String, Map<String, AchievementProgress>> recovered = read(backupPath());
            if (Files.exists(path))
            {
                Files.move(path, path.resolveSibling(path.getFileName() + ".corrupt-" + System.currentTimeMillis()));
            }
            SEASONS.putAll(recovered);
            dirty = true;
            JobsPlus.LOGGER.error("Achievement data recovered from backup; original data was preserved", originalFailure);
        }
        catch (Exception backupFailure)
        {
            backupFailure.addSuppressed(originalFailure);
            throw new IllegalStateException("Cannot safely load achievement data; refusing to erase progress", backupFailure);
        }
    }

    private static Map<String, Map<String, AchievementProgress>> read(Path source) throws IOException
    {
        try (Reader reader = Files.newBufferedReader(source, StandardCharsets.UTF_8))
        {
            JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
            if (!root.has("version") || root.get("version").getAsInt() != 1 || !root.has("seasons"))
            {
                throw new IOException("Unsupported achievement data version");
            }
            Map<String, Map<String, AchievementProgress>> loaded = CODEC.parse(JsonOps.INSTANCE, root.get("seasons")).getOrThrow();
            Map<String, Map<String, AchievementProgress>> result = new HashMap<>();
            for (Map.Entry<String, Map<String, AchievementProgress>> season : loaded.entrySet())
            {
                if (!season.getKey().matches("[A-Za-z0-9_-]{1,32}"))
                {
                    throw new IOException("Invalid achievement season ID");
                }
                for (String playerId : season.getValue().keySet())
                {
                    UUID.fromString(playerId);
                    for (String id : season.getValue().get(playerId).completed())
                    {
                        if (AchievementCatalog.get(id) == null)
                        {
                            throw new IOException("Unknown completed achievement: " + id);
                        }
                    }
                }
                result.put(season.getKey(), new HashMap<>(season.getValue()));
            }
            return result;
        }
    }

    public static String season()
    {
        String season = JobsPlusConfig.achievementSeason.get();
        if (season == null || season.isBlank())
        {
            return "season3";
        }
        return season;
    }

    public static AchievementProgress get(MinecraftServer server, UUID playerId)
    {
        if (server != activeServer || path == null)
        {
            throw new IllegalStateException("Achievement storage is not loaded for this server");
        }
        Map<String, AchievementProgress> players = SEASONS.computeIfAbsent(season(), key -> new HashMap<>());
        AchievementProgress progress = players.get(playerId.toString());
        if (progress == null)
        {
            progress = new AchievementProgress();
            players.put(playerId.toString(), progress);
            dirty = true;
        }
        return progress;
    }

    public static void markDirty()
    {
        dirty = true;
    }

    public static boolean save()
    {
        if (path == null)
        {
            return false;
        }
        if (!dirty)
        {
            return true;
        }
        Path temporary = path.resolveSibling(path.getFileName() + ".tmp");
        try
        {
            JsonElement seasons = CODEC.encodeStart(JsonOps.INSTANCE, SEASONS).getOrThrow();
            JsonObject root = new JsonObject();
            root.addProperty("version", 1);
            root.add("seasons", seasons);
            Files.writeString(temporary, GSON.toJson(root), StandardCharsets.UTF_8);
            if (Files.exists(path))
            {
                Files.copy(path, backupPath(), StandardCopyOption.REPLACE_EXISTING);
            }
            replace(temporary, path);
            dirty = false;
            return true;
        }
        catch (Exception exception)
        {
            JobsPlus.LOGGER.error("Failed to save achievement data; progress remains dirty for retry", exception);
            return false;
        }
    }

    public static void replace(Path source, Path destination) throws IOException
    {
        try
        {
            Files.move(source, destination, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
        }
        catch (AtomicMoveNotSupportedException exception)
        {
            Files.move(source, destination, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private static Path backupPath()
    {
        return path.resolveSibling(path.getFileName() + ".bak");
    }

    public static void close()
    {
        SEASONS.clear();
        path = null;
        activeServer = null;
        dirty = false;
    }
}
