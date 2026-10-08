package com.autovw.advancednetherite.common.entity;

import com.autovw.advancednetherite.common.pet.PetAttackMode;
import com.autovw.advancednetherite.common.pet.PetManager;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtByTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class DialgaPetEntity extends TamableAnimal
{
    /** 주인과 이 거리(10칸)보다 멀어지면 곁으로 순간이동한다. */
    private static final double TELEPORT_DISTANCE_SQR = 10.0 * 10.0;
    /**
     * 공격 대상이 없어진 뒤 이 시간(2초) 동안은 순간이동으로 돌아가지 않는다.
     * 그 사이 사냥 AI가 다음 사냥감을 골라, 한 마리 잡을 때마다 주인 곁으로 끌려오지 않게 한다.
     * 사냥 AI는 1초마다 찾으므로 한 번 놓쳐도 다시 찾을 여유가 있다.
     */
    private static final int RETURN_DELAY_AFTER_COMBAT_TICKS = 40;
    /**
     * 추격 대상이 주인에게서 이 거리(56칸)보다 멀어지면 추격을 포기하고 주인에게 돌아간다.
     * 사냥 범위(3청크, 48칸)보다 넉넉하게 두어, 범위 경계의 몹을 잡았다 놓았다 반복하지 않게 한다.
     */
    private static final double PURSUIT_RESET_DISTANCE = HuntNearbyMonstersGoal.HUNT_RADIUS + 8.0;
    private static final double PURSUIT_RESET_DISTANCE_SQR = PURSUIT_RESET_DISTANCE * PURSUIT_RESET_DISTANCE;
    /** 추격 대상을 이 시간(10초) 동안 한 대도 못 때리면 닿지 못하는 곳에 있는 것으로 보고 놓는다. */
    private static final int PURSUIT_STALL_TICKS = 200;
    /** 그렇게 놓은 대상은 이 시간(10초) 동안 사냥감으로 다시 고르지 않는다. */
    private static final int IGNORE_TARGET_TICKS = 200;

    /**
     * 추격 중인 대상. 대상이 죽거나 사라질 때까지 추격을 유지하기 위해 별도로 기억한다.
     * 리스폰한 플레이어는 새 엔티티라서 이전 참조가 죽은 상태로 남으므로 자동으로 초기화된다.
     */
    private LivingEntity pursuitTarget;
    /** 추격 대상을 마지막으로 때린 틱. 추격을 시작한 틱부터 잰다. */
    private int lastPursuitHitTick;
    /** 닿지 못해 놓은 대상과, 그 대상을 다시 고르지 않는 마지막 틱 */
    private int ignoredTargetId = -1;
    private int ignoreTargetUntilTick;
    /** 사냥 AI가 잡은 추격 대상의 ID. 주인이 일반공격으로 바꾸면 이 대상만 놓는다. 반격으로 잡은 대상이면 -1이다. */
    private int huntTargetId = -1;
    /** 마지막으로 공격 대상이 있던 틱. 전투가 끝난 뒤 순간이동을 미루는 데 쓴다. */
    private int lastCombatTick;

    private static final String TAG_RECORD_ID = "PetRecordId";
    /** 주인 조회가 잠깐 비어도(사망→리스폰 전환 등) 이 시간(5초) 안에 돌아오면 소멸하지 않는다. */
    private static final int OWNER_MISSING_GRACE_TICKS = 100;

    /** 자리 번호를 다시 읽는 주기(1초). 매 틱 저장소를 뒤질 필요는 없다. */
    private static final int RING_REFRESH_INTERVAL_TICKS = 20;
    /** 펫 한 마리만 있을 때의 원 반지름 */
    private static final double RING_BASE_RADIUS = 1.6;
    /** 펫이 한 마리 늘 때마다 더하는 반지름 */
    private static final double RING_RADIUS_PER_PET = 0.12;
    /** 원이 너무 커져 펫이 멀어지지 않도록 둔 상한 */
    private static final double RING_MAX_RADIUS = 4.0;

    /** 전투가 끝난 뒤 맞지도 때리지도 않고 기다려야 회복이 시작되는 시간(5초) */
    private static final int REGEN_DELAY_TICKS = 100;
    /** 회복 주기(5초)와 한 번에 회복하는 최대 체력 비율(1%). 0에서 가득 차기까지 약 8분 20초다. */
    private static final int REGEN_INTERVAL_TICKS = 100;
    private static final float REGEN_FRACTION = 0.01F;

    /**
     * 펫 저장소의 기록 ID. 차원 이동 시 엔티티가 새 개체로 복사되므로 NBT로도 승계한다.
     * 기록 없는 펫은 존재 자격이 없어 소멸한다.
     */
    private UUID recordId;

    private int ownerMissingTicks;

    /** 주인의 펫관리 탭에 마지막으로 알린 체력. 달라지면 다음 동기화 때 목록을 다시 보낸다. */
    private float lastReportedHealth = -1.0F;

    /**
     * 다음에 체력을 회복할 수 있는 틱. 맞거나 때리거나 추격 대상이 있으면 5초 뒤로 미룬다.
     * 소환된 뒤에도 한 주기(5초)가 지나야 첫 회복이 일어난다.
     */
    private int nextRegenTick = REGEN_INTERVAL_TICKS;

    /** 주인 둘레에서 맡은 자리 번호와 같이 서 있는 펫 수. 주기적으로 다시 읽는다. */
    private int ringSlot;
    private int ringCount = 1;

    public DialgaPetEntity(EntityType<? extends DialgaPetEntity> entityType, Level level)
    {
        super(entityType, level);
    }

    public static AttributeSupplier.Builder createAttributes()
    {
        // 추적 범위는 길찾기 거리와 대상 유지 거리를 함께 정한다.
        // 추격을 놓는 거리(주인에게서 56칸)에 주인 둘레 자리까지 더해도 닿도록 64칸으로 둔다.
        // 이동 속도는 늑대와 같은 0.3이다. 공격(1.4배)·따라가기(1.25배) 배율을 곱해 달리므로
        // 몹 이동은 속도의 제곱에 비례해 빨라진다. 0.35에서는 공격 시 달리기하는 주인의 2배 가까이 빨랐다.
        return TamableAnimal.createAnimalAttributes()
                .add(Attributes.MAX_HEALTH, 100.0)
                .add(Attributes.MOVEMENT_SPEED, 0.3)
                .add(Attributes.FOLLOW_RANGE, 64.0)
                .add(Attributes.ATTACK_DAMAGE, 1.0);
    }

    @Override
    protected void registerGoals()
    {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new PetMeleeAttackGoal(this, 1.4));
        this.goalSelector.addGoal(2, new FollowOwnerRingGoal(this, 1.25, 3.0F, 1.0F));
        this.goalSelector.addGoal(3, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(4, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new RecentOwnerHurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new HuntNearbyMonstersGoal(this));
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

        // 피격·회복·레벨업 등 체력이 바뀌는 경로가 여러 곳이라 한 곳에서 비교해 알린다.
        if (owner instanceof ServerPlayer healthOwner && this.getHealth() != this.lastReportedHealth)
        {
            this.lastReportedHealth = this.getHealth();
            PetManager.markStatusChanged(healthOwner);
        }

        if (owner instanceof ServerPlayer slotOwner && this.tickCount % RING_REFRESH_INTERVAL_TICKS == 0)
        {
            refreshRingSlot(slotOwner);
        }

        if (owner instanceof ServerPlayer serverPlayer && serverPlayer.level() != serverLevel)
        {
            this.pursuitTarget = null;
            this.setTarget(null);
            teleportToOwnerLevel(serverPlayer);
            return;
        }

        updatePursuit(owner);

        regenerateOutOfCombat();

        // 추격 중이 아닐 때 주인과 10칸 이상 벌어지면 곁으로 순간이동한다.
        // 사냥감을 잡은 직후에는 2초를 기다려, 다음 사냥감이 있으면 그 자리에서 바로 이어서 사냥한다.
        if (this.getTarget() != null)
        {
            this.lastCombatTick = this.tickCount;
        }
        boolean isIdle = this.tickCount - this.lastCombatTick > RETURN_DELAY_AFTER_COMBAT_TICKS;
        if (isIdle && this.distanceToSqr(owner) > TELEPORT_DISTANCE_SQR)
        {
            teleportBesideOwner(owner);
        }

        super.customServerAiStep(serverLevel);
    }

    /**
     * 전투가 끝나고 5초 동안 맞지도 때리지도 않으면 5초마다 최대 체력의 1%를 회복한다.
     * 추격 대상이 있는 동안은 아직 전투 중으로 본다.
     * 소환된 펫만 이 코드를 돌므로 꺼 두거나 접속을 끊은 동안에는 회복하지 않는다.
     */
    private void regenerateOutOfCombat()
    {
        if (this.getTarget() != null)
        {
            delayRegeneration();
            return;
        }

        boolean regenDue = this.tickCount >= this.nextRegenTick;
        boolean damaged = this.getHealth() < this.getMaxHealth();
        if (regenDue && damaged)
        {
            this.heal(this.getMaxHealth() * REGEN_FRACTION);
            this.nextRegenTick = this.tickCount + REGEN_INTERVAL_TICKS;
        }
    }

    /** 맞거나 때리거나 추격 중이면 회복을 5초 뒤로 미룬다. */
    private void delayRegeneration()
    {
        this.nextRegenTick = this.tickCount + REGEN_DELAY_TICKS;
    }

    public UUID getRecordId()
    {
        return this.recordId;
    }

    public void setRecordId(UUID recordId)
    {
        this.recordId = recordId;
    }

    /**
     * 어떤 이유로 사라지든 살아 있는 펫 목록에서 자신을 지운다.
     * 토글과 접속 종료 외에도 유예 시간 초과, 청크 언로드, 명령 처치 같은 경로가 있어서
     * 여기서 한 번에 정리하지 않으면 사라진 개체의 참조가 계속 남는다.
     */
    @Override
    public void remove(Entity.RemovalReason removalReason)
    {
        super.remove(removalReason);
        if (!this.level().isClientSide())
        {
            PetManager.releasePet(this);
        }
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
        Vec3 spot = ringPosition(owner);
        this.teleportTo(spot.x, spot.y, spot.z);
        this.setDeltaMovement(Vec3.ZERO);
        this.resetFallDistance();
    }

    /** 주인의 켜진 펫 목록에서 이 펫의 자리 번호를 다시 읽는다. */
    private void refreshRingSlot(ServerPlayer owner)
    {
        if (this.recordId == null)
        {
            return;
        }

        int slot = PetManager.enabledPetIndex(owner.getUUID(), this.recordId);
        if (slot >= 0)
        {
            this.ringSlot = slot;
            this.ringCount = Math.max(1, PetManager.enabledPetCount(owner.getUUID()));
        }
    }

    /**
     * 주인 둘레에서 이 펫이 설 자리.
     *
     * <p>자리 번호대로 원을 나눠 갖고, 펫이 많을수록 원을 키워 서로 간격을 둔다.
     * 각도는 세계 기준이라 주인이 몸을 돌려도 펫이 따라 돌지 않는다.
     */
    public Vec3 ringPosition(LivingEntity owner)
    {
        double angle = Math.TAU / this.ringCount * this.ringSlot;
        double radius = Math.min(RING_MAX_RADIUS, RING_BASE_RADIUS + this.ringCount * RING_RADIUS_PER_PET);
        return owner.position().add(Math.cos(angle) * radius, 0.0, Math.sin(angle) * radius);
    }

    /**
     * 공격 대상을 죽을 때까지 놓지 않는다.
     * 일반 타겟 AI는 거리가 벌어지면 추격을 포기하므로, 대상이 살아 있는 동안 타겟을 다시 지정한다.
     * 단, 대상이 주인에게서 56칸보다 멀어지거나 10초 동안 한 대도 못 때리면 추격을 포기하고 주인 곁으로 돌아간다.
     */
    private void updatePursuit(LivingEntity owner)
    {
        LivingEntity currentTarget = this.getTarget();
        if (currentTarget != null && currentTarget != this.pursuitTarget)
        {
            this.pursuitTarget = currentTarget;
            this.lastPursuitHitTick = this.tickCount;
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
        boolean stalled = !targetGone && this.tickCount - this.lastPursuitHitTick > PURSUIT_STALL_TICKS;
        if (stalled)
        {
            // 높은 곳이나 물 건너처럼 닿지 못하는 대상 앞에 계속 서 있지 않게 한다.
            this.ignoredTargetId = this.pursuitTarget.getId();
            this.ignoreTargetUntilTick = this.tickCount + IGNORE_TARGET_TICKS;
        }
        // 주인이 일반공격으로 바꾸면 사냥으로 쫓던 대상은 놓고, 주인을 때린 대상만 계속 쫓는다.
        boolean huntStopped = !targetGone
                && this.pursuitTarget.getId() == this.huntTargetId
                && PetManager.getAttackMode(owner) != PetAttackMode.AUTO;
        if (targetGone || targetTooFarFromOwner || stalled || huntStopped)
        {
            this.pursuitTarget = null;
            this.huntTargetId = -1;
            this.setTarget(null);
            return;
        }

        if (this.getTarget() == null)
        {
            this.setTarget(this.pursuitTarget);
        }
    }

    /** 닿지 못해 방금 놓은 대상인지. 이 시간 동안은 사냥감으로 다시 고르지 않는다. */
    public boolean isIgnoringTarget(Entity entity)
    {
        return entity.getId() == this.ignoredTargetId && this.tickCount < this.ignoreTargetUntilTick;
    }

    /** 사냥 AI가 고른 대상임을 남긴다. */
    void markHuntTarget(LivingEntity target)
    {
        this.huntTargetId = target.getId();
    }

    /** 반격으로 대상을 잡으면 사냥 표시를 지운다. 사냥하던 몹이 주인을 때렸다면 반격 대상으로 계속 쫓는다. */
    void clearHuntTarget()
    {
        this.huntTargetId = -1;
    }

    /**
     * 펫끼리 싸우면 서로의 펫만 닳고 정작 주인을 때린 쪽은 멀쩡하다.
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
     * 시킨 쪽은 주인이므로 펫이 아니라 주인을 물어야 전투가 성립한다.
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

        // /kill 명령(BYPASSES_INVULNERABILITY)은 그대로 통과시켜 운영 중 정리가 가능하게 한다.
        if (damageSource.is(DamageTypeTags.BYPASSES_INVULNERABILITY))
        {
            return super.hurtServer(serverLevel, damageSource, amount);
        }

        // 누군가에게 맞은 피해만 받는다. 낙하·질식·화염 같은 환경 피해는 무시한다.
        // 주인을 따라 순간이동하는 펫이 지형 때문에 죽는 일을 막기 위해서다.
        Entity attacker = damageSource.getEntity();
        if (!(attacker instanceof LivingEntity) || isFriendly(attacker))
        {
            return false;
        }
        boolean damaged = super.hurtServer(serverLevel, damageSource, amount);
        if (damaged)
        {
            delayRegeneration();
        }
        return damaged;
    }

    /**
     * 주인과 주인의 다른 펫이 건 해로운 효과는 받지 않는다. 몹에게 던진 투척·잔류 포션이 곁의 펫에게 번져도 멀쩡하다.
     * 이로운 효과는 그대로 받아, 주인이 포션으로 펫을 도울 수 있다.
     */
    @Override
    public boolean addEffect(MobEffectInstance effect, @Nullable Entity source)
    {
        boolean harmful = effect.getEffect().value().getCategory() == MobEffectCategory.HARMFUL;
        if (harmful && source != null && isFriendly(source))
        {
            return false;
        }
        return super.addEffect(effect, source);
    }

    /** 주인과 주인의 다른 펫에게서는 피해를 받지 않는다. */
    private boolean isFriendly(Entity attacker)
    {
        LivingEntity owner = this.getOwner();
        if (owner == null)
        {
            return false;
        }
        return attacker == owner || (attacker instanceof DialgaPetEntity otherPet && otherPet.getOwner() == owner);
    }

    /**
     * 몹을 한 대 때릴 때마다 경험치를 얻는다. 플레이어를 때린 것은 세지 않는다.
     * 누구를 때렸든 전투 중이므로 회복은 미룬다.
     */
    @Override
    public boolean doHurtTarget(ServerLevel serverLevel, Entity target)
    {
        boolean hit = super.doHurtTarget(serverLevel, target);
        if (hit)
        {
            delayRegeneration();
        }
        if (hit && target == this.pursuitTarget)
        {
            this.lastPursuitHitTick = this.tickCount;
        }
        if (hit && target instanceof Mob)
        {
            PetManager.handlePetHit(this);
        }
        return hit;
    }

    /** 죽으면 기록에 부활 시각을 남긴다. 쓰러지는 모습을 보여준 뒤 바닐라 경로로 사라진다. */
    @Override
    public void die(DamageSource damageSource)
    {
        super.die(damageSource);
        if (!this.level().isClientSide())
        {
            PetManager.handlePetDeath(this);
        }
    }

    @Override
    protected boolean shouldDropLoot(ServerLevel serverLevel)
    {
        return false;
    }

    @Override
    public boolean shouldDropExperience()
    {
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
     * 주인이 최근에 맞았을 때만 복수에 나서는 AI. 자동공격과 일반공격 모두에서 동작한다.
     * <p>
     * 바닐라 {@link OwnerHurtByTargetGoal}은 피격 시각의 최신 여부만 비교해서,
     * 재접속으로 AI나 플레이어 인스턴스가 새로 만들어지면 낡은 전투 기억을
     * 새 피격으로 착각하고 이미 끝난 싸움의 상대를 다시 공격한다.
     * 실제 피격 후 짧은 시간 안에만 발동하도록 제한해 이를 막는다.
     * <p>
     * 멀리서 쏜 상대를 쫓아 펫이 주인 곁을 떠나지 않도록, 공격자가 사냥 범위(3청크) 안에 있을 때만 반격한다.
     */
    private static class RecentOwnerHurtByTargetGoal extends OwnerHurtByTargetGoal
    {
        /** 주인이 맞은 지 이 시간(5초)이 지나면 복수하지 않는다. */
        private static final int MAX_HURT_AGE_TICKS = 100;
        /** 공격자가 주인에게서 이 거리(3청크, 48칸) 안에 있을 때만 반격한다. 자동공격의 사냥 범위와 같다. */
        private static final double RETALIATION_RANGE_SQR =
                HuntNearbyMonstersGoal.HUNT_RADIUS * HuntNearbyMonstersGoal.HUNT_RADIUS;

        private final DialgaPetEntity pet;

        RecentOwnerHurtByTargetGoal(DialgaPetEntity pet)
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
            LivingEntity attacker = owner.getLastHurtByMob();
            boolean attackerInRange = attacker != null && attacker.distanceToSqr(owner) <= RETALIATION_RANGE_SQR;
            return isRecentHurt && attackerInRange && super.canUse();
        }

        @Override
        public void start()
        {
            super.start();
            this.pet.clearHuntTarget();
        }
    }
}
