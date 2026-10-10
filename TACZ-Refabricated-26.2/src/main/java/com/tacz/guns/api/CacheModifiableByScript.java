package com.tacz.guns.api;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 문서용 어노테이션. 이 어노테이션이 붙은 총기 속성은 부착물 캐시 안의 값을 로직 스크립트가 바꿀 수 있다.
 *
 * @author ChloePrime
 * @see com.tacz.guns.resource.modifier.AttachmentPropertyManager#postChangeEvent(LivingEntity, ItemStack)
 * @since 1.1.7
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.SOURCE)
public @interface CacheModifiableByScript {
}