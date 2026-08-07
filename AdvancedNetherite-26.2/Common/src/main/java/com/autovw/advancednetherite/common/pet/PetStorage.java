package com.autovw.advancednetherite.common.pet;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.logging.LogUtils;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelResource;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 플레이어별 펫 소유 기록 저장소.
 * <p>
 * 월드 폴더의 JSON 파일 하나로 관리한다. 펫 엔티티는 접속을 종료할 때 회수되므로
 * 이 저장소가 소유의 유일한 원본이다.
 * <p>
 * 펫 획득처럼 되돌릴 수 없는 변경은 즉시 파일에 기록하고, ON/OFF 토글처럼 잦은 변경은
 * 표시만 해 두었다가 주기적으로 함께 기록한다. 토글마다 파일 전체를 다시 쓰면
 * 토글을 연타하는 것만으로 서버가 디스크 작업에 묶이기 때문이다.
 */
public final class PetStorage
{
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String FILE_NAME = "advancednetherite_pets.json";
    private static final String BACKUP_FILE_NAME = FILE_NAME + ".bak";
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    /** 플레이어 UUID → 소유한 펫 기록 목록 */
    private static final Map<UUID, List<PetRecord>> PETS = new ConcurrentHashMap<>();
    private static Path savePath;
    private static volatile boolean saveEnabled;

    /** 아직 파일에 반영되지 않은 변경이 있는지 여부 */
    private static volatile boolean dirty;

    private PetStorage()
    {
    }

    public static synchronized void load(MinecraftServer server)
    {
        PETS.clear();
        savePath = server.getWorldPath(LevelResource.ROOT).resolve(FILE_NAME);
        saveEnabled = false;
        dirty = false;
        if (!Files.exists(savePath))
        {
            saveEnabled = true;
            return;
        }

        try
        {
            PETS.putAll(parsePets(savePath));
            saveEnabled = true;
            LOGGER.info("Loaded pet records for {} players", PETS.size());
            return;
        }
        catch (Exception exception)
        {
            LOGGER.error("Failed to read pet records. Trying backup file.", exception);
        }
        loadFromBackup();
    }

    /**
     * 본 파일이 손상됐을 때 마지막 정상 저장본으로 복구한다.
     * 백업까지 읽을 수 없으면 기존 파일을 보호하기 위해 저장을 잠근다.
     */
    private static void loadFromBackup()
    {
        Path backupPath = savePath.resolveSibling(BACKUP_FILE_NAME);
        if (!Files.exists(backupPath))
        {
            LOGGER.error(
                    "Failed to read pet records and no backup exists. Pet saving is locked to protect the file.");
            return;
        }

        try
        {
            Map<UUID, List<PetRecord>> loadedPets = parsePets(backupPath);
            PETS.putAll(loadedPets);
            saveEnabled = true;
            LOGGER.warn(
                    "Restored pet records from backup for {} players. Recent changes may be missing.", PETS.size());
        }
        catch (Exception exception)
        {
            LOGGER.error(
                    "Backup pet records are also unreadable. Pet saving is locked to protect the files.", exception);
        }
    }

