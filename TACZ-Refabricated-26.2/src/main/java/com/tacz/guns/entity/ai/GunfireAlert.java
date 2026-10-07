package com.tacz.guns.entity.ai;

import com.tacz.guns.api.event.common.EntityHurtByGunEvent;
import com.tacz.guns.api.event.common.EntityKillByGunEvent;
import com.tacz.guns.entity.shooter.MonsterGunController;
import com.tacz.guns.mixin.common.PiglinAiInvoker;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 플레이어의 총격을 주변 몬스터가 알아채게 한다.
 * <p>
 * 몬스터를 맞히거나 죽이면 맞은 몬스터 주변에, 탄이 몬스터를 스치면 그 몬스터에게, 총성이 들리면 들리는 거리 안의
 * 쉬던 몬스터에게 알린다. 멀리서 저격해도 맞은 몬스터와 주변 몬스터가 쏜 플레이어를 노린다.
 * <p>
 * 몬스터는 쏜 플레이어의 정확한 위치를 알지 못한다. 직접 보이면 그 자리를, 보이지 않으면 거리에 비례한 오차를 둔
 * 짐작 위치를 기억한다. 총·활을 든 몬스터는 대상을 잡고 짐작 위치를 바탕으로 숨거나 다가가며, 근접 몬스터는 벽 너머
 * 플레이어의 지금 위치를 쫓지 않도록 대상을 잡지 않고 짐작 위치로 다가가 살핀다.
 */
public final class GunfireAlert {
    /** 맞은 몬스터를 중심으로 경보가 퍼지는 반경(칸). */
    private static final double ALERT_RADIUS = 24.0;
    /** 쏜 플레이어가 이 거리(칸) 안이면 대상으로 잡고, 더 멀면 그쪽으로 다가가기만 한다. */
    private static final double LOCK_ON_DISTANCE = 128.0;
    /** 경보를 받은 몬스터가 추적 범위와 관계없이 대상을 붙잡아 두는 거리(칸). 경보 반경만큼 여유를 둔다. */
    public static final double ALERT_FOLLOW_DISTANCE = LOCK_ON_DISTANCE + ALERT_RADIUS;
    /** 경보로 대상을 붙잡아 두는 시간(20초). 이후에는 바닐라 규칙대로 대상을 잊을 수 있다. */
    private static final int ALERT_HOLD_TICKS = 400;
    /** 한 번에 경보를 받는 최대 몬스터 수. 맞은 몬스터에게 가까운 순서다. 동굴 하나가 통째로 몰려오지 않게 한다. */
    private static final int MAX_ALERTED = 12;
    /** 같은 플레이어의 명중으로 경보를 다시 내기까지 기다리는 시간(틱). 연사 중 매 발마다 주변을 훑지 않게 한다. */
    private static final int ALERT_INTERVAL_TICKS = 10;
    /** 총성을 듣는 거리(칸). 소음기를 달면 바로 옆이 아니면 듣지 못한다. */
    private static final double LOUD_HEARING_RADIUS = 32.0;
    private static final double SILENCED_HEARING_RADIUS = 8.0;
    /** 한 번의 총성을 듣고 움직이는 최대 몬스터 수와, 같은 플레이어의 총성을 다시 처리하기까지의 간격(틱) */
    private static final int MAX_HEARING = 8;
    private static final int GUNSHOT_INTERVAL_TICKS = 20;
    /** 맞거나 스친 탄으로 짐작하는 위치의 오차. 거리에 비례하되 이 값(칸)을 넘지 않는다. */
    private static final double SHOT_GUESS_ERROR_RATIO = 0.08;
    private static final double MAX_SHOT_GUESS_ERROR = 8.0;
    /** 소리만 듣고 짐작하는 위치의 오차. 총탄을 본 것보다 훨씬 부정확하다. */
    private static final double SOUND_GUESS_ERROR_RATIO = 0.2;
    private static final double MAX_SOUND_GUESS_ERROR = 12.0;
    /** 짐작 위치 쪽으로 한 번에 다가가는 거리(칸). */
    private static final double INVESTIGATE_STEP = 24.0;
    private static final double INVESTIGATE_SPEED = 1.0;

    /** 플레이어 UUID → 마지막으로 명중 경보를 낸 게임 틱. 서버 스레드에서만 쓴다. */
    private static final Map<UUID, Long> LAST_ALERT_TICKS = new HashMap<>();
    /** 플레이어 UUID → 마지막으로 총성을 처리한 게임 틱. 서버 스레드에서만 쓴다. */
    private static final Map<UUID, Long> LAST_GUNSHOT_TICKS = new HashMap<>();

    private GunfireAlert() {
    }

    public static void onHurtByGun(EntityHurtByGunEvent.Post event) {
        alert(event.getHurtEntity(), event.getAttacker());
    }

