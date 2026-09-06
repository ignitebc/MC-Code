package com.mcserver.serverutilities;

import com.mcserver.serverutilities.config.AtomicProperties;
import com.mcserver.serverutilities.config.UtilitiesConfig;
import com.mcserver.serverutilities.sleep.SleepRuleManager;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;

/** 파일 설정과 월드별 수면 복원을 외부 테스트 라이브러리 없이 검증한다. */
public final class RegressionTests {
    private static int checks;

    public static void main(String[] args) throws Exception {
        Path directory = Files.createTempDirectory("serverutilities-tests-");
        try {
            configTests(directory);
            sleepTests(directory);
            System.out.println("Server Utilities regression checks passed: " + checks);
        } finally {
            try (var files = Files.walk(directory)) {
                for (Path path : files.sorted(Comparator.reverseOrder()).toList()) Files.delete(path);
            }
        }
    }

    private static void configTests(Path directory) throws Exception {
        Path path = directory.resolve("config.properties");
        check(UtilitiesConfig.load(path).equals(UtilitiesConfig.DEFAULT), "default config");
        check(Files.isRegularFile(path), "default config persisted");
        Properties values = new Properties();
        values.setProperty("sleep.enabled", "false");
        AtomicProperties.write(path, values, "test");
        UtilitiesConfig config = UtilitiesConfig.load(path);
        check(!config.singlePlayerSleep() && config.combatElytra(), "partial config defaults");

        String[][] invalid = {
                {"sleep.enabled", "yes"}, {"combat.range", "0"}, {"combat.range", "129"},
                {"combat.range", "NaN"}, {"balance.hunger.multiplier", "Infinity"},
                {"balance.creeper.multiplier", "-1"}, {"balance.creeper.multiplier", "101"},
                {"balance.creeper.multiplier", "text"}, {"sleep.enable", "true"}
        };
        for (String[] entry : invalid) {
            values = new Properties();
            values.setProperty(entry[0], entry[1]);
            AtomicProperties.write(path, values, "test");
            expectFailure(() -> UtilitiesConfig.load(path), "invalid " + entry[0] + "=" + entry[1]);
        }
        values = UtilitiesConfig.DEFAULT.toProperties();
        values.setProperty("balance.creeper.multiplier", "0");
        values.setProperty("combat.range", "128");
        AtomicProperties.write(path, values, "test");
        check(UtilitiesConfig.load(path).creeperMultiplier() == 0, "zero damage multiplier");
        check(UtilitiesConfig.load(path).combatRange() == 128, "range boundary");
    }

    private static void sleepTests(Path directory) throws Exception {
        Path state = directory.resolve("world-a/sleep.properties");
        AtomicInteger rule = new AtomicInteger(75);
        SleepRuleManager.apply(state, false, rule::get, rule::set);
        check(rule.get() == 75 && !Files.exists(state), "disabled leaves unmanaged world alone");
        SleepRuleManager.apply(state, true, rule::get, rule::set);
        check(rule.get() == 0, "one sleeper");
        check(AtomicProperties.read(state).getProperty("original").equals("75"), "original recorded");
        // 파일을 다시 읽어 프로세스 내 캐시에 의존하지 않는 재시작을 재현한다.
        SleepRuleManager.apply(state, true, rule::get, rule::set);
        SleepRuleManager.apply(state, false, rule::get, rule::set);
        check(rule.get() == 75 && !Files.exists(state), "restart then restore original");

        SleepRuleManager.apply(state, true, rule::get, rule::set);
        rule.set(30);
        SleepRuleManager.apply(state, false, rule::get, rule::set);
        check(rule.get() == 30 && !Files.exists(state), "preserve operator change");

        rule.set(0);
        SleepRuleManager.apply(state, true, rule::get, rule::set);
        SleepRuleManager.apply(state, false, rule::get, rule::set);
        check(rule.get() == 0, "original zero");

        rule.set(100);
        SleepRuleManager.apply(state, true, rule::get, rule::set);
        Path otherState = directory.resolve("world-b/sleep.properties");
        AtomicInteger otherRule = new AtomicInteger(50);
        SleepRuleManager.apply(otherState, true, otherRule::get, otherRule::set);
        SleepRuleManager.apply(otherState, false, otherRule::get, otherRule::set);
        check(otherRule.get() == 50 && rule.get() == 0, "independent worlds");

        expectFailure(() -> SleepRuleManager.apply(state, false, rule::get, value -> {
            throw new IllegalArgumentException("simulated game rule write failure");
        }), "restore failure");
        check(Files.exists(state), "failed restore keeps record");
        SleepRuleManager.apply(state, false, rule::get, rule::set);
        check(rule.get() == 100, "restore retry");

        Files.writeString(state, "original=broken");
        expectFailure(() -> SleepRuleManager.apply(state, true, rule::get, rule::set), "corrupt state");
        check(rule.get() == 100 && Files.readString(state).equals("original=broken"), "corrupt record not overwritten");

        Path blockedParent = directory.resolve("not-a-directory");
        Files.writeString(blockedParent, "keep");
        expectFailure(() -> SleepRuleManager.apply(blockedParent.resolve("state"), true, rule::get, rule::set), "record write failure");
        check(rule.get() == 100, "do not change rule if backup cannot be saved");
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
        checks++;
    }

    private static void expectFailure(CheckedTask task, String message) throws Exception {
        try {
            task.run();
        } catch (IOException | IllegalArgumentException expected) {
            checks++;
            return;
        }
        throw new AssertionError("Expected failure: " + message);
    }

    @FunctionalInterface
    private interface CheckedTask { void run() throws Exception; }
}
