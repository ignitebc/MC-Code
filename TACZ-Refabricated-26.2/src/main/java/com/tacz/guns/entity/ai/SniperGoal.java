package com.tacz.guns.entity.ai;

import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.api.item.IGun;
import com.tacz.guns.entity.shooter.MonsterGunAmmo;
import com.tacz.guns.entity.shooter.MonsterGunController;
import com.tacz.guns.resource.pojo.data.gun.GunData;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.EnumSet;

/**
 * 저격총·지정사수소총을 든 몬스터가 멀리서 멈춰 서서 한 발씩 쏘게 한다.
 * <p>
 * 사격과 박자는 {@link MonsterGunController}가 맡고 이 Goal은 자리와 시선만 정한다. 대상을 발견하면 대상이 보이고 사거리 안인 자리,
 * 그중 높고 하체가 가려지는 자리를 찾아가 멈춰 선다. 자리를 잡은 뒤에는 움직이지 않고 겨누며, 짧은 사이 두 번 맞거나
 * 대상을 한동안 보지 못하면 다른 자리로 옮긴다.
 * <p>
 * 플레이어가 무엇을 들었든 저격한다. 대상이 {@link #CHARGE_DISTANCE} 안으로 다가오면 이 Goal을 내려놓아,
 * 몬스터 원래의 근접 추격으로 달려가면서 총을 쏜다.
 */
public class SniperGoal extends Goal {
    /** 대상이 이 거리(칸) 안으로 오면 저격을 그만두고 달려가며 쏜다. */
    private static final double CHARGE_DISTANCE = 12.0;
    /** 달려가던 중 대상이 이 거리(칸)보다 멀어져야 다시 저격한다. 경계에서 저격과 돌진을 오가지 않게 한다. */
    private static final double RESUME_DISTANCE = 16.0;
    /** 저격 자리는 대상에게서 이 거리(칸) 이상 떨어져야 한다. 자리를 잡자마자 돌진으로 바뀌지 않게 여유를 둔다. */
    private static final double MIN_SPOT_DISTANCE = RESUME_DISTANCE + 2.0;
    /** 대상이 이미 보일 때 더 나은 자리를 찾아 걸어가는 최대 거리(칸). 멀리 돌아다니지 않고 근처에서만 고쳐 선다. */
    private static final double MAX_ADJUST_TRAVEL = 10.0;
    /** 대상이 보이지 않아 쏠 자리를 찾을 때 걸어가는 최대 거리(칸) */
    private static final double MAX_SEARCH_TRAVEL = 24.0;
    private static final double MOVE_SPEED = 1.0;
    /** 저격 자리로 옮길 때는 달린다. */
    private static final double SPRINT_SPEED_FACTOR = 1.25;
    /** 자리를 찾지 못했을 때 다시 찾기까지 기다리는 시간(틱) */
    private static final int RESEARCH_DELAY_TICKS = 40;
    /** 저격 자리까지 이 시간(틱) 안에 닿지 못하면 그 자리에서 멈춘다. */
    private static final int MOVE_TIMEOUT_TICKS = 120;
    /** 자리를 잡은 뒤 대상을 이 시간(틱) 넘게 보지 못하면 새 자리를 찾는다. */
    private static final int REPOSITION_UNSEEN_TICKS = 100;
    /** 대상을 이 시간(틱) 넘게 보지 못하면 저격을 그만둔다. 사라진 대상을 붙잡고 서 있지 않게 한다. */
    private static final int GIVE_UP_UNSEEN_TICKS = 400;
    /** 저격을 그만둔 뒤 다시 저격하기까지 기다리는 시간(틱). 바닐라 대상 지정이 대상을 잊을 시간을 준다. */
    private static final int GIVE_UP_DELAY_TICKS = 100;
    /** 이 시간(틱) 안에 두 번 맞으면 자리가 드러난 것으로 보고 옮긴다. */
    private static final int RELOCATE_HIT_WINDOW_TICKS = 100;
    private static final int RELOCATE_HITS = 2;
    /** 대상 지정이 저격 중 대상을 놓지 않도록 붙잡아 두는 여유 시간(틱). 대상 지정은 두 틱마다 검사한다. */
    private static final int TARGET_HOLD_TICKS = 3;
    private static final double ARRIVE_DISTANCE_SQR = 0.6 * 0.6;

