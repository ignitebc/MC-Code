package com.tacz.guns.entity.ai;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;

/**
 * 엄폐 중인 몬스터의 상태를 다른 AI와 다른 몬스터에게 알린다. Mixin으로 모든 Mob에 붙는다.
 * <p>
 * 바닐라 대상 지정은 대상이 3초 동안 보이지 않으면 대상을 버린다. 엄폐는 일부러 시야를 끊는 행동이므로,
 * 엄폐 행동이 붙잡고 있는 동안에는 보이는 것으로 친다.
 * 또 몬스터가 잡아 둔 엄폐 칸, 제압 상태, 사격 중 여부, 측면 우회 역할, 대상을 마지막으로 본 위치를 함께 둔다.
 * 무리의 다른 몬스터와 총알·총성 처리처럼 Goal 밖에서도 읽고 써야 하는 값이기 때문이다.
 */
public interface CoverCombatant {
    /** 이 틱까지 대상을 붙잡는다. 엄폐 행동이 매 틱 갱신한다. */
    void tacz$holdTarget(Entity target, long untilTick);

    void tacz$releaseTarget();

    boolean tacz$isHoldingTarget(Entity target, long gameTime);

    /** 이 몬스터가 숨으려고 잡아 둔 엄폐 칸. 다른 몬스터는 이 칸을 고르지 않는다. 없으면 null. */
    @Nullable
    BlockPos tacz$getCoverPos();

    void tacz$setCoverPos(@Nullable BlockPos coverPos);

    /**
     * 총소리 경보로 이 틱까지 대상을 붙잡는다({@link GunfireAlert}).
     * 그동안은 대상이 보이지 않거나 추적 범위 밖에 있어도 대상을 버리지 않는다.
     */
    void tacz$alertTarget(Entity target, long untilTick);

    boolean tacz$isAlertedTo(Entity target, long gameTime);

    /** 공격 대상 Goal로 대상을 고르는 몬스터인지. 피글린처럼 Brain으로만 고르는 몬스터는 false다. */
    boolean tacz$usesTargetGoals();

    /** 가까이로 총알이 지나가 이 틱까지 제압당한다({@link BulletSuppression}). */
    void tacz$suppress(long untilTick);

    boolean tacz$isSuppressed(long gameTime);

    /** 몸을 내밀어 쏘거나 엄폐 없이 싸우는 중임을 알린다. 무리의 다른 몬스터가 사격 공백을 메우는 데 쓴다. */
    void tacz$markFiring(long gameTime);

    boolean tacz$isFiring(long gameTime);

    /** 무리 안에서 측면으로 돌아 들어가는 역할을 맡았는지 */
    boolean tacz$isFlanker();

    void tacz$setFlanker(boolean flanker);

    /** 대상을 마지막으로 본(또는 짐작한) 위치를 기억한다. 보이지 않는 대상의 지금 위치 대신 이 위치를 쓴다. */
    void tacz$rememberThreat(Entity threat, Vec3 position);

    /** 이 대상을 마지막으로 본 위치. 이 대상에 대한 기억이 없으면 null */
    @Nullable
    Vec3 tacz$lastKnownThreatPos(Entity threat);
}
