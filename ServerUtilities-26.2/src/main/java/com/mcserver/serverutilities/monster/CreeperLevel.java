package com.mcserver.serverutilities.monster;

/**
 * 크리퍼 레벨별 폭발 배율, 화약 드롭 배수, 위험 단계. 레벨은 LV1~LV10이 같은 확률로 붙는다.
 *
 * <p>폭발 배율은 엔티티가 받는 폭발 피해와 블록을 부수는 폭발 범위에 똑같이 곱한다. 피해는 바닐라가 같은 자리에서
 * 주는 값의 배수이며, 피해가 닿는 거리와 넉백은 바닐라 그대로다. 마인크래프트 클래스에 의존하지 않아
 * 회귀 검사에서 바로 확인할 수 있다.
 */
public final class CreeperLevel {
    /** LV1~LV10 폭발 배율(%). LV1이 바닐라이고 레벨마다 50%씩 오른다. 표와 같은 값을 쓰도록 백분율 정수로 둔다. */
    private static final int[] EXPLOSION_PERCENT = {100, 150, 200, 250, 300, 350, 400, 450, 500, 550};
    /** LV1~LV10 화약 드롭 배수. 두 레벨마다 한 배씩 오른다. */
    private static final int[] GUNPOWDER_MULTIPLIERS = {1, 1, 2, 2, 3, 3, 4, 4, 5, 5};
    /**
     * LV1~LV10의 위험 단계(1~7). 글자 색, 경험치 구슬, 업적이 이 단계를 쓴다.
     * LV1~LV7을 뽑던 때와 단계별 비율이 비슷하도록 10레벨을 7단계에 나눠 담는다.
     */
    private static final int[] DANGER_STAGES = {1, 2, 3, 3, 4, 5, 5, 6, 7, 7};
    private static final float PERCENT = 100.0F;
    /** 뽑는 레벨 수. LV1부터 이 값까지 같은 확률이다. */
    public static final int LEVEL_COUNT = EXPLOSION_PERCENT.length;
    /** 레벨이 없는 크리퍼의 배율. 바닐라와 같다. */
    public static final float VANILLA_MULTIPLIER = 1.0F;
    /** 레벨이 없는 크리퍼의 화약 배수. 바닐라와 같다. */
    public static final int VANILLA_GUNPOWDER_MULTIPLIER = 1;

    private CreeperLevel() { }

    /**
     * 0 이상 {@link #LEVEL_COUNT} 미만의 난수를 레벨로 바꾼다.
     *
     * @throws IllegalArgumentException 난수가 범위를 벗어난 경우
     */
    public static int fromRoll(int roll) {
        if (roll < 0 || roll >= LEVEL_COUNT) {
            throw new IllegalArgumentException("크리퍼 레벨 난수 범위를 벗어났습니다: " + roll);
        }
        return roll + MonsterLevel.MIN_LEVEL;
    }

    /**
     * 레벨의 폭발 배율. 레벨이 없거나 범위를 벗어나면 바닐라 배율
     *
     * <p>장비 몬스터의 레벨은 LV18까지 올라가므로 표시 가능 범위가 아니라 크리퍼 레벨 범위로 검사한다.
     */
    public static float explosionMultiplier(int level) {
        if (!isCreeperLevel(level)) return VANILLA_MULTIPLIER;
        return EXPLOSION_PERCENT[level - MonsterLevel.MIN_LEVEL] / PERCENT;
    }

    /** 레벨의 화약 드롭 배수. 레벨이 없거나 범위를 벗어나면 바닐라 배수 */
    public static int gunpowderMultiplier(int level) {
        if (!isCreeperLevel(level)) return VANILLA_GUNPOWDER_MULTIPLIER;
        return GUNPOWDER_MULTIPLIERS[level - MonsterLevel.MIN_LEVEL];
    }

    /** 레벨의 위험 단계. 레벨이 없거나 범위를 벗어나면 {@link MonsterLevel#NONE} */
    public static int dangerStage(int level) {
        if (!isCreeperLevel(level)) return MonsterLevel.NONE;
        return DANGER_STAGES[level - MonsterLevel.MIN_LEVEL];
    }

    /** 크리퍼가 뽑을 수 있는 레벨인지 */
    public static boolean isCreeperLevel(int level) {
        return level >= MonsterLevel.MIN_LEVEL && level <= LEVEL_COUNT;
    }
}
