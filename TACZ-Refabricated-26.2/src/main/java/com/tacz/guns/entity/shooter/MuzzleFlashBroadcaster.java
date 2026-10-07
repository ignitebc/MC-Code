package com.tacz.guns.entity.shooter;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

/**
 * 총을 쏜 자리에 짧은 섬광을 띄워, 밤에도 어디서 쏘는지 보이게 한다.
 * <p>
 * TACZ의 총구 화염은 자기 캐릭터에만 그려져 몬스터나 다른 플레이어가 쏠 때는 보이지 않는다.
 * 2~4틱만 남는 밝은 불꽃 입자를 총구 근처에 띄운다. 입자는 바닐라 기본값으로 32칸 안에서만 보이므로
 * 거리 제한을 넘기는 옵션으로 예광탄 전송 거리(128칸)까지 보낸다. 쏜 본인에게는 보내지 않는다.
 * 1인칭 화면에는 이미 총구 화염이 있어서 겹쳐 보이기 때문이다.
 */
public final class MuzzleFlashBroadcaster {
    /** 섬광을 받아 보는 플레이어와 쏜 자리 사이의 최대 거리(칸). 예광탄 전송 거리와 같다. */
    private static final double VIEW_DISTANCE = 128.0;
    /** 눈 위치에서 총구까지 앞으로 나가는 거리와 아래로 내려가는 거리(칸) */
    private static final double MUZZLE_FORWARD = 0.8;
    private static final double MUZZLE_DROP = 0.15;
    private static final int SPARK_COUNT = 4;
    private static final double SPARK_SPREAD = 0.04;
    private static final double SPARK_SPEED = 0.02;

    private MuzzleFlashBroadcaster() {
    }

    public static void broadcast(LivingEntity shooter) {
        if (!(shooter.level() instanceof ServerLevel level)) {
            return;
        }
        Vec3 muzzle = shooter.getEyePosition()
                .add(shooter.getViewVector(1.0f).scale(MUZZLE_FORWARD))
                .subtract(0.0, MUZZLE_DROP, 0.0);
        double viewDistanceSqr = VIEW_DISTANCE * VIEW_DISTANCE;
        for (ServerPlayer viewer : level.players()) {
            boolean isShooter = viewer == shooter;
            if (isShooter || viewer.distanceToSqr(muzzle) > viewDistanceSqr) {
                continue;
            }
            level.sendParticles(viewer, ParticleTypes.ELECTRIC_SPARK, true, true,
                    muzzle.x, muzzle.y, muzzle.z, SPARK_COUNT, SPARK_SPREAD, SPARK_SPREAD, SPARK_SPREAD, SPARK_SPEED);
        }
    }
}
