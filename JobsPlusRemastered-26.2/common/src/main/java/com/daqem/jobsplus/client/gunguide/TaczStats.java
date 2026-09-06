package com.daqem.jobsplus.client.gunguide;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializer;
import net.minecraft.resources.Identifier;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import it.unimi.dsi.fastutil.Pair;
import net.minecraft.network.chat.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** Formats loaded pack values without evaluating scripts or changing weapon properties. */
final class TaczStats {
    private static final Gson GSON = new GsonBuilder().registerTypeAdapter(Identifier.class,
            (JsonSerializer<Identifier>) (value, type, context) -> new JsonPrimitive(value.toString())).create();
    private static final Map<String, String> LABELS = Map.ofEntries(
            Map.entry("damage", "피해량"), Map.entry("head_shot", "헤드샷 배율"),
            Map.entry("armor_ignore", "방어구 무시율"), Map.entry("ads", "조준 소요 시간(초)"),
            Map.entry("ammo_speed", "탄속(m/s)"), Map.entry("rpm", "분당 발사 수"),
            Map.entry("effective_range", "유효 사거리(m)"), Map.entry("knockback", "넉백"),
            Map.entry("pierce", "관통 대상 수"), Map.entry("weight_modifier", "무게(kg)"),
            Map.entry("recoil", "반동"), Map.entry("pitch", "수직 반동"), Map.entry("yaw", "수평 반동"),
            Map.entry("inaccuracy", "탄퍼짐"), Map.entry("aim", "조준"), Map.entry("stand", "서 있을 때"),
            Map.entry("move", "이동 중"), Map.entry("sneak", "웅크릴 때"), Map.entry("lie", "엎드릴 때"),
            Map.entry("movement_speed", "이동 속도"), Map.entry("base", "기본"), Map.entry("reload", "재장전 중"),
            Map.entry("silence", "총성"), Map.entry("explosion", "폭발"), Map.entry("explode", "폭발 사용"),
            Map.entry("radius", "반경"), Map.entry("destroy_block", "블록 파괴"), Map.entry("delay", "지연 시간(초)"),
            Map.entry("ignite", "발화"), Map.entry("igniteentity", "대상 발화"), Map.entry("igniteblock", "블록 발화"),
            Map.entry("distance", "거리(m)"), Map.entry("range_angle", "공격 각도"), Map.entry("cooldown", "재사용 대기(초)"),
            Map.entry("prep", "준비 시간(초)"), Map.entry("effects", "상태 효과"), Map.entry("duration", "지속 시간"),
            Map.entry("amplifier", "효과 단계"), Map.entry("probability", "확률"), Map.entry("effect", "효과"),
            Map.entry("id", "효과 ID"), Map.entry("default", "기본 공격"), Map.entry("type", "방식"),
            Map.entry("max", "최대 열량"), Map.entry("per_shot", "발당 열량"),
            Map.entry("cooling_multiplier", "냉각 배율"), Map.entry("cooling_delay", "냉각 대기(ms)"),
            Map.entry("over_heat_time", "과열 지속(ms)"), Map.entry("min_inaccuracy", "최소 탄퍼짐 배율"),
            Map.entry("max_inaccuracy", "최대 탄퍼짐 배율"), Map.entry("min_rpm_mod", "최소 연사 배율"),
            Map.entry("max_rpm_mod", "최대 연사 배율"), Map.entry("increase_per_tick", "틱당 충전량"),
            Map.entry("decrease_per_tick", "틱당 방전량"), Map.entry("decrease_on_fire", "발사 시 소모량"),
            Map.entry("max_charge", "최대 충전량"), Map.entry("fire_threshold", "발사 필요 충전량"),
            Map.entry("charge_during_cooldown", "대기 중 충전"), Map.entry("head_shot_multiplier", "헤드샷 배율"),
            Map.entry("speed", "탄속"), Map.entry("aim_inaccuracy", "조준 탄퍼짐"),
            Map.entry("other_inaccuracy", "비조준 탄퍼짐"), Map.entry("animation_type", "공격 동작"),
            Map.entry("fire_multiplier", "일반 총성 배율"), Map.entry("silence_multiplier", "소음기 총성 배율"), Map.entry("time", "지속 시간(초)"),
            Map.entry("hide_particles", "효과 입자 숨김"));

