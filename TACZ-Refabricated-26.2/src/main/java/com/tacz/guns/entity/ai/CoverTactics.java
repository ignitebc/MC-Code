package com.tacz.guns.entity.ai;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;

/**
 * 엄폐 칸으로 이동해 숨었다가 옆으로 나와 쏘고 다시 숨는 흐름을 관리한다. 총과 활이 함께 쓴다.
 * <p>
 * 언제 나와도 되는지(장전·활시위)와 언제 사격을 마쳤는지는 무기마다 달라서 호출하는 Goal이 알려 준다.
 * 숨는 시간을 길게, 노출 시간을 짧게 잡은 신중한 성향이다.
 */
public final class CoverTactics {
    public enum Phase {
        MOVE_TO_COVER,
        HIDE,
        PEEK
    }

    /** 엄폐 칸에서 숨어 있는 시간(틱). 이 범위에서 무작위로 정한다. */
    private static final int MIN_HIDE_TICKS = 60;
    private static final int MAX_HIDE_TICKS = 80;
    /**
     * 노출 칸에서 기다리는 최대 시간(틱).
     * 한 번도 대상을 보지 못했으면 노출 칸이 쓸모없는 것이라 엄폐 칸을 버리고, 쏘던 중 대상이 숨었으면 다시 숨는다.
     */
    private static final int PEEK_TIMEOUT_TICKS = 60;
    /** 엄폐 칸까지 이 시간 안에 닿지 못하면 엄폐 칸을 버린다. */
    private static final int MOVE_TIMEOUT_TICKS = 100;
    /** 대상이 움직여 엄폐 칸이 드러났는지 다시 확인하는 간격(틱). */
    private static final int VALIDATE_INTERVAL = 10;
    /** 엄폐 칸을 찾지 못했을 때 다시 찾기까지 기다리는 시간(틱). */
    private static final int RESEARCH_DELAY = 40;
    /** 엄폐 칸을 버린 뒤 새 칸을 다시 찾기까지 기다리는 시간(틱). 같은 지형에서 실패를 반복하지 않게 한다. */
    private static final int ABANDON_DELAY = 20;
    /** 대상을 이 시간 넘게 보지 못하면 엄폐를 그만두고 다가간다. 대상이 사라졌는데 숨어만 있지 않게 한다. */
    private static final int MAX_UNSEEN_TICKS = 300;
    /**
     * 대상을 오래 보지 못해 엄폐를 그만둔 뒤 다시 엄폐하기까지 기다리는 시간(틱).
     * 바닐라 대상 지정이 대상을 버리는 3초보다 길게 두어, 정말 사라진 대상은 바닐라 규칙대로 잊게 한다.
     */
    private static final int UNSEEN_GIVE_UP_DELAY = 100;
    /** 대상 지정이 엄폐 중 대상을 놓지 않도록 붙잡아 두는 여유 시간(틱). 대상 지정은 두 틱마다 검사한다. */
    private static final int TARGET_HOLD_TICKS = 3;
    private static final double ARRIVE_DISTANCE_SQR = 0.4 * 0.4;
    private static final double NEAR_DISTANCE_SQR = 1.0;

    private final PathfinderMob mob;
    private final double speed;
    @Nullable
    private CoverSpot spot;
    private Phase phase = Phase.MOVE_TO_COVER;
    private int phaseTicks;
    private int hideTicks;
    private int validateCooldown;
    private int unseenTicks;
    private boolean seenDuringPeek;
    private long nextSearchTick = Long.MIN_VALUE;

    public CoverTactics(PathfinderMob mob, double speed) {
        this.mob = mob;
        this.speed = speed;
    }

    public boolean hasSpot() {
        return this.spot != null;
    }

    public Phase phase() {
        return this.phase;
    }

    /** 엄폐를 완전히 그만둔다. 다음 탐색은 바로 할 수 있다. */
    public void reset() {
        this.spot = null;
        this.unseenTicks = 0;
        this.nextSearchTick = Long.MIN_VALUE;
        release();
    }

    /**
     * 엄폐 칸이 없으면 찾는다. 탐색 간격과 서버 전체의 탐색 몫을 지킨다.
     *
     * @param range 노출 칸에서 대상까지 허용하는 최대 거리
     * @return 엄폐 칸을 가지고 있으면 true
     */
    public boolean trySearch(LivingEntity threat, double range) {
        if (this.spot != null) {
            return true;
        }
        long gameTime = this.mob.level().getGameTime();
        // 공중에서는 경로를 만들지 못해 탐색이 헛돈다. 땅에 닿은 뒤에 찾는다.
        if (gameTime < this.nextSearchTick || !this.mob.onGround()) {
            return false;
        }
        if (!CoverFinder.tryReserveSearch(gameTime)) {
            // 이번 틱 몫이 모자라면 몬스터마다 조금씩 어긋나게 다시 시도한다.
            this.nextSearchTick = gameTime + 1 + this.mob.getRandom().nextInt(4);
            return false;
        }
        CoverSpot found = CoverFinder.find(this.mob, threat, range);
        if (found == null) {
            this.nextSearchTick = gameTime + RESEARCH_DELAY;
            return false;
        }
        this.spot = found;
        this.phase = Phase.MOVE_TO_COVER;
        this.phaseTicks = 0;
        this.validateCooldown = VALIDATE_INTERVAL;
        this.mob.getNavigation().moveTo(found.path(), this.speed);
        return true;
    }

