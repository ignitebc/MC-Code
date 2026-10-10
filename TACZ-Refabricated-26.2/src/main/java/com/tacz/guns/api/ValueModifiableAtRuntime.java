package com.tacz.guns.api;

import net.minecraft.world.phys.Vec3;

import java.lang.annotation.*;

/**
 * 문서용 어노테이션. 이 어노테이션이 붙은 총기 속성은 적용 시점의 값을 로직 스크립트가 바꿀 수 있다.
 *
 * @author ChloePrime
 * @see com.tacz.guns.entity.EntityKineticBullet
 * @see com.tacz.guns.entity.EntityKineticBullet#getDamage(Vec3)
 * @see com.tacz.guns.item.ModernKineticGunScriptAPI#shootOnce(boolean)
 * @since 1.1.7
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.SOURCE)
@Repeatable(Holder.class)
public @interface ValueModifiableAtRuntime {
    /**
     * 실행 시 값의 종류
     */
    Class<?> value();
}


/**
 * @author ChloePrime
 * @since 1.1.7
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.SOURCE)
@interface Holder {
    ValueModifiableAtRuntime[] value();
}