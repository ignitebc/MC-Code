package com.tacz.guns.api.event.server;

import cn.sh1rocu.tacz.api.event.BaseEvent;
import cn.sh1rocu.tacz.api.event.ICancellableEvent;
import com.tacz.guns.entity.EntityKineticBullet;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/**
 * 탄환이 블록에 맞을 때 발생하는 이벤트. 지금은 서버에서만 발생한다
 */
public class AmmoHitBlockEvent extends BaseEvent implements ICancellableEvent {
    private final Level level;
    private final BlockHitResult hitResult;
    private final BlockState state;
    private final EntityKineticBullet ammo;

    public static final Event<Callback> CALLBACK = EventFactory.createArrayBacked(Callback.class, callbacks -> event -> {
        for (Callback e : callbacks) e.post(event);
    });

    public interface Callback {
        void post(AmmoHitBlockEvent event);
    }

    public AmmoHitBlockEvent(Level level, BlockHitResult hitResult, BlockState state, EntityKineticBullet ammo) {
        this.level = level;
        this.hitResult = hitResult;
        this.state = state;
        this.ammo = ammo;
    }

    public Level getLevel() {
        return level;
    }

    public BlockHitResult getHitResult() {
        return hitResult;
    }

    public BlockState getState() {
        return state;
    }

    public EntityKineticBullet getAmmo() {
        return ammo;
    }
}
