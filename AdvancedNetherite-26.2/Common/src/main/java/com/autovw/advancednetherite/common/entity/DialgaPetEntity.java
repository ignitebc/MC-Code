package com.autovw.advancednetherite.common.entity;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.FollowOwnerGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtByTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.phys.Vec3;

public class DialgaPetEntity extends TamableAnimal
{
    /**
     * 주인을 때린 대상. 대상이 죽거나 사라질 때까지 추격을 유지하기 위해 별도로 기억한다.
     * 리스폰한 플레이어는 새 엔티티라서 이전 참조가 죽은 상태로 남으므로 자동으로 초기화된다.
     */
    /** 주인과 이 거리(5칸)보다 멀어지면 곁으로 순간이동한다. */
    private static final double TELEPORT_DISTANCE_SQR = 5.0 * 5.0;

    private LivingEntity pursuitTarget;

    public DialgaPetEntity(EntityType<? extends DialgaPetEntity> entityType, Level level)
    {
        super(entityType, level);
    }

    public static AttributeSupplier.Builder createAttributes()
    {
        return TamableAnimal.createAnimalAttributes()
                .add(Attributes.MAX_HEALTH, 20.0)
                .add(Attributes.MOVEMENT_SPEED, 0.35)
                .add(Attributes.FOLLOW_RANGE, 48.0)
                .add(Attributes.ATTACK_DAMAGE, 1.0);
    }

    @Override
    protected void registerGoals()
    {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.4, true));
        this.goalSelector.addGoal(2, new FollowOwnerGoal(this, 1.25, 3.0F, 1.5F));
        this.goalSelector.addGoal(3, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(4, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new OwnerHurtByTargetGoal(this));
    }

    @Override
    protected void customServerAiStep(ServerLevel serverLevel)
    {
        LivingEntity owner = this.getOwner();
        if (owner instanceof ServerPlayer serverPlayer && serverPlayer.level() != serverLevel)
        {
            this.pursuitTarget = null;
            this.setTarget(null);
            teleportToOwnerLevel(serverPlayer);
            return;
        }

        updatePursuit();

        // 추격 중이 아닐 때 주인과 5칸 이상 벌어지면 곁으로 순간이동한다.
        boolean isIdle = this.getTarget() == null;
        if (isIdle && owner != null && this.distanceToSqr(owner) > TELEPORT_DISTANCE_SQR)
        {
            teleportBesideOwner(owner);
        }

        super.customServerAiStep(serverLevel);
    }

    private void teleportToOwnerLevel(ServerPlayer serverPlayer)
    {
        this.teleport(new TeleportTransition(
                serverPlayer.level(),
                serverPlayer.position().add(1.0, 0.0, 1.0),
                Vec3.ZERO,
                serverPlayer.getYRot(),
                0.0F,
                TeleportTransition.DO_NOTHING));
    }

    private void teleportBesideOwner(LivingEntity owner)
    {
        this.teleportTo(owner.getX(), owner.getY(), owner.getZ());
        this.setDeltaMovement(Vec3.ZERO);
        this.resetFallDistance();
    }

    /**
     * 주인을 때린 대상을 죽을 때까지 놓지 않는다.
     * 일반 타겟 AI는 거리가 벌어지면 추격을 포기하므로, 대상이 살아 있는 동안 타겟을 다시 지정한다.
     */
    private void updatePursuit()
    {
        LivingEntity currentTarget = this.getTarget();
        if (currentTarget != null && currentTarget != this.pursuitTarget)
        {
            this.pursuitTarget = currentTarget;
        }

        if (this.pursuitTarget == null)
        {
            return;
        }

        boolean targetGone = !this.pursuitTarget.isAlive()
                || this.pursuitTarget.isRemoved()
                || this.pursuitTarget.level() != this.level();
        if (targetGone)
        {
            this.pursuitTarget = null;
            this.setTarget(null);
            return;
        }

        if (this.getTarget() == null)
        {
            this.setTarget(this.pursuitTarget);
        }
    }

    @Override
    public boolean hurtServer(ServerLevel serverLevel, DamageSource damageSource, float amount)
    {
        // 공허에 떨어져도 죽지 않고 주인 곁으로 귀환한다.
        if (damageSource.is(DamageTypes.FELL_OUT_OF_WORLD))
        {
            LivingEntity owner = this.getOwner();
            if (owner instanceof ServerPlayer serverPlayer && serverPlayer.level() != serverLevel)
            {
                teleportToOwnerLevel(serverPlayer);
                return false;
            }
            if (owner != null)
            {
                teleportBesideOwner(owner);
                return false;
            }
            // 주인이 오프라인이면 공허에서 영원히 떨어지지 않도록 소멸을 허용한다.
            return super.hurtServer(serverLevel, damageSource, amount);
        }

        // 펫은 전투 대상이 아니므로 모든 피해를 무시한다.
        // /kill 명령(BYPASSES_INVULNERABILITY)만 예외로 두어 운영 중 정리가 가능하게 한다.
        if (damageSource.is(DamageTypeTags.BYPASSES_INVULNERABILITY))
        {
            return super.hurtServer(serverLevel, damageSource, amount);
        }
        return false;
    }

    @Override
    public boolean isFood(ItemStack itemStack)
    {
        return false;
    }

    @Override
    public DialgaPetEntity getBreedOffspring(ServerLevel serverLevel, AgeableMob partner)
    {
        return null;
    }
}
