package com.mcserver.serverutilities.boss;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.projectile.hurtingprojectile.DragonFireball;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/** 원래 발사 위치와 속도를 유지하면서 조준축 주위로 다섯 발을 분산한다. */
public final class DragonFireballVolley {
    private static final double HORIZONTAL_SPREAD = Math.tan(Math.toRadians(12.0D));
    private static final double VERTICAL_SPREAD = Math.tan(Math.toRadians(8.0D));

    private DragonFireballVolley() { }

    public static boolean spawn(ServerLevel level, Entity entity) {
        if (!(entity instanceof DragonFireball original) || !(original.getOwner() instanceof EnderDragon dragon)) {
            return level.addFreshEntity(entity);
        }
        Vec3 velocity = original.getDeltaMovement();
        if (velocity.lengthSqr() < 1.0E-12D) return level.addFreshEntity(original);
        List<Vec3> directions = directions(velocity);
        boolean added = level.addFreshEntity(original);
        // 다른 모드가 원래 발사를 취소하면 추가 탄환도 만들지 않는다.
        if (!added) return false;
        for (int index = 1; index < directions.size(); index++) {
            DragonFireball extra = new DragonFireball(level, dragon, directions.get(index));
            extra.snapTo(original.position(), original.getYRot(), original.getXRot());
            extra.setDeltaMovement(directions.get(index).scale(velocity.length()));
            extra.accelerationPower = original.accelerationPower;
            level.addFreshEntity(extra);
        }
        return true;
    }

    static List<Vec3> directions(Vec3 velocity) {
        Vec3 forward = velocity.normalize();
        Vec3 right = forward.cross(new Vec3(0, 1, 0));
        // 거의 수직인 조준에서도 유효한 좌우·상하 축을 만든다.
        if (right.lengthSqr() < 1.0E-6D) right = forward.cross(new Vec3(0, 0, 1));
        right = right.normalize();
        Vec3 up = right.cross(forward).normalize();
        return List.of(forward,
                forward.add(right.scale(HORIZONTAL_SPREAD)).normalize(),
                forward.subtract(right.scale(HORIZONTAL_SPREAD)).normalize(),
                forward.add(up.scale(VERTICAL_SPREAD)).normalize(),
                forward.subtract(up.scale(VERTICAL_SPREAD)).normalize());
    }
}
