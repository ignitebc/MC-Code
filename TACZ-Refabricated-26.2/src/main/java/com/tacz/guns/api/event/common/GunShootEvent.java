package com.tacz.guns.api.event.common;

import cn.sh1rocu.tacz.api.LogicalSide;
import cn.sh1rocu.tacz.api.event.BaseEvent;
import cn.sh1rocu.tacz.api.event.ICancellableEvent;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

/**
 * 생물이 사격할 때 발생하는 이벤트. {@link GunFireEvent}와 달리 방아쇠를 한 번 당기면 한 번만 발생하지만, {@link GunFireEvent}는 여러 번 발생할 수 있다(예: 총기가 Burst 모드일 때)
 */
public class GunShootEvent extends BaseEvent implements ICancellableEvent {
    private final LivingEntity shooter;
    private final ItemStack gunItemStack;
    private final LogicalSide logicalSide;

    public static final Event<Callback> CALLBACK = EventFactory.createArrayBacked(Callback.class, callbacks -> event -> {
        for (Callback callback : callbacks) {
            callback.post(event);
        }
    });

    public interface Callback {
        void post(GunShootEvent event);
    }

    public GunShootEvent(LivingEntity shooter, ItemStack gunItemStack, LogicalSide side) {
        this.shooter = shooter;
        this.gunItemStack = gunItemStack;
        this.logicalSide = side;
    }

    public LivingEntity getShooter() {
        return shooter;
    }

    public ItemStack getGunItemStack() {
        return gunItemStack;
    }

    public LogicalSide getLogicalSide() {
        return logicalSide;
    }
}
