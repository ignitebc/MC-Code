package com.daqem.arc.api.entity;

/**
 * 찌 하나가 물에 떠 있는 동안 자동 낚시 판정에 필요한 근거를 모은다.
 * FishingHook 에 mixin 으로 구현된다.
 */
public interface ArcFishingHook {

    /** 찌가 던져진 뒤 소유자가 블록을 우클릭한 횟수. */
    int arc$getBlockInteractionCount();

    /** 찌가 던져진 뒤 시선(yaw)이 움직인 최대 각도. */
    float arc$getMaxYawDelta();

    /** 찌가 던져진 뒤 시선(pitch)이 움직인 최대 각도. */
    float arc$getMaxPitchDelta();

    /** 찌가 던져진 뒤 소유자가 움직인 최대 거리(블록). */
    double arc$getMaxPositionDelta();

    /** 찌를 추적한 틱 수. 표본이 너무 적으면 판정하지 않는다. */
    int arc$getTrackedTicks();
}
