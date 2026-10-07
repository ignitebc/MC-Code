package com.tacz.guns.entity.ai;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.List;

/**
 * 엄폐 칸으로 달려가 숨었다가 옆으로 살짝 몸을 내밀어 쏘고 다시 숨는 흐름을 관리한다. 총과 활이 함께 쓴다.
 * <p>
 * 언제 나와도 되는지(장전·활시위)와 언제 사격을 마쳤는지는 무기마다 달라서 호출하는 Goal이 알려 준다.
 * 한 번 숨은 뒤에는 숨은 자리가 드러나도 새 엄폐 칸으로 달아나지 않고 그 자리에서 맞서 쏜다(교전).
 * 다가오는 대상을 피해 엄폐 칸을 옮겨 다니다 대상 주위를 맴도는 일을 막기 위해서다.
 * 교전 중에는 엄폐 칸을 찾지 않으므로 호출한 Goal이 엄폐 없이 싸운다.
 * <p>
 * 사람처럼 보이도록 숨는 시간과 내미는 쪽을 매번 바꾸고, 맞으면 움츠러들거나 자리를 옮기며, 총알이 스쳐 가면 나오지 않는다.
 * 무리에서는 일부가 측면으로 돌고, 아무도 쏘지 않는 공백이 생기면 일찍 나와 메운다.
 * 보이지 않는 대상은 지금 위치 대신 마지막으로 본 위치를 기준으로 숨을 곳을 고른다.
 */
public final class CoverTactics {
    public enum Phase {
        /** 엄폐 칸으로 달려간다. */
        MOVE_TO_COVER,
        /** 엄폐 칸 한가운데에서 숨는다. */
        HIDE,
        /** 옆으로 살짝 몸을 내밀어 쏜다. */
        PEEK,
        /** 쏜 뒤 엄폐 칸 한가운데로 몸을 들인다. */
        RETURN
    }

