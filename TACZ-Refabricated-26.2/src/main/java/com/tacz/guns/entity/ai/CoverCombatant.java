package com.tacz.guns.entity.ai;

import net.minecraft.world.entity.Entity;

/**
 * 엄폐 중인 몬스터가 공격 대상을 놓치지 않게 한다. Mixin으로 모든 Mob에 붙는다.
 * <p>
 * 바닐라 대상 지정은 대상이 3초 동안 보이지 않으면 대상을 버린다. 엄폐는 일부러 시야를 끊는 행동이므로,
 * 엄폐 행동이 붙잡고 있는 동안에는 보이는 것으로 친다.
 */
public interface CoverCombatant {
    /** 이 틱까지 대상을 붙잡는다. 엄폐 행동이 매 틱 갱신한다. */
    void tacz$holdTarget(Entity target, long untilTick);

    void tacz$releaseTarget();

    boolean tacz$isHoldingTarget(Entity target, long gameTime);
}
