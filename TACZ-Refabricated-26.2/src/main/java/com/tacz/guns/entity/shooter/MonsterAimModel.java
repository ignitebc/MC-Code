package com.tacz.guns.entity.shooter;

import com.tacz.guns.api.item.gun.FireMode;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;

/**
 * 몬스터가 사람처럼 쏘게 하는 반응·조준·점사 모델.
 * <p>
 * 몬스터 총에는 반동이 없고 매 발 대상 몸통 정중앙을 계산해 쏘므로, 이 모델이 없으면 정조준 직후부터 거의 모든 탄이 맞는다.
 * 대상을 발견하면 잠깐 반응한 뒤 쏘기 시작하고, 조준 오차는 처음에 크다가 계속 겨눌수록 줄어든다.
 * 대상이 움직이거나 자신이 움직이거나 제압당하면 오차가 커진다. 연발 총은 몇 발씩 끊어 쏘고 단발 총은 사람 손가락 속도로 쏜다.
 * 엄폐 칸에서 다시 몸을 내밀었을 때 대상이 아까 그 자리에 있으면, 미리 겨눠 둔 셈이라 반응과 오차를 줄인다.
 * 저격총과 지정사수소총은 {@link Profile}로 반응과 다음 발까지의 간격을 조금씩 늘려 한 발씩 끊어 쏜다.
 */
public final class MonsterAimModel {
    /**
     * 총 종류별 사격 박자(틱). 조준 오차는 종류와 관계없이 같다.
     * <p>
     * 저격 계열은 연발 모드여도 한 발씩 끊어 쏜다. 저격총은 볼트를 당긴 뒤 다시 겨누는 시간도 기다린다.
     */
    public enum Profile {
        /** 일반 총. 처음 발견 0.3~0.6초, 미리 겨눈 자리 0.1~0.2초, 단발 간격 0.2~0.35초 */
        STANDARD(6, 12, 2, 4, 4, 7, 0, 0),
        /** 지정사수소총. 일반 총보다 반응 +2틱, 단발 간격 +2틱 */
        MARKSMAN(8, 14, 2, 4, 6, 9, 0, 0),
        /** 저격총. 일반 총보다 반응 +3틱, 단발 간격 +4틱, 볼트 뒤 다시 겨누기 3~6틱 */
        SNIPER(9, 15, 3, 5, 8, 11, 3, 6);

        private final int minReactionTicks;
        private final int maxReactionTicks;
        private final int minPreaimedReactionTicks;
        private final int maxPreaimedReactionTicks;
        private final int minSemiIntervalTicks;
        private final int maxSemiIntervalTicks;
        private final int minPostBoltTicks;
        private final int maxPostBoltTicks;

        Profile(int minReactionTicks, int maxReactionTicks, int minPreaimedReactionTicks, int maxPreaimedReactionTicks,
                int minSemiIntervalTicks, int maxSemiIntervalTicks, int minPostBoltTicks, int maxPostBoltTicks) {
            this.minReactionTicks = minReactionTicks;
            this.maxReactionTicks = maxReactionTicks;
            this.minPreaimedReactionTicks = minPreaimedReactionTicks;
            this.maxPreaimedReactionTicks = maxPreaimedReactionTicks;
            this.minSemiIntervalTicks = minSemiIntervalTicks;
            this.maxSemiIntervalTicks = maxSemiIntervalTicks;
            this.minPostBoltTicks = minPostBoltTicks;
            this.maxPostBoltTicks = maxPostBoltTicks;
        }

        /** 연발 모드여도 한 발씩 끊어 쏘는지 */
        boolean firesSingleShots() {
            return this != STANDARD;
        }
    }

