package com.tacz.guns.entity.ai;

import com.tacz.guns.api.event.common.EntityHurtByGunEvent;
import com.tacz.guns.api.event.common.EntityKillByGunEvent;
import com.tacz.guns.entity.shooter.MonsterGunController;
import com.tacz.guns.mixin.common.PiglinAiInvoker;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 플레이어가 총으로 몬스터를 맞히거나 죽이면 주변 몬스터에게 알린다.
 * <p>
 * 멀리서 저격해도 맞은 몬스터와 그 주변 몬스터가 쏜 플레이어를 노리게 한다. 총을 든 몬스터는 엄폐 AI가 이어받아
 * 숨거나 다가가며 쏘고, 근접 몬스터는 다가간다. 쏜 플레이어가 너무 멀면 대상으로 잡지 않고 쏜 쪽으로 다가가기만 한다.
 * 바닐라 대상 지정은 추적 범위 밖의 대상을 바로 버리므로, 경보가 유지되는 동안은 대상을 붙잡아 둔다.
 */
public final class GunfireAlert {
    /** 맞은 몬스터를 중심으로 경보가 퍼지는 반경(칸). */
    private static final double ALERT_RADIUS = 24.0;
    /** 쏜 플레이어가 맞은 몬스터에게서 이 거리(칸) 안이면 대상으로 잡고, 더 멀면 그쪽으로 다가가기만 한다. */
    private static final double LOCK_ON_DISTANCE = 128.0;
    /** 경보를 받은 몬스터가 추적 범위와 관계없이 대상을 붙잡아 두는 거리(칸). 경보 반경만큼 여유를 둔다. */
    public static final double ALERT_FOLLOW_DISTANCE = LOCK_ON_DISTANCE + ALERT_RADIUS;
    /** 경보로 대상을 붙잡아 두는 시간(20초). 이후에는 바닐라 규칙대로 대상을 잊을 수 있다. */
    private static final int ALERT_HOLD_TICKS = 400;
    /** 한 번에 경보를 받는 최대 몬스터 수. 맞은 몬스터에게 가까운 순서다. 동굴 하나가 통째로 몰려오지 않게 한다. */
    private static final int MAX_ALERTED = 12;
    /** 같은 플레이어의 사격으로 경보를 다시 내기까지 기다리는 시간(틱). 연사 중 매 발마다 주변을 훑지 않게 한다. */
    private static final int ALERT_INTERVAL_TICKS = 10;
    /** 너무 먼 플레이어 쪽으로 한 번에 다가가는 거리(칸). */
    private static final double INVESTIGATE_STEP = 24.0;
    private static final double INVESTIGATE_SPEED = 1.0;

    /** 플레이어 UUID → 마지막으로 경보를 낸 게임 틱. 서버 스레드에서만 쓴다. */
    private static final Map<UUID, Long> LAST_ALERT_TICKS = new HashMap<>();

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
    }

    private static void alert(Entity hitEntity, LivingEntity attacker) {
        if (!(attacker instanceof ServerPlayer shooter) || shooter.isCreative() || shooter.isSpectator()) {
            return;
        }
        if (!(hitEntity instanceof LivingEntity victim) || !MonsterGunController.isMonster(victim)) {
            return;
        }
        if (!(victim.level() instanceof ServerLevel level) || !passedInterval(shooter, level.getGameTime())) {
            return;
        }

        boolean lockOn = victim.distanceToSqr(shooter) <= LOCK_ON_DISTANCE * LOCK_ON_DISTANCE;
        AABB area = victim.getBoundingBox().inflate(ALERT_RADIUS);
        List<Mob> listeners = level.getEntitiesOfClass(Mob.class, area, mob -> canHear(mob, victim, shooter));
        listeners.stream()
                .sorted(Comparator.comparingDouble(mob -> mob.distanceToSqr(victim)))
                .limit(MAX_ALERTED)
                .forEach(mob -> respond(level, mob, shooter, lockOn));
    }

    private static boolean passedInterval(ServerPlayer shooter, long gameTime) {
        Long lastTick = LAST_ALERT_TICKS.get(shooter.getUUID());
        if (lastTick != null && gameTime - lastTick < ALERT_INTERVAL_TICKS) {
            return false;
        }
        LAST_ALERT_TICKS.put(shooter.getUUID(), gameTime);
        return true;
    }

    /** 경보를 들을 몬스터. 이미 다른 대상과 싸우는 몬스터는 그 싸움을 이어 간다. 맞은 몬스터 자신은 죽지 않았으면 포함한다. */
    private static boolean canHear(Mob mob, LivingEntity victim, ServerPlayer shooter) {
        if (!mob.isAlive() || mob.isNoAi() || !MonsterGunController.isMonster(mob)) {
            return false;
        }
        LivingEntity currentTarget = MonsterFriendlyFire.currentTarget(mob);
        boolean busyWithOther = currentTarget != null && currentTarget != shooter && currentTarget.isAlive();
        return !busyWithOther || mob == victim;
    }

    private static void respond(ServerLevel level, Mob mob, ServerPlayer shooter, boolean lockOn) {
        if (!lockOn) {
            investigate(mob, shooter);
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
        mob.setTarget(shooter);
        combatant.tacz$alertTarget(shooter, level.getGameTime() + ALERT_HOLD_TICKS);
    }

    /** 쏜 플레이어가 너무 멀다. 대상으로 잡지 않고 그쪽으로 조금 다가가 살핀다. */
    private static void investigate(Mob mob, ServerPlayer shooter) {
        boolean idle = mob.getTarget() == null && mob.getNavigation().isDone();
        if (!(mob instanceof PathfinderMob) || !idle) {
            return;
        }
        Vec3 toShooter = shooter.position().subtract(mob.position());
        double distance = toShooter.horizontalDistance();
        if (distance < 1.0) {
            return;
        }
        double step = Math.min(INVESTIGATE_STEP, distance);
        Vec3 destination = mob.position().add(toShooter.x / distance * step, 0.0, toShooter.z / distance * step);
        mob.getNavigation().moveTo(destination.x, destination.y, destination.z, INVESTIGATE_SPEED);
    }
}
