package com.tacz.guns.api.entity;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.EntityHitResult;

/**
 * {@link LivingEntity}는 아니지만 탄환에 맞을 수 있는 특수 엔티티를 처리할 때 쓴다
 */
public interface ITargetEntity {
    /**
     * @param projectile 투사체 엔티티
     * @param result     엔티티에 맞은 위치
     * @param source     피해 원인 종류
     * @param damage     피해량
     */
    void onProjectileHit(Entity projectile, EntityHitResult result, DamageSource source, float damage);
}
