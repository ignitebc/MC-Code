package com.autovw.advancednetherite.common.pet;

/**
 * 펫이 싸움에 나서는 방식. 플레이어마다 하나이며 보유한 모든 펫에 같이 적용한다.
 * <p>
 * 싸우지 않게 하려면 펫을 OFF로 넣으면 되므로 두 방식만 둔다.
 */
public enum PetAttackMode
{
    /** 주인 주변 3청크 안의 적대 몹(중립 몹 포함)을 먼저 공격하고, 주인이 맞으면 공격자도 노린다. */
    AUTO("자동공격"),
    /** 주인이 맞았을 때만 주인 주변 3청크 안의 공격자를 노린다. 엔드처럼 중립 몹이 많은 곳에서 쓴다. */
    NORMAL("일반공격");

    private final String label;

    PetAttackMode(String label)
    {
        this.label = label;
    }

    /** 화면에 표시하는 이름 */
    public String label()
    {
        return this.label;
    }

    public PetAttackMode next()
    {
        if (this == AUTO)
        {
            return NORMAL;
        }
        return AUTO;
    }

    /** 저장된 이름을 읽는다. 모르는 값이면 기존 동작인 자동공격이다. */
    public static PetAttackMode fromName(String name)
    {
        for (PetAttackMode mode : values())
        {
            if (mode.name().equals(name))
            {
                return mode;
            }
        }
        return AUTO;
    }
}
