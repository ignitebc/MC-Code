package com.tacz.guns.entity.ai;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * 저격 몬스터가 멈춰 서서 쏠 자리를 찾는다.
 * <p>
 * 대상이 보이고(몬스터 총기가 쏘는 기준인 바닐라 시야) 총 사거리 안이며, 돌진으로 바뀌는 거리보다 충분히 먼 칸만 후보로 삼는다.
 * 그중 대상보다 높은 칸과 대상 쪽 탄이 하체에 닿지 않는 칸(허리 높이 엄폐)을 고르고, 가까운 칸을 조금 더 선호한다.
 */
final class SniperSpotFinder {
    /** 몬스터를 중심으로 자리를 찾는 수평 반경(칸). 엄폐 칸보다 넓게 찾는다. */
    private static final int SEARCH_RADIUS = 16;
    /** 몬스터의 발 높이에서 위아래로 살펴보는 높이(칸). 높은 자리를 찾을 수 있게 엄폐 칸보다 넓다. */
    private static final int VERTICAL_RANGE = 4;
    /** 한 번 탐색할 때 살펴보는 무작위 칸 수 */
    private static final int SAMPLE_COUNT = 48;
    /** 후보 중 경로를 실제로 계산해 보는 최대 개수. 경로 계산이 탐색에서 가장 비싸다. */
    private static final int PATH_CHECKS = 3;
    /** 대상보다 1칸 높을 때마다 주는 가산점(칸 단위 점수). 이 높이까지만 센다. */
    private static final double HEIGHT_BONUS_PER_BLOCK = 1.5;
    private static final double MAX_HEIGHT_BONUS_BLOCKS = 6.0;
    /** 대상 쪽에서 하체가 가려지는 칸의 가산점 */
    private static final double CONCEALED_BONUS = 8.0;
    /** 사거리의 이 비율 근처 거리를 선호한다. 사거리 끝에 서면 대상이 조금만 물러나도 사거리를 벗어난다. */
    private static final double PREFERRED_RANGE_RATIO = 0.75;
    private static final double RANGE_DEVIATION_WEIGHT = 0.2;
    /** 자리를 옮길 때 지금 자리에서 이 거리(칸) 안의 칸은 고르지 않는다. 드러난 자리 바로 옆으로 가 봐야 소용없다. */
    private static final double RELOCATE_MIN_MOVE = 4.0;

    private SniperSpotFinder() {
    }

    /**
     * 저격 자리 탐색 조건.
     *
     * @param targetEye   대상 눈 위치. 보이지 않는 대상은 마지막으로 본 위치 기준이다.
     * @param targetFeet  대상 발 위치
     * @param range       총 사거리. 자리에서 대상 눈까지 허용하는 최대 거리다.
     * @param minDistance 자리와 대상 사이의 최소 거리(칸)
     * @param maxTravel   몬스터가 지금 위치에서 걸어갈 최대 직선 거리(칸)
     * @param avoid       자리를 옮기는 중이면 버리는 자리. 아니면 null
     */
    record Search(Vec3 targetEye, Vec3 targetFeet, double range, double minDistance, double maxTravel,
                  @Nullable BlockPos avoid) {
    }

    record Spot(BlockPos pos, Path path, boolean concealed) {
    }

