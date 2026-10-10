package com.tacz.guns.api.entity;

import net.minecraft.world.entity.LivingEntity;

/**
 * 엔티티가 탄환에 맞은 뒤 밀려나는 효과를 바꾸기 위한 설계
 * 모든 LivingEntity에 기본으로 이 인터페이스를 붙인다
 */
public interface KnockBackModifier {
    /**
     * LivingEntity가 Mixin으로 이 인터페이스를 구현한다
     */
    static KnockBackModifier fromLivingEntity(LivingEntity entity) {
        return (KnockBackModifier) entity;
    }

    /**
     * 밀어내기 효과를 초기화한다. 엔티티는 바닐라 밀어내기 로직으로 돌아간다
     */
    void resetKnockBackStrength();

    /**
     * 밀어내기 세기를 얻는다
     */
    double getKnockBackStrength();

    /**
     * 밀어내기 세기를 설정한다
     */
    void setKnockBackStrength(double strength);
}
