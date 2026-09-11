package com.mcserver.serverutilities.combat;

/**
 * 방어도가 높은 구간의 피해 감소 규칙.
 *
 * <p>바닐라는 유효 방어도를 20으로 자른 뒤 25로 나누므로 기본 방어 계산의 피해 감소가 80%에서 멈춘다.
 * 여기서는 그 상한을 없애고 20을 넘는 구간을 완만한 곡선으로 잇는다. 유한한 방어도로는 감소율이
 * 1.0에 도달하지 않으므로 무적 상태가 생기지 않는다.
 */
public final class ArmorRules {
    /** 바닐라가 유효 방어도를 자르던 상한. 이 값까지는 기존 계산과 결과가 같다. */
    private static final float LINEAR_LIMIT = 20.0F;

    /** 바닐라가 유효 방어도를 감소율로 바꿀 때 나누는 값. */
    private static final float PROTECTION_DIVIDER = 25.0F;

    /**
     * 곡선 구간의 완만함을 정하는 값.
     *
     * <p>두 구간이 경계에서 같은 감소율을 내려면 이 값이 PROTECTION_DIVIDER 빼기 LINEAR_LIMIT 이어야 한다.
     * 다른 값을 쓰면 방어도 20 근처에서 감소율이 튀므로 설정으로 열지 않는다.
     */
    private static final float CURVE_OFFSET = PROTECTION_DIVIDER - LINEAR_LIMIT;

    private ArmorRules() { }

    /**
     * 바닐라의 유효 방어도 clamp 자리를 대신한다.
     *
     * <p>바닐라는 이 자리에서 나온 값을 곧바로 PROTECTION_DIVIDER로 나눠 감소율을 만든다.
     * 그래서 곡선 구간에서는 원하는 감소율에 PROTECTION_DIVIDER를 다시 곱해 돌려주어야
     * 최종 감소율이 의도한 값이 된다.
     *
     * @param rawArmor     방어도에서 피해량 보정을 뺀 값
     * @param minimumArmor 방어도의 20%. 큰 피해를 받아도 남는 최소 방어량
     * @return 바닐라가 그대로 나눠 쓸 수 있는 유효 방어도
     */
    public static float effectiveArmor(float rawArmor, float minimumArmor) {
        float effectiveArmor = Math.max(rawArmor, minimumArmor);
        if (effectiveArmor <= LINEAR_LIMIT) {
            return effectiveArmor;
        }

        float reduction = effectiveArmor / (effectiveArmor + CURVE_OFFSET);
        return reduction * PROTECTION_DIVIDER;
    }

    /**
     * 특정 유효 방어도에서의 피해 감소율. 표시와 검증에 쓴다.
     *
     * @param effectiveArmor 상한을 적용하지 않은 유효 방어도
     * @return 0 이상 1 미만의 피해 감소율
     */
    public static float damageReduction(float effectiveArmor) {
        if (effectiveArmor <= LINEAR_LIMIT) {
            return effectiveArmor / PROTECTION_DIVIDER;
        }
        return effectiveArmor / (effectiveArmor + CURVE_OFFSET);
    }
}
