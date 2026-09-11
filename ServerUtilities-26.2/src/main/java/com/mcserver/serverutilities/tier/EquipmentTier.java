package com.mcserver.serverutilities.tier;

/**
 * 장비에 무작위로 붙는 등급. S가 가장 좋고 F가 가장 나쁘다.
 *
 * <p>S는 아이템의 기본 수치를 그대로 쓰고, 등급이 내려갈수록 내구도는 7%씩, 그 밖의 수치는
 * 4%씩 낮아진다. 일곱 등급이 같은 확률로 붙는다.
 *
 * <p>배율을 실수가 아니라 백분율 정수로 들고 있는다. 내구도는 정수 연산으로 반올림해야
 * 0.79 같은 값의 이진 오차 때문에 기준표와 1씩 어긋나는 일이 생기지 않는다.
 */
public enum EquipmentTier {
    S(1, "S", 100, 100),
    A(2, "A", 93, 96),
    B(3, "B", 86, 92),
    C(4, "C", 79, 88),
    D(5, "D", 72, 84),
    E(6, "E", 65, 80),
    F(7, "F", 58, 76);

    private static final int PERCENT = 100;

    /** 커스텀 데이터에 기록하는 번호. 글자는 바뀔 수 있어도 이 번호는 고정이다. */
    private final int level;
    private final String label;
    private final int durabilityPercent;
    private final int performancePercent;

    EquipmentTier(int level, String label, int durabilityPercent, int performancePercent) {
        this.level = level;
        this.label = label;
        this.durabilityPercent = durabilityPercent;
        this.performancePercent = performancePercent;
    }

    public int level() {
        return this.level;
    }

    /** 화면에 표시하는 등급 글자 */
    public String label() {
        return this.label;
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
     * 기록된 등급 번호에 해당하는 등급을 찾는다.
     *
     * @return 해당하는 등급. 번호가 범위 밖이면 null
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
