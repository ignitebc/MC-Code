package com.mcserver.serverutilities.monster;

/**
 * 크리퍼 레벨별 폭발 배율. 레벨은 LV1~LV7이 같은 확률로 붙는다.
 *
 * <p>배율은 엔티티가 받는 폭발 피해와 블록을 부수는 폭발 범위에 똑같이 곱한다. 피해는 바닐라가 같은 자리에서
 * 주는 값의 배수이며, 피해가 닿는 거리와 넉백은 바닐라 그대로다. 마인크래프트 클래스에 의존하지 않아
 * 회귀 검사에서 바로 확인할 수 있다.
 */
public final class CreeperLevel {
    /** LV1~LV7 폭발 배율(%). 표와 같은 값을 그대로 쓰도록 백분율 정수로 둔다. */
    private static final int[] EXPLOSION_PERCENT = {100, 130, 160, 190, 210, 240, 270};
    private static final float PERCENT = 100.0F;
    /** 뽑는 레벨 수. LV1부터 이 값까지 같은 확률이다. */
    public static final int LEVEL_COUNT = EXPLOSION_PERCENT.length;
    /** 레벨이 없는 크리퍼의 배율. 바닐라와 같다. */
    public static final float VANILLA_MULTIPLIER = 1.0F;

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
        return roll + MonsterLevel.MIN_SCORE;
    }

    /** 레벨의 폭발 배율. 레벨이 없거나 범위를 벗어나면 바닐라 배율 */
    public static float explosionMultiplier(int level) {
        if (!MonsterLevel.isVisible(level)) return VANILLA_MULTIPLIER;
        return EXPLOSION_PERCENT[level - MonsterLevel.MIN_SCORE] / PERCENT;
    }
}
