package com.mcserver.serverutilities.monster;

/**
 * 몬스터 머리 위에 표시하는 레벨. 방어구 점수와 무기 점수의 평균을 반올림한다.
 *
 * <p>점수는 등급 F=1부터 S=7까지다. 지급받지 못한 쪽은 가장 낮은 1점으로 치고, 검·도끼·창은
 * 등급표가 없어 2점으로 친다. 마인크래프트 클래스에 의존하지 않아 회귀 검사에서 바로 확인할 수 있다.
 */
public final class MonsterLevel {
    /** 레벨을 표시하지 않는 개체 */
    public static final int NONE = 0;
    public static final int MIN_SCORE = 1;
    public static final int MAX_SCORE = 7;
    /** 지급받지 못한 방어구나 무기의 점수 */
    public static final int MISSING_SCORE = MIN_SCORE;
    /** 검·도끼·창의 점수 */
    public static final int MELEE_WEAPON_SCORE = 2;

    private MonsterLevel() { }

    /** 두 점수의 평균을 반올림한 레벨. 평균이 .5로 끝나면 올린다. */
    public static int of(int armorScore, int weaponScore) {
        int total = clampScore(armorScore) + clampScore(weaponScore);
        // 합이 홀수면 평균이 .5로 끝나므로 1을 더한 뒤 정수 나눗셈을 하면 올림이 된다.
        return (total + 1) / 2;
    }

    /** 머리 위에 표시할 수 있는 레벨인지 */
    public static boolean isVisible(int level) {
        return level >= MIN_SCORE && level <= MAX_SCORE;
    }

    private static int clampScore(int score) {
        return Math.max(MIN_SCORE, Math.min(MAX_SCORE, score));
    }
}