    private TaczStats() { }

    static void gun(List<String> lines, Object data) throws ReflectiveOperationException {
        lines.add("기본 성능 · 팩 기준 / 서버 배율·장착 효과 적용 전");
        Object bullet = call(data, "getBulletData");
        add(lines, "기본 피해량(탄환 1개)", bullet, "getDamageAmount", "");
        Object extra = call(bullet, "getExtraDamage");
        double head = extra == null ? 1 : number(extra, "getHeadShotMultiplier");
        double armor = extra == null ? 0 : number(extra, "getArmorIgnore");
        lines.add("헤드샷 배율: " + format(head) + "×");
        lines.add("방어구 무시율: " + format(armor * 100) + "%");
        if (extra != null) {
            List<?> ranges = (List<?>) call(extra, "getDamageAdjust");
            if (ranges != null) {
                double previous = 0;
                for (Object range : ranges) {
                    double distance = number(range, "getDistance");
                    String interval = distance > 1.0e9 ? format(previous) + "m 이후" : format(previous) + "~" + format(distance) + "m";
                    lines.add("거리별 피해량 · " + interval + ": " + format(number(range, "getDamage")));
                    previous = distance;
                }
            }
        }
        add(lines, "발당 탄환 수", bullet, "getBulletAmount", "개");
        add(lines, "탄속", bullet, "getSpeed", "m/s");
        add(lines, "관통 대상 수", bullet, "getPierce", "");
        add(lines, "탄환 유지 시간", bullet, "getLifeSecond", "초");
        add(lines, "탄환 중력 계수", bullet, "getGravity", "");
        add(lines, "탄환 공기 저항", bullet, "getFriction", "");
        if (Boolean.TRUE.equals(call(bullet, "hasTracerAmmo"))) {
            add(lines, "예광탄 사이 일반탄 수", bullet, "getTracerCountInterval", "발");
        }
        add(lines, "넉백", bullet, "getKnockback", "");
        Object ignite = call(bullet, "getIgnite");
        lines.add("대상 발화: " + yes(call(ignite, "isIgniteEntity")) + " / 블록 발화: " + yes(call(ignite, "isIgniteBlock")));
        if (Boolean.TRUE.equals(call(ignite, "isIgniteEntity"))) {
            add(lines, "발화 시간", bullet, "getIgniteEntityTime", "초");
        }
        Object explosion = call(bullet, "getExplosionData");
        if (explosion != null && Boolean.TRUE.equals(call(explosion, "isExplode"))) {
            bean(lines, "폭발", explosion);
        }
        int[] magazines = (int[]) call(data, "getExtendedMagAmmoAmount");
        if (magazines != null) {
            for (int i = 0; i < magazines.length; i++) {
                lines.add("확장 탄창 " + (i + 1) + "단계 장탄수: " + magazines[i] + "발");
            }
        }
        String bolt = call(data, "getBolt").toString();
        lines.add("작동 방식: " + switch (bolt) {
            case "OPEN_BOLT" -> "오픈 볼트";
            case "CLOSED_BOLT" -> "클로즈드 볼트";
            case "MANUAL_ACTION" -> "수동 장전";
            default -> bolt;
        });
        bean(lines, "총성", call(data, "getFireSound"));
        add(lines, "분당 발사 수", data, "getRoundsPerMinute", "발/분");
        add(lines, "무게", data, "getWeight", "kg");
        add(lines, "조준 소요 시간", data, "getAimTime", "초");
        add(lines, "꺼내기 시간", data, "getDrawTime", "초");
        add(lines, "집어넣기 시간", data, "getPutAwayTime", "초");
        add(lines, "질주 전환 시간", data, "getSprintTime", "초");
        add(lines, "볼트 작동 시간", data, "getBoltActionTime", "초");
        double boltFeed = number(data, "getBoltFeedTime");
        if (boltFeed >= 0) {
            lines.add("볼트 급탄 시간: " + format(boltFeed) + "초");
        }
        Object reload = call(data, "getReloadData");
        lines.add("급탄 방식: " + switch (call(reload, "getType").toString()) {
            case "MAGAZINE" -> "탄창 교체";
            case "MANUAL" -> "한 발씩 장전";
            case "FUEL" -> "연료 충전";
            case "INVENTORY" -> "인벤토리 급탄";
            default -> call(reload, "getType").toString();
        });
        Object feed = call(reload, "getFeed");
        Object cooldown = call(reload, "getCooldown");
        add(lines, "빈 탄창 급탄", feed, "getEmptyTime", "초");
        add(lines, "전술 급탄", feed, "getTacticalTime", "초");
        add(lines, "빈 탄창 재장전 완료", cooldown, "getEmptyTime", "초");
        add(lines, "전술 재장전 완료", cooldown, "getTacticalTime", "초");
        lines.add("무한 급탄: " + yes(call(reload, "isInfinite")));
        bean(lines, "탄퍼짐", call(data, "getInaccuracy"));
        bean(lines, "이동 속도 보정", call(data, "getMoveSpeed"));
        Object recoil = call(data, "getRecoil");
        if (recoil != null) {
            recoil(lines, "수직 반동", call(recoil, "getPitch"));
            recoil(lines, "수평 반동", call(recoil, "getYaw"));
        }
        add(lines, "엎드림 반동 배율", data, "getCrawlRecoilMultiplier", "×");
        add(lines, "피격 흔들림 배율", data, "getHurtBobTweakMultiplier", "×");
        bean(lines, "근접 공격", call(data, "getMeleeData"));
        bean(lines, "과열", call(data, "getHeatData"));
        lines.add("엎드리기: " + yes(call(data, "isCanCrawl")) + " / 슬라이딩: " + yes(call(data, "canSlide")));
        List<?> modes = (List<?>) call(data, "getFireModeSet");
        for (Object mode : modes) {
            Object adjustment = data.getClass().getMethod("getFireModeAdjustData", mode.getClass()).invoke(data, mode);
            bean(lines, modeName(mode.toString()) + " 추가 보정", adjustment);
            Object charge = data.getClass().getMethod("getChargeData", mode.getClass()).invoke(data, mode);
            bean(lines, modeName(mode.toString()) + " 충전", charge);
            if (mode.toString().equals("BURST")) {
                Object burst = call(data, "getBurstData");
                add(lines, "점사당 발사 수", burst, "getCount", "발");
                lines.add("누른 채 연속 점사: " + yes(call(burst, "isContinuousShoot")));
                add(lines, "점사 속도", burst, "getBpm", "회/분");
                add(lines, "점사 최소 간격", burst, "getMinInterval", "초");
            }
        }
    }