    /** 대상을 이 시간(틱) 넘게 보지 못하다 다시 보면 새로 발견한 것으로 본다. */
    private static final int REACQUIRE_TICKS = 30;
    /** 다시 발견한 대상이 마지막으로 본 자리에서 이 거리(칸) 안이면 미리 겨눠 둔 것으로 본다. */
    private static final double PREAIMED_DISTANCE = 2.0;
    /** 계속 겨눌 때도 남는 손떨림 오차(도, 표준편차) */
    private static final double BASE_ERROR_DEGREES = 0.5;
    /** 발견 직후 더해지는 오차(도). {@link #CONVERGE_TICKS}에 걸쳐 0으로 줄어든다. */
    private static final double ACQUIRE_ERROR_DEGREES = 3.0;
    /** 미리 겨눠 둔 경우 발견 직후 오차에 곱하는 비율 */
    private static final double PREAIMED_ACQUIRE_RATIO = 0.4;
    private static final int CONVERGE_TICKS = 25;
    /** 대상이 전력 질주할 때 더해지는 오차(도). 대상 속도에 비례해 더한다. */
    private static final double TARGET_MOVE_ERROR_DEGREES = 1.5;
    /** 플레이어 전력 질주 속도(칸/틱). 이 속도에서 대상 이동 오차를 모두 더한다. */
    private static final double SPRINT_SPEED = 0.28;
    /** 자신이 움직이며 쏠 때 더해지는 오차(도) */
    private static final double SELF_MOVE_ERROR_DEGREES = 0.8;
    /** 자신이 움직이는 것으로 보는 속도(칸/틱)의 제곱 */
    private static final double SELF_MOVING_SPEED_SQR = 0.03 * 0.03;
    /** 제압당한 동안 더해지는 오차(도) */
    private static final double SUPPRESSED_ERROR_DEGREES = 2.0;
    /** 오차의 최대값(도). 오차가 몇 겹 겹쳐도 엉뚱한 곳으로 난사하지 않게 한다. */
    private static final double MAX_ERROR_DEGREES = 8.0;
    /** 연발 총 한 번에 이어 쏘는 발수 */
    private static final int MIN_BURST_SHOTS = 3;
    private static final int MAX_BURST_SHOTS = 5;
    /** 연발 총 점사 사이의 멈춤(틱). 0.3~0.6초 */
    private static final int MIN_BURST_PAUSE_TICKS = 6;
    private static final int MAX_BURST_PAUSE_TICKS = 12;
    /** 대상 속도를 매 틱 섞는 비율. 한 틱 이동량은 들쭉날쭉해 부드럽게 평균 낸다. */
    private static final double SPEED_SMOOTHING = 0.3;

    /** 지금 든 총의 사격 박자. 총 자체의 연사 간격이나 볼트 시간이 더 길면 그쪽을 따른다. */
    private Profile profile = Profile.STANDARD;
    private int targetId = -1;
    private long lastSeenTick = Long.MIN_VALUE;
    private long acquiredTick;
    private boolean preaimed;
    /** 이 틱부터 쏠 수 있다. 반응 시간, 점사 사이 멈춤, 단발 간격이 모두 여기에 모인다. */
    private long readyTick;
    private int burstShotsLeft;
    @Nullable
    private Vec3 lastTargetPos;
    private double targetSpeed;

    /** 대상이 보이는 동안 매 틱 부른다. 처음 보거나 오랜만에 보면 반응 시간을 새로 잡는다. */
    public void observe(LivingEntity target, long gameTime, RandomSource random) {
        Vec3 targetPos = target.position();
        boolean newTarget = target.getId() != this.targetId;
        boolean reacquired = gameTime - this.lastSeenTick > REACQUIRE_TICKS;
        if (newTarget || reacquired) {
            boolean sameSpot = !newTarget && this.lastTargetPos != null
                    && this.lastTargetPos.distanceToSqr(targetPos) <= PREAIMED_DISTANCE * PREAIMED_DISTANCE;
            startAcquire(target, gameTime, sameSpot, random);
        } else if (this.lastTargetPos != null) {
            double moved = targetPos.subtract(this.lastTargetPos).horizontalDistance();
            this.targetSpeed = this.targetSpeed * (1 - SPEED_SMOOTHING) + moved * SPEED_SMOOTHING;
        }
        this.lastTargetPos = targetPos;
        this.lastSeenTick = gameTime;
    }

    /** 든 총이 바뀌었을 때 부른다. 다음 반응과 사격 간격부터 새 박자를 쓴다. */
    public void setProfile(Profile profile) {
        this.profile = profile;
    }

