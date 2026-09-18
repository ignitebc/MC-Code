package com.mcserver.serverutilities.spawn;

import com.mcserver.serverutilities.config.AtomicProperties;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.Properties;

/** 최초 접속자의 시작 좌표. 이후 접속자의 무작위 시작점은 모두 이 좌표를 기준으로 잡는다. */
public record SpawnAnchor(int x, int y, int z) {
    public static final int WORLD_LIMIT = 29_999_984;

    public static Optional<SpawnAnchor> read(Path path) throws IOException {
        if (!Files.exists(path)) return Optional.empty();
        Properties state = AtomicProperties.read(path);
        try {
            if (state.size() != 3) throw new NumberFormatException();
            return Optional.of(new SpawnAnchor(coordinate(state, "x"), coordinate(state, "y"), coordinate(state, "z")));
        } catch (NumberFormatException exception) {
            throw new IOException("시작 위치 기준 좌표 기록이 잘못되었습니다. 기존 파일을 확인하세요.", exception);
        }
    }

    public static void write(Path path, SpawnAnchor anchor) throws IOException {
        Properties state = new Properties();
        state.setProperty("x", Integer.toString(anchor.x()));
        state.setProperty("y", Integer.toString(anchor.y()));
        state.setProperty("z", Integer.toString(anchor.z()));
        AtomicProperties.write(path, state, "First player start position; do not edit");
    }

    /** roll은 0 이상 radius * 2 이하이며, 결과는 기준 좌표 ±radius 안에서 월드 경계로 잘린다. */
    public int scatteredX(int radius, int roll) {
        return scatter(x, radius, roll);
    }

    public int scatteredZ(int radius, int roll) {
        return scatter(z, radius, roll);
    }

    private static int scatter(int origin, int radius, int roll) {
        if (radius < 1) throw new IllegalArgumentException("반경은 1 이상이어야 합니다.");
        if (roll < 0 || roll > radius * 2) throw new IllegalArgumentException("추첨값이 반경을 벗어났습니다.");
        long scattered = (long) origin - radius + roll;
        return Math.clamp(scattered, -WORLD_LIMIT, WORLD_LIMIT);
    }

    private static int coordinate(Properties state, String key) {
        int value = Integer.parseInt(state.getProperty(key, "").trim());
        if (value < -WORLD_LIMIT || value > WORLD_LIMIT) throw new NumberFormatException();
        return value;
    }
}
