package com.mcserver.serverutilities.tier;

/** 장비 때문에 잘린 체력을 보관하는 플레이어 */
public interface HealthReservePlayer {
    float serverutilities$getHealthReserve();
    void serverutilities$setHealthReserve(float reserve);
    boolean serverutilities$isRespawnHealPending();
    void serverutilities$setRespawnHealPending(boolean pending);
}
