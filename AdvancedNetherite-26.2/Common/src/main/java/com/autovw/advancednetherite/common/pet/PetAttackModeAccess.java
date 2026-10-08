package com.autovw.advancednetherite.common.pet;

/** 플레이어 저장 데이터에 둔 펫 공격 방식. 서버 플레이어 Mixin이 구현하며, 사망·재접속 후에도 유지된다. */
public interface PetAttackModeAccess
{
    PetAttackMode advancednetherite$getPetAttackMode();

    void advancednetherite$setPetAttackMode(PetAttackMode mode);
}
