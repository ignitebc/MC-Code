package com.daqem.itemrestrictions.chunk;

import com.daqem.itemrestrictions.ItemRestrictions;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.LevelResource;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 청크 소유권 저장소.
 * <p>
 * 월드 폴더의 JSON 파일 하나로 관리한다. 구매는 드물게 일어나고 조회는 블록 조작마다 일어나므로,
 * 메모리에 전부 올려 두고 변경이 생길 때만 파일에 기록한다.
 */
public final class ChunkOwnership {

    private static final String FILE_NAME = "chunk_owners.json";
    private static final String BACKUP_FILE_NAME = FILE_NAME + ".bak";
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    /** 차원별 청크 소유자. 키는 {@link ChunkPos#pack(int, int)} 값이다. */
    private static final Map<Identifier, Map<Long, Owner>> OWNERS = new ConcurrentHashMap<>();
    @Nullable
    private static Path savePath;
    private static volatile boolean saveEnabled;

    private ChunkOwnership() {
    }

    public record Owner(UUID uuid, @Nullable String name) {
    }

    public static synchronized void load(MinecraftServer server) {
        OWNERS.clear();
        savePath = server.getWorldPath(LevelResource.ROOT).resolve(FILE_NAME);
        saveEnabled = false;
        if (!Files.exists(savePath)) {
            saveEnabled = true;
            return;
        }

        try {
            OWNERS.putAll(parseOwners(savePath));
            saveEnabled = true;
            ItemRestrictions.LOGGER.info("Loaded chunk ownership for {} dimensions", OWNERS.size());
            return;
        } catch (Exception exception) {
            ItemRestrictions.LOGGER.error(
                    "Failed to read chunk ownership data. Trying backup file.",
                    exception);
        }
        loadFromBackup();
    }

    /**
     * 본 파일이 손상됐을 때 마지막 정상 저장본으로 복구한다.
     * 손상 파일은 이름을 바꿔 보존하고, 백업까지 읽을 수 없을 때만 기동을 중단한다.
     */
    private static void loadFromBackup() {
        Path backupPath = savePath.resolveSibling(BACKUP_FILE_NAME);
        if (!Files.exists(backupPath)) {
            ItemRestrictions.LOGGER.error(
                    "Failed to read chunk ownership data and no backup exists. "
                            + "Server startup is aborted to protect the existing file.");
            throw new IllegalStateException("Cannot safely load chunk ownership data");
        }

        try {
            Map<Identifier, Map<Long, Owner>> loadedOwners = parseOwners(backupPath);
            quarantineCorruptFile();
            OWNERS.putAll(loadedOwners);
            saveEnabled = true;
            save();
            ItemRestrictions.LOGGER.warn(
                    "Restored chunk ownership from backup for {} dimensions. "
                            + "Claims made after the last backup may be missing.",
                    OWNERS.size());
        } catch (Exception exception) {
            ItemRestrictions.LOGGER.error(
                    "Backup chunk ownership data is also unreadable. "
                            + "Server startup is aborted to protect the existing files.",
                    exception);
            throw new IllegalStateException("Cannot safely load chunk ownership data", exception);
        }
    }

