package com.autovw.advancednetherite.common.pet;

/**
 * 펫의 성장 규칙. 레벨은 1부터 시작하고, 몹을 한 대 칠 때마다 경험치 1을 얻는다.
 */
public final class PetStats
{
    public static final int MAX_LEVEL = 100;

    /** 레벨이 하나 오를 때마다 더해지는 공격력 */
    private static final double ATTACK_PER_LEVEL = 0.1;
    /** 레벨이 하나 오를 때마다 더해지는 최대 체력 */
    private static final double HEALTH_PER_LEVEL = 10.0;
    /** 죽은 펫이 부활하기까지 레벨 하나당 걸리는 시간(10초) */
    private static final long REVIVE_MILLIS_PER_LEVEL = 10_000L;

    private PetStats()
    {
    }

    /**
     * 다음 레벨까지 필요한 경험치. Jobs+ 직업 레벨과 같은 곡선이다.
     * Jobs+가 이 모드에 의존하는 구조라 그쪽 코드를 부를 수 없어 같은 식을 여기에 둔다.
     */
    public static int expToLevelUp(int level)
    {
        return (int) (100 + level * level * 0.5791);
    }

    public static double attackDamage(PetRarity rarity, int level)
    {
        // 0.1을 여러 번 더하면 1.2000000000000002 같은 값이 나오므로 소수 첫째 자리로 맞춘다.
        return Math.round((rarity.attackDamage() + ATTACK_PER_LEVEL * (level - 1)) * 10.0) / 10.0;
    }

    public static double maxHealth(PetRarity rarity, int level)
    {
        return rarity.baseHealth() + HEALTH_PER_LEVEL * (level - 1);
    }

    /** 이 레벨에서 죽었을 때 부활까지 걸리는 시간. 10레벨이면 100초 */
    public static long reviveMillis(int level)
    {
        return level * REVIVE_MILLIS_PER_LEVEL;
    }

    public static int clampLevel(int level)
    {
        return Math.max(1, Math.min(level, MAX_LEVEL));
    }
}