    /** 엄폐 칸에서 숨어 있는 시간(틱). 1.5~3초 사이에서 매번 다르게 정해 나오는 박자를 읽기 어렵게 한다. */
    private static final int MIN_HIDE_TICKS = 30;
    private static final int MAX_HIDE_TICKS = 60;
    /** 쏘고 들어간 뒤 가끔은 짧게만 숨었다가 바로 다시 나온다. 그 확률과 숨는 시간(틱) */
    private static final float QUICK_REPEEK_CHANCE = 0.25f;
    private static final int MIN_QUICK_HIDE_TICKS = 8;
    private static final int MAX_QUICK_HIDE_TICKS = 14;
    /** 내밀었다가 맞거나 총알이 스쳐 움츠러든 뒤 숨는 시간(틱) */
    private static final int MIN_FLINCH_HIDE_TICKS = 50;
    private static final int MAX_FLINCH_HIDE_TICKS = 80;
    /** 체력이 이 비율 아래면 부상으로 보고 대상에게서 먼 엄폐 칸으로 물러나며 더 오래 숨는다. */
    private static final float WOUNDED_HEALTH_RATIO = 0.35f;
    private static final double WOUNDED_HIDE_FACTOR = 1.5;
    /** 무리 중 아무도 쏘지 않으면 이 시간(틱)만 숨고 나와 사격 공백을 메운다. */
    private static final int COVER_FIRE_MIN_HIDE_TICKS = 20;
    /** 무리가 쏘고 있는지 다시 확인하는 간격(틱). 주변 몬스터를 훑는 비용이 있어 매 틱 보지 않는다. */
    private static final int SQUAD_CHECK_INTERVAL = 5;
    /** 양쪽으로 내밀 수 있을 때 지난번과 다른 쪽으로 나올 확률 */
    private static final float PEEK_SWITCH_CHANCE = 0.7f;
    /** 엄폐 칸으로 옮길 때 달리는 속도 배율. 몸을 내밀고 들일 때는 걷는다. */
    private static final double SPRINT_SPEED_FACTOR = 1.35;
    /**
     * 몸을 내민 뒤 대상을 보지 못하고 기다리는 최대 시간(틱).
     * 한 번도 보지 못했으면 대상이 자리를 옮긴 것이라 내밀 위치를 다시 잡고, 쏘던 중 대상이 숨었으면 다시 숨는다.
     */
    private static final int PEEK_TIMEOUT_TICKS = 30;
    /** 엄폐 칸까지 이 시간 안에 닿지 못하면 엄폐 칸을 버린다. */
    private static final int MOVE_TIMEOUT_TICKS = 100;
    /** 엄폐 칸 한가운데로 몸을 들이는 데 이 시간이 넘게 걸리면 그 자리에서 숨는다. 숨는 동안에도 계속 가운데로 붙는다. */
    private static final int RETURN_TIMEOUT_TICKS = 30;
    /** 숨기 시작한 뒤 이 시간(틱)은 한가운데로 자리를 잡는 중이라, 대상이 보이거나 맞아도 엄폐 칸을 탓하지 않는다. */
    private static final int SETTLE_TICKS = 10;
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
    /** 교전 중 대상을 이 시간(틱) 넘게 보지 못해야 교전을 끝내고 다시 숨을 곳을 찾는다. */
    private static final int ENGAGE_LOST_SIGHT_TICKS = 20;
    /**
     * 교전 중 대상이 이 거리(칸) 안에 있으면 숨을 곳을 다시 찾지 않고 그 자리에서 싸운다.
     * 가까운 대상을 두고 자리를 옮기면 대상 주위를 맴돌게 된다.
     */
    private static final double NO_RELOCATE_DISTANCE = 8.0;
    /**
     * 같은 대상과 이 시간(틱) 안에 다시 싸우면 앞선 싸움을 이어 간다.
     * 잠깐 근접전으로 바뀌었다가 돌아왔을 때 처음부터 다시 숨으러 달아나지 않게 한다.
     */
    private static final int FIGHT_MEMORY_TICKS = 200;
    /** 이 틱 안에 맞은 것만 새로 맞은 것으로 본다. Goal이 시작되기 전에 맞은 일에 늦게 반응하지 않게 한다. */
    private static final int RECENT_HURT_TICKS = 2;
    /** 보이지 않는 곳에서 맞았을 때 쏜 쪽을 짐작하는 오차. 거리에 비례하되 이 값(칸)을 넘지 않는다. */
    private static final double HIT_GUESS_ERROR_RATIO = 0.08;
    private static final double MAX_HIT_GUESS_ERROR = 6.0;
    /** 대상 지정이 엄폐 중 대상을 놓지 않도록 붙잡아 두는 여유 시간(틱). 대상 지정은 두 틱마다 검사한다. */
    private static final int TARGET_HOLD_TICKS = 3;
    private static final double ARRIVE_DISTANCE_SQR = 0.4 * 0.4;
    private static final double NEAR_DISTANCE_SQR = 1.0;
    /** 몸을 내밀거나 들일 때 목표 위치에 이만큼 가까워지면 멈춘다. 멈춘 뒤 미끄러지는 거리까지 감안한 값이다. */
    private static final double LEAN_ARRIVE_DISTANCE_SQR = 0.15 * 0.15;

