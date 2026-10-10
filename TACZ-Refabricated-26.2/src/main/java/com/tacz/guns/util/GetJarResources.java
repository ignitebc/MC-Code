package com.tacz.guns.util;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.tacz.guns.GunMod;
import net.fabricmc.loader.api.FabricLoader;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.comparator.LastModifiedFileComparator;
import org.apache.commons.io.filefilter.TrueFileFilter;

import javax.annotation.Nullable;
import java.io.*;
import java.net.JarURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public final class GetJarResources {
    /**
     * 묶은 시간이 압축 파일의 해시값에 영향을 주므로 시간을 직접 지정한다
     * <p>
     * 이 시간은 TaCZ의 첫 커밋 시간이다
     */
    private static final Instant BACKUP_TIME = Instant.parse("2024-02-26T12:28:08.000Z");
    private static final Path BACKUP_PATH = Paths.get("config", GunMod.MOD_ID, "backup");
    private static final SimpleDateFormat BACKUP_DATE_FORMAT = new SimpleDateFormat("yyyyMMdd-HHmmss");
    private static final int MAX_BACKUP_COUNT = 10;
    private static final String EXPORT_STATE_FILE_NAME = ".export-state.json";
    private static final int EXPORT_STATE_VERSION = 1;
    private static final Gson EXPORT_STATE_GSON = new GsonBuilder().setPrettyPrinting().create();

    private GetJarResources() {
    }

    /**
     * 이 모드의 파일을 지정한 폴더로 복사한다. 원래 파일을 강제로 덮어쓴다.
     *
     * @param srcPath jar 안의 원본 파일 주소
     * @param root    복사할 루트 디렉터리
     * @param path    복사한 뒤의 경로
     */
    public static void copyModFile(String srcPath, Path root, String path) {
        URL url = GunMod.class.getResource(srcPath);
        try {
            if (url != null) {
                FileUtils.copyURLToFile(url, root.resolve(path).toFile());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 이 모드의 폴더를 지정한 폴더로 복사한다. 원래 폴더를 강제로 덮어쓴다.
     *
     * @param srcPath jar 안의 원본 파일 주소
     * @param root    복사할 루트 디렉터리
     * @param path    복사한 뒤의 경로
     */
    public static void copyModDirectory(Class<?> resourceClass, String srcPath, Path root, String path) {
        URL url = resourceClass.getResource(srcPath);
        try {
            if (url != null) {
                exportFolderIfChanged(resourceClass, srcPath, url, root, path);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 이 모드의 폴더를 지정한 폴더로 복사한다. 원래 폴더를 강제로 덮어쓴다.
     *
     * @param srcPath jar 안의 원본 파일 주소
     * @param root    복사할 루트 디렉터리
     * @param path    복사한 뒤의 경로
     */
    public static void copyModDirectory(String srcPath, Path root, String path) {
        copyModDirectory(GunMod.class, srcPath, root, path);
    }

    @Nullable
    public static InputStream readModFile(String filePath) {
        URL url = GunMod.class.getResource(filePath);
        try {
            if (url != null) {
                return url.openStream();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static void exportFolderIfChanged(Class<?> resourceClass, String srcPath, URL sourceUrl, Path root, String path) throws IOException {
        String stateKey = getExportStateKey(resourceClass, srcPath, path);
        String sourceFingerprint = calculateSourceFingerprint(sourceUrl);
        ExportStateFile stateFile = readExportState(root);
        Path targetPath = root.resolve(path);
        String previousFingerprint = stateFile.entries.get(stateKey);
        if (Files.isDirectory(targetPath) && sourceFingerprint.equals(previousFingerprint)) {
            GunMod.LOGGER.debug("Skipping unchanged exported resource {}", targetPath);
            return;
        }

        GunMod.LOGGER.info("Exporting resource pack {} to {}", srcPath, targetPath);
        copyFolder(sourceUrl, targetPath);
        stateFile.version = EXPORT_STATE_VERSION;
        stateFile.entries.put(stateKey, sourceFingerprint);
        writeExportState(root, stateFile);
    }

    private static String getExportStateKey(Class<?> resourceClass, String srcPath, String path) {
        return resourceClass.getName() + "|" + srcPath + "|" + path;
    }

    private static ExportStateFile readExportState(Path root) {
        Path statePath = root.resolve(EXPORT_STATE_FILE_NAME);
        if (!Files.isRegularFile(statePath)) {
            return new ExportStateFile();
        }
        try (Reader reader = Files.newBufferedReader(statePath, StandardCharsets.UTF_8)) {
            ExportStateFile state = EXPORT_STATE_GSON.fromJson(reader, ExportStateFile.class);
            if (state == null || state.version != EXPORT_STATE_VERSION || state.entries == null) {
                return new ExportStateFile();
            }
            return state;
        } catch (Exception exception) {
            GunMod.LOGGER.warn("Failed to read export state from {}, forcing full export", statePath, exception);
            return new ExportStateFile();
        }
    }

    private static void writeExportState(Path root, ExportStateFile stateFile) throws IOException {
        Files.createDirectories(root);
        Path statePath = root.resolve(EXPORT_STATE_FILE_NAME);
        try (Writer writer = Files.newBufferedWriter(statePath, StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE)) {
            EXPORT_STATE_GSON.toJson(stateFile, writer);
        }
    }

    private static String calculateSourceFingerprint(URL sourceUrl) throws IOException {
        List<String> lines = "jar".equals(sourceUrl.getProtocol())
                ? collectJarFingerprintLines(sourceUrl)
                : collectPathFingerprintLines(resolveSourcePath(sourceUrl));
        Collections.sort(lines);
        return Md5Utils.md5Hex(String.join("\n", lines).getBytes(StandardCharsets.UTF_8));
    }

    private static List<String> collectPathFingerprintLines(Path sourceRoot) throws IOException {
        List<String> lines = new ArrayList<>();
        try (Stream<Path> stream = Files.walk(sourceRoot, Integer.MAX_VALUE)) {
            stream.filter(Files::isRegularFile).forEach(path -> {
                try {
                    BasicFileAttributes attributes = Files.readAttributes(path, BasicFileAttributes.class);
                    String relativePath = sourceRoot.relativize(path).toString().replace('\\', '/');
                    lines.add(relativePath + "|" + attributes.size() + "|" + attributes.lastModifiedTime().toMillis());
                } catch (IOException exception) {
                    throw new UncheckedIOException(exception);
                }
            });
        } catch (UncheckedIOException exception) {
            throw exception.getCause();
        }
        return lines;
    }

    private static List<String> collectJarFingerprintLines(URL sourceUrl) throws IOException {
        JarURLConnection connection = (JarURLConnection) sourceUrl.openConnection();
        connection.setUseCaches(false);
        String rootEntry = normalizeDirectoryEntryName(connection.getEntryName());
        List<String> lines = new ArrayList<>();
        try (JarFile jarFile = connection.getJarFile()) {
            Enumeration<JarEntry> entries = jarFile.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                if (entry.isDirectory() || !entry.getName().startsWith(rootEntry)) {
                    continue;
                }
                String relativePath = entry.getName().substring(rootEntry.length());
                if (relativePath.isEmpty()) {
                    continue;
                }
                lines.add(relativePath + "|" + entry.getSize() + "|" + entry.getTime() + "|" + entry.getCrc());
            }
        }
        return lines;
    }

    private static void copyFolder(URL sourceUrl, Path targetPath) throws IOException {
        if (Files.isDirectory(targetPath)) {
            // 원래 폴더 백업
            backupFiles(targetPath);
            // 원래 폴더를 지워 강제로 덮어쓰는 효과를 낸다
            deleteFiles(targetPath);
        } else if (Files.exists(targetPath)) {
            Files.delete(targetPath);
        }

        if ("jar".equals(sourceUrl.getProtocol())) {
            copyJarProtocolFolder(sourceUrl, targetPath);
        } else {
            copyPathBackedFolder(resolveSourcePath(sourceUrl), targetPath);
        }
    }

    private static void copyPathBackedFolder(Path sourceRoot, Path targetPath) throws IOException {
        Files.createDirectories(targetPath);
        try (Stream<Path> stream = Files.walk(sourceRoot, Integer.MAX_VALUE)) {
            stream.forEach(source -> {
                Path target = targetPath.resolve(sourceRoot.relativize(source).toString());
                try {
                    if (Files.isDirectory(source)) {
                        Files.createDirectories(target);
                    } else {
                        Files.createDirectories(target.getParent());
                        Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
                    }
                } catch (IOException exception) {
                    throw new UncheckedIOException(exception);
                }
            });
        } catch (UncheckedIOException exception) {
            throw exception.getCause();
        }
    }

    private static void copyJarProtocolFolder(URL sourceUrl, Path targetPath) throws IOException {
        JarURLConnection connection = (JarURLConnection) sourceUrl.openConnection();
        connection.setUseCaches(false);
        String rootEntry = normalizeDirectoryEntryName(connection.getEntryName());
        Files.createDirectories(targetPath);
        try (JarFile jarFile = connection.getJarFile()) {
            Enumeration<JarEntry> entries = jarFile.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                if (!entry.getName().startsWith(rootEntry)) {
                    continue;
                }
                String relativePath = entry.getName().substring(rootEntry.length());
                if (relativePath.isEmpty()) {
                    continue;
                }
                Path target = targetPath.resolve(relativePath);
                if (entry.isDirectory()) {
                    Files.createDirectories(target);
                    continue;
                }
                Files.createDirectories(target.getParent());
                try (InputStream inputStream = jarFile.getInputStream(entry)) {
                    Files.copy(inputStream, target, StandardCopyOption.REPLACE_EXISTING);
                }
            }
        }
    }

    private static String normalizeDirectoryEntryName(String entryName) {
        if (entryName == null || entryName.isEmpty()) {
            return "";
        }
        return entryName.endsWith("/") ? entryName : entryName + "/";
    }

    private static Path resolveSourcePath(URL sourceUrl) throws IOException {
        try {
            return Paths.get(sourceUrl.toURI());
        } catch (Exception exception) {
            throw new IOException("Failed to resolve source path " + sourceUrl, exception);
        }
    }

    private static void backupFiles(Path targetPath) throws IOException {
        // 하위 백업 폴더 만들기
        String dirName = targetPath.getFileName().toString();
        Path resourcePacksPath = FabricLoader.getInstance().getGameDir().resolve("tacz_backup");
        Path backupPath = resourcePacksPath.resolve(dirName);
        if (!Files.isDirectory(backupPath)) {
            Files.createDirectories(backupPath);
        }

        // 백업 파일 수를 확인해 열 개를 넘으면 가장 오래된 것을 지운다
        // 모든 백업의 md5도 함께 얻는다
        Set<String> cacheMd5 = checkOldBackups(backupPath);

        // 먼저 임시 파일을 하나 만든다
        File tempFile = File.createTempFile(dirName, ".tmp");
        FileTime fileTime = FileTime.from(BACKUP_TIME);

        // 파일 쓰기 시작
        try (ZipOutputStream zs = new ZipOutputStream(new FileOutputStream(tempFile));
             Stream<Path> fileWalks = Files.walk(targetPath)) {
            fileWalks.filter(Files::isRegularFile).forEach(path -> {
                String entryPath = targetPath.relativize(path).toString();
                ZipEntry zipEntry = new ZipEntry(entryPath);
                // 해시값이 달라지지 않도록 고정 시간을 지정해야 한다
                zipEntry.setLastModifiedTime(fileTime);
                try {
                    zs.putNextEntry(zipEntry);
                    Files.copy(path, zs);
                    zs.closeEntry();
                } catch (IOException e) {
                    GunMod.LOGGER.info("Error in zip file: {}", e.getMessage());
                }
            });
        }

        // 해시값 계산을 시도한다
        try (FileInputStream inputStream = new FileInputStream(tempFile)) {
            String md5Hex = Md5Utils.md5Hex(inputStream);
            // 이 백업이 있는지 확인한다
            if (cacheMd5.contains(md5Hex)) {
                // 있으면 백업을 지운다
                tempFile.deleteOnExit();
            } else {
                // 아니면 백업 파일을 하나 복사한다
                String dataName = BACKUP_DATE_FORMAT.format(new Date()).toLowerCase(Locale.ENGLISH);
                Path backupZipFilePath = backupPath.resolve(String.format("backup-%s-%s.zip", dataName, md5Hex));
                FileUtils.copyFile(tempFile, backupZipFilePath.toFile());
            }
        }
    }

    private static Set<String> checkOldBackups(Path backupPath) {
        // 임시 캐시 파일 md5
        Set<String> allMd5Hex = Sets.newHashSet();
        if (!Files.isDirectory(backupPath)) {
            return allMd5Hex;
        }
        try {
            List<File> delFiles = Lists.newArrayList(FileUtils.listFiles(backupPath.toFile(), TrueFileFilter.TRUE, null));
            delFiles.sort(LastModifiedFileComparator.LASTMODIFIED_REVERSE);
            int count = 1;
            for (File file : delFiles) {
                if (count >= MAX_BACKUP_COUNT) {
                    // 열 개를 넘는 것은 지운다
                    GunMod.LOGGER.info("Deleting old backup gun pack {}", file.getName());
                    FileUtils.deleteQuietly(file);
                } else {
                    // 열 개 이내는 md5를 계산해 중복이 있는지 본다
                    try (FileInputStream inputStream = new FileInputStream(file)) {
                        allMd5Hex.add(Md5Utils.md5Hex(inputStream));
                    }
                }
                count++;
            }
        } catch (Exception exception) {
            GunMod.LOGGER.error("Error while checking old backup gun pack : {}", exception.getMessage());
        }
        return allMd5Hex;
    }

    private static void deleteFiles(Path targetPath) throws IOException {
        Files.walkFileTree(targetPath, new SimpleFileVisitor<>() {
            // 먼저 훑으며 파일을 지운다
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.delete(file);
                return FileVisitResult.CONTINUE;
            }

            // 그다음 훑으며 디렉터리를 지운다
            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                Files.delete(dir);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    private static final class ExportStateFile {
        private int version = EXPORT_STATE_VERSION;
        private Map<String, String> entries = new HashMap<>();
    }
}
