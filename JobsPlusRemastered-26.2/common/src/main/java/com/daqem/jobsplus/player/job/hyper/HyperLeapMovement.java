package com.daqem.jobsplus.player.job.hyper;

import com.daqem.jobsplus.accessor.HyperPlayerAccess;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

/** 클라이언트와 서버가 같은 이동식을 사용하고 충돌·중력은 바닐라 이동에 맡긴다. */
public final class HyperLeapMovement
{
    private static final double LEAP_HEIGHT = 30.0D;
    private static final int ASCENT_TICKS = 25;
    private static final int MOTION_TICKS = 55;

    private HyperLeapMovement() {}

    public static void start(Player player, Vec3 direction, double distance)
    {
        HyperPlayerState state = ((HyperPlayerAccess) player).jobsplus$getHyperState();
        state.leapProtected = true;
        state.leftGround = false;
        state.leapOrigin = player.position();
        state.leapDirection = direction;
        state.leapDistance = distance;
        state.leapMotionTicks = MOTION_TICKS;
        state.leapElapsedTicks = 0;
        state.leapMotionStopped = false;
        state.leapSpeed = distance / state.leapMotionTicks;
        // 바닐라 중력 0.08·감쇠 0.98에서 25틱째 30블록 정점에 도달한다.
        // 거리에 관계없이 같은 높이로 띄우고, 평지 착지 직전까지 수평 이동을 끝낸다.
        double sum = (1.0D - Math.pow(0.98D, ASCENT_TICKS)) / 0.02D;
        double verticalSpeed = (LEAP_HEIGHT + 3.92D * (ASCENT_TICKS - sum)) / sum;
        player.setDeltaMovement(direction.scale(state.leapSpeed).add(0, verticalSpeed, 0));
        player.setOnGround(false);
        player.resetFallDistance();
    }

    public static Vec3 beforeTravel(Player player, Vec3 input)
    {
        HyperPlayerState state = ((HyperPlayerAccess) player).jobsplus$getHyperState();
        if (!state.leapProtected || state.leapMotionTicks == 0) return input;
        if (player.onGround() && state.leftGround || player.isInWater() || player.isInLava()
                || player.isPassenger() || player.isFallFlying())
        {
            state.leapMotionStopped = true;
        }
        double progress = player.position().subtract(state.leapOrigin).dot(state.leapDirection);
        double remaining = Math.max(0, state.leapDistance - progress);
        if (state.leapElapsedTicks >= state.leapMotionTicks || remaining < 0.01D
                || player.horizontalCollision && state.leapElapsedTicks > 0)
        {
            state.leapMotionStopped = true;
        }
        Vec3 horizontal = state.leapDirection.scale(Math.min(state.leapSpeed, remaining));
        BlockPos next = BlockPos.containing(player.position().add(horizontal.scale(2.0D)));
        if (!player.level().hasChunkAt(next) || !player.level().getWorldBorder().isWithinBounds(next))
        {
            state.leapMotionStopped = true;
        }
        if (state.leapMotionStopped) horizontal = Vec3.ZERO;
        player.setDeltaMovement(horizontal.x, player.getDeltaMovement().y, horizontal.z);
        player.resetFallDistance();
        state.leapElapsedTicks++;
        if (player.level().isClientSide() && !player.onGround()) state.leftGround = true;
        return Vec3.ZERO;
    }
}