    private final PathfinderMob mob;
    private final CoverCombatant combatant;
    private final double speed;
    @Nullable
    private CoverSpot spot;
    private Phase phase = Phase.MOVE_TO_COVER;
    private int phaseTicks;
    private int hideTicks;
    private int validateCooldown;
    private int unseenTicks;
    private boolean seenDuringPeek;
    /** 이번에 몸을 내밀며 내밀 위치를 이미 한 번 다시 잡았는지. 좌우를 번갈아 헤매지 않게 한 번만 허용한다. */
    private boolean peekRetargeted;
    /** 지금 내미는 쪽과 지난번에 내밀었던 쪽(엄폐 칸의 내밀 위치 목록 번호). 지난번 기록이 없으면 -1 */
    private int peekIndex;
    private int lastPeekIndex = -1;
    private long nextSearchTick = Long.MIN_VALUE;
    /** 내밀 위치를 다시 잡을 때 쓰는 무기 사거리. 엄폐 칸을 찾을 때 받은 값이다. */
    private double range;
    /** 지금 싸우는 대상과 마지막으로 싸운 틱. 대상이 바뀌거나 오래 쉬었으면 새 싸움으로 본다. */
    private int threatId = -1;
    private long lastFightTick = Long.MIN_VALUE;
    /** 이번 싸움에서 한 번이라도 엄폐 칸에 숨었는지. 숨은 뒤로는 드러나도 달아나지 않고 맞서 쏜다. */
    private boolean hiddenOnce;
    /** 숨은 자리가 드러나 그 자리에서 맞서 쏘는 중인지. 이 동안에는 숨을 곳을 찾지 않는다. */
    private boolean engaging;
    private long lastEngageSightTick;
    /** 부상으로 물러나는 중인지. 체력이 다시 차지 않으므로 싸움이 끝날 때까지 유지한다. */
    private boolean wounded;
    /** 내밀었다가 맞거나 총알이 스쳐 움츠러들었는지. 다음 숨기를 길게 한다. */
    private boolean flinched;
    private int lastHandledHurtTimestamp;
    private int squadCheckCooldown;
    private boolean squadSilent;

    public CoverTactics(PathfinderMob mob, double speed) {
        this.mob = mob;
        this.combatant = (CoverCombatant) mob;
        this.speed = speed;
    }

    public boolean hasSpot() {
        return this.spot != null;
    }

    public Phase phase() {
        return this.phase;
    }

    /**
     * 엄폐 칸을 내려놓는다. 다음 탐색은 바로 할 수 있다.
     * 싸움의 기억(숨은 적이 있는지, 교전 중인지, 부상인지)은 남겨 두고, 대상이 바뀌거나 오래 쉬었을 때만 지운다.
     */
    public void reset() {
        dropSpot();
        this.unseenTicks = 0;
        this.nextSearchTick = Long.MIN_VALUE;
    }

    /**
     * 매 틱 가장 먼저 부른다. 대상이 보이면 그 위치를 기억하고, 대상에게 새로 맞았으면 반응한다.
     */
    public void observe(LivingEntity threat) {
        long gameTime = this.mob.level().getGameTime();
        trackFight(threat, gameTime);
        if (this.mob.getSensing().hasLineOfSight(threat)) {
            this.combatant.tacz$rememberThreat(threat, threat.position());
        }
        int hurtTimestamp = this.mob.getLastHurtByMobTimestamp();
        boolean newHurt = hurtTimestamp != this.lastHandledHurtTimestamp
                && this.mob.tickCount - hurtTimestamp <= RECENT_HURT_TICKS;
        this.lastHandledHurtTimestamp = hurtTimestamp;
        if (newHurt && this.mob.getLastHurtByMob() == threat) {
            onHurtByThreat(threat, gameTime);
        }
    }

