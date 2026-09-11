package com.mcserver.serverutilities.tier;

/**
 * 장비에 무작위로 붙는 등급.
 *
 * <p>1티어는 아이템의 기본 수치를 그대로 쓰고, 등급이 내려갈수록 수치가 낮아진다.
 * 내구도는 다른 항목보다 가파르게 떨어진다.
 *
 * <p>배율을 실수가 아니라 백분율 정수로 들고 있는다. 내구도는 정수 연산으로 반올림해야
 * 0.7 같은 값의 이진 오차 때문에 기준표와 1씩 어긋나는 일이 생기지 않는다.
 */
public enum EquipmentTier {
    ONE(1, 100, 100),
    TWO(2, 90, 95),
    THREE(3, 80, 90),
    FOUR(4, 70, 85),
    FIVE(5, 60, 80);

    private static final int PERCENT = 100;

    private final int level;
    private final int durabilityPercent;
    private final int performancePercent;

    EquipmentTier(int level, int durabilityPercent, int performancePercent) {
        this.level = level;
        this.durabilityPercent = durabilityPercent;
        this.performancePercent = performancePercent;
    }

    public int level() {
        return this.level;
    }

    /**
     * 기본 최대 내구도에 등급을 적용한 값. 소수점은 반올림하고 최소 1을 보장한다.
     *
     * <p>정수만으로 계산하므로 기준표와 항상 같은 값이 나온다.
     */
    public int scaleDurability(int baseMaxDamage) {
        long scaled = (baseMaxDamage * (long) this.durabilityPercent + PERCENT / 2) / PERCENT;
        return (int) Math.max(1L, scaled);
    }

    /** 채굴 속도, 공격력, 방어도에 공통으로 곱하는 값 */
    public double performanceMultiplier() {
        return this.performancePercent / (double) PERCENT;
    }

    /**
     * 기록된 등급 번호에 해당하는 티어를 찾는다.
     *
     * @return 해당하는 티어. 번호가 범위 밖이면 null
     */
    public static EquipmentTier byLevel(int level) {
        for (EquipmentTier tier : values()) {
            if (tier.level == level) {
                return tier;
            }
        }
        return null;
    }
}