    static void attachment(List<String> lines, Object data) throws ReflectiveOperationException {
        double weight = number(data, "getWeight");
        if (weight != 0) {
            lines.add("부품 무게: " + format(weight) + "kg");
        }
        int level = ((Number) call(data, "getExtendedMagLevel")).intValue();
        if (level > 0) {
            lines.add("확장 탄창: " + level + "단계 · 장탄수는 총기별로 다름");
        }
        bean(lines, "근접 공격", call(data, "getMeleeData"));
        Map<?, ?> modifiers = (Map<?, ?>) call(data, "getModifier");
        for (Map.Entry<?, ?> modifier : modifiers.entrySet().stream().sorted(java.util.Comparator.comparing(e -> e.getKey().toString())).toList()) {
            String key = modifier.getKey().toString();
            Object property = modifier.getValue();
            List<?> nativeText = (List<?>) call(property, "getComponents");
            int before = lines.size();
            Object value = call(property, "getValue");
            if (value instanceof Pair<?, ?> pair) {
                if (key.equals("recoil")) {
                    bean(lines, "수직 반동", pair.left());
                    bean(lines, "수평 반동", pair.right());
                } else if (key.equals("silence")) {
                    bean(lines, "총성 전달 거리(m)", pair.left());
                    lines.add("소음기 총성: " + yes(pair.right()));
                }
            } else {
                bean(lines, label(key), value);
            }
            if (lines.size() == before) {
                for (Object text : nativeText) {
                    lines.add(((Component) text).getString());
                }
            }
        }
    }