    private static Map<Identifier, Map<Long, Owner>> parseOwners(Path path) throws IOException {
        Map<Identifier, Map<Long, Owner>> loadedOwners = new HashMap<>();
        try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
            for (String dimensionId : root.keySet()) {
                Identifier dimension = Identifier.tryParse(dimensionId);
                if (dimension == null) {
                    throw new IllegalStateException("Invalid dimension id: " + dimensionId);
                }
                Map<Long, Owner> chunks = new ConcurrentHashMap<>();
                JsonObject entries = root.getAsJsonObject(dimensionId);
                for (String chunkKey : entries.keySet()) {
                    JsonObject owner = entries.getAsJsonObject(chunkKey);
                    // 이름 없이 저장된 예전 데이터도 읽을 수 있어야 한다.
                    String ownerName = null;
                    if (owner.has("name")) {
                        ownerName = owner.get("name").getAsString();
                    }
                    chunks.put(Long.parseLong(chunkKey),
                            new Owner(UUID.fromString(owner.get("uuid").getAsString()), ownerName));
                }
                loadedOwners.put(dimension, chunks);
            }
        }
        return loadedOwners;
    }

    private static void quarantineCorruptFile() {
        Path corruptPath = savePath.resolveSibling(FILE_NAME + ".corrupt-" + System.currentTimeMillis());
        try {
            Files.move(savePath, corruptPath, StandardCopyOption.REPLACE_EXISTING);
            ItemRestrictions.LOGGER.warn("Moved corrupt chunk ownership file to {}", corruptPath.getFileName());
        } catch (IOException exception) {
            ItemRestrictions.LOGGER.warn("Failed to move corrupt chunk ownership file aside", exception);
        }
    }

    public static synchronized boolean save() {
        if (savePath == null || !saveEnabled) {
            return false;
        }

        JsonObject root = new JsonObject();
        OWNERS.forEach((dimension, chunks) -> {
            JsonObject entries = new JsonObject();
            chunks.forEach((chunkKey, owner) -> {
                JsonObject value = new JsonObject();
                value.addProperty("uuid", owner.uuid().toString());
                if (owner.name() != null) {
                    value.addProperty("name", owner.name());
                }
                entries.add(Long.toString(chunkKey), value);
            });
            root.add(dimension.toString(), entries);
        });

        Path temporaryPath = savePath.resolveSibling(FILE_NAME + ".tmp");
        try (Writer writer = Files.newBufferedWriter(temporaryPath,
                StandardCharsets.UTF_8,
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING,
                StandardOpenOption.WRITE)) {
            GSON.toJson(root, writer);
        } catch (Exception exception) {
            ItemRestrictions.LOGGER.error("Failed to write chunk ownership data", exception);
            return false;
        }

        backupCurrentFile();
        try {
            moveTemporaryFile(temporaryPath);
            return true;
        } catch (IOException exception) {
            ItemRestrictions.LOGGER.error("Failed to replace chunk ownership data", exception);
            try {
                Files.deleteIfExists(temporaryPath);
            } catch (IOException cleanupException) {
                ItemRestrictions.LOGGER.warn("Failed to delete temporary chunk ownership data", cleanupException);
            }
            return false;
        }
    }

    /**
     * 새 내용으로 덮어쓰기 직전의 본 파일을 백업으로 남긴다.
     * 본 파일은 항상 임시 파일 교체로만 기록되므로 이 백업은 마지막 정상 저장본이다.
     * 백업 실패가 저장 자체를 막지는 않는다.
     */
    private static void backupCurrentFile() {
        if (!Files.exists(savePath)) {
            return;
        }
        Path backupPath = savePath.resolveSibling(BACKUP_FILE_NAME);
        try {
            Files.copy(savePath, backupPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException exception) {
            ItemRestrictions.LOGGER.warn("Failed to back up chunk ownership data", exception);
        }
    }

    private static void moveTemporaryFile(Path temporaryPath) throws IOException {
        try {
            Files.move(temporaryPath,
                    savePath,
                    StandardCopyOption.ATOMIC_MOVE,
                    StandardCopyOption.REPLACE_EXISTING);
        } catch (AtomicMoveNotSupportedException exception) {
            Files.move(temporaryPath, savePath, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    @Nullable
    public static Owner getOwner(Level level, ChunkPos chunkPos) {
        Map<Long, Owner> chunks = OWNERS.get(level.dimension().identifier());
        if (chunks == null) {
            return null;
        }
        return chunks.get(chunkPos.pack());
    }

    @Nullable
    public static Owner getOwner(Level level, BlockPos blockPos) {
        return getOwner(level, ChunkPos.containing(blockPos));
    }

    /**
     * 비어 있는 청크만 등록한다. 이미 주인이 있으면 아무것도 바꾸지 않고 false를 돌려준다.
     */
    public static synchronized boolean claim(Level level, ChunkPos chunkPos, Owner owner) {
        if (!saveEnabled) {
            return false;
        }

        Identifier dimension = level.dimension().identifier();
        Map<Long, Owner> chunks = OWNERS.computeIfAbsent(dimension,
                (Identifier key) -> new ConcurrentHashMap<>());
        long chunkKey = chunkPos.pack();
        if (chunks.putIfAbsent(chunkKey, owner) != null) {
            return false;
        }

        if (save()) {
            return true;
        }

        chunks.remove(chunkKey, owner);
        if (chunks.isEmpty()) {
            OWNERS.remove(dimension, chunks);
        }
        return false;
    }

    public static boolean isOwnedBy(Level level, BlockPos blockPos, UUID playerId) {
        Owner owner = getOwner(level, blockPos);
        return owner != null && owner.uuid().equals(playerId);
    }

    public static boolean isAvailable() {
        return saveEnabled;
    }

    public static Map<Identifier, Map<Long, Owner>> getAll() {
        Map<Identifier, Map<Long, Owner>> owners = new HashMap<>();
        OWNERS.forEach((dimension, chunks) -> owners.put(dimension, Map.copyOf(chunks)));
        return Map.copyOf(owners);
    }
}