    /**
     * 엄폐 칸이 없으면 찾는다. 탐색 간격과 서버 전체의 탐색 몫을 지키고, 교전 중에는 찾지 않는다.
     *
     * @param range 내민 위치에서 대상까지 허용하는 최대 거리
     * @return 엄폐 칸을 가지고 있으면 true
     */
    public boolean trySearch(LivingEntity threat, double range) {
        long gameTime = this.mob.level().getGameTime();
        trackFight(threat, gameTime);
        if (this.spot != null) {
            return true;
        }
        if (isHoldingGround(threat, gameTime)) {
            return false;
        }
        // 공중에서는 경로를 만들지 못해 탐색이 헛돈다. 땅에 닿은 뒤에 찾는다.
        if (gameTime < this.nextSearchTick || !this.mob.onGround()) {
            return false;
        }
        if (!CoverFinder.tryReserveSearch(gameTime)) {
            // 이번 틱 몫이 모자라면 몬스터마다 조금씩 어긋나게 다시 시도한다.
            this.nextSearchTick = gameTime + 1 + this.mob.getRandom().nextInt(4);
            return false;
        }
        CoverSpot found = CoverFinder.find(this.mob, buildSearch(threat, range));
        if (found == null) {
            this.nextSearchTick = gameTime + RESEARCH_DELAY;
            return false;
        }
        this.range = range;
        takeSpot(found);
        this.lastPeekIndex = -1;
        this.phase = Phase.MOVE_TO_COVER;
        this.phaseTicks = 0;
        this.validateCooldown = VALIDATE_INTERVAL;
        this.mob.getNavigation().moveTo(found.path(), sprintSpeed());
        return true;
    }

    /**
     * 엄폐 행동을 한 틱 진행한다. {@link #hasSpot()}이 true일 때만 부른다.
     *
     * @param readyToPeek  무기가 준비되어 나가서 쏠 수 있는지
     * @param peekFinished 몸을 내민 채 사격을 마쳤는지
     * @return 엄폐 칸을 잃었으면 false. 호출한 Goal은 이번 틱에 엄폐 없이 싸운다.
     */
    public boolean tick(LivingEntity threat, boolean readyToPeek, boolean peekFinished) {
        long gameTime = this.mob.level().getGameTime();
        trackFight(threat, gameTime);
        CoverSpot current = this.spot;
        if (current == null) {
            return false;
        }
        boolean sees = this.mob.getSensing().hasLineOfSight(threat);
        this.unseenTicks = sees ? 0 : this.unseenTicks + 1;
        if (this.unseenTicks > MAX_UNSEEN_TICKS) {
            this.unseenTicks = 0;
            return abandon(gameTime + UNSEEN_GIVE_UP_DELAY);
        }
        if (isExposed(current, threat, sees)) {
            if (this.hiddenOnce) {
                // 이미 한 번 숨었다. 새 엄폐 칸으로 달아나지 않고 그 자리에서 맞서 쏜다.
                return engage(gameTime);
            }
            // 처음 숨으러 가는 길에 엄폐 칸이 드러났다. 지체 없이 새 엄폐 칸을 찾게 한다.
            return abandon(gameTime);
        }
        this.combatant.tacz$holdTarget(threat, gameTime + TARGET_HOLD_TICKS);
        lookAtThreat(threat, sees);
        this.phaseTicks++;
        return switch (this.phase) {
            case MOVE_TO_COVER -> tickMoveToCover(current, gameTime);
            case HIDE -> tickHide(current, threat, readyToPeek, gameTime);
            case PEEK -> tickPeek(current, threat, sees, peekFinished, gameTime);
            case RETURN -> tickReturn(current);
        };
    }

    private boolean tickMoveToCover(CoverSpot current, long gameTime) {
        PathNavigation navigation = this.mob.getNavigation();
        if (isAt(current.cover())) {
            navigation.stop();
            startHiding(false);
            return true;
        }
        if (this.phaseTicks > MOVE_TIMEOUT_TICKS) {
            return abandon(gameTime + ABANDON_DELAY);
        }
        if (navigation.isDone() && !moveTo(current.cover())) {
            return abandon(gameTime + ABANDON_DELAY);
        }
        return true;
    }

    private boolean tickHide(CoverSpot current, LivingEntity threat, boolean readyToPeek, long gameTime) {
        PathNavigation navigation = this.mob.getNavigation();
        if (!navigation.isDone()) {
            navigation.stop();
        }
        // 길찾기는 칸 가운데 근처에서 멈추므로 몸이 칸 밖으로 비어져 나올 수 있다. 한가운데로 붙어 몸 전체를 가린다.
        leanTo(current.coverCenter());
        if (!readyToPeek || this.combatant.tacz$isSuppressed(gameTime)) {
            // 장전 중이거나 총알이 스쳐 가는 동안에는 나오지 않는다.
            return true;
        }
        boolean timeUp = this.phaseTicks >= this.hideTicks;
        boolean fillGap = this.phaseTicks >= COVER_FIRE_MIN_HIDE_TICKS && isSquadSilent(threat, gameTime);
        if (timeUp || fillGap) {
            startPeek(current);
        }
        return true;
    }

