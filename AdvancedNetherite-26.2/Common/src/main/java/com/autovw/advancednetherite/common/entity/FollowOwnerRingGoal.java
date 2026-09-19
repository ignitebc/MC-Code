package com.autovw.advancednetherite.common.entity;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

/**
 * 주인 둘레에 배정된 제 자리로 따라간다.
 *
 * <p>바닐라 FollowOwnerGoal은 주인 자체를 향해 걷기 때문에 펫이 여럿이면 한 점에 겹쳐 쌓인다.
 * 여기서는 펫마다 다른 각도의 지점을 목표로 삼아 주인을 둘러싸듯 흩어지게 한다.
 */
public class FollowOwnerRingGoal extends Goal
{
    /** 목표 지점을 다시 계산하는 주기. 주인이 움직이므로 주기적으로 갱신해야 한다. */
    private static final int REPATH_INTERVAL_TICKS = 10;

    private final DialgaPetEntity pet;
    private final double speedModifier;
    /** 제 자리에서 이만큼 벌어지면 따라가기 시작한다. */
    private final double startDistanceSqr;
    /** 제 자리에 이만큼 가까워지면 멈춘다. */
    private final double arriveDistanceSqr;

    private LivingEntity owner;
    private int repathCooldown;

    public FollowOwnerRingGoal(DialgaPetEntity pet, double speedModifier, float startDistance, float arriveDistance)
    {
        this.pet = pet;
        this.speedModifier = speedModifier;
        this.startDistanceSqr = startDistance * startDistance;
        this.arriveDistanceSqr = arriveDistance * arriveDistance;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse()
    {
        LivingEntity candidate = this.pet.getOwner();
        if (candidate == null || candidate.isSpectator() || this.pet.isOrderedToSit())
        {
            return false;
        }
        if (this.pet.distanceToSqr(this.pet.ringPosition(candidate)) < this.startDistanceSqr)
        {
            return false;
        }
        this.owner = candidate;
        return true;
    }

    @Override
    public boolean canContinueToUse()
    {
        if (this.owner == null || this.pet.isOrderedToSit())
        {
            return false;
        }
        return this.pet.distanceToSqr(this.pet.ringPosition(this.owner)) > this.arriveDistanceSqr;
    }

    @Override
    public void start()
    {
        this.repathCooldown = 0;
    }

    @Override
    public void stop()
    {
        this.owner = null;
        this.pet.getNavigation().stop();
    }

    @Override
    public void tick()
    {
        this.pet.getLookControl().setLookAt(this.owner, 10.0F, this.pet.getMaxHeadXRot());
        if (--this.repathCooldown > 0)
        {
            return;
        }

        this.repathCooldown = REPATH_INTERVAL_TICKS;
        Vec3 spot = this.pet.ringPosition(this.owner);
        this.pet.getNavigation().moveTo(spot.x, spot.y, spot.z, this.speedModifier);
    }
}