    private final PathfinderMob mob;
    private final ThreatSearch threatSearch = new ThreatSearch();
    /** 찾아가는 중이거나 잡아 둔 저격 자리. 없으면 null */
    @Nullable
    private BlockPos spot;
    /** 자리를 잡고 멈춰 섰는지 */
    private boolean positioned;
    /** 자리를 옮기는 중이면 버린 자리. 새 자리를 찾을 때 그 근처를 피한다. */
    @Nullable
    private BlockPos abandonedSpot;
    private int moveTicks;
    private long nextSearchTick;
    private long lastSeenTick;
    private long giveUpUntilTick = Long.MIN_VALUE;
    private int lastHandledHurtTimestamp;
    private long hitWindowStartTick;
    private int hitsInWindow;

    public SniperGoal(PathfinderMob mob) {
        this.mob = mob;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (this.mob.level().getGameTime() < this.giveUpUntilTick) {
            return false;
        }
        LivingEntity target = MonsterGunController.currentTarget(this.mob);
        return isEligible(target) && this.mob.distanceToSqr(target) > RESUME_DISTANCE * RESUME_DISTANCE;
    }

    @Override
    public boolean canContinueToUse() {
        if (this.mob.level().getGameTime() < this.giveUpUntilTick) {
            return false;
        }
        LivingEntity target = MonsterGunController.currentTarget(this.mob);
        return isEligible(target) && this.mob.distanceToSqr(target) > CHARGE_DISTANCE * CHARGE_DISTANCE;
    }

    @Override
    public void start() {
        long gameTime = this.mob.level().getGameTime();
        this.mob.setAggressive(true);
        this.spot = null;
        this.positioned = false;
        this.abandonedSpot = null;
        this.moveTicks = 0;
        this.nextSearchTick = gameTime;
        this.lastSeenTick = gameTime;
        this.lastHandledHurtTimestamp = this.mob.getLastHurtByMobTimestamp();
        this.hitsInWindow = 0;
        this.threatSearch.reset();
    }

    @Override
    public void stop() {
        this.mob.setAggressive(false);
        this.mob.getNavigation().stop();
        CoverCombatant combatant = (CoverCombatant) this.mob;
        combatant.tacz$setCoverPos(null);
        combatant.tacz$releaseTarget();
        this.spot = null;
        this.positioned = false;
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        LivingEntity target = MonsterGunController.currentTarget(this.mob);
        GunData data = heldGunData();
        if (target == null || data == null) {
            return;
        }
        long gameTime = this.mob.level().getGameTime();
        CoverCombatant combatant = (CoverCombatant) this.mob;
        double range = MonsterGunController.getEffectiveRange(data);
        boolean visible = this.mob.getSensing().hasLineOfSight(target);
        boolean inRange = this.mob.distanceToSqr(target) <= range * range;
        if (visible) {
            this.lastSeenTick = gameTime;
            combatant.tacz$rememberThreat(target, target.position());
        }
        if (gameTime - this.lastSeenTick > GIVE_UP_UNSEEN_TICKS) {
            // 다음 canContinueToUse에서 내려놓고, 대상 지정이 대상을 잊을 수 있게 붙잡아 두지 않는다.
            this.giveUpUntilTick = gameTime + GIVE_UP_DELAY_TICKS;
            return;
        }
        combatant.tacz$holdTarget(target, gameTime + TARGET_HOLD_TICKS);
        trackHits(gameTime);

        if (this.positioned) {
            boolean lostTooLong = gameTime - this.lastSeenTick > REPOSITION_UNSEEN_TICKS;
            if (!lostTooLong && inRange) {
                holdPosition(target, visible, gameTime);
                return;
            }
            relocate(gameTime);
        }
        if (this.spot != null) {
            moveToSpot(target, visible);
            return;
        }
        chooseSpot(target, visible && inRange, range, gameTime);
    }

