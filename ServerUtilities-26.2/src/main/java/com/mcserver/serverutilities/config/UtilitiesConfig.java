package com.mcserver.serverutilities.config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public record UtilitiesConfig(boolean singlePlayerSleep, boolean combatElytra, boolean combatGolems,
                              double combatRange, boolean creeperDamage, float creeperMultiplier,
                              boolean hunger, float hungerMultiplier, boolean armorCurve,
                              boolean equipmentTiers, boolean deathPenalty, boolean deathProtection) {
    public static final UtilitiesConfig DEFAULT = new UtilitiesConfig(
            true, true, true, 80.0, true, 4.0F / 3.0F, true, 1.5F, true, true, true, true);

    public static UtilitiesConfig load(Path path) throws IOException {
        Properties defaults = DEFAULT.toProperties();
        if (!Files.exists(path)) {
            AtomicProperties.write(path, defaults, "Server Utilities - edit then /serverutilities reload");
        }
        Properties values = AtomicProperties.read(path);
        for (String key : values.stringPropertyNames()) {
            if (!defaults.containsKey(key)) {
                throw new IllegalArgumentException("알 수 없는 설정: " + key);
            }
        }
        Properties merged = new Properties();
        merged.putAll(defaults);
        merged.putAll(values);
        return new UtilitiesConfig(
                bool(merged, "sleep.enabled"), bool(merged, "combat.elytra.enabled"),
                bool(merged, "combat.golems.enabled"), number(merged, "combat.range", 1, 128),
                bool(merged, "balance.creeper.enabled"), (float) number(merged, "balance.creeper.multiplier", 0, 100),
                bool(merged, "balance.hunger.enabled"), (float) number(merged, "balance.hunger.multiplier", 0, 100),
                bool(merged, "balance.armor.enabled"), bool(merged, "balance.tier.enabled"),
                bool(merged, "death.penalty.enabled"), bool(merged, "death.protection.enabled"));
    }

    public Properties toProperties() {
        Properties values = new Properties();
        values.setProperty("sleep.enabled", Boolean.toString(singlePlayerSleep));
        values.setProperty("combat.elytra.enabled", Boolean.toString(combatElytra));
        values.setProperty("combat.golems.enabled", Boolean.toString(combatGolems));
        values.setProperty("combat.range", Double.toString(combatRange));
        values.setProperty("balance.creeper.enabled", Boolean.toString(creeperDamage));
        values.setProperty("balance.creeper.multiplier", Float.toString(creeperMultiplier));
        values.setProperty("balance.hunger.enabled", Boolean.toString(hunger));
        values.setProperty("balance.hunger.multiplier", Float.toString(hungerMultiplier));
        values.setProperty("balance.armor.enabled", Boolean.toString(armorCurve));
        values.setProperty("balance.tier.enabled", Boolean.toString(equipmentTiers));
        values.setProperty("death.penalty.enabled", Boolean.toString(deathPenalty));
        values.setProperty("death.protection.enabled", Boolean.toString(deathProtection));
        return values;
    }

    private static boolean bool(Properties values, String key) {
        String value = values.getProperty(key).trim();
        if (value.equalsIgnoreCase("true")) return true;
        if (value.equalsIgnoreCase("false")) return false;
        throw new IllegalArgumentException(key + ": true 또는 false여야 합니다.");
    }

    private static double number(Properties values, String key, double min, double max) {
        double value;
        try {
            value = Double.parseDouble(values.getProperty(key).trim());
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException(key + ": 숫자여야 합니다.", exception);
        }
        if (!Double.isFinite(value) || value < min || value > max) {
            throw new IllegalArgumentException(key + ": 범위는 " + min + " ~ " + max + "입니다.");
        }
        return value;
    }
}