    static String modeName(String mode) {
        return switch (mode) {
            case "AUTO" -> "자동";
            case "SEMI" -> "반자동";
            case "BURST" -> "점사";
            default -> mode;
        };
    }

    private static void recoil(List<String> lines, String title, Object frames) throws ReflectiveOperationException {
        if (frames == null) {
            return;
        }
        double min = Double.POSITIVE_INFINITY;
        double max = Double.NEGATIVE_INFINITY;
        for (Object frame : (Object[]) frames) {
            for (float value : (float[]) call(frame, "getValue")) {
                min = Math.min(min, value);
                max = Math.max(max, value);
            }
        }
        if (Double.isFinite(min)) {
            lines.add(title + " 범위: " + format(min) + " ~ " + format(max));
        }
    }

    private static void bean(List<String> lines, String title, Object value) {
        if (value != null) {
            json(lines, title, GSON.toJsonTree(value));
        }
    }

    private static void json(List<String> lines, String title, JsonElement value) {
        if (value.isJsonNull()) {
            return;
        }
        if (value.isJsonObject()) {
            JsonObject object = value.getAsJsonObject();
            if (object.has("addend") && object.has("percent") && object.has("multiplier")) {
                List<String> changes = new ArrayList<>();
                double add = object.get("addend").getAsDouble();
                double percent = object.get("percent").getAsDouble();
                double multiplier = object.get("multiplier").getAsDouble();
                if (add != 0) {
                    changes.add((add > 0 ? "+" : "") + format(title.contains("방어구") ? add * 100 : add)
                            + (title.contains("방어구") ? "%p" : ""));
                }
                if (percent != 0) {
                    changes.add(format(Math.abs(percent * 100)) + "% " + (percent > 0 ? "증가" : "감소"));
                }
                if (multiplier != 1) {
                    changes.add("×" + format(Math.max(0, multiplier)));
                }
                if (object.has("function") && !object.get("function").isJsonNull()
                        && !object.get("function").getAsString().isEmpty()) {
                    changes.add("총기 기본값에 따른 조건부 효과");
                }
                if (!changes.isEmpty()) {
                    lines.add(title + ": " + String.join(" · ", changes));
                }
                return;
            }
            for (Map.Entry<String, JsonElement> field : object.entrySet()) {
                json(lines, title + " · " + label(field.getKey()), field.getValue());
            }
        } else if (value.isJsonArray()) {
            for (int i = 0; i < value.getAsJsonArray().size(); i++) {
                json(lines, title + " " + (i + 1), value.getAsJsonArray().get(i));
            }
        } else if (value.getAsJsonPrimitive().isBoolean()) {
            lines.add(title + ": " + yes(value.getAsBoolean()));
        } else if (value.getAsJsonPrimitive().isNumber()) {
            double amount = value.getAsDouble();
            if (title.endsWith("효과 단계")) {
                lines.add(title + ": " + format(amount + 1));
            } else if (title.contains("이동 속도")) {
                lines.add(title + ": " + (amount > 0 ? "+" : "") + format(amount * 100) + "%");
            } else if (title.contains("방어구 무시율")) {
                lines.add(title + ": " + format(amount * 100) + "%p");
            } else {
                lines.add(title + ": " + format(amount));
            }
        } else {
            lines.add(title + ": " + value.getAsString());
        }
    }

    private static String label(String key) {
        return LABELS.getOrDefault(key.toLowerCase(java.util.Locale.ROOT), key);
    }

    private static String yes(Object value) {
        return Boolean.TRUE.equals(value) ? "사용" : "미사용";
    }

    private static void add(List<String> lines, String title, Object target, String method, String unit) throws ReflectiveOperationException {
        lines.add(title + ": " + format(number(target, method)) + unit);
    }

    private static double number(Object target, String method) throws ReflectiveOperationException {
        return ((Number) call(target, method)).doubleValue();
    }

    private static Object call(Object target, String method) throws ReflectiveOperationException {
        return target.getClass().getMethod(method).invoke(target);
    }

    private static String format(double value) {
        return BigDecimal.valueOf(value).setScale(3, java.math.RoundingMode.HALF_UP).stripTrailingZeros().toPlainString();
    }
}
