package com.autovw.advancednetherite.common.pet;

import java.util.UUID;

/**
 * 플레이어가 소유한 펫 한 마리의 영속 기록.
 * 펫 엔티티는 주인이 접속 중일 때만 존재하며, 이 기록이 소유의 원본이다.
 *
 * @param id           기록 고유 ID. 살아 있는 펫 엔티티와 1:1로 연결된다.
 * @param petTypeId    펫 엔티티 타입의 레지스트리 ID (예: advancednetherite:dialga_pet)
 * @param attackDamage 펫 상자 등급에 따라 정해진 공격력
 * @param enabled      ON/OFF 상태. OFF면 접속해도 소환되지 않는다.
 */
public record PetRecord(UUID id, String petTypeId, double attackDamage, boolean enabled)
{
    public PetRecord withEnabled(boolean newEnabled)
    {
        return new PetRecord(this.id, this.petTypeId, this.attackDamage, newEnabled);
    }
}
