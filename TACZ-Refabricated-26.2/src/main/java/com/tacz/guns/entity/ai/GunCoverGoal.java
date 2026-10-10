package com.tacz.guns.entity.ai;

import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.api.entity.IGunOperator;
import com.tacz.guns.api.item.IGun;
import com.tacz.guns.entity.shooter.MonsterGunAmmo;
import com.tacz.guns.entity.shooter.MonsterGunController;
import com.tacz.guns.resource.pojo.data.gun.GunData;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.EnumSet;

/**
 * 원거리 무기를 든 플레이어를 상대할 때 총을 든 몬스터가 엄폐물 뒤에서 싸우게 한다.
 * <p>
 * 사격은 {@link MonsterGunController}가 맡고 이 Goal은 이동과 시선만 정한다. 엄폐물이 있으면 약 2초 숨어서 장전하고
 * 옆으로 살짝 몸을 내밀어 쏜 뒤 다시 숨는다. 한 번 숨은 뒤로는 숨은 자리가 드러나도 달아나지 않고 그 자리에서 맞서 쏜다.
 * 엄폐물이 없거나 맞서 쏘는 동안에는 돌진하지 않고 사거리 안에서 좌우로 옆걸음질하며 쏜다.
 * 플레이어가 원거리 무기를 내려놓거나 가까이 붙거나 탄약이 떨어지면 기존 근접 추격으로 돌아간다.
 */
public class GunCoverGoal extends Goal {
    /** 대상이 이 거리 안으로 붙으면 엄폐를 풀고 근접 공격으로 돌아간다. */
    private static final double MELEE_SWITCH_DISTANCE = 4.0;
    /** 엄폐 없이 싸울 때 사거리의 이 비율 안에서 대상이 보이면 다가가지 않고 그 자리에서 쏜다. */
    private static final double HOLD_RANGE_RATIO = 0.8;
    /** 한 번 몸을 내밀었을 때 대상을 보며 쏘는 시간(틱). */
    private static final int PEEK_FIRE_TICKS = 30;
    /** 숨어 있는 동안 탄창이 이 비율보다 적게 남았으면 미리 재장전한다. */
    private static final float TACTICAL_RELOAD_RATIO = 0.5f;
    /** 엄폐 없이 다가갈 때 경로를 다시 계산하는 간격(틱). */
    private static final int REPATH_INTERVAL = 10;
    private static final double MOVE_SPEED = 1.0;
    /** 엄폐 없이 쏠 때 옆걸음 속도. 바닐라 스켈레톤 활 공격과 같은 값이다. */
    private static final float STRAFE_SPEED = 0.5f;
    /** 옆걸음 방향을 바꾸는 간격(틱). 이 범위에서 무작위로 정해 움직임을 읽기 어렵게 한다. */
    private static final int MIN_STRAFE_TICKS = 20;
    private static final int MAX_STRAFE_TICKS = 40;
    /** 사선이 겹쳤는지 다시 확인하는 간격(틱). 주변 몬스터의 사선을 훑는 비용이 있어 매 틱 보지 않는다. */
    private static final int LANE_CHECK_INTERVAL = 5;

    private final PathfinderMob mob;
    private final CoverTactics tactics;
    private final RangedThreat threat = new RangedThreat();
    private final ThreatSearch threatSearch = new ThreatSearch();
    private int peekFireTicks;
    private int repathCooldown;
    private boolean strafing;
    /** 옆걸음 값의 부호(+1 또는 -1) */
    private int strafeSign = 1;
    private int strafeTicks;
    private int laneCheckCooldown;

    public GunCoverGoal(PathfinderMob mob) {
        this.mob = mob;
        this.tactics = new CoverTactics(mob, MOVE_SPEED);
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        return isEligible();
    }

    @Override
    public boolean canContinueToUse() {
        return isEligible();
    }

    @Override
    public void start() {
        this.mob.setAggressive(true);
        this.tactics.reset();
        this.peekFireTicks = 0;
        this.repathCooldown = 0;
        this.strafeTicks = 0;
        this.threatSearch.reset();
    }

    @Override
    public void stop() {
        this.mob.setAggressive(false);
        this.mob.getNavigation().stop();
        stopStrafing();
        this.tactics.reset();
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        LivingEntity target = this.mob.getTarget();
        GunData data = heldGunData();
        if (target == null || data == null) {
            return;
        }
        // 대상이 보이면 위치를 기억하고, 대상에게 맞았으면 움츠러들거나 자리를 옮긴다.
        this.tactics.observe(target);
        ItemStack stack = this.mob.getMainHandItem();
        IGun gun = (IGun) stack.getItem();
        double range = MonsterGunController.getEffectiveRange(data);
        int loadedAmmo = MonsterGunController.loadedAmmo(gun, stack, data);
        boolean reloading = IGunOperator.fromLivingEntity(this.mob).getDataHolder().reloadStateType.isReloading();

        if (this.tactics.hasSpot() || this.tactics.trySearch(target, range)) {
            stopStrafing();
            CoverTactics.Phase phase = this.tactics.phase();
            if (phase != CoverTactics.Phase.PEEK) {
                this.peekFireTicks = 0;
            } else if (this.mob.getSensing().hasLineOfSight(target)) {
                this.peekFireTicks++;
            }
            if (phase == CoverTactics.Phase.HIDE && !reloading
                    && loadedAmmo < Math.max(1, Math.round(data.getAmmoAmount() * TACTICAL_RELOAD_RATIO))) {
                IGunOperator.fromLivingEntity(this.mob).reload();
            }
            boolean readyToPeek = !reloading && loadedAmmo > 0;
            // 정해진 시간만큼 쐈거나, 탄창을 비웠거나, 재장전이 시작됐으면 다시 숨는다.
            boolean peekFinished = this.peekFireTicks >= PEEK_FIRE_TICKS || loadedAmmo <= 0 || reloading;
            if (this.tactics.tick(target, readyToPeek, peekFinished)) {
                return;
            }
        }
        fightInOpen(target, range);
    }