    public static void onKillByGun(EntityKillByGunEvent event) {
        alert(event.getKilledEntity(), event.getAttacker());
    }

    /** 서버를 끌 때 플레이어별 경보 시각을 비운다. */
    public static void clear() {
        LAST_ALERT_TICKS.clear();
        LAST_GUNSHOT_TICKS.clear();
    }

    /**
     * 플레이어의 탄이 몬스터를 처음 스쳤다({@link BulletSuppression}).
     * 이미 이 플레이어와 싸우는 몬스터는 탄이 날아온 쪽으로 위치를 고쳐 기억하고, 쉬던 몬스터는 쏜 쪽을 알아챈다.
     */
    public static void onNearMiss(Mob mob, Player shooter) {
        if (!canAlertFor(shooter) || !(mob.level() instanceof ServerLevel level)) {
            return;
        }
        LivingEntity currentTarget = MonsterFriendlyFire.currentTarget(mob);
        if (currentTarget == shooter) {
            remember(mob, shooter, guessPosition(mob, shooter, SHOT_GUESS_ERROR_RATIO, MAX_SHOT_GUESS_ERROR));
            return;
        }
        boolean busyWithOther = currentTarget != null && currentTarget.isAlive();
        if (busyWithOther) {
            return;
        }
        boolean lockOn = mob.distanceToSqr(shooter) <= LOCK_ON_DISTANCE * LOCK_ON_DISTANCE;
        respond(level, mob, shooter, lockOn, SHOT_GUESS_ERROR_RATIO, MAX_SHOT_GUESS_ERROR);
    }

    /**
     * 플레이어가 총을 쐈다. 총성이 들리는 거리 안에서 쉬던 몬스터가 쏜 쪽을 살피러 간다.
     * 쏜 플레이어가 직접 보이는 몬스터는 바로 대상으로 잡는다.
     */
    public static void onGunshot(Player shooter, boolean silenced) {
        if (!canAlertFor(shooter) || !(shooter.level() instanceof ServerLevel level)) {
            return;
        }
        if (!passedInterval(LAST_GUNSHOT_TICKS, shooter, level.getGameTime(), GUNSHOT_INTERVAL_TICKS)) {
            return;
        }
        double radius = LOUD_HEARING_RADIUS;
        if (silenced) {
            radius = SILENCED_HEARING_RADIUS;
        }
        double radiusSqr = radius * radius;
        AABB area = shooter.getBoundingBox().inflate(radius);
        List<Mob> listeners = level.getEntitiesOfClass(Mob.class, area,
                mob -> isIdleMonster(mob) && mob.distanceToSqr(shooter) <= radiusSqr);
        listeners.stream()
                .sorted(Comparator.comparingDouble(mob -> mob.distanceToSqr(shooter)))
                .limit(MAX_HEARING)
                .forEach(mob -> respond(level, mob, shooter, true, SOUND_GUESS_ERROR_RATIO, MAX_SOUND_GUESS_ERROR));
    }

    private static void alert(Entity hitEntity, LivingEntity attacker) {
        if (!(attacker instanceof ServerPlayer shooter) || !canAlertFor(shooter)) {
            return;
        }
        if (!(hitEntity instanceof LivingEntity victim) || !MonsterGunController.isMonster(victim)) {
            return;
        }
        if (!(victim.level() instanceof ServerLevel level)) {
            return;
        }
        if (!passedInterval(LAST_ALERT_TICKS, shooter, level.getGameTime(), ALERT_INTERVAL_TICKS)) {
            return;
        }

        boolean lockOn = victim.distanceToSqr(shooter) <= LOCK_ON_DISTANCE * LOCK_ON_DISTANCE;
        AABB area = victim.getBoundingBox().inflate(ALERT_RADIUS);
        List<Mob> listeners = level.getEntitiesOfClass(Mob.class, area, mob -> canHear(mob, victim, shooter));
        listeners.stream()
                .sorted(Comparator.comparingDouble(mob -> mob.distanceToSqr(victim)))
                .limit(MAX_ALERTED)
                .forEach(mob -> respond(level, mob, shooter, lockOn, SHOT_GUESS_ERROR_RATIO, MAX_SHOT_GUESS_ERROR));
    }

    private static boolean canAlertFor(Player shooter) {
        return !shooter.isCreative() && !shooter.isSpectator();
    }

    private static boolean passedInterval(Map<UUID, Long> lastTicks, Player shooter, long gameTime, int interval) {
        Long lastTick = lastTicks.get(shooter.getUUID());
        if (lastTick != null && gameTime - lastTick < interval) {
            return false;
        }
        lastTicks.put(shooter.getUUID(), gameTime);
        return true;
    }

