package com.daqem.jobsplus.player.job.hyper;

import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

/** 강화 데이터와 별도로 보관하는 사용 상태. 재접속·토글로 재충전을 생략하지 않는다. */
public final class HyperPlayerState
{
    public long shieldReadyAt = -1;
    public long shieldUntil;
    public long leapReadyAt;
    public long landingUntil;
    public long leechReadyAt;
    public long lastLeechAttempt = -1;
    public long chargeStartedAt = -1;
    public int chargeSequence;
    public long lastChargeRequestAt = -1;
    public boolean leapProtected;
    public boolean leftGround;
    public long leapStartedAt;
    public long suppressMovementUntil;
    public Vec3 leapOrigin = Vec3.ZERO;
    public Vec3 leapDirection = Vec3.ZERO;
    public double leapDistance;
    public double leapSpeed;
    public int leapMotionTicks;
    public int leapElapsedTicks;
    public boolean leapMotionStopped;
    public boolean restoreFallProtection;
    public int lastSyncedHash;
    /** 농부 하이퍼 음식 흡수 중첩의 몫. 흡수 효과와 함께 재접속·차원 이동 뒤에도 유지한다. */
    public final List<FoodAbsorptionShare> foodAbsorptionShares = new ArrayList<>();

    public void save(ValueOutput output)
    {
        ValueOutput state = output.child("JobsPlusHyperRuntime");
        state.putLong("shield_ready_at", shieldReadyAt);
        state.putLong("leap_ready_at", leapReadyAt);
        state.putLong("leech_ready_at", leechReadyAt);
        state.putBoolean("leap_protected", leapProtected);
        state.store("food_absorption", FoodAbsorptionShare.LIST_CODEC, foodAbsorptionShares);
    }

    public void load(ValueInput input)
    {
        input.child("JobsPlusHyperRuntime").ifPresent(state -> {
            shieldReadyAt = state.getLongOr("shield_ready_at", -1L);
            leapReadyAt = state.getLongOr("leap_ready_at", 0L);
            leechReadyAt = state.getLongOr("leech_ready_at", 0L);
            restoreFallProtection = state.getBooleanOr("leap_protected", false);
            foodAbsorptionShares.clear();
            state.read("food_absorption", FoodAbsorptionShare.LIST_CODEC).ifPresent(foodAbsorptionShares::addAll);
        });
    }

    public void copyCooldowns(HyperPlayerState previous, boolean alive)
    {
        shieldReadyAt = previous.shieldReadyAt;
        leapReadyAt = previous.leapReadyAt;
        leechReadyAt = previous.leechReadyAt;
        restoreFallProtection = alive && previous.leapProtected;
        // 사망하면 효과가 모두 사라지므로 흡수 몫은 살아서 넘어갈 때(엔드 귀환)만 옮긴다.
        foodAbsorptionShares.clear();
        if (alive)
        {
            for (FoodAbsorptionShare share : previous.foodAbsorptionShares)
            {
                foodAbsorptionShares.add(share.copy());
            }
        }
    }

    public void clearTransient()
    {
        shieldUntil = 0;
        landingUntil = 0;
        chargeStartedAt = -1;
        leapProtected = false;
        leftGround = false;
        leapMotionStopped = true;
        leapMotionTicks = 0;
        restoreFallProtection = false;
    }
}