    /** @return 찾지 못하면 null */
    @Nullable
    static Spot find(PathfinderMob mob, Search search) {
        RandomSource random = mob.getRandom();
        Level level = mob.level();
        BlockPos origin = mob.blockPosition();
        double rangeSqr = search.range() * search.range();
        double minDistanceSqr = search.minDistance() * search.minDistance();
        double maxTravelSqr = search.maxTravel() * search.maxTravel();
        LongSet taken = takenCells(mob, origin);
        LongSet visited = new LongOpenHashSet();
        List<Candidate> candidates = new ArrayList<>();
        for (int i = 0; i < SAMPLE_COUNT; i++) {
            int dx = random.nextInt(SEARCH_RADIUS * 2 + 1) - SEARCH_RADIUS;
            int dz = random.nextInt(SEARCH_RADIUS * 2 + 1) - SEARCH_RADIUS;
            BlockPos pos = CoverFinder.findStandable(mob, origin.offset(dx, 0, dz), VERTICAL_RANGE);
            if (pos == null || !visited.add(pos.asLong()) || taken.contains(pos.asLong()) || !mob.isWithinHome(pos)) {
                continue;
            }
            Vec3 feet = Vec3.atBottomCenterOf(pos);
            if (mob.position().distanceToSqr(feet) > maxTravelSqr) {
                continue;
            }
            boolean tooCloseToAvoided = search.avoid() != null
                    && feet.distanceToSqr(Vec3.atBottomCenterOf(search.avoid())) < RELOCATE_MIN_MOVE * RELOCATE_MIN_MOVE;
            if (tooCloseToAvoided) {
                continue;
            }
            Vec3 eye = feet.add(0, mob.getEyeHeight(), 0);
            double targetDistanceSqr = eye.distanceToSqr(search.targetEye());
            boolean outOfRange = targetDistanceSqr > rangeSqr;
            boolean tooClose = feet.distanceToSqr(search.targetFeet()) < minDistanceSqr;
            if (outOfRange || tooClose || !canSee(level, eye, search.targetEye())) {
                continue;
            }
            boolean concealed = isConcealed(mob, pos, search.targetEye());
            double score = score(mob, feet, Math.sqrt(targetDistanceSqr), concealed, search);
            candidates.add(new Candidate(pos, concealed, score));
        }
        candidates.sort(Comparator.comparingDouble(Candidate::score));
        int pathChecks = Math.min(PATH_CHECKS, candidates.size());
        for (int i = 0; i < pathChecks; i++) {
            Candidate candidate = candidates.get(i);
            Path path = mob.getNavigation().createPath(candidate.pos(), 0);
            if (path != null && path.canReach()) {
                return new Spot(candidate.pos(), path, candidate.concealed());
            }
        }
        return null;
    }

    /**
     * 몬스터가 이 칸에 섰을 때 대상 쪽에서 날아온 탄이 발과 허리에 닿지 않는지(허리 높이 엄폐).
     * 눈 높이는 검사하지 않는다. 눈이 드러나야 쏠 수 있다.
     */
    static boolean isConcealed(PathfinderMob mob, BlockPos pos, Vec3 targetEye) {
        Level level = mob.level();
        double x = pos.getX() + 0.5;
        double y = pos.getY();
        double z = pos.getZ() + 0.5;
        double waist = y + mob.getBbHeight() * 0.5;
        return CoverFinder.blocksBullet(level, targetEye, new Vec3(x, y + 0.2, z))
                && CoverFinder.blocksBullet(level, targetEye, new Vec3(x, waist, z));
    }

    /** 이 눈 위치에서 대상 눈이 보이는지. 몬스터 총기는 바닐라 시야가 트여야 쏘므로 같은 블록 충돌 기준으로 본다. */
    private static boolean canSee(Level level, Vec3 eye, Vec3 targetEye) {
        HitResult sight = level.clip(new ClipContext(eye, targetEye,
                ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, CollisionContext.empty()));
        return sight.getType() == HitResult.Type.MISS;
    }

    /** 자리 점수. 낮을수록 좋다. 가까운 칸을 먼저 쓰되 높고 하체가 가려지는 칸을 앞세운다. */
    private static double score(PathfinderMob mob, Vec3 feet, double targetDistance, boolean concealed, Search search) {
        double travel = mob.position().distanceTo(feet);
        double heightBlocks = Mth.clamp(feet.y - search.targetFeet().y, 0.0, MAX_HEIGHT_BONUS_BLOCKS);
        double rangeDeviation = Math.abs(targetDistance - search.range() * PREFERRED_RANGE_RATIO);
        double score = travel + RANGE_DEVIATION_WEIGHT * rangeDeviation - HEIGHT_BONUS_PER_BLOCK * heightBlocks;
        if (concealed) {
            score -= CONCEALED_BONUS;
        }
        return score;
    }

    /** 다른 몬스터가 서 있거나 자리로 잡아 둔 칸. 여럿이 한 칸으로 몰리면 서로 밀려나 자리를 잃는다. */
    private static LongSet takenCells(PathfinderMob mob, BlockPos origin) {
        AABB area = new AABB(origin).inflate(SEARCH_RADIUS * 2 + 1, VERTICAL_RANGE * 2 + 1, SEARCH_RADIUS * 2 + 1);
        LongSet taken = new LongOpenHashSet();
        for (Mob other : mob.level().getEntitiesOfClass(Mob.class, area, other -> other != mob && other.isAlive())) {
            taken.add(other.blockPosition().asLong());
            if (other instanceof CoverCombatant combatant && combatant.tacz$getCoverPos() != null) {
                taken.add(combatant.tacz$getCoverPos().asLong());
            }
        }
        return taken;
    }

    private record Candidate(BlockPos pos, boolean concealed, double score) {
    }
}
