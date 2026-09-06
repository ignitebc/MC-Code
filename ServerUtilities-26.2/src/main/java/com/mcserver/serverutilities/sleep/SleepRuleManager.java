package com.mcserver.serverutilities.sleep;

import com.mcserver.serverutilities.config.AtomicProperties;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;

public final class SleepRuleManager {
    private SleepRuleManager() { }

    public static void apply(Path statePath, boolean enabled, IntSupplier current, IntConsumer setRule)
            throws IOException {
        Integer original = readOriginal(statePath);
        if (enabled) {
            if (original == null) {
                Properties state = new Properties();
                state.setProperty("original", Integer.toString(current.getAsInt()));
                // 게임 규칙 변경 전에 복원 근거부터 기록한다.
                AtomicProperties.write(statePath, state, "Original players_sleeping_percentage; do not edit");
            }
            setRule.accept(0);
        } else if (original != null) {
            // 관리자가 따로 바꾼 값은 보존한다. 복원 실패 시 상태 파일도 유지한다.
            if (current.getAsInt() == 0) setRule.accept(original);
            Files.delete(statePath);
        }
    }

    private static Integer readOriginal(Path path) throws IOException {
        if (!Files.exists(path)) return null;
        Properties state = AtomicProperties.read(path);
        try {
            int original = Integer.parseInt(state.getProperty("original", ""));
            if (original < 0 || state.size() != 1) throw new NumberFormatException();
            return original;
        } catch (NumberFormatException exception) {
            throw new IOException("수면 규칙 복원 기록이 잘못되었습니다. 기존 파일을 확인하세요.", exception);
        }
    }
}