    private boolean tickPeek(CoverSpot current, LivingEntity threat, boolean sees, boolean peekFinished, long gameTime) {
        if (peekFinished) {
            startReturning();
            return true;
        }
        if (this.combatant.tacz$isSuppressed(gameTime)) {
            // 내밀었는데 총알이 스쳤다. 바로 몸을 들이고 평소보다 오래 숨는다.
            this.flinched = true;
            startReturning();
            return true;
        }
        if (sees) {
            // 대상이 보이면 더 나가지 않고 그 자리에서 쏜다. 몸을 덜 내놓는다.
            this.seenDuringPeek = true;
            this.combatant.tacz$markFiring(gameTime);
            this.mob.getMoveControl().setWait();
            return true;
        }
        if (this.phaseTicks <= PEEK_TIMEOUT_TICKS) {
            leanTo(currentPeek(current));
            return true;
        }
        if (this.seenDuringPeek) {
            // 쏘던 중에 대상이 숨었다. 다시 숨어서 나올 때를 기다린다.
            startReturning();
            return true;
        }
        // 한 번도 보지 못했다. 대상이 자리를 옮겼으니 같은 엄폐 칸에서 지금 대상이 보이는 쪽으로 다시 내민다.
        if (retargetPeek(current, threat)) {
            return true;
        }
        return abandon(gameTime);
    }

    private boolean tickReturn(CoverSpot current) {
        boolean arrived = leanTo(current.coverCenter());
        if (arrived || this.phaseTicks > RETURN_TIMEOUT_TICKS) {
            startHiding(true);
        }
        return true;
    }

    /**
     * 숨기 시작한다. 숨는 시간은 매번 다르게 정하고, 쏘고 들어온 뒤에는 가끔 짧게만 숨는다.
     * 움츠러든 뒤에는 길게, 부상이면 더 길게 숨는다.
     */
    private void startHiding(boolean afterPeek) {
        this.phase = Phase.HIDE;
        this.phaseTicks = 0;
        this.hiddenOnce = true;
        RandomSource random = this.mob.getRandom();
        int minTicks = MIN_HIDE_TICKS;
        int maxTicks = MAX_HIDE_TICKS;
        if (this.flinched) {
            minTicks = MIN_FLINCH_HIDE_TICKS;
            maxTicks = MAX_FLINCH_HIDE_TICKS;
            this.flinched = false;
        } else if (afterPeek && random.nextFloat() < QUICK_REPEEK_CHANCE) {
            minTicks = MIN_QUICK_HIDE_TICKS;
            maxTicks = MAX_QUICK_HIDE_TICKS;
        }
        int ticks = minTicks + random.nextInt(maxTicks - minTicks + 1);
        if (this.wounded) {
            ticks = (int) Math.round(ticks * WOUNDED_HIDE_FACTOR);
        }
        this.hideTicks = ticks;
    }

    private void startPeek(CoverSpot current) {
        this.phase = Phase.PEEK;
        this.phaseTicks = 0;
        this.seenDuringPeek = false;
        this.peekRetargeted = false;
        this.peekIndex = choosePeekSide(current);
        this.lastPeekIndex = this.peekIndex;
    }

    private void startReturning() {
        this.phase = Phase.RETURN;
        this.phaseTicks = 0;
    }

