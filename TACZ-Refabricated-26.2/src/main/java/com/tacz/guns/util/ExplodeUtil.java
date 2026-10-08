package com.tacz.guns.util;

import com.tacz.guns.config.common.AmmoConfig;
import com.tacz.guns.util.block.BlockRayTrace;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerExplosion;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;

import javax.annotation.Nullable;

/**
 * 폭발탄의 폭발.
 * <p>
 * 바닐라 폭발은 소리·입자·블록 파괴에만 쓰고, 엔티티 피해와 밀어내기는 원본 TACZ 공식으로 한 번만 계산한다.
 * 바닐라 폭발 피해(중심 14 × 반경 + 1)까지 주면 총기 데이터의 폭발 피해에 그보다 큰 피해가 한 번 더 들어간다.
 * <p>
 * 피해는 폭발 중심에서 반경 끝까지 직선으로 줄고, 벽에 완전히 가린 대상은 받지 않는다.
 * 쏜 사람 자신, 다른 플레이어, 몬스터 모두 레벨 배율까지 곱한 같은 피해를 받는다.
 */
public final class ExplodeUtil {
    /** 바닐라 폭발이 엔티티를 다치게 하거나 밀어내지 않게 한다. 블록 파괴 판정은 기본값을 그대로 쓴다. */
    private static final ExplosionDamageCalculator EFFECTS_ONLY = new ExplosionDamageCalculator() {
        @Override
        public boolean shouldDamageEntity(Explosion explosion, Entity entity) {
            return false;
        }

        @Override
        public float getKnockbackMultiplier(Entity entity) {
            return 0.0F;
        }
    };
    /** 밀어내기 세기 = 폭발 피해 × 반경 × 2 / 500. 원본 TACZ 와 같다. */
    private static final double KNOCKBACK_DIVISOR = 500.0D;
    /** 마법부여 보호 수치를 밀어내기 감소율로 바꾸는 상한과 분모. 원본 TACZ 와 같다. */
    private static final double MAX_PROTECTION = 20.0D;
    private static final double PROTECTION_DIVISOR = 25.0D;

    private ExplodeUtil() {
    }

    /**
     * @param baseDamage       총기 데이터의 폭발 피해. 밀어내기 세기에도 쓴다.
     * @param damageMultiplier 총기 레벨의 폭발 피해 배율. 피해에만 곱하고 반경과 밀어내기는 바꾸지 않는다.
     * @param shotContext      폭발을 일으킨 탄의 사격 정보. 폭발로 몬스터를 다치게 해도 총기 경험치를 준다.
     */
    public static void createExplosion(@Nullable Entity owner, Entity exploder, float baseDamage, float damageMultiplier,
                                       float radius, boolean knockback, boolean destroy, Vec3 hitPos,
                                       @Nullable GunShotContext shotContext) {
        if (!(exploder.level() instanceof ServerLevel level)) {
            return;
        }
        DamageSource source = exploder.damageSources().explosion(exploder, owner);
        Level.ExplosionInteraction interaction = Level.ExplosionInteraction.NONE;
        Explosion.BlockInteraction blockInteraction = Explosion.BlockInteraction.KEEP;
        if (destroy) {
            interaction = Level.ExplosionInteraction.TNT;
            blockInteraction = Explosion.BlockInteraction.DESTROY_WITH_DECAY;
        }
        level.explode(exploder, source, EFFECTS_ONLY, hitPos.x(), hitPos.y(), hitPos.z(), radius, false, interaction);
        if (baseDamage <= 0 || radius <= 0) {
            return;
        }
        // 그림·액자·떨어진 아이템처럼 폭발 종류에 따라 영향을 받지 않는 엔티티를 바닐라와 같은 기준으로 거르는 데만 쓴다.
        ServerExplosion explosion = new ServerExplosion(level, exploder, source, EFFECTS_ONLY, hitPos, radius, false, blockInteraction);
        Blast blast = new Blast(level, explosion, owner, source, hitPos, baseDamage, damageMultiplier, radius, knockback, shotContext);
        hurtEntities(blast, exploder);
    }

    private static void hurtEntities(Blast blast, Entity exploder) {
        Vec3 center = blast.center();
        double reach = blast.radius() + 1.0D;
        AABB area = new AABB(center.x - reach, center.y - reach, center.z - reach,
                center.x + reach, center.y + reach, center.z + reach);
        for (Entity entity : blast.level().getEntities(exploder, area)) {
            if (entity.ignoreExplosion(blast.explosion())) {
                continue;
            }
            AABB hitbox = null;
            if (entity instanceof LivingEntity) {
                hitbox = HitboxHelper.getFixedBoundingBox(entity, blast.owner());
            }
            double strength = exposedDistance(blast, entity, hitbox) / blast.radius();
            if (strength >= 1.0D) {
                continue;
            }
            double impact = 1.0D - strength;
            hurt(blast, entity, impact);
            push(blast, entity, hitbox, impact);
        }
    }

