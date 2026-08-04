package com.autovw.advancednetherite.common.entity;

import com.autovw.advancednetherite.common.pet.PetManager;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
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
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

public class DialgaPetEntity extends TamableAnimal
{
    /** 주인과 이 거리(10칸)보다 멀어지면 곁으로 순간이동한다. */
    private static final double TELEPORT_DISTANCE_SQR = 10.0 * 10.0;
    /** 추격 대상이 주인에게서 이 거리(20칸)보다 멀어지면 추격을 포기하고 주인에게 돌아간다. */
    private static final double PURSUIT_RESET_DISTANCE_SQR = 20.0 * 20.0;

    /**
     * 주인을 때린 대상. 대상이 죽거나 사라질 때까지 추격을 유지하기 위해 별도로 기억한다.
     * 리스폰한 플레이어는 새 엔티티라서 이전 참조가 죽은 상태로 남으므로 자동으로 초기화된다.
     */
    private LivingEntity pursuitTarget;

    private static final String TAG_RECORD_ID = "PetRecordId";
    /** 주인 조회가 잠깐 비어도(사망→리스폰 전환 등) 이 시간(5초) 안에 돌아오면 소멸하지 않는다. */
    private static final int OWNER_MISSING_GRACE_TICKS = 100;

    /**
     * 펫 저장소의 기록 ID. 차원 이동 시 엔티티가 새 개체로 복사되므로 NBT로도 승계한다.
     * 기록 없는 펫은 존재 자격이 없어 소멸한다.
     */
    private UUID recordId;

    private int ownerMissingTicks;

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