    /** 양쪽으로 내밀 수 있으면 대개 지난번과 다른 쪽을 고른다. 매번 같은 곳으로 나오면 조준을 걸어 두고 기다리면 그만이다. */
    private int choosePeekSide(CoverSpot current) {
        int sideCount = current.peeks().size();
        if (sideCount < 2) {
            return 0;
        }
        RandomSource random = this.mob.getRandom();
        if (this.lastPeekIndex < 0 || this.lastPeekIndex >= sideCount) {
            return random.nextInt(sideCount);
        }
        if (random.nextFloat() < PEEK_SWITCH_CHANCE) {
            return 1 - this.lastPeekIndex;
        }
        return this.lastPeekIndex;
    }

    private Vec3 currentPeek(CoverSpot current) {
        int index = Math.min(this.peekIndex, current.peeks().size() - 1);
        return current.peeks().get(index);
    }

    /** 무리 중 아무도 쏘지 않는지. 혼자면 공백을 메울 동료가 없으므로 false다. */
    private boolean isSquadSilent(LivingEntity threat, long gameTime) {
        if (--this.squadCheckCooldown <= 0) {
            this.squadCheckCooldown = SQUAD_CHECK_INTERVAL;
            CombatSquad squad = CombatSquad.scan(this.mob, threat);
            this.squadSilent = squad.hasMates() && !squad.anyoneFiring(gameTime);
        }
        return this.squadSilent;
    }

    /**
     * 대상에게 맞았다. 내밀었다 맞으면 움츠러들어 들어가고, 자리를 잡고 숨었는데 맞으면 엄폐 칸이 소용없으니 옮긴다.
     * 엄폐 없이 싸우다 맞으면, 대상이 멀거나 부상일 때 숨을 곳을 찾는다.
     */
    private void onHurtByThreat(LivingEntity threat, long gameTime) {
        if (!this.mob.getSensing().hasLineOfSight(threat)) {
            this.combatant.tacz$rememberThreat(threat, guessThreatPosition(threat));
        }
        this.wounded = this.mob.getHealth() < this.mob.getMaxHealth() * WOUNDED_HEALTH_RATIO;
        CoverSpot current = this.spot;
        if (current == null) {
            boolean far = this.mob.distanceToSqr(threat) >= NO_RELOCATE_DISTANCE * NO_RELOCATE_DISTANCE;
            if (far || this.wounded) {
                this.engaging = false;
                this.nextSearchTick = gameTime;
            }
            return;
        }
        if (this.phase == Phase.PEEK) {
            this.flinched = true;
            startReturning();
            return;
        }
        if (this.phase == Phase.HIDE && this.phaseTicks >= SETTLE_TICKS) {
            abandon(gameTime);
        }
    }

    /** 보이지 않는 곳에서 맞았을 때 짐작하는 쏜 쪽 위치. 멀수록 크게 빗나간다. */
    private Vec3 guessThreatPosition(LivingEntity threat) {
        double distance = Math.sqrt(this.mob.distanceToSqr(threat));
        double errorRadius = Math.min(MAX_HIT_GUESS_ERROR, distance * HIT_GUESS_ERROR_RATIO);
        RandomSource random = this.mob.getRandom();
        double angle = random.nextDouble() * Math.PI * 2;
        double offset = random.nextDouble() * errorRadius;
        return threat.position().add(Math.cos(angle) * offset, 0.0, Math.sin(angle) * offset);
    }

    /**
     * 엄폐 칸을 찾을 조건을 만든다. 무리가 셋 이상이면 일부가 측면 역할을 맡아 정면을 피한 칸을 넓게 찾는다.
     * 부상이면 측면으로 돌지 않고 대상에게서 먼 칸으로 물러난다.
     */
    private CoverFinder.Search buildSearch(LivingEntity threat, double range) {
        Vec3 feet = threatFeet(threat);
        Vec3 eye = feet.add(0, threat.getEyeHeight(), 0);
        CombatSquad squad = CombatSquad.scan(this.mob, threat);
        boolean needsFlanker = squad.flankerCount() < CombatSquad.neededFlankers(squad.size());
        if (!this.combatant.tacz$isFlanker() && needsFlanker) {
            this.combatant.tacz$setFlanker(true);
        }
        Vec3 flankFront = null;
        if (this.combatant.tacz$isFlanker() && !this.wounded) {
            flankFront = squad.frontDirection(feet);
        }
        int radius = CoverFinder.SEARCH_RADIUS;
        if (flankFront != null) {
            radius = CoverFinder.FLANK_SEARCH_RADIUS;
        }
        return new CoverFinder.Search(eye, feet, range, radius, flankFront, this.wounded);
    }

