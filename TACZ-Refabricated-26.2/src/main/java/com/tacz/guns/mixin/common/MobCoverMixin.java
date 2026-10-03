package com.tacz.guns.mixin.common;

import com.tacz.guns.entity.ai.CoverCombatant;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/** 엄폐 행동이 붙잡은 공격 대상을 기록한다. 대상 엔티티 대신 ID를 들고 있어 사라진 엔티티를 붙잡아 두지 않는다. */
@Mixin(Mob.class)
public abstract class MobCoverMixin implements CoverCombatant {
    @Unique
    private int tacz$heldTargetId = -1;
    @Unique
    private long tacz$holdUntilTick = Long.MIN_VALUE;

    @Override
    public void tacz$holdTarget(Entity target, long untilTick) {
        this.tacz$heldTargetId = target.getId();
        this.tacz$holdUntilTick = untilTick;
    }

    @Override
    public void tacz$releaseTarget() {
        this.tacz$heldTargetId = -1;
        this.tacz$holdUntilTick = Long.MIN_VALUE;
    }

    @Override
    public boolean tacz$isHoldingTarget(Entity target, long gameTime) {
        return this.tacz$heldTargetId == target.getId() && gameTime <= this.tacz$holdUntilTick;
    }
}