    public Profile profile() {
        return this.profile;
    }

    private void startAcquire(LivingEntity target, long gameTime, boolean sameSpot, RandomSource random) {
        this.targetId = target.getId();
        this.acquiredTick = gameTime;
        this.preaimed = sameSpot;
        int minReaction = this.profile.minReactionTicks;
        int maxReaction = this.profile.maxReactionTicks;
        if (sameSpot) {
            minReaction = this.profile.minPreaimedReactionTicks;
            maxReaction = this.profile.maxPreaimedReactionTicks;
        }
        this.readyTick = Math.max(this.readyTick, gameTime + minReaction + random.nextInt(maxReaction - minReaction + 1));
        this.burstShotsLeft = 0;
        this.targetSpeed = 0;
    }

    /** 반응 시간, 점사 사이 멈춤, 단발 간격이 모두 지나 방아쇠를 당길 수 있는지 */
    public boolean canFire(long gameTime) {
        return gameTime >= this.readyTick;
    }

    /** 이번 발의 조준 오차(도, 표준편차) */
    public double errorDegrees(Mob shooter, long gameTime, boolean suppressed) {
        double sinceAcquire = gameTime - this.acquiredTick;
        double acquireRatio = Math.max(0, 1 - sinceAcquire / CONVERGE_TICKS);
        double acquireError = ACQUIRE_ERROR_DEGREES * acquireRatio;
        if (this.preaimed) {
            acquireError *= PREAIMED_ACQUIRE_RATIO;
        }
        double targetMoveError = TARGET_MOVE_ERROR_DEGREES * Math.min(1, this.targetSpeed / SPRINT_SPEED);
        boolean selfMoving = shooter.getDeltaMovement().horizontalDistanceSqr() > SELF_MOVING_SPEED_SQR;
        double error = BASE_ERROR_DEGREES + acquireError + targetMoveError;
        if (selfMoving) {
            error += SELF_MOVE_ERROR_DEGREES;
        }
        if (suppressed) {
            error += SUPPRESSED_ERROR_DEGREES;
        }
        return Math.min(MAX_ERROR_DEGREES, error);
    }

    /** 쏜 뒤 부른다. 연발 총은 점사 발수를 세고, 단발 총과 저격 계열은 다음 방아쇠까지 간격을 둔다. */
    public void onShot(FireMode fireMode, long gameTime, RandomSource random) {
        boolean singleShot = fireMode != FireMode.AUTO || this.profile.firesSingleShots();
        if (singleShot) {
            this.readyTick = gameTime + randomBetween(random,
                    this.profile.minSemiIntervalTicks, this.profile.maxSemiIntervalTicks);
            return;
        }
        if (this.burstShotsLeft <= 0) {
            this.burstShotsLeft = MIN_BURST_SHOTS + random.nextInt(MAX_BURST_SHOTS - MIN_BURST_SHOTS + 1);
        }
        this.burstShotsLeft--;
        if (this.burstShotsLeft <= 0) {
            this.readyTick = gameTime + MIN_BURST_PAUSE_TICKS
                    + random.nextInt(MAX_BURST_PAUSE_TICKS - MIN_BURST_PAUSE_TICKS + 1);
        }
    }

    /**
     * 볼트를 다 당긴 틱에 부른다. 저격총은 볼트를 당기느라 흐트러진 조준을 다시 맞추는 시간을 기다린다.
     * <p>
     * 단발 간격은 쏜 틱부터 세므로 볼트 동작 안에 묻힌다. 볼트가 끝난 뒤를 따로 세야 연사가 실제로 늦춰진다.
     */
    public void onBoltFinished(long gameTime, RandomSource random) {
        if (this.profile.maxPostBoltTicks <= 0) {
            return;
        }
        int reaimTicks = randomBetween(random, this.profile.minPostBoltTicks, this.profile.maxPostBoltTicks);
        this.readyTick = Math.max(this.readyTick, gameTime + reaimTicks);
    }

    private static int randomBetween(RandomSource random, int min, int max) {
        return min + random.nextInt(max - min + 1);
    }
}
