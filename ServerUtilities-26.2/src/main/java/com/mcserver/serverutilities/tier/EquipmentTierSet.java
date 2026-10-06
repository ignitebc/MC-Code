package com.mcserver.serverutilities.tier;

/**
 * 장비 하나에 붙은 등급 묶음. 세 등급은 서로 따로 추첨한다.
 *
 * @param durability  최대 내구도에 쓰는 등급
 * @param performance 채굴 속도, 공격력, 방어도 중 그 장비가 쓰는 항목에 쓰는 등급
 * @param health      최대 체력에 쓰는 등급. 체력 등급을 받기 전의 예전 장비를 읽을 때만 null이다.
 */
public record EquipmentTierSet(EquipmentTier durability, EquipmentTier performance, EquipmentTier health) {
}