    /**
     * 폭발 중심에서 대상까지의 거리. 생물은 판정 상자 15곳 중 벽에 가리지 않은 가장 가까운 곳까지 재고,
     * 모두 가려지면 닿지 않는 것으로 본다. 생물이 아니면 위치까지의 직선 거리다.
     */
    private static double exposedDistance(Blast blast, Entity entity, @Nullable AABB hitbox) {
        Vec3 center = blast.center();
        if (hitbox == null) {
            return Math.sqrt(entity.distanceToSqr(center));
        }
        double nearest = Double.MAX_VALUE;
        for (Vec3 point : samplePoints(hitbox)) {
            ClipContext clip = new ClipContext(center, point, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, CollisionContext.empty());
            BlockHitResult result = BlockRayTrace.rayTraceBlocks(blast.level(), clip);
            boolean blocked = result.getType() == HitResult.Type.BLOCK;
            if (!blocked) {
                nearest = Math.min(nearest, center.distanceTo(point));
            }
        }
        return nearest;
    }

    /** 판정 상자의 꼭짓점 8곳, 면 중심 6곳, 정중앙 */
    private static Vec3[] samplePoints(AABB box) {
        Vec3 middle = box.getCenter();
        return new Vec3[]{
                new Vec3(box.minX, box.minY, box.minZ),
                new Vec3(box.minX, box.minY, box.maxZ),
                new Vec3(box.minX, box.maxY, box.minZ),
                new Vec3(box.maxX, box.minY, box.minZ),
                new Vec3(box.minX, box.maxY, box.maxZ),
                new Vec3(box.maxX, box.minY, box.maxZ),
                new Vec3(box.maxX, box.maxY, box.minZ),
                new Vec3(box.maxX, box.maxY, box.maxZ),
                new Vec3(box.minX, middle.y, middle.z),
                new Vec3(box.maxX, middle.y, middle.z),
                new Vec3(middle.x, box.minY, middle.z),
                new Vec3(middle.x, box.maxY, middle.z),
                new Vec3(middle.x, middle.y, box.minZ),
                new Vec3(middle.x, middle.y, box.maxZ),
                middle
        };
    }

    private static void hurt(Blast blast, Entity entity, double impact) {
        float damage = (float) (blast.baseDamage() * blast.damageMultiplier() * impact);
        float healthBefore = 0.0F;
        float absorptionBefore = 0.0F;
        if (entity instanceof LivingEntity living) {
            healthBefore = living.getHealth();
            absorptionBefore = living.getAbsorptionAmount();
        }
        // 직격 피해를 받은 바로 그 대상에게도 폭발 피해가 깎이지 않고 들어가도록 무적 시간을 지운다.
        entity.invulnerableTime = 0;
        entity.hurtServer(blast.level(), blast.source(), damage);
        if (entity instanceof LivingEntity living) {
            awardExperience(blast, living, healthBefore, absorptionBefore);
        }
    }

    /** 폭발로 체력이나 흡수 체력이 줄었으면 직격과 같은 기준으로 총기 경험치를 준다. 사격 1회당 한 번뿐이다. */
    private static void awardExperience(Blast blast, LivingEntity target, float healthBefore, float absorptionBefore) {
        GunShotContext shotContext = blast.shotContext();
        if (shotContext == null || healthBefore <= 0) {
            return;
        }
        boolean tookDamage = target.getHealth() < healthBefore || target.getAbsorptionAmount() < absorptionBefore;
        if (tookDamage) {
            shotContext.awardExperience(target);
        }
    }

    private static void push(Blast blast, Entity entity, @Nullable AABB hitbox, double impact) {
        if (!blast.knockback() || !AmmoConfig.EXPLOSIVE_AMMO_KNOCK_BACK.get()) {
            return;
        }
        if (entity instanceof Player player) {
            boolean unmovable = player.isSpectator() || (player.isCreative() && player.getAbilities().flying);
            if (unmovable) {
                return;
            }
        }
        double strength = impact;
        if (entity instanceof LivingEntity living) {
            float protection = EnchantmentHelper.getDamageProtection(blast.level(), living, blast.source());
            strength *= 1.0D - Math.min(protection, MAX_PROTECTION) / PROTECTION_DIVISOR;
        }
        double multiplier = blast.baseDamage() * blast.radius() * 2.0D / KNOCKBACK_DIVISOR;
        Vec3 push = pushDirection(blast.center(), entity, hitbox).scale(strength * multiplier);
        entity.setDeltaMovement(entity.getDeltaMovement().add(push));
        // 플레이어 이동은 클라이언트가 정하므로 바뀐 속도를 보내야 실제로 밀려난다.
        entity.hurtMarked = true;
    }

    private static Vec3 pushDirection(Vec3 center, Entity entity, @Nullable AABB hitbox) {
        Vec3 target;
        if (hitbox != null) {
            target = hitbox.getCenter();
        } else if (entity instanceof PrimedTnt) {
            target = entity.position();
        } else {
            target = new Vec3(entity.getX(), entity.getEyeY(), entity.getZ());
        }
        Vec3 direction = target.subtract(center);
        if (direction.lengthSqr() == 0.0D) {
            return Vec3.ZERO;
        }
        return direction.normalize();
    }

    private record Blast(ServerLevel level, ServerExplosion explosion, @Nullable Entity owner, DamageSource source,
                         Vec3 center, float baseDamage, float damageMultiplier, float radius, boolean knockback,
                         @Nullable GunShotContext shotContext) {
    }
}