    /**
     * 자리를 고른다. 대상이 이미 보이면 근처에서 높거나 하체가 가려지는 자리로만 고쳐 서고, 없으면 그 자리에 선다.
     * 보이지 않으면 대상이 보이는 자리를 찾고, 그것도 없으면 마지막으로 본 곳으로 다가간다.
     */
    private void chooseSpot(LivingEntity target, boolean canShootHere, double range, long gameTime) {
        Vec3 lastKnown = lastKnownPosition(target);
        boolean searchReady = gameTime >= this.nextSearchTick;
        SniperSpotFinder.Spot found = null;
        boolean searched = false;
        if (searchReady && CoverFinder.tryReserveSearch(gameTime)) {
            searched = true;
            double maxTravel = canShootHere ? MAX_ADJUST_TRAVEL : MAX_SEARCH_TRAVEL;
            Vec3 targetEye = lastKnown.add(0, target.getEyeHeight(), 0);
            found = SniperSpotFinder.find(this.mob, new SniperSpotFinder.Search(
                    targetEye, lastKnown, range, MIN_SPOT_DISTANCE, maxTravel, this.abandonedSpot));
        }
        if (canShootHere) {
            boolean hereConcealed = SniperSpotFinder.isConcealed(this.mob, this.mob.blockPosition(),
                    target.getEyePosition());
            // 지금 자리가 이미 가려져 있거나 더 나은 자리가 없으면 옮기지 않고 그 자리에서 쏜다.
            boolean betterSpot = found != null && found.concealed() && !hereConcealed;
            if (betterSpot) {
                goTo(found);
                return;
            }
            if (searched || !searchReady) {
                takePosition(this.mob.blockPosition());
                return;
            }
            // 이번 틱에 탐색 몫이 없었다. 겨누기만 하고 다음 틱에 다시 찾는다.
            this.mob.getNavigation().stop();
            return;
        }
        if (found != null) {
            goTo(found);
            return;
        }
        if (searched) {
            this.nextSearchTick = gameTime + RESEARCH_DELAY_TICKS;
        }
        this.threatSearch.tick(this.mob, lastKnown, MOVE_SPEED);
    }

    private void goTo(SniperSpotFinder.Spot found) {
        this.spot = found.pos();
        this.moveTicks = 0;
        this.threatSearch.reset();
        ((CoverCombatant) this.mob).tacz$setCoverPos(found.pos());
        this.mob.getNavigation().moveTo(found.path(), MOVE_SPEED * SPRINT_SPEED_FACTOR);
    }

    /** 저격 자리로 달려간다. 닿았거나 너무 오래 걸리면 그 자리에 멈춰 선다. */
    private void moveToSpot(LivingEntity target, boolean visible) {
        this.moveTicks++;
        Vec3 center = Vec3.atBottomCenterOf(this.spot);
        boolean arrived = this.mob.position().distanceToSqr(center) <= ARRIVE_DISTANCE_SQR;
        boolean stuck = this.mob.getNavigation().isDone() || this.moveTicks > MOVE_TIMEOUT_TICKS;
        if (arrived || stuck) {
            takePosition(this.mob.blockPosition());
            return;
        }
        if (visible) {
            this.mob.getLookControl().setLookAt(target, 30.0f, 30.0f);
        }
    }

    private void takePosition(BlockPos pos) {
        this.spot = pos;
        this.positioned = true;
        this.abandonedSpot = null;
        this.threatSearch.reset();
        this.mob.getNavigation().stop();
        ((CoverCombatant) this.mob).tacz$setCoverPos(pos);
    }