    /** 대상 발 위치. 보이면 지금 위치를, 보이지 않으면 마지막으로 본(짐작한) 위치를 쓴다. */
    private Vec3 threatFeet(LivingEntity threat) {
        if (this.mob.getSensing().hasLineOfSight(threat)) {
            return threat.position();
        }
        Vec3 known = this.combatant.tacz$lastKnownThreatPos(threat);
        if (known == null) {
            return threat.position();
        }
        return known;
    }

    private Vec3 threatEye(LivingEntity threat) {
        return threatFeet(threat).add(0, threat.getEyeHeight(), 0);
    }

    /** 보이면 대상을, 보이지 않으면 마지막으로 본 곳을 바라본다. 벽 너머 대상을 따라 고개를 돌리지 않게 한다. */
    private void lookAtThreat(LivingEntity threat, boolean sees) {
        if (sees) {
            this.mob.getLookControl().setLookAt(threat, 30.0f, 30.0f);
            return;
        }
        Vec3 eye = threatEye(threat);
        this.mob.getLookControl().setLookAt(eye.x, eye.y, eye.z);
    }

    /**
     * 숨은 자리가 대상에게 드러났는지.
     * <p>
     * 몸을 내민 동안에는 일부러 보이는 것이고, 몸을 들이는 동안에는 아직 보이는 것이 당연하므로 시야로는 따지지 않는다.
     * 자리를 잡고 숨었는데 대상이 보이면 대상이 돌아 들어온 것이다.
     * 한 번 숨은 뒤로는 새 엄폐 칸으로 가는 길에 대상과 마주쳐도 드러난 것으로 본다.
     * 탄이 닿는지는 대상이 보이지 않으면 마지막으로 본 위치 기준으로 따진다.
     */
    private boolean isExposed(CoverSpot current, LivingEntity threat, boolean sees) {
        if (this.phase == Phase.PEEK) {
            return false;
        }
        boolean settledInCover = this.phase == Phase.HIDE && this.phaseTicks >= SETTLE_TICKS;
        boolean metOnTheWay = this.phase == Phase.MOVE_TO_COVER && this.hiddenOnce;
        if (sees && (settledInCover || metOnTheWay)) {
            return true;
        }
        if (--this.validateCooldown > 0) {
            return false;
        }
        this.validateCooldown = VALIDATE_INTERVAL;
        return !CoverFinder.isProtected(this.mob, current.cover(), threatEye(threat));
    }

    /** 같은 엄폐 칸에서 지금 대상이 보이는 내밀 위치를 다시 찾는다. 찾으면 그 위치로 다시 내민다. */
    private boolean retargetPeek(CoverSpot current, LivingEntity threat) {
        if (this.peekRetargeted) {
            return false;
        }
        this.peekRetargeted = true;
        List<Vec3> peeks = CoverFinder.findPeeks(this.mob, current.cover(), threatEye(threat), this.range * this.range);
        Vec3 failedPeek = currentPeek(current);
        for (int i = 0; i < peeks.size(); i++) {
            if (peeks.get(i).distanceToSqr(failedPeek) < LEAN_ARRIVE_DISTANCE_SQR) {
                continue;
            }
            takeSpot(new CoverSpot(current.cover(), peeks, current.path()));
            this.peekIndex = i;
            this.lastPeekIndex = i;
            this.phaseTicks = 0;
            return true;
        }
        return false;
    }

