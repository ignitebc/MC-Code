package com.tacz.guns.event;

import com.tacz.guns.api.entity.IGunOperator;
import com.tacz.guns.api.item.IGun;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

/**
 * 차원을 넘을 때 총기 데이터가 새로 고쳐지지 않는 문제를 고친다. 서버 쪽 새로 고침이다
 */
public class TravelToDimensionEvent {
    public static void onTravelToDimension(Entity originalEntity, Entity newEntity, ServerLevel origin, ServerLevel destination) {
        if (newEntity instanceof LivingEntity livingEntity && livingEntity.getMainHandItem().getItem() instanceof IGun) {
            IGunOperator.fromLivingEntity(livingEntity).initialData();
        }
    }
}