    /**
     * 엄폐 없이 싸운다. 엄폐물이 없거나, 숨은 자리가 드러나 그 자리에서 맞서 쏘는 중일 때다.
     * 사거리 안에서 대상이 보이면 다가가지 않고 좌우로 옆걸음질하며 쏘고, 사거리 밖이면 다가간다.
     * 보이지 않으면 대상의 지금 위치를 쫓지 않고 마지막으로 본 곳으로 가서 살핀다.
     */
    private void fightInOpen(LivingEntity target, double range) {
        if (!this.mob.getSensing().hasLineOfSight(target)) {
            stopStrafing();
            this.threatSearch.tick(this.mob, lastKnownPosition(target), MOVE_SPEED);
            return;
        }
        this.threatSearch.reset();
        this.mob.getLookControl().setLookAt(target, 30.0f, 30.0f);
        double holdDistance = range * HOLD_RANGE_RATIO;
        boolean inHoldRange = this.mob.distanceToSqr(target) <= holdDistance * holdDistance;
        if (inHoldRange) {
            this.mob.getNavigation().stop();
            this.repathCooldown = 0;
            strafe(target);
            ((CoverCombatant) this.mob).tacz$markFiring(this.mob.level().getGameTime());
            return;
        }
        stopStrafing();
        if (--this.repathCooldown <= 0) {
            this.repathCooldown = REPATH_INTERVAL;
            this.mob.getNavigation().moveTo(target, MOVE_SPEED);
        }
    }

    /** 대상을 마지막으로 본(짐작한) 위치. 기억이 없으면 지금 위치를 쓴다. */
    private Vec3 lastKnownPosition(LivingEntity target) {
        Vec3 known = ((CoverCombatant) this.mob).tacz$lastKnownThreatPos(target);
        if (known == null) {
            return target.position();
        }
        return known;
    }

    /**
     * 좌우로 옆걸음질한다. 옆걸음은 몸이 향한 방향 기준이므로 몸을 대상 쪽으로 돌려 둔다.
     * 다른 몬스터와 사선이 겹치면 겹치지 않는 쪽으로 방향을 바꾼다.
     */
    private void strafe(LivingEntity target) {
        if (--this.strafeTicks <= 0) {
            this.strafeTicks = MIN_STRAFE_TICKS + this.mob.getRandom().nextInt(MAX_STRAFE_TICKS - MIN_STRAFE_TICKS + 1);
            this.strafeSign = -this.strafeSign;
        }
        if (--this.laneCheckCooldown <= 0) {
            this.laneCheckCooldown = LANE_CHECK_INTERVAL;
            int dodge = FriendlyFireLanes.dodgeDirection(this.mob, target);
            if (dodge != 0) {
                // 사선에서 벗어날 때까지 같은 방향으로 움직이도록 방향 전환을 잠시 미룬다.
                this.strafeSign = dodge;
                this.strafeTicks = MIN_STRAFE_TICKS;
            }
        }
        this.mob.lookAt(target, 30.0f, 30.0f);
        this.mob.getMoveControl().strafe(0.0f, this.strafeSign * STRAFE_SPEED);
        this.strafing = true;
    }

    /** 옆걸음을 멈춘다. 이동 제어는 옆걸음 입력을 스스로 지우지 않아 그대로 두면 옆으로 계속 미끄러진다. */
    private void stopStrafing() {
        if (!this.strafing) {
            return;
        }
        this.strafing = false;
        this.mob.setXxa(0.0f);
        this.mob.getMoveControl().setWait();
    }

    private boolean isEligible() {
        LivingEntity target = this.mob.getTarget();
        if (target == null || !target.isAlive()) {
            return false;
        }
        // 아기 좀비는 너무 작고 빨라 엄폐가 어색하다. 탈것에 탄 몬스터는 스스로 걷지 못한다.
        // 불이 붙으면 엄폐를 잠시 내려놓는다. 스켈레톤은 바닐라처럼 햇빛을 피해 그늘로 가야 한다.
        if (this.mob.isBaby() || this.mob.isPassenger() || this.mob.isInWater() || this.mob.isOnFire()) {
            return false;
        }
        if (heldGunData() == null || !MonsterGunAmmo.hasAmmo(this.mob.getMainHandItem())) {
            return false;
        }
        // 저격 계열은 멀리서는 SniperGoal이 맡고, 가까이 오면 숨지 않고 근접 추격으로 달려가며 쏜다.
        if (SniperGuns.isSniperClass(this.mob.getMainHandItem())) {
            return false;
        }
        if (this.mob.distanceToSqr(target) <= MELEE_SWITCH_DISTANCE * MELEE_SWITCH_DISTANCE) {
            return false;
        }
        return this.threat.isThreat(target, this.mob.level().getGameTime());
    }

    @Nullable
    private GunData heldGunData() {
        ItemStack stack = this.mob.getMainHandItem();
        if (!(stack.getItem() instanceof IGun gun)) {
            return null;
        }
        return TimelessAPI.getCommonGunIndex(gun.getGunId(stack)).map(index -> index.getGunData()).orElse(null);
    }
}