    private static Map<UUID, List<PetRecord>> parsePets(Path path) throws IOException
    {
        Map<UUID, List<PetRecord>> loadedPets = new HashMap<>();
        try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8))
        {
            JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
            for (String playerId : root.keySet())
            {
                List<PetRecord> records = new ArrayList<>();
                JsonArray entries = root.getAsJsonArray(playerId);
                entries.forEach(entry -> {
                    JsonObject pet = entry.getAsJsonObject();
                    records.add(new PetRecord(
                            UUID.fromString(pet.get("id").getAsString()),
                            pet.get("type").getAsString(),
                            pet.get("attackDamage").getAsDouble(),
                            pet.get("enabled").getAsBoolean()));
                });
                loadedPets.put(UUID.fromString(playerId), records);
            }
        }
        return loadedPets;
    }

    public static synchronized boolean save()
    {
        if (savePath == null || !saveEnabled)
        {
            return false;
        }

        JsonObject root = new JsonObject();
        PETS.forEach((playerId, records) -> {
            JsonArray entries = new JsonArray();
            for (PetRecord record : records)
            {
                JsonObject pet = new JsonObject();
                pet.addProperty("id", record.id().toString());
                pet.addProperty("type", record.petTypeId());
                pet.addProperty("attackDamage", record.attackDamage());
                pet.addProperty("enabled", record.enabled());
                entries.add(pet);
            }
            root.add(playerId.toString(), entries);
        });

        Path temporaryPath = savePath.resolveSibling(FILE_NAME + ".tmp");
        try (Writer writer = Files.newBufferedWriter(temporaryPath,
                StandardCharsets.UTF_8,
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING,
                StandardOpenOption.WRITE))
        {
            GSON.toJson(root, writer);
        }
        catch (Exception exception)
        {
            LOGGER.error("Failed to write pet records", exception);
            return false;
        }

        backupCurrentFile();
        try
        {
            moveTemporaryFile(temporaryPath);
            dirty = false;
            return true;
        }
        catch (IOException exception)
        {
            LOGGER.error("Failed to replace pet records", exception);
            return false;
        }
    }

    /**
     * 밀린 변경이 있을 때만 파일에 기록한다. 서버가 주기적으로 호출한다.
     * 기록에 실패하면 표시가 남아 다음 호출에서 다시 시도한다.
     */
    public static synchronized void saveIfDirty()
    {
        if (!dirty)
        {
            return;
        }
        save();
    }

    private static void backupCurrentFile()
    {
        if (!Files.exists(savePath))
        {
            return;
        }
        Path backupPath = savePath.resolveSibling(BACKUP_FILE_NAME);
        try
        {
            Files.copy(savePath, backupPath, StandardCopyOption.REPLACE_EXISTING);
        }
        catch (IOException exception)
        {
            LOGGER.warn("Failed to back up pet records", exception);
        }
    }

    private static void moveTemporaryFile(Path temporaryPath) throws IOException
    {
        try
        {
            Files.move(temporaryPath,
                    savePath,
                    StandardCopyOption.ATOMIC_MOVE,
                    StandardCopyOption.REPLACE_EXISTING);
        }
        catch (AtomicMoveNotSupportedException exception)
        {
            Files.move(temporaryPath, savePath, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    /** 플레이어의 펫 기록 사본을 돌려준다. */
    public static List<PetRecord> getPets(UUID playerId)
    {
        List<PetRecord> records = PETS.get(playerId);
        if (records == null)
        {
            return List.of();
        }
        synchronized (PetStorage.class)
        {
            return List.copyOf(records);
        }
    }

    public static PetRecord findPet(UUID playerId, UUID recordId)
    {
        for (PetRecord record : getPets(playerId))
        {
            if (record.id().equals(recordId))
            {
                return record;
            }
        }
        return null;
    }

    public static synchronized boolean addPet(UUID playerId, PetRecord record)
    {
        if (!saveEnabled)
        {
            return false;
        }
        List<PetRecord> records = PETS.computeIfAbsent(playerId, (UUID key) -> new ArrayList<>());
        records.add(record);
        if (save())
        {
            return true;
        }
        records.remove(record);
        return false;
    }

    /**
     * 기록의 ON/OFF 상태를 바꾸고 갱신된 기록을 돌려준다. 대상이 없으면 null.
     * <p>
     * 파일 기록은 {@link #saveIfDirty()}에 맡긴다. 아이템 획득과 달리 토글은
     * 되돌릴 수 없는 손해가 없어서, 저장 직전에 서버가 죽으면 이전 상태로 남는 정도를 감수한다.
     */
    public static synchronized PetRecord setEnabled(UUID playerId, UUID recordId, boolean enabled)
    {
        if (!saveEnabled)
        {
            return null;
        }
        List<PetRecord> records = PETS.get(playerId);
        if (records == null)
        {
            return null;
        }
        for (int i = 0; i < records.size(); i++)
        {
            if (!records.get(i).id().equals(recordId))
            {
                continue;
            }
            PetRecord updated = records.get(i).withEnabled(enabled);
            records.set(i, updated);
            dirty = true;
            return updated;
        }
        return null;
    }

    public static boolean isAvailable()
    {
        return saveEnabled;
    }
}