    /** 경보를 들을 몬스터. 이미 다른 대상과 싸우는 몬스터는 그 싸움을 이어 간다. 맞은 몬스터 자신은 죽지 않았으면 포함한다. */
    private static boolean canHear(Mob mob, LivingEntity victim, Player shooter) {
        if (!mob.isAlive() || mob.isNoAi() || !MonsterGunController.isMonster(mob)) {
            return false;
        }
        LivingEntity currentTarget = MonsterFriendlyFire.currentTarget(mob);
        boolean busyWithOther = currentTarget != null && currentTarget != shooter && currentTarget.isAlive();
        return !busyWithOther || mob == victim;
    }

    /** 아무 대상과도 싸우지 않는 몬스터 */
    private static boolean isIdleMonster(Mob mob) {
        if (!mob.isAlive() || mob.isNoAi() || !MonsterGunController.isMonster(mob)) {
            return false;
        }
        LivingEntity currentTarget = MonsterFriendlyFire.currentTarget(mob);
        return currentTarget == null || !currentTarget.isAlive();
    }

    private static void respond(ServerLevel level, Mob mob, Player shooter, boolean lockOn,
                                double guessErrorRatio, double maxGuessError) {
        Vec3 guess = guessPosition(mob, shooter, guessErrorRatio, maxGuessError);
        if (!lockOn) {
            investigate(mob, guess);
            return;
        }
        if (mob instanceof Piglin piglin) {
            // 피글린은 Brain으로 대상을 고른다. 맞았을 때와 같은 경로로 화나게 해 주변 피글린에게도 퍼지게 한다.
            PiglinAiInvoker.tacz$wasHurtBy(level, piglin, shooter);
            return;
        }
        if (!(mob instanceof CoverCombatant combatant) || !combatant.tacz$usesTargetGoals()) {
            // 그 밖의 Brain 몬스터(호글린, 워든 등)는 대상 지정 방식이 제각각이라 건드리지 않는다.
            return;
        }
        boolean seesShooter = mob.getSensing().hasLineOfSight(shooter);
        if (!seesShooter && !isRangedFighter(mob)) {
            // 근접 몬스터에게 대상을 잡아 주면 바닐라 추격이 벽 너머 지금 위치를 그대로 쫓는다. 짐작한 곳으로만 간다.
            investigate(mob, guess);
            return;
        }
        mob.setTarget(shooter);
        combatant.tacz$alertTarget(shooter, level.getGameTime() + ALERT_HOLD_TICKS);
        if (seesShooter) {
            combatant.tacz$rememberThreat(shooter, shooter.position());
        } else {
            combatant.tacz$rememberThreat(shooter, guess);
        }
    }

    /** 총이나 활·석궁을 든 몬스터. 엄폐 AI가 대상의 짐작 위치를 바탕으로 싸운다. */
    private static boolean isRangedFighter(Mob mob) {
        return MonsterFriendlyFire.isArmed(mob)
                || mob.getMainHandItem().getItem() instanceof ProjectileWeaponItem
                || mob.getOffhandItem().getItem() instanceof ProjectileWeaponItem;
    }

    private static void remember(Mob mob, Player shooter, Vec3 position) {
        if (mob instanceof CoverCombatant combatant) {
            combatant.tacz$rememberThreat(shooter, position);
        }
    }

    /** 몬스터가 짐작하는 쏜 플레이어의 위치. 멀수록 수평으로 크게 빗나간다. */
    private static Vec3 guessPosition(Mob mob, Player shooter, double errorRatio, double maxError) {
        double distance = Math.sqrt(mob.distanceToSqr(shooter));
        double errorRadius = Math.min(maxError, distance * errorRatio);
        RandomSource random = mob.getRandom();
        double angle = random.nextDouble() * Math.PI * 2;
        double offset = random.nextDouble() * errorRadius;
        return shooter.position().add(Math.cos(angle) * offset, 0.0, Math.sin(angle) * offset);
    }

    /** 대상으로 잡지 않고 짐작 위치 쪽으로 다가가 살핀다. 이미 싸우는 대상이 있으면 그 싸움을 잇는다. */
    private static void investigate(Mob mob, Vec3 guess) {
        if (!(mob instanceof PathfinderMob) || mob.getTarget() != null) {
            return;
        }
        Vec3 toGuess = guess.subtract(mob.position());
        double distance = toGuess.horizontalDistance();
        if (distance < 1.0) {
            return;
        }
        double step = Math.min(INVESTIGATE_STEP, distance);
        double destinationX = mob.getX() + toGuess.x / distance * step;
        double destinationZ = mob.getZ() + toGuess.z / distance * step;
        double destinationY = mob.getY();
        if (step >= distance) {
            destinationY = guess.y;
        }
        mob.getNavigation().moveTo(destinationX, destinationY, destinationZ, INVESTIGATE_SPEED);
        mob.getLookControl().setLookAt(guess.x, guess.y + 1.5, guess.z);
    }
}
