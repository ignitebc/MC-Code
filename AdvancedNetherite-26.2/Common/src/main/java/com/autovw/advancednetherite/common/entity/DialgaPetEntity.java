package com.autovw.advancednetherite.common.entity;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
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
            this.teleport(new TeleportTransition(
                    serverPlayer.level(),
                    serverPlayer.position().add(1.0, 0.0, 1.0),
                    Vec3.ZERO,
                    serverPlayer.getYRot(),
                    0.0F,
                    TeleportTransition.DO_NOTHING));
            return;
        }

        updatePursuit();
        super.customServerAiStep(serverLevel);
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