    /**
     * 엄폐 행동을 한 틱 진행한다. {@link #hasSpot()}이 true일 때만 부른다.
     *
     * @param readyToPeek  무기가 준비되어 나가서 쏠 수 있는지
     * @param peekFinished 노출 칸에서 사격을 마쳤는지
     * @return 엄폐 칸을 잃었으면 false. 호출한 Goal은 이번 틱에 엄폐 없이 싸운다.
     */
    public boolean tick(LivingEntity threat, boolean readyToPeek, boolean peekFinished) {
        CoverSpot current = this.spot;
        if (current == null) {
            return false;
        }
        long gameTime = this.mob.level().getGameTime();
        boolean sees = this.mob.getSensing().hasLineOfSight(threat);
        this.unseenTicks = sees ? 0 : this.unseenTicks + 1;
        if (this.unseenTicks > MAX_UNSEEN_TICKS) {
            this.unseenTicks = 0;
            return abandon(gameTime + UNSEEN_GIVE_UP_DELAY);
        }
        // 노출 칸은 대상이 보여야 하는 칸이라 숨으러 가거나 숨어 있는 동안에만 엄폐 칸이 드러났는지 본다.
        if (this.phase != Phase.PEEK && --this.validateCooldown <= 0) {
            this.validateCooldown = VALIDATE_INTERVAL;
            if (!CoverFinder.isProtected(this.mob, current.cover(), threat.getEyePosition())) {
                // 대상이 돌아 들어왔다. 지체 없이 새 엄폐 칸을 찾게 한다.
                return abandon(gameTime);
            }
        }
        if (this.mob instanceof CoverCombatant combatant) {
            combatant.tacz$holdTarget(threat, gameTime + TARGET_HOLD_TICKS);
        }
        this.mob.getLookControl().setLookAt(threat, 30.0f, 30.0f);
        this.phaseTicks++;
        PathNavigation navigation = this.mob.getNavigation();
        switch (this.phase) {
            case MOVE_TO_COVER -> {
                if (isAt(current.cover())) {
                    navigation.stop();
                    this.phase = Phase.HIDE;
                    this.phaseTicks = 0;
                    this.hideTicks = MIN_HIDE_TICKS + this.mob.getRandom().nextInt(MAX_HIDE_TICKS - MIN_HIDE_TICKS + 1);
                } else if (this.phaseTicks > MOVE_TIMEOUT_TICKS || (navigation.isDone() && !moveTo(current.cover()))) {
                    return abandon(gameTime + ABANDON_DELAY);
                }
            }
            case HIDE -> {
                if (!navigation.isDone()) {
                    navigation.stop();
                }
                if (this.phaseTicks >= this.hideTicks && readyToPeek) {
                    this.phase = Phase.PEEK;
                    this.phaseTicks = 0;
                    this.seenDuringPeek = false;
                    moveTo(current.peek());
                }
            }
            case PEEK -> {
                if (peekFinished) {
                    returnToCover(current);
                } else if (sees) {
                    // 대상이 보이면 그 자리에서 멈춰 쏜다. 몸을 덜 내놓는다.
                    this.seenDuringPeek = true;
                    navigation.stop();
                } else if (this.phaseTicks > PEEK_TIMEOUT_TICKS) {
                    if (!this.seenDuringPeek) {
                        return abandon(gameTime + ABANDON_DELAY);
                    }
                    returnToCover(current);
                } else if (navigation.isDone() && !isAt(current.peek())) {
                    moveTo(current.peek());
                }
            }
        }
        return true;
    }

    private void returnToCover(CoverSpot current) {
        this.phase = Phase.MOVE_TO_COVER;
        this.phaseTicks = 0;
        moveTo(current.cover());
    }

    private boolean moveTo(BlockPos pos) {
        return this.mob.getNavigation().moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, this.speed);
    }

    /** 칸 한가운데에 섰는지. 경로가 끝났으면 칸 안에만 들어와도 도착으로 본다. */
    private boolean isAt(BlockPos pos) {
        Vec3 position = this.mob.position();
        Vec3 center = Vec3.atBottomCenterOf(pos);
        if (Math.abs(position.y - center.y) > 1.0) {
            return false;
        }
        double dx = position.x - center.x;
        double dz = position.z - center.z;
        double horizontalSqr = dx * dx + dz * dz;
        return horizontalSqr < ARRIVE_DISTANCE_SQR
                || (this.mob.getNavigation().isDone() && horizontalSqr < NEAR_DISTANCE_SQR);
    }

    /** 엄폐 칸을 버린다. 지정한 틱이 되기 전에는 다시 찾지 않는다. */
    private boolean abandon(long nextSearchTick) {
        this.spot = null;
        this.nextSearchTick = nextSearchTick;
        this.mob.getNavigation().stop();
        release();
        return false;
    }

    private void release() {
        if (this.mob instanceof CoverCombatant combatant) {
            combatant.tacz$releaseTarget();
        }
    }
}
