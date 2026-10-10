package cn.sh1rocu.tacz.util.forge;

import com.mojang.logging.LogUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FileUtil;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.AbstractPackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.resources.IoSupplier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 임의의 Path로 리소스 팩을 정의한다.
 * <p>
 * 주로 모드 안에 선택적 리소스 팩을 넣을 때 쓴다. 예를 들어 Programmer Art와 함께 쓸
 * 대체 텍스처나, 호환성을 위한 선택적 대체 제작법, 바닐라 제작법을 바꾸는 제작법 등이다.
 */
public class PathPackResources extends AbstractPackResources {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final Path source;

    /**
     * java.nio.Path 기반 리소스 팩을 만든다.
     *
     * @param packId    팩 식별자.
     *                  팩 탐색기 안에서 고유해야 하며, 리소스가 든 파일이나 폴더 이름을 쓰는 것이 좋다.
     * @param isBuiltin 이 팩 리소스를 내장으로 볼지
     * @param source    팩의 루트 경로. assets 폴더 자체가 아니라 "assets"·"data"가 들어 있는 폴더를 가리켜야 한다!
     */
    public PathPackResources(String packId, boolean isBuiltin, final Path source) {
        super(new PackLocationInfo(packId, Component.literal(packId), PackSource.DEFAULT, Optional.empty()));
        this.source = source;
    }

    /**
     * 리소스 팩이 든 원본 경로를 돌려준다.
     * 오류 표시에 쓴다.
     *
     * @return 리소스의 루트 경로
     */
    public Path getSource() {
        return this.source;
    }

    /**
     * 주어진 경로 조각들에 대한 파일·폴더 경로를 돌려주도록 구현한다.
     *
     * @param paths 해석할 경로 문자열 하나 이상. 슬래시로 구분한 경로를 넣을 수 있다.
     * @return 결과 경로. 실제로 없을 수도 있다.
     */
    protected Path resolve(String... paths) {
        Path path = getSource();
        for (String name : paths)
            path = path.resolve(name);
        return path;
    }

    @Nullable
    @Override
    public IoSupplier<InputStream> getRootResource(String... paths) {
        final Path path = resolve(paths);
        if (!Files.exists(path))
            return null;

        return IoSupplier.create(path);
    }

    @Override
    public void listResources(PackType type, String namespace, String path, ResourceOutput resourceOutput) {
        FileUtil.decomposePath(path).result().ifPresent(parts ->
                net.minecraft.server.packs.PathPackResources.listPath(namespace, resolve(type.getDirectory(), namespace).toAbsolutePath(), parts, resourceOutput));
    }

    @Override
    public Set<String> getNamespaces(PackType type) {
        return getNamespacesFromDisk(type);
    }

    @NotNull
    private Set<String> getNamespacesFromDisk(final PackType type) {
        try {
            Path root = resolve(type.getDirectory());
            try (Stream<Path> walker = Files.walk(root, 1)) {
                return walker
                        .filter(Files::isDirectory)
                        .map(root::relativize)
                        .filter(p -> p.getNameCount() > 0) // 루트 항목은 건너뛴다
                        .map(p -> p.toString().replaceAll("/$", "")) // 끝의 슬래시가 있으면 뺀다
                        .filter(s -> !s.isEmpty()) // 빈 문자열은 거른다. 그렇지 않으면 ResourceLocation에서 빈 문자열이 minecraft 네임스페이스로 처리된다
                        .collect(Collectors.toSet());
            }
        } catch (IOException e) {
            if (type == PackType.SERVER_DATA) // 서버에서도 assets에 든 언어 파일을 불러오므로, 클라이언트 리소스가 있으면 리소스 네임스페이스를 추가해야 한다
            {
                return this.getNamespaces(PackType.CLIENT_RESOURCES);
            } else {
                return Collections.emptySet();
            }
        }
    }

    @Override
    public IoSupplier<InputStream> getResource(PackType type, Identifier location) {
        return this.getRootResource(getPathFromLocation(location.getPath().startsWith("lang/") ? PackType.CLIENT_RESOURCES : type, location));
    }

    private static String[] getPathFromLocation(PackType type, Identifier location) {
        String[] parts = location.getPath().split("/");
        String[] result = new String[parts.length + 2];
        result[0] = type.getDirectory();
        result[1] = location.getNamespace();
        System.arraycopy(parts, 0, result, 2, parts.length);
        return result;
    }

    @Override
    public void close() {
    }

    @Override
    public String toString() {
        return String.format(Locale.ROOT, "%s: %s (%s)", getClass().getName(), this.packId(), getSource());
    }
}