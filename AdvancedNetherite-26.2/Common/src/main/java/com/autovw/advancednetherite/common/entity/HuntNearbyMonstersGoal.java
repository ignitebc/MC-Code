package com.autovw.advancednetherite.common.entity;

import com.autovw.advancednetherite.common.pet.PetAttackMode;
import com.autovw.advancednetherite.common.pet.PetManager;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.AABB;

import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;

/**
 * 주인 둘레 3청크 안의 적대 몹을 찾아 먼저 공격한다. 주인이 맞지 않아도 덤빈다.
 * 주인이 펫 공격 방식을 자동공격으로 둔 동안에만 동작한다.
 *
 * <p>주인에게 가까운 몹부터 고르고, 걸어서 닿을 수 없는 몹(공중에 뜬 몹, 막힌 동굴 속 몹 등)은 건너뛴다.
 * 주인을 때린 상대를 노리는 복수 AI가 더 높은 우선순위라서, 사냥 중에도 주인이 맞으면 그쪽으로 바꾼다.
 */
public class HuntNearbyMonstersGoal extends TargetGoal
{
    /** 사냥 범위. 주인을 중심으로 3청크(48칸)다. */
    public static final double HUNT_RADIUS = 3 * 16.0;
    private static final double HUNT_RADIUS_SQR = HUNT_RADIUS * HUNT_RADIUS;
    /** 주인 발 높이에서 위아래로 살피는 높이. 발밑 깊은 동굴의 몹까지 노리면 길찾기만 헛돈다. */
    private static final double HUNT_VERTICAL_RANGE = 16.0;
    /** 사냥감을 다시 찾는 주기(1초). 주인 둘레를 훑는 비용이 있어 매 틱 찾지 않는다. */
    private static final int SEARCH_INTERVAL_TICKS = 20;
    /** 한 번 찾을 때 걸어서 닿는지 길찾기로 확인하는 최대 마리 수. 탐색에서 길찾기가 가장 비싸다. */
    private static final int MAX_REACH_CHECKS = 3;
    /** 길 끝이 몹에게서 이 수평 거리(1.5칸) 안이면 닿는 것으로 본다. 바닐라 대상 지정과 같은 기준이다. */
    private static final double REACH_HORIZONTAL_SQR = 1.5 * 1.5;
    /** 길 끝과 몹의 높이 차이 허용값(칸). 이보다 차이가 크면 근접 공격이 닿지 않는다. */
    private static final int REACH_VERTICAL = 1;

    private final DialgaPetEntity pet;
    private final TargetingConditions huntConditions = TargetingConditions.forCombat().ignoreLineOfSight();
    private LivingEntity prey;
    private int searchCooldown;

    public HuntNearbyMonstersGoal(DialgaPetEntity pet)
    {
        super(pet, false);
        this.pet = pet;
        // 펫마다 찾는 틱을 흩어 여러 마리가 한 틱에 몰리지 않게 한다.
        this.searchCooldown = pet.getRandom().nextInt(SEARCH_INTERVAL_TICKS) + 1;
        this.setFlags(EnumSet.of(Goal.Flag.TARGET));
    }

    @Override
    public boolean canUse()
    {
        if (--this.searchCooldown > 0)
        {
            return false;
        }
        this.searchCooldown = SEARCH_INTERVAL_TICKS;

        LivingEntity owner = this.pet.getOwner();
        if (owner == null || owner.level() != this.pet.level())
        {
            return false;
        }
        // 일반공격을 고른 주인의 펫은 먼저 덤비지 않고, 주인이 맞았을 때만 반격 AI가 움직인다.
        if (PetManager.getAttackMode(owner) != PetAttackMode.AUTO)
        {
            return false;
        }
        this.prey = findPrey(owner);
        return this.prey != null;
    }

    @Override
    public void start()
    {
        this.mob.setTarget(this.prey);
        // 일반공격으로 바꾸면 이 대상만 놓을 수 있도록 사냥으로 잡은 대상임을 남긴다.
        this.pet.markHuntTarget(this.prey);
        super.start();
    }

    @Override
    public void stop()
    {
        super.stop();
        this.prey = null;
        // 사냥감을 잡았거나 놓쳤으면 다음 사냥감을 바로 찾는다.
        this.searchCooldown = 0;
    }

    /** 주인에게 가까운 순으로 살피며 걸어서 닿는 첫 몹을 고른다. 없으면 null. */
    private LivingEntity findPrey(LivingEntity owner)
    {
        AABB area = owner.getBoundingBox().inflate(HUNT_RADIUS, HUNT_VERTICAL_RANGE, HUNT_RADIUS);
        List<Mob> candidates = this.pet.level().getEntitiesOfClass(Mob.class, area,
                candidate -> isHuntable(candidate, owner));
        return candidates.stream()
                .sorted(Comparator.comparingDouble(candidate -> candidate.distanceToSqr(owner)))
                .limit(MAX_REACH_CHECKS)
                .filter(this::canWalkTo)
                .findFirst()
                .orElse(null);
    }

    /** 적대 몹만 노린다. 움직이지 않는 장식용·무적 몹과, 닿지 못해 잠시 포기한 몹은 건너뛴다. */
    private boolean isHuntable(Mob candidate, LivingEntity owner)
    {
        boolean hostile = candidate instanceof Enemy && candidate.isAlive();
        boolean inert = candidate.isNoAi() || candidate.isInvulnerable();
        if (!hostile || inert || this.pet.isIgnoringTarget(candidate))
        {
            return false;
        }
        if (candidate.distanceToSqr(owner) > HUNT_RADIUS_SQR)
        {
            return false;
        }
        return this.huntConditions.test(getServerLevel(this.pet), this.pet, candidate);
    }

    /** 펫이 몹 바로 옆까지 걸어갈 수 있는지. 공중에 뜬 몹이나 막힌 곳의 몹은 닿지 않는다. */
    private boolean canWalkTo(Mob candidate)
    {
        Path path = this.pet.getNavigation().createPath(candidate, 0);
        if (path == null)
        {
            return false;
        }
        Node end = path.getEndNode();
        if (end == null)
        {
            return false;
        }

        int dx = end.x - candidate.getBlockX();
        int dy = end.y - candidate.getBlockY();
        int dz = end.z - candidate.getBlockZ();
        boolean closeHorizontally = dx * dx + dz * dz <= REACH_HORIZONTAL_SQR;
        return closeHorizontally && Math.abs(dy) <= REACH_VERTICAL;
    }
}
