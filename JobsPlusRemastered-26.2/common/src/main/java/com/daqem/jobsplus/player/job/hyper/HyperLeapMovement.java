package com.daqem.jobsplus.player.job.hyper;

import com.daqem.jobsplus.accessor.HyperPlayerAccess;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

/** 클라이언트와 서버가 같은 이동식을 사용하고 충돌·중력은 바닐라 이동에 맡긴다. */
public final class HyperLeapMovement
{
    private HyperLeapMovement() {}

    public static void start(Player player, Vec3 direction, double distance)
    {
        HyperPlayerState state = ((HyperPlayerAccess) player).jobsplus$getHyperState();
        state.leapProtected = true;
        state.leftGround = false;
        state.leapOrigin = player.position();
        state.leapDirection = direction;
        state.leapDistance = distance;
        state.leapMotionTicks = 20 + (int) Math.ceil(distance / 4.0D);
        state.leapElapsedTicks = 0;
        state.leapMotionStopped = false;
        state.leapSpeed = distance / state.leapMotionTicks;
        // 중력 0.08, 수직 감쇠 0.98인 기본 이동에서 평지에 돌아오는 시간을 맞춘다.
        double sum = (1.0D - Math.pow(0.98D, state.leapMotionTicks)) / 0.02D;
        double verticalSpeed = 3.92D * (state.leapMotionTicks - sum) / sum;
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
