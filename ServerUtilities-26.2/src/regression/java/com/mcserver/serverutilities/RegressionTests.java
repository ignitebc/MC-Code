package com.mcserver.serverutilities;

import com.mcserver.serverutilities.config.AtomicProperties;
import com.mcserver.serverutilities.config.UtilitiesConfig;
import com.mcserver.serverutilities.monster.CreeperLevel;
import com.mcserver.serverutilities.monster.MonsterExperience;
import com.mcserver.serverutilities.monster.MonsterLevel;
import com.mcserver.serverutilities.sleep.SleepRuleManager;
import com.mcserver.serverutilities.spawn.SpawnAnchor;

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
            spawnTests(directory);
            monsterLevelTests();
            creeperLevelTests();
            monsterExperienceTests();
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
                {"balance.creeper.enabled", "on"}, {"balance.creeper.multiplier", "1.3333334"},
                {"sleep.enable", "true"},
                {"spawn.scatter.enabled", "on"}, {"spawn.scatter.radius", "0"},
                {"spawn.scatter.radius", "1.5"}, {"spawn.scatter.radius", "1000001"},
                {"starter.kit.enabled", "1"}, {"death.chest.enabled", "on"},
                {"death.chest.expire_seconds", "0"}, {"death.chest.expire_seconds", "86401"},
                {"death.chest.expire_seconds", "2.5"}, {"death.chest.empty_seconds", "-1"},
                {"death.chest.empty_seconds", "3601"}
        };
        for (String[] entry : invalid) {
            values = new Properties();
            values.setProperty(entry[0], entry[1]);
            AtomicProperties.write(path, values, "test");
            expectFailure(() -> UtilitiesConfig.load(path), "invalid " + entry[0] + "=" + entry[1]);
        }
        values = UtilitiesConfig.DEFAULT.toProperties();
        values.setProperty("balance.creeper.enabled", "false");
        values.setProperty("combat.range", "128");
        AtomicProperties.write(path, values, "test");
        check(!UtilitiesConfig.load(path).creeperLevels(), "creeper levels can be disabled");
        check(UtilitiesConfig.load(path).combatRange() == 128, "range boundary");

        values = UtilitiesConfig.DEFAULT.toProperties();
        values.setProperty("spawn.scatter.radius", "1");
        AtomicProperties.write(path, values, "test");
        check(UtilitiesConfig.load(path).spawnScatterRadius() == 1, "smallest scatter radius");
        values.setProperty("spawn.scatter.radius", "1000000");
        AtomicProperties.write(path, values, "test");
        check(UtilitiesConfig.load(path).spawnScatterRadius() == 1_000_000, "largest scatter radius");
        check(UtilitiesConfig.DEFAULT.spawnScatter() && UtilitiesConfig.DEFAULT.spawnScatterRadius() == 3000,
                "scatter defaults");
        check(UtilitiesConfig.DEFAULT.starterKit(), "starter kit default");
        values = new Properties();
        values.setProperty("starter.kit.enabled", "false");
        AtomicProperties.write(path, values, "test");
        check(!UtilitiesConfig.load(path).starterKit(), "starter kit can be disabled");

        check(UtilitiesConfig.DEFAULT.deathChest() && UtilitiesConfig.DEFAULT.deathChestExpireSeconds() == 300
                && UtilitiesConfig.DEFAULT.deathChestEmptySeconds() == 3, "death chest defaults");
        values = new Properties();
        values.setProperty("death.chest.enabled", "false");
        values.setProperty("death.chest.expire_seconds", "1");
        values.setProperty("death.chest.empty_seconds", "0");
        AtomicProperties.write(path, values, "test");
        config = UtilitiesConfig.load(path);
        check(!config.deathChest() && config.deathChestExpireSeconds() == 1 && config.deathChestEmptySeconds() == 0,
                "death chest smallest values");
        values.setProperty("death.chest.expire_seconds", "86400");
        values.setProperty("death.chest.empty_seconds", "3600");
        AtomicProperties.write(path, values, "test");
        config = UtilitiesConfig.load(path);
        check(config.deathChestExpireSeconds() == 86_400 && config.deathChestEmptySeconds() == 3_600,
                "death chest largest values");
    }

    private static void spawnTests(Path directory) throws Exception {
        Path path = directory.resolve("world-c/spawn.properties");
        check(SpawnAnchor.read(path).isEmpty(), "no anchor before first join");
        SpawnAnchor anchor = new SpawnAnchor(120, 71, -340);
        SpawnAnchor.write(path, anchor);
        // 파일을 다시 읽어 프로세스 내 캐시에 의존하지 않는 재시작을 재현한다.
        check(SpawnAnchor.read(path).orElseThrow().equals(anchor), "anchor survives restart");

        check(anchor.scatteredX(2000, 0) == 120 - 2000, "lowest roll hits minus radius");
        check(anchor.scatteredX(2000, 4000) == 120 + 2000, "highest roll hits plus radius");
        check(anchor.scatteredZ(2000, 2000) == -340, "middle roll keeps anchor");
        check(anchor.scatteredZ(1, 0) == -341, "smallest radius");
        for (int roll = 0; roll <= 4000; roll += 137) {
            int scattered = anchor.scatteredX(2000, roll);
            check(scattered >= 120 - 2000 && scattered <= 120 + 2000, "roll " + roll + " stays in range");
        }

        SpawnAnchor edge = new SpawnAnchor(SpawnAnchor.WORLD_LIMIT, 64, -SpawnAnchor.WORLD_LIMIT);
        check(edge.scatteredX(2000, 4000) == SpawnAnchor.WORLD_LIMIT, "clamped to world limit");
        check(edge.scatteredZ(2000, 0) == -SpawnAnchor.WORLD_LIMIT, "clamped to negative world limit");

        expectFailure(() -> anchor.scatteredX(0, 0), "radius below one");
        expectFailure(() -> anchor.scatteredX(2000, -1), "roll below zero");
        expectFailure(() -> anchor.scatteredX(2000, 4001), "roll above radius");

        Files.writeString(path, "x=1");
        expectFailure(() -> SpawnAnchor.read(path), "incomplete anchor record");
        Files.writeString(path, "x=1\ny=2\nz=broken");
        expectFailure(() -> SpawnAnchor.read(path), "corrupt anchor record");
        Files.writeString(path, "x=99999999\ny=64\nz=0");
        expectFailure(() -> SpawnAnchor.read(path), "anchor outside world limit");
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

    private static void monsterLevelTests() {
        // 등급 점수 F=1 ~ S=7 의 모든 조합이 평균 반올림(.5 올림)과 같은지 실수 계산으로 대조한다.
        for (int armor = MonsterLevel.MIN_SCORE; armor <= MonsterLevel.MAX_SCORE; armor++) {
            for (int weapon = MonsterLevel.MIN_SCORE; weapon <= MonsterLevel.MAX_SCORE; weapon++) {
                int expected = (int) Math.floor((armor + weapon) / 2.0 + 0.5);
                check(MonsterLevel.of(armor, weapon) == expected, "level armor " + armor + " weapon " + weapon);
            }
        }
        check(MonsterLevel.of(1, 1) == 1, "leather with F gun");
        check(MonsterLevel.of(7, 7) == 7, "netherite with S gun");
        check(MonsterLevel.of(7, 1) == 4, "netherite with F gun");
        check(MonsterLevel.of(6, 7) == 7, "half rounds up");
        check(MonsterLevel.of(4, 3) == 4, "chainmail with D gun");

        int missing = MonsterLevel.MISSING_SCORE;
        int melee = MonsterLevel.MELEE_WEAPON_SCORE;
        check(MonsterLevel.of(7, missing) == 4, "netherite without weapon");
        check(MonsterLevel.of(missing, 7) == 4, "S gun without armor");
        check(MonsterLevel.of(missing, missing) == 1, "no equipment");
        check(MonsterLevel.of(missing, melee) == 2, "melee without armor");
        check(MonsterLevel.of(7, melee) == 5, "netherite with melee");
        check(MonsterLevel.of(0, 99) == 4, "scores clamped to grade range");

        check(!MonsterLevel.isVisible(MonsterLevel.NONE), "no level hidden");
        check(MonsterLevel.isVisible(1) && MonsterLevel.isVisible(7), "level range visible");
        check(!MonsterLevel.isVisible(8), "level above range hidden");
    }

    private static void creeperLevelTests() throws Exception {
        check(CreeperLevel.LEVEL_COUNT == MonsterLevel.MAX_SCORE, "creeper levels match label range");
        // 레벨마다 0.3배씩 올라 LV1 1.0 ~ LV7 2.8배가 되는지 확인한다.
        float[] expected = {1.0F, 1.3F, 1.6F, 1.9F, 2.2F, 2.5F, 2.8F};
        for (int roll = 0; roll < CreeperLevel.LEVEL_COUNT; roll++) {
            int level = CreeperLevel.fromRoll(roll);
            check(level == roll + 1, "roll " + roll + " becomes level " + (roll + 1));
            check(CreeperLevel.explosionMultiplier(level) == expected[roll], "creeper LV" + level + " multiplier");
        }
        check(CreeperLevel.explosionMultiplier(MonsterLevel.NONE) == CreeperLevel.VANILLA_MULTIPLIER,
                "creeper without level stays vanilla");
        check(CreeperLevel.explosionMultiplier(8) == CreeperLevel.VANILLA_MULTIPLIER, "unknown level stays vanilla");
        expectFailure(() -> CreeperLevel.fromRoll(-1), "creeper roll below zero");
        expectFailure(() -> CreeperLevel.fromRoll(CreeperLevel.LEVEL_COUNT), "creeper roll above range");
    }

    private static void monsterExperienceTests() {
        // 레벨마다 0.3배씩 올라 LV1 1.0 ~ LV7 2.8배가 되는지 확인한다.
        int[] expectedPercent = {100, 130, 160, 190, 220, 250, 280};
        for (int level = MonsterLevel.MIN_SCORE; level <= MonsterLevel.MAX_SCORE; level++) {
            int percent = MonsterExperience.experiencePercent(level);
            check(percent == expectedPercent[level - MonsterLevel.MIN_SCORE], "experience LV" + level + " percent");
        }
        check(MonsterExperience.experiencePercent(MonsterLevel.NONE) == MonsterExperience.VANILLA_PERCENT,
                "no level keeps vanilla experience");
        check(MonsterExperience.experiencePercent(8) == MonsterExperience.VANILLA_PERCENT,
                "unknown level keeps vanilla experience");

        check(MonsterExperience.scale(5, 1) == 5, "LV1 keeps experience");
        check(MonsterExperience.scale(5, 2) == 7, "6.5 rounds up");
        check(MonsterExperience.scale(5, 4) == 10, "9.5 rounds up");
        check(MonsterExperience.scale(3, 2) == 4, "3.9 rounds up");
        check(MonsterExperience.scale(1, 2) == 1, "1.3 rounds down");
        check(MonsterExperience.scale(10, 7) == 28, "LV7 multiplies 2.8");
        check(MonsterExperience.scale(5, MonsterLevel.NONE) == 5, "unlevelled keeps experience");
        check(MonsterExperience.scale(0, 7) == 0, "no experience stays zero");

        long credit = MonsterExperience.PLAYER_HURT_CREDIT_TICKS;
        check(credit == 400, "player hurt credit is 20 seconds");
        check(MonsterExperience.withinPlayerHurtCredit(1_000, 1_000 + credit), "credit boundary included");
        check(!MonsterExperience.withinPlayerHurtCredit(1_000, 1_000 + credit + 1), "credit expires");
        check(!MonsterExperience.withinPlayerHurtCredit(1_000, 999), "future hurt ignored");
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
