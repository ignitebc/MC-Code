package com.mcserver.serverutilities.monster;

/**
 * 몬스터 레벨별 경험치 구슬 배율과, 플레이어가 공격한 몹의 구슬을 인정하는 시간.
 *
 * <p>배율은 LV1 1배에서 레벨마다 0.3배씩 올라 LV7이 2.8배다. 몹이 죽을 때 떨어뜨리는 구슬에만 곱하며,
 * 사냥꾼 영혼 수확처럼 몹의 원래 경험치를 기준으로 하는 보상에는 곱하지 않는다. 마인크래프트 클래스에
 * 의존하지 않아 회귀 검사에서 바로 확인할 수 있다.
 */
public final class MonsterExperience {
    /** LV1~LV7 구슬 배율(%). 소수 오차 없이 반올림하도록 백분율 정수로 둔다. */
    private static final int[] EXPERIENCE_PERCENT = {100, 130, 160, 190, 220, 250, 280};
    private static final int PERCENT = 100;
    /** 레벨이 없는 몹의 배율(%). 바닐라와 같다. */
    public static final int VANILLA_PERCENT = PERCENT;
    /**
     * 플레이어가 마지막으로 공격한 뒤 공격자 없는 피해로 죽어도 구슬을 떨어뜨리는 시간(틱).
     *
     * <p>바닐라는 5초만 인정해 맹독 불화살처럼 오래 타다 죽은 몹은 구슬을 떨어뜨리지 않는다.
     * 직업 처치 보상(ArcLib {@code KillCreditTracker})과 같은 20초로 맞춘다.
     */
    public static final long PLAYER_HURT_CREDIT_TICKS = 20 * 20;

    private MonsterExperience() { }

    /** 레벨의 구슬 배율(%). 레벨이 없거나 범위를 벗어나면 바닐라 배율 */
    public static int experiencePercent(int level) {
        if (!MonsterLevel.isVisible(level)) return VANILLA_PERCENT;
        return EXPERIENCE_PERCENT[level - MonsterLevel.MIN_SCORE];
    }

    /** 구슬 경험치에 레벨 배율을 곱해 반올림한다. .5는 올린다. */
    public static int scale(int amount, int level) {
        if (amount <= 0) return amount;
        int scaledPercent = amount * experiencePercent(level);
        // 음수가 아니므로 절반을 더한 뒤 정수 나눗셈하면 반올림이 된다.
        return (scaledPercent + PERCENT / 2) / PERCENT;
    }

    /** 플레이어가 공격한 시각에서 인정 시간이 지나지 않았는지 */
    public static boolean withinPlayerHurtCredit(long playerHurtGameTime, long currentGameTime) {
        long elapsedTicks = currentGameTime - playerHurtGameTime;
        return elapsedTicks >= 0 && elapsedTicks <= PLAYER_HURT_CREDIT_TICKS;
    }
}
