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

import javax.annotation.Nullable;
import java.util.EnumSet;

/**
 * 원거리 무기를 든 플레이어를 상대할 때 총을 든 몬스터가 엄폐물 뒤에서 싸우게 한다.
 * <p>
 * 사격은 {@link MonsterGunController}가 맡고 이 Goal은 이동과 시선만 정한다. 엄폐물이 있으면 숨어서 장전하고
 * 옆으로 잠깐 나와 쏜 뒤 다시 숨는다. 엄폐물이 없으면 돌진하지 않고 사거리 안에서 멈춰 쏜다.
 * 플레이어가 원거리 무기를 내려놓거나 가까이 붙거나 탄약이 떨어지면 기존 근접 추격으로 돌아간다.
 */
public class GunCoverGoal extends Goal {
    /** 대상이 이 거리 안으로 붙으면 엄폐를 풀고 근접 공격으로 돌아간다. */
    private static final double MELEE_SWITCH_DISTANCE = 4.0;
    /** 엄폐물이 없을 때 사거리의 이 비율 안에서 대상이 보이면 다가가지 않고 멈춰 쏜다. */
    private static final double HOLD_RANGE_RATIO = 0.8;
    /** 한 번 나왔을 때 대상을 보며 쏘는 시간(틱). 신중한 성향이라 짧게 둔다. */
    private static final int PEEK_FIRE_TICKS = 30;
    /** 숨어 있는 동안 탄창이 이 비율보다 적게 남았으면 미리 재장전한다. */
    private static final float TACTICAL_RELOAD_RATIO = 0.5f;
    /** 엄폐물 없이 다가갈 때 경로를 다시 계산하는 간격(틱). */
    private static final int REPATH_INTERVAL = 10;
    private static final double MOVE_SPEED = 1.0;

    private final PathfinderMob mob;
    private final CoverTactics tactics;
    private final RangedThreat threat = new RangedThreat();
    private int peekFireTicks;
    private int repathCooldown;

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
    }

    @Override
    public void stop() {
        this.mob.setAggressive(false);
        this.mob.getNavigation().stop();
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
        ItemStack stack = this.mob.getMainHandItem();
        IGun gun = (IGun) stack.getItem();
        double range = MonsterGunController.getEffectiveRange(data);
        int loadedAmmo = MonsterGunController.loadedAmmo(gun, stack, data);
        boolean reloading = IGunOperator.fromLivingEntity(this.mob).getDataHolder().reloadStateType.isReloading();

        if (this.tactics.hasSpot() || this.tactics.trySearch(target, range)) {
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
        holdRange(target, range);
    }

    /** 엄폐물이 없을 때. 사거리 안에서 대상이 보이면 멈춰 쏘고, 아니면 다가간다. */
    private void holdRange(LivingEntity target, double range) {
        this.mob.getLookControl().setLookAt(target, 30.0f, 30.0f);
        double holdDistance = range * HOLD_RANGE_RATIO;
        if (this.mob.getSensing().hasLineOfSight(target) && this.mob.distanceToSqr(target) <= holdDistance * holdDistance) {
            this.mob.getNavigation().stop();
            this.repathCooldown = 0;
            return;
        }
        if (--this.repathCooldown <= 0) {
            this.repathCooldown = REPATH_INTERVAL;
            this.mob.getNavigation().moveTo(target, MOVE_SPEED);
        }
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
