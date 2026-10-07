package com.tacz.guns.mixin.common;

import com.tacz.guns.entity.ai.CoverCombatant;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.GoalSelector;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import javax.annotation.Nullable;

/**
 * 엄폐 행동이 붙잡은 공격 대상, 잡아 둔 엄폐 칸, 총소리 경보 대상을 기록한다.
 * 대상 엔티티 대신 ID를 들고 있어 사라진 엔티티를 붙잡아 두지 않는다.
 */
@Mixin(Mob.class)
public abstract class MobCoverMixin implements CoverCombatant {
    @Shadow
    @Final
    protected GoalSelector targetSelector;

    @Unique
    private int tacz$heldTargetId = -1;
    @Unique
    private long tacz$holdUntilTick = Long.MIN_VALUE;
    @Unique
    @Nullable
    private BlockPos tacz$coverPos;
    @Unique
    private int tacz$alertTargetId = -1;
    @Unique
    private long tacz$alertUntilTick = Long.MIN_VALUE;

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

    @Override
    @Nullable
    public BlockPos tacz$getCoverPos() {
        return this.tacz$coverPos;
    }

    @Override
    public void tacz$setCoverPos(@Nullable BlockPos coverPos) {
        this.tacz$coverPos = coverPos;
    }

    @Override
    public void tacz$alertTarget(Entity target, long untilTick) {
        this.tacz$alertTargetId = target.getId();
        this.tacz$alertUntilTick = untilTick;
    }

    @Override
    public boolean tacz$isAlertedTo(Entity target, long gameTime) {
        return this.tacz$alertTargetId == target.getId() && gameTime <= this.tacz$alertUntilTick;
    }

    @Override
    public boolean tacz$usesTargetGoals() {
        return !this.targetSelector.getAvailableGoals().isEmpty();
    }
}
