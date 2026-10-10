package com.tacz.guns.util;

import com.tacz.guns.config.util.HeadShotAABBConfigRead;
import com.tacz.guns.entity.EntityKineticBullet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class EntityUtil {
    private static final Predicate<Entity> PROJECTILE_TARGETS = input -> input != null && input.isPickable() && !input.isSpectator();

    @Nullable
    public static EntityKineticBullet.EntityResult findEntityOnPath(Projectile bulletEntity, Vec3 startVec, Vec3 endVec) {

        Vec3 hitVec = null;
        Entity hitEntity = null;
        boolean headshot = false;
        // 탄환 tick 경로 위의 모든 엔티티를 가져온다
        List<Entity> entities = bulletEntity.level().getEntities(bulletEntity, bulletEntity.getBoundingBox().expandTowards(bulletEntity.getDeltaMovement()).inflate(1.0), PROJECTILE_TARGETS);
        double closestDistance = Double.MAX_VALUE;
        Entity owner = bulletEntity.getOwner();
        for (Entity entity : entities) {
            // 자신에게 피해를 주지 못하게 한다(필요하면 자신에게 주는 피해를 켜는 Config를 추가할 수 있다)
            if (!entity.equals(owner)) {
                // 사격은 자신의 탈것과 그 탈것의 다른 탑승자를 무시한다
                if (owner != null && entity.isPassengerOfSameVehicle(owner)) {
                    continue;
                }
                if (isShootersPet(entity, owner)) {
                    continue;
                }
                EntityKineticBullet.EntityResult result = getHitResult(bulletEntity, entity, startVec, endVec);
                if (result == null) {
                    continue;
                }
                Vec3 hitPos = result.getHitPos();
                double distanceToHit = startVec.distanceTo(hitPos);
                if (entity.isAlive()) {
                    if (distanceToHit < closestDistance) {
                        hitVec = hitPos;
                        hitEntity = entity;
                        closestDistance = distanceToHit;
                        headshot = result.isHeadshot();
                    }
                }
            }
        }
        return hitEntity != null ? new EntityKineticBullet.EntityResult(hitEntity, hitVec, headshot) : null;
    }

    @NotNull
    public static List<EntityKineticBullet.EntityResult> findEntitiesOnPath(Projectile bulletEntity, Vec3 startVec, Vec3 endVec) {
        List<EntityKineticBullet.EntityResult> hitEntities = new ArrayList<>();
        List<Entity> entities = bulletEntity.level().getEntities(bulletEntity, bulletEntity.getBoundingBox().expandTowards(bulletEntity.getDeltaMovement()).inflate(1.0), PROJECTILE_TARGETS);
        Entity owner = bulletEntity.getOwner();
        for (Entity entity : entities) {
            if (!entity.equals(owner)) {
                if (owner != null && entity.equals(owner.getVehicle())) {
                    continue;
                }
                if (isShootersPet(entity, owner)) {
                    continue;
                }
                EntityKineticBullet.EntityResult result = getHitResult(bulletEntity, entity, startVec, endVec);
                if (result == null) {
                    continue;
                }
                if (entity.isAlive()) {
                    hitEntities.add(result);
                }
            }
        }
        return hitEntities;
    }

    /**
     * 쏜 사람이 기르는 펫인지. 주인의 탄·폭발·총기 근접 공격은 펫을 그대로 통과한다.
     * <p>
     * 펫은 주인의 탄에 피해를 받지 않지만, 통과시키지 않으면 탄이 펫에서 멈춰 펫이 붙어 싸우는 몹을 맞힐 수 없다.
     */
    public static boolean isShootersPet(Entity entity, @Nullable Entity shooter) {
        return shooter != null && entity instanceof OwnableEntity pet && pet.getOwner() == shooter;
    }

    @Nullable
    protected static EntityKineticBullet.EntityResult getHitResult(Projectile bulletEntity, Entity entity, Vec3 startVec, Vec3 endVec) {
        AABB boundingBox = HitboxHelper.getFixedBoundingBox(entity, bulletEntity.getOwner());
        // 광선과 엔티티 boundingBox의 교점을 계산한다
        Vec3 hitPos = boundingBox.clip(startVec, endVec).orElse(null);
        // 헤드샷 판정
        if (hitPos == null) {
            return null;
        }
        Vec3 hitBoxPos = hitPos.subtract(entity.position());
        Identifier entityId = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());
        // 설정이 있으면 설정을 쓴다
        if (entityId != null) {
            AABB aabb = HeadShotAABBConfigRead.getAABB(entityId);
            if (aabb != null) {
                return new EntityKineticBullet.EntityResult(entity, hitPos, aabb.contains(hitBoxPos));
            }
        }
        // 설정이 없으면 기본값을 하나 준다
        boolean headshot = false;
        float eyeHeight = entity.getEyeHeight();
        if ((eyeHeight - 0.25) < hitBoxPos.y && hitBoxPos.y < (eyeHeight + 0.25)) {
            headshot = true;
        }
        return new EntityKineticBullet.EntityResult(entity, hitPos, headshot);
    }
}