        this.targetSelector.addGoal(1, new RecentOwnerHurtByTargetGoal(this));
    }

    @Override
    protected void customServerAiStep(ServerLevel serverLevel)
    {
        LivingEntity owner = this.getOwner();

        // 주인이 접속을 종료하면 펫도 함께 퇴장한다.
        // 재접속하면 저장소 기록을 바탕으로 모든 행동이 초기화된 새 펫이 소환된다.
        if (owner instanceof ServerPlayer disconnectCheck && disconnectCheck.hasDisconnected())
        {
            this.discard();
            return;
        }

        // 주인 사망→리스폰 전환 순간에는 주인 조회가 잠깐 비므로 바로 소멸하지 않고 유예한다.
        // 실제 접속 종료는 위의 즉시 검사와 접속 종료 이벤트가 처리한다.
        if (owner == null)
        {
            this.ownerMissingTicks++;
            if (this.ownerMissingTicks > OWNER_MISSING_GRACE_TICKS)
            {
                this.discard();
            }
            return;
        }
        this.ownerMissingTicks = 0;

        // 저장소 기록과 대조해 존재 자격을 확인한다.
        // 기록이 없는 펫과 OFF 상태·중복 개체는 여기서 소멸한다.
        if (owner instanceof ServerPlayer ownerPlayer && !PetManager.validatePet(ownerPlayer, this))
        {
            this.discard();
            return;
        }

        if (owner instanceof ServerPlayer serverPlayer && serverPlayer.level() != serverLevel)
        {
            this.pursuitTarget = null;
            this.setTarget(null);
            teleportToOwnerLevel(serverPlayer);
            return;
        }

        updatePursuit(owner);

        // 추격 중이 아닐 때 주인과 10칸 이상 벌어지면 곁으로 순간이동한다.
        boolean isIdle = this.getTarget() == null;
        if (isIdle && this.distanceToSqr(owner) > TELEPORT_DISTANCE_SQR)
        {
            teleportBesideOwner(owner);
        }

        super.customServerAiStep(serverLevel);
    }

    public UUID getRecordId()
    {
        return this.recordId;
    }

    public void setRecordId(UUID recordId)
    {
        this.recordId = recordId;
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
     * 단, 대상이 주인에게서 20칸보다 멀어지면 추격을 포기하고 주인 곁으로 돌아간다.
     */
    private void updatePursuit(LivingEntity owner)
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
                || this.pursuitTarget.level() != this.level()
                || this.pursuitTarget instanceof DialgaPetEntity
                || (this.pursuitTarget instanceof ServerPlayer targetPlayer && targetPlayer.hasDisconnected());
        boolean targetTooFarFromOwner = !targetGone
                && this.pursuitTarget.distanceToSqr(owner) > PURSUIT_RESET_DISTANCE_SQR;
        if (targetGone || targetTooFarFromOwner)
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

    /**
     * 펫은 어떤 공격도 받지 않으므로 펫끼리의 전투는 영원히 끝나지 않는다.
     * 주인이 펫에게 맞은 경우, 그 펫 대신 펫의 주인을 대상으로 잡을 수 있을 때만 복수를 허용한다.
     */
    @Override
    public boolean wantsToAttack(LivingEntity target, LivingEntity targetOwner)
    {
        if (target instanceof DialgaPetEntity attackingPet)
        {
            return resolveAttackingPetOwner(attackingPet) != null;
        }
        return super.wantsToAttack(target, targetOwner);
    }

    /**
     * 공격 대상이 펫이면 그 펫의 주인으로 치환한다.
     * 펫은 무적이라 직접 때릴 가치가 없고, 주인을 물어야 전투가 성립한다.
     */
    @Override
    public void setTarget(LivingEntity target)
    {
        if (target instanceof DialgaPetEntity attackingPet)
        {
            target = resolveAttackingPetOwner(attackingPet);
        }
        super.setTarget(target);
    }

    private LivingEntity resolveAttackingPetOwner(DialgaPetEntity attackingPet)
    {
        LivingEntity attackerOwner = attackingPet.getOwner();
        boolean isValidTarget = attackerOwner != null
                && attackerOwner != this.getOwner()
                && attackerOwner.level() == this.level();
        if (isValidTarget)
        {
            return attackerOwner;
        }
        return null;
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput valueOutput)
    {
        super.addAdditionalSaveData(valueOutput);
        if (this.recordId != null)
        {
            valueOutput.putString(TAG_RECORD_ID, this.recordId.toString());
        }
    }

    @Override
    protected void readAdditionalSaveData(ValueInput valueInput)
    {
        super.readAdditionalSaveData(valueInput);
        String savedRecordId = valueInput.getStringOr(TAG_RECORD_ID, "");
        if (!savedRecordId.isEmpty())
        {
            try
            {
                this.recordId = UUID.fromString(savedRecordId);
            }
            catch (IllegalArgumentException exception)
            {
                this.recordId = null;
            }
        }
    }

    /**
     * 여러 마리가 주인 주변에 몰려도 주인을 밀지 않도록 밀림·밀기 충돌을 없앤다.
     * 블록 통과(유체화)는 아니며, 밀치기만 서로 주고받지 않는다.
     */
    @Override
    public boolean isPushable()
    {
        return false;
    }

    @Override
    protected void doPush(Entity entity)
    {
        if (entity == this.getOwner())
        {
            return;
        }
        super.doPush(entity);
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

    /**
     * 주인이 최근에 맞았을 때만 복수에 나서는 AI.
     * <p>
     * 바닐라 {@link OwnerHurtByTargetGoal}은 피격 시각의 최신 여부만 비교해서,
     * 재접속으로 AI나 플레이어 인스턴스가 새로 만들어지면 낡은 전투 기억을
     * 새 피격으로 착각하고 이미 끝난 싸움의 상대를 다시 공격한다.
     * 실제 피격 후 짧은 시간 안에만 발동하도록 제한해 이를 막는다.
     */
    private static class RecentOwnerHurtByTargetGoal extends OwnerHurtByTargetGoal
    {
        /** 주인이 맞은 지 이 시간(5초)이 지나면 복수하지 않는다. */
        private static final int MAX_HURT_AGE_TICKS = 100;

        private final TamableAnimal pet;

        RecentOwnerHurtByTargetGoal(TamableAnimal pet)
        {
            super(pet);
            this.pet = pet;
        }

        @Override
        public boolean canUse()
        {
            LivingEntity owner = this.pet.getOwner();
            if (owner == null)
            {
                return false;
            }

            int hurtAge = owner.tickCount - owner.getLastHurtByMobTimestamp();
            boolean isRecentHurt = hurtAge >= 0 && hurtAge <= MAX_HURT_AGE_TICKS;
            return isRecentHurt && super.canUse();
        }
    }
}
