package com.tacz.guns.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.tacz.guns.GunMod;
import net.minecraft.resources.Identifier;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 총기 ID별 S~F 등급을 점수(F=1 ~ S=7)로 돌려준다.
 *
 * <p>등급 정본은 {@code tools/apply_tier_colors.py} 의 표다. 이름 색은 클라이언트 언어 파일에만 들어가
 * 서버가 읽을 수 없으므로, 스크립트가 같은 표를 {@code tacz/gun_grades.json} 으로도 만든다.
 * 등급표를 고칠 때는 스크립트를 다시 실행해 두 결과를 함께 갱신한다.
 */
public final class GunGrades {
    private static final String GRADES_FILE = "/tacz/gun_grades.json";
    /** 등급 글자 순서. 앞에서부터 1점이다. */
    private static final String GRADE_ORDER = "FEDCBAS";
    /** 등급표에 없는 총기의 점수. 다른 총기팩의 총기가 여기에 해당한다. */
    public static final int UNKNOWN_SCORE = 1;

    private static final Map<Identifier, Integer> SCORES = load();
    /** 등급표에 없는 총기를 한 번씩만 경고하기 위한 기록 */
    private static final Set<Identifier> WARNED = ConcurrentHashMap.newKeySet();

    private GunGrades() {
    }

    /** 총기 등급 점수. 등급표에 없으면 가장 낮은 F 점수를 쓴다. */
    public static int score(Identifier gunId) {
        Integer score = SCORES.get(gunId);
        if (score != null) {
            return score;
        }
        if (WARNED.add(gunId)) {
            GunMod.LOGGER.warn("총기 등급표에 없는 총기입니다. F 등급으로 계산합니다: {}", gunId);
        }
        return UNKNOWN_SCORE;
    }

    private static Map<Identifier, Integer> load() {
        try (InputStream stream = GunMod.class.getResourceAsStream(GRADES_FILE)) {
            if (stream == null) {
                throw new IllegalStateException("총기 등급표가 없습니다: " + GRADES_FILE);
            }
            try (Reader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
                return parse(JsonParser.parseReader(reader).getAsJsonObject());
            }
        } catch (IOException exception) {
            throw new IllegalStateException("총기 등급표를 읽지 못했습니다: " + GRADES_FILE, exception);
        }
    }

    private static Map<Identifier, Integer> parse(JsonObject grades) {
        Map<Identifier, Integer> scores = new HashMap<>();
        for (Map.Entry<String, JsonElement> entry : grades.entrySet()) {
            Identifier gunId = Identifier.tryParse(entry.getKey());
            String grade = entry.getValue().getAsString();
            int gradeIndex = GRADE_ORDER.indexOf(grade);
            boolean validGrade = grade.length() == 1 && gradeIndex >= 0;
            if (gunId == null || !validGrade) {
                throw new IllegalStateException("총기 등급표 항목이 잘못되었습니다: " + entry.getKey() + "=" + grade);
            }
            scores.put(gunId, gradeIndex + 1);
        }
        return Map.copyOf(scores);
    }
}