    /** 자리를 잡은 채 움직이지 않고 겨눈다. 대상이 잠깐 숨으면 마지막으로 본 곳을 계속 겨눈다. */
    private void holdPosition(LivingEntity target, boolean visible, long gameTime) {
        this.mob.getNavigation().stop();
        if (visible) {
            this.mob.getLookControl().setLookAt(target, 30.0f, 30.0f);
            ((CoverCombatant) this.mob).tacz$markFiring(gameTime);
            return;
        }
        Vec3 lastKnown = lastKnownPosition(target);
        this.mob.getLookControl().setLookAt(lastKnown.x, lastKnown.y + target.getEyeHeight(), lastKnown.z);
    }

    /** 지금 자리를 버리고 새 자리를 찾는다. */
    private void relocate(long gameTime) {
        this.abandonedSpot = this.spot;
        this.spot = null;
        this.positioned = false;
        this.nextSearchTick = gameTime;
        ((CoverCombatant) this.mob).tacz$setCoverPos(null);
    }

    /** 자리를 잡은 뒤 짧은 사이 두 번 맞으면 자리가 드러난 것으로 보고 옮긴다. */
    private void trackHits(long gameTime) {
        int hurtTimestamp = this.mob.getLastHurtByMobTimestamp();
        if (hurtTimestamp == this.lastHandledHurtTimestamp) {
            return;
        }
        this.lastHandledHurtTimestamp = hurtTimestamp;
        if (gameTime - this.hitWindowStartTick > RELOCATE_HIT_WINDOW_TICKS) {
            this.hitWindowStartTick = gameTime;
            this.hitsInWindow = 0;
        }
        this.hitsInWindow++;
        if (this.positioned && this.hitsInWindow >= RELOCATE_HITS) {
            this.hitsInWindow = 0;
            relocate(gameTime);
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

    private boolean isEligible(@Nullable LivingEntity target) {
        if (target == null || !target.isAlive()) {
            return false;
        }
        if (target instanceof Player player && (player.isCreative() || player.isSpectator())) {
            return false;
        }
        // 탈것에 탄 몬스터는 스스로 걷지 못한다. 불이 붙으면 저격을 잠시 내려놓는다. 스켈레톤은 햇빛을 피해 그늘로 가야 한다.
        if (this.mob.isBaby() || this.mob.isPassenger() || this.mob.isInWater() || this.mob.isOnFire()) {
            return false;
        }
        ItemStack stack = this.mob.getMainHandItem();
        return SniperGuns.isSniperClass(stack) && MonsterGunAmmo.hasAmmo(stack);
    }

    @Nullable
    private GunData heldGunData() {
        ItemStack stack = this.mob.getMainHandItem();
        if (!(stack.getItem() instanceof IGun gun)) {
            return null;
        }
        return TimelessAPI.getCommonGunIndex(gun.getGunId(stack)).map(index -> index.getGunData()).orElse(null);
    }

    /** Brain 방식 몬스터(피글린)는 Goal 선택기를 쓰지 않으므로, 이 Goal을 직접 켜고 끄며 돌린다. */
    public static final class BrainDriver {
        private final SniperGoal goal;
        private boolean running;

        public BrainDriver(PathfinderMob mob) {
            this.goal = new SniperGoal(mob);
        }

        /**
         * 저격할 수 있으면 한 틱 돌린다.
         *
         * @return 저격 중이면 true. 이번 틱은 Brain의 이동·전투 행동을 건너뛰어야 한다.
         */
        public boolean tick() {
            boolean usable = this.running ? this.goal.canContinueToUse() : this.goal.canUse();
            if (!usable) {
                if (this.running) {
                    this.goal.stop();
                    this.running = false;
                }
                return false;
            }
            if (!this.running) {
                this.goal.start();
                this.running = true;
            }
            this.goal.tick();
            return true;
        }
    }
}
