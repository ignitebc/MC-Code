package com.mcserver.serverutilities.monster;

/**
 * 몬스터 머리 위에 표시하는 레벨과 그 레벨의 위험 단계.
 *
 * <p>장비 몬스터의 레벨은 방어구 점수와 총기 점수의 합이다. 방어구는 가죽 1점부터 서리빛 11점까지,
 * 총기는 총기 등급 F 1점부터 S 7점까지다. 지급받지 못한 쪽과 근접 무기, 원래 무기는 0점이며
 * 합이 0이어도 LV1로 표시한다.
 *
 * <p>위험 단계는 머리 위 글자 색, 경험치 구슬 배율, 재료 보상, 업적에 쓰는 1~7단계다. 장비 몬스터는
 * 레벨 구간으로 정하고, LV1~LV10을 뽑는 크리퍼는 {@link CreeperLevel#dangerStage}의 표를 쓴다.
 * 마인크래프트 클래스에 의존하지 않아 회귀 검사에서 바로 확인할 수 있다.
 */
public final class MonsterLevel {
    /** 레벨이나 단계를 표시하지 않는 개체 */
    public static final int NONE = 0;
    public static final int MIN_LEVEL = 1;
    /** 서리빛 풀세트의 점수 */
    public static final int MAX_ARMOR_SCORE = 11;
    /** S등급 총기의 점수 */
    public static final int MAX_WEAPON_SCORE = 7;
    public static final int MAX_LEVEL = MAX_ARMOR_SCORE + MAX_WEAPON_SCORE;
    /** 지급받지 못했거나 레벨 계산에서 제외하는 장비의 점수 */
    public static final int MISSING_SCORE = 0;
    public static final int MIN_STAGE = 1;
    public static final int MAX_STAGE = 7;
    /** 장비 몬스터의 단계별 마지막 레벨. n번째 값까지가 n+1단계다. */
    private static final int[] STAGE_LAST_LEVELS = {2, 4, 6, 8, 11, 14, MAX_LEVEL};

    private MonsterLevel() { }

    /** 두 점수를 더한 레벨. 합이 0이면 LV1이다. */
    public static int of(int armorScore, int weaponScore) {
        int total = clampScore(armorScore, MAX_ARMOR_SCORE) + clampScore(weaponScore, MAX_WEAPON_SCORE);
        return Math.max(MIN_LEVEL, total);
    }

    /** 머리 위에 표시할 수 있는 레벨인지 */
    public static boolean isVisible(int level) {
        return level >= MIN_LEVEL && level <= MAX_LEVEL;
    }

    /**
     * 레벨의 위험 단계.
     *
     * @param creeper 크리퍼인지. 크리퍼는 레벨 구간 대신 크리퍼 레벨 표로 단계를 정한다.
     * @return 1~7단계. 레벨이 없거나 범위를 벗어나면 {@link #NONE}
     */
    public static int stage(int level, boolean creeper) {
        if (creeper) return CreeperLevel.dangerStage(level);
        if (!isVisible(level)) return NONE;

        for (int index = 0; index < STAGE_LAST_LEVELS.length; index++) {
            if (level <= STAGE_LAST_LEVELS[index]) return index + MIN_STAGE;
        }
        return NONE;
    }

    private static int clampScore(int score, int maxScore) {
        return Math.max(MISSING_SCORE, Math.min(maxScore, score));
    }
}
