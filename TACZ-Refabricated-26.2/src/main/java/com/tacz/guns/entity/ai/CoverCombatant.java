package com.tacz.guns.entity.ai;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;

import javax.annotation.Nullable;

/**
 * 엄폐 중인 몬스터의 상태를 다른 AI와 다른 몬스터에게 알린다. Mixin으로 모든 Mob에 붙는다.
 * <p>
 * 바닐라 대상 지정은 대상이 3초 동안 보이지 않으면 대상을 버린다. 엄폐는 일부러 시야를 끊는 행동이므로,
 * 엄폐 행동이 붙잡고 있는 동안에는 보이는 것으로 친다.
 * 또 몬스터가 잡아 둔 엄폐 칸을 알려, 여러 마리가 한 칸에 몰려 서로를 밀어내지 않게 한다.
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
}