    /**
     * 교전 중이면 숨을 곳을 찾지 않고 그 자리에서 싸우게 한다.
     * 대상을 잠시 놓쳤고 거리도 떨어져 있을 때만 교전을 끝내고 다시 숨을 곳을 찾는다. 부상이면 맞서지 않고 물러난다.
     */
    private boolean isHoldingGround(LivingEntity threat, long gameTime) {
        if (!this.engaging) {
            return false;
        }
        if (this.wounded) {
            this.engaging = false;
            return false;
        }
        if (this.mob.getSensing().hasLineOfSight(threat)) {
            this.lastEngageSightTick = gameTime;
        }
        boolean recentlySeen = gameTime - this.lastEngageSightTick <= ENGAGE_LOST_SIGHT_TICKS;
        boolean close = this.mob.distanceToSqr(threat) < NO_RELOCATE_DISTANCE * NO_RELOCATE_DISTANCE;
        if (recentlySeen || close) {
            return true;
        }
        this.engaging = false;
        return false;
    }

    /**
     * 대상이 바뀌었거나 한동안 싸우지 않았으면 앞선 싸움의 기억을 버린다. 새 싸움에서는 처음처럼 먼저 숨는다.
     */
    private void trackFight(LivingEntity threat, long gameTime) {
        boolean sameFight = threat.getId() == this.threatId && gameTime - this.lastFightTick <= FIGHT_MEMORY_TICKS;
        this.lastFightTick = gameTime;
        if (sameFight) {
            return;
        }
        dropSpot();
        this.threatId = threat.getId();
        this.unseenTicks = 0;
        this.hiddenOnce = false;
        this.engaging = false;
        this.wounded = false;
        this.flinched = false;
        this.lastPeekIndex = -1;
        this.combatant.tacz$setFlanker(false);
    }

    /**
     * 짧은 거리를 길찾기 없이 곧장 움직인다. 칸 단위로 움직이는 길찾기로는 반 칸만 몸을 내밀 수 없다.
     *
     * @return 이미 도착했으면 true
     */
    private boolean leanTo(Vec3 position) {
        double dx = position.x - this.mob.getX();
        double dz = position.z - this.mob.getZ();
        if (dx * dx + dz * dz <= LEAN_ARRIVE_DISTANCE_SQR) {
            return true;
        }
        this.mob.getMoveControl().setWantedPosition(position.x, position.y, position.z, this.speed);
        return false;
    }

    private boolean moveTo(BlockPos pos) {
        return this.mob.getNavigation().moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, sprintSpeed());
    }

    /** 엄폐 칸 사이를 옮길 때의 속도. 드러난 채 걸어가면 좋은 표적이 되므로 달린다. */
    private double sprintSpeed() {
        return this.speed * SPRINT_SPEED_FACTOR;
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
        dropSpot();
        this.nextSearchTick = nextSearchTick;
        this.mob.getNavigation().stop();
        return false;
    }

    /** 숨은 자리가 드러났다. 새 엄폐 칸으로 달아나지 않고 그 자리에서 맞서 쏜다. 대상을 놓쳐야 다시 숨을 곳을 찾는다. */
    private boolean engage(long gameTime) {
        dropSpot();
        this.engaging = true;
        this.lastEngageSightTick = gameTime;
        this.mob.getNavigation().stop();
        this.mob.getMoveControl().setWait();
        return false;
    }

    private void takeSpot(CoverSpot found) {
        this.spot = found;
        this.combatant.tacz$setCoverPos(found.cover());
    }

    /** 엄폐 칸과 붙잡아 둔 대상을 함께 내려놓는다. 다른 몬스터가 이 칸을 다시 고를 수 있다. */
    private void dropSpot() {
        this.spot = null;
        this.combatant.tacz$setCoverPos(null);
        this.combatant.tacz$releaseTarget();
    }
}
