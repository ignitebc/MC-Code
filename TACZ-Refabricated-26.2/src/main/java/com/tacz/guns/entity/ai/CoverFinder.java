package com.tacz.guns.entity.ai;

import com.tacz.guns.event.ammo.DestroyGlassBlock;
import com.tacz.guns.util.block.BlockRayTrace;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * 몬스터 주변에서 원거리 공격을 막아 주는 엄폐 칸을 찾는다.
 * <p>
 * 엄폐 여부는 몬스터의 시야가 아니라 탄의 판정으로 정한다. TACZ 탄은 나뭇잎·울타리·철창을 통과하고
 * 유리를 깨므로, 시야만 가리는 블록 뒤를 안전하다고 보면 몬스터가 숨은 채로 맞는다.
 * 화살은 이 블록들에 막히지만, 더 엄격한 탄 기준 하나로 두 무기를 함께 판단한다.
 */
public final class CoverFinder {
    /** 몬스터를 중심으로 엄폐 칸을 찾는 기본 수평 반경(칸). */
    public static final int SEARCH_RADIUS = 10;
    /** 측면으로 돌아 들어가는 몬스터는 더 넓게 찾는다. */
    public static final int FLANK_SEARCH_RADIUS = 14;
    /** 몬스터의 발 높이에서 위아래로 살펴보는 높이(칸). */
    private static final int VERTICAL_RANGE = 3;
    /** 기본 반경에서 한 번 탐색할 때 살펴보는 무작위 칸 수. 반경이 넓으면 그만큼 늘린다. */
    private static final int SAMPLE_COUNT = 40;
    /** 측면 역할의 엄폐 칸은 대상 기준으로 무리 정면에서 이 각도(도) 이상 벗어나야 측면으로 본다. */
    private static final double FLANK_MIN_ANGLE_DEGREES = 60.0;
    /** 측면 각도가 모자란 칸에 붙이는 감점(칸 단위 점수). 정면에 가까울수록 크다. */
    private static final double FLANK_PENALTY = 16.0;
    /** 평소에는 대상과의 거리가 사거리의 이 비율 근처인 칸을, 부상으로 물러날 때는 더 먼 칸을 선호한다. */
    private static final double PREFERRED_RANGE_RATIO = 0.6;
    private static final double RETREAT_RANGE_RATIO = 0.9;
    /** 후보 중 경로를 실제로 계산해 보는 최대 개수. 경로 계산이 탐색에서 가장 비싸다. */
    private static final int PATH_CHECKS = 3;
    /** 대상에게 이보다 가까운 칸은 엄폐물로 쓰지 않는다. */
    private static final double MIN_THREAT_DISTANCE = 5.0;
    /** 사격할 때 엄폐 칸 한가운데에서 옆으로 몸을 내미는 거리(칸). 덜 내미는 쪽부터 살핀다. */
    private static final double[] PEEK_OFFSETS = {0.5, 0.75, 1.0};
    /** 서버 전체에서 한 틱에 허용하는 탐색 횟수. 몬스터 여럿이 한꺼번에 탐색해도 부하가 몰리지 않게 한다. */
    private static final int MAX_SEARCHES_PER_TICK = 4;

    private static long budgetTick = Long.MIN_VALUE;
    private static int searchesThisTick;

    private CoverFinder() {
    }

    /** 이번 틱의 탐색 몫이 남았으면 하나를 쓰고 true. 모자라면 다음 틱에 다시 시도한다. */
    public static boolean tryReserveSearch(long gameTime) {
        if (gameTime != budgetTick) {
            budgetTick = gameTime;
            searchesThisTick = 0;
        }
        if (searchesThisTick >= MAX_SEARCHES_PER_TICK) {
            return false;
        }
        searchesThisTick++;
        return true;
    }

    /**
     * 엄폐 칸을 찾는 조건.
     *
     * @param threatEye  대상 눈 위치. 보이지 않는 대상은 마지막으로 본 위치 기준이다.
     * @param threatFeet 대상 발 위치. 보이지 않는 대상은 마지막으로 본 위치다.
     * @param range      무기 사거리. 내민 위치에서 대상까지 허용하는 최대 거리다.
     * @param radius     몬스터 주변에서 찾는 수평 반경(칸)
     * @param flankFront 측면 역할일 때 무리가 있는 방향(대상에서 무리 쪽, 수평 단위 벡터). 측면 역할이 아니면 null
     * @param retreat    부상으로 물러나는 중인지. 대상에게서 지금보다 먼 칸만 고른다.
     */
    public record Search(Vec3 threatEye, Vec3 threatFeet, double range, int radius,
                         @Nullable Vec3 flankFront, boolean retreat) {
    }

    /**
     * 대상의 탄을 막아 주고, 옆으로 살짝 내밀면 대상을 쏠 수 있으며, 실제로 걸어갈 수 있는 엄폐 칸을 찾는다.
     *
     * @return 찾지 못하면 null
     */
    @Nullable
    public static CoverSpot find(PathfinderMob mob, Search search) {
        RandomSource random = mob.getRandom();
        BlockPos origin = mob.blockPosition();
        int radius = search.radius();
        int samples = SAMPLE_COUNT * radius / SEARCH_RADIUS;
        double rangeSqr = search.range() * search.range();
        double currentThreatDistance = mob.position().distanceTo(search.threatFeet());
        NearbyAllies allies = scanAllies(mob, origin, radius);
        LongSet visited = new LongOpenHashSet();
        List<Candidate> candidates = new ArrayList<>();
        for (int i = 0; i < samples; i++) {
            int dx = random.nextInt(radius * 2 + 1) - radius;
            int dz = random.nextInt(radius * 2 + 1) - radius;
            BlockPos cover = findStandable(mob, origin.offset(dx, 0, dz), VERTICAL_RANGE);
            if (cover == null || !visited.add(cover.asLong()) || !mob.isWithinHome(cover)) {
                continue;
            }
            if (allies.takenCells().contains(cover.asLong()) || allies.isInLane(mob, cover)) {
                continue;
            }
            Vec3 center = Vec3.atBottomCenterOf(cover);
            double threatDistance = center.distanceTo(search.threatFeet());
            if (threatDistance < MIN_THREAT_DISTANCE || threatDistance > search.range()) {
                continue;
            }
            if (search.retreat() && threatDistance < currentThreatDistance) {
                continue;
            }
            if (!isProtected(mob, cover, search.threatEye())) {
                continue;
            }
            List<Vec3> peeks = findPeeks(mob, cover, search.threatEye(), rangeSqr);
            if (peeks.isEmpty()) {
                continue;
            }
            candidates.add(new Candidate(cover, peeks, score(mob, center, threatDistance, search)));
        }
        candidates.sort(Comparator.comparingDouble(Candidate::score));
        int pathChecks = Math.min(PATH_CHECKS, candidates.size());
        for (int i = 0; i < pathChecks; i++) {
            Candidate candidate = candidates.get(i);
            Path path = mob.getNavigation().createPath(candidate.cover(), 0);
            if (path != null && path.canReach()) {
                return new CoverSpot(candidate.cover(), candidate.peeks(), path);
            }
        }
        return null;
    }

    /**
     * 엄폐 칸 점수. 낮을수록 좋다.
     * <p>
     * 가까운 칸을 먼저 쓰되, 대상과의 거리가 사거리의 60%(물러날 때는 90%) 근처인 칸을 조금 더 선호한다.
     * 측면 역할이면 대상 기준으로 무리 정면에서 충분히 벗어난 칸을 고르도록, 정면에 가까운 칸을 깎는다.
     */
    private static double score(PathfinderMob mob, Vec3 center, double threatDistance, Search search) {
        double preferredRatio = PREFERRED_RANGE_RATIO;
        if (search.retreat()) {
            preferredRatio = RETREAT_RANGE_RATIO;
        }
        double score = mob.position().distanceTo(center)
                + 0.5 * Math.abs(threatDistance - search.range() * preferredRatio);
        Vec3 flankFront = search.flankFront();
        if (flankFront == null) {
            return score;
        }
        Vec3 fromThreat = new Vec3(center.x - search.threatFeet().x, 0, center.z - search.threatFeet().z);
        if (fromThreat.lengthSqr() < 1.0E-4) {
            return score + FLANK_PENALTY;
        }
        double cosine = Math.max(-1.0, Math.min(1.0, fromThreat.normalize().dot(flankFront)));
        double angle = Math.toDegrees(Math.acos(cosine));
        if (angle < FLANK_MIN_ANGLE_DEGREES) {
            score += (FLANK_MIN_ANGLE_DEGREES - angle) / FLANK_MIN_ANGLE_DEGREES * FLANK_PENALTY;
        }
        return score;
    }

    /**
     * 주변 몬스터가 서 있거나 숨으려고 잡아 둔 칸과, 총을 든 몬스터의 사선을 한 번에 모은다.
     * <p>
     * 여럿이 한 칸으로 몰리면 겹쳐 선 몬스터가 칸 밖으로 밀려나 드러난다. 다른 몬스터도 자기 주변 탐색 반경
     * 안에서 칸을 잡으므로, 그 칸까지 놓치지 않도록 탐색 반경의 두 배 안의 몬스터를 살핀다.
     * 다른 몬스터의 사선 위에 숨으면 그 몬스터가 쏘지 못하거나 탄이 막히므로 그런 칸도 피한다.
     */
    private static NearbyAllies scanAllies(PathfinderMob mob, BlockPos origin, int searchRadius) {
        int horizontal = searchRadius * 2 + 1;
        int vertical = VERTICAL_RANGE * 2 + 1;
        AABB area = new AABB(origin).inflate(horizontal, vertical, horizontal);
        LongSet taken = new LongOpenHashSet();
        List<FriendlyFireLanes.Lane> lanes = new ArrayList<>();
        for (Mob other : mob.level().getEntitiesOfClass(Mob.class, area, other -> other != mob && other.isAlive())) {
            taken.add(other.blockPosition().asLong());
            if (other instanceof CoverCombatant combatant && combatant.tacz$getCoverPos() != null) {
                taken.add(combatant.tacz$getCoverPos().asLong());
            }
            FriendlyFireLanes.Lane lane = FriendlyFireLanes.laneOf(other);
            if (lane != null) {
                lanes.add(lane);
            }
        }
        return new NearbyAllies(taken, lanes);
    }

    /** 주변 몬스터가 차지한 칸과 총을 든 몬스터의 사선 */
    private record NearbyAllies(LongSet takenCells, List<FriendlyFireLanes.Lane> lanes) {
        /** 몬스터가 이 칸에 섰을 때 다른 몬스터의 사선에 걸리는지 */
        boolean isInLane(PathfinderMob mob, BlockPos cover) {
            if (this.lanes.isEmpty()) {
                return false;
            }
            AABB body = mob.getDimensions(mob.getPose()).makeBoundingBox(Vec3.atBottomCenterOf(cover));
            for (FriendlyFireLanes.Lane lane : this.lanes) {
                if (lane.crosses(body)) {
                    return true;
                }
            }
            return false;
        }
    }

    /**
     * 몬스터가 이 칸에 섰을 때 대상의 탄이 몸 어디에도 닿지 않는지.
     * <p>
     * 발, 허리, 눈, 머리 위와 허리 양옆을 검사한다. 눈 높이를 넣었으므로 엄폐 중에는 바닐라 시야도 끊긴다.
     */
    public static boolean isProtected(PathfinderMob mob, BlockPos cover, Vec3 threatEye) {
        Level level = mob.level();
        double x = cover.getX() + 0.5;
        double y = cover.getY();
        double z = cover.getZ() + 0.5;
        double height = mob.getBbHeight();
        Vec3 toCover = new Vec3(x - threatEye.x, 0, z - threatEye.z);
        if (toCover.lengthSqr() < 1.0E-4) {
            return false;
        }
        Vec3 side = new Vec3(-toCover.z, 0, toCover.x).normalize().scale(mob.getBbWidth() * 0.5);
        double waist = y + height * 0.5;
        // 가장 먼저 드러나기 쉬운 눈 높이부터 검사해 노출된 칸은 광선 하나로 거른다.
        return blocksBullet(level, threatEye, new Vec3(x, y + mob.getEyeHeight(), z))
                && blocksBullet(level, threatEye, new Vec3(x, y + height - 0.05, z))
                && blocksBullet(level, threatEye, new Vec3(x + side.x, waist, z + side.z))
                && blocksBullet(level, threatEye, new Vec3(x - side.x, waist, z - side.z))
                && blocksBullet(level, threatEye, new Vec3(x, waist, z))
                && blocksBullet(level, threatEye, new Vec3(x, y + 0.2, z));
    }

    /** 대상의 눈에서 날아온 탄이 목표 지점 전에 블록에 막히는지. 깨지는 유리에 막히면 막지 못한 것으로 본다. */
    private static boolean blocksBullet(Level level, Vec3 from, Vec3 to) {
        BlockHitResult hit = BlockRayTrace.rayTraceBlocks(level,
                new ClipContext(from, to, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, CollisionContext.empty()));
        if (hit.getType() == HitResult.Type.MISS) {
            return false;
        }
        return !DestroyGlassBlock.isBreakableGlass(level.getBlockState(hit.getBlockPos()));
    }

    /**
     * 엄폐 칸 한가운데에서 좌우로 0.5~1칸만 몸을 내밀어 대상이 보이는 위치를 좌우 한쪽에 하나씩 찾는다.
     * 쪽마다 덜 내미는 위치부터 살핀다. 양쪽을 모두 찾아 두면 매번 같은 쪽으로만 나오지 않고 번갈아 나올 수 있다.
     * <p>
     * 옆 칸이 설 수 있는 칸일 때만 내민다. 1칸을 다 내밀면 옆 칸에 올라서고, 덜 내밀어도 몸 일부가 옆 칸 위에 걸치기 때문이다.
     * 시야는 바닐라 시야 판정과 같은 블록 충돌 기준으로 본다. 몬스터 총기와 활은 이 시야가 트여야 쏜다.
     *
     * @return 내민 발 위치 목록(0~2개)
     */
    public static List<Vec3> findPeeks(PathfinderMob mob, BlockPos cover, Vec3 threatEye, double rangeSqr) {
        Vec3 center = Vec3.atBottomCenterOf(cover);
        Vec3 toThreat = new Vec3(threatEye.x - center.x, 0, threatEye.z - center.z);
        Direction side = Direction.getApproximateNearest(-toThreat.z, 0, toThreat.x);
        List<Vec3> peeks = new ArrayList<>(2);
        for (Direction direction : new Direction[]{side, side.getOpposite()}) {
            if (!isStandable(mob, cover.relative(direction))) {
                continue;
            }
            Vec3 peek = findPeekOnSide(mob, center, direction, threatEye, rangeSqr);
            if (peek != null) {
                peeks.add(peek);
            }
        }
        return peeks;
    }

    @Nullable
    private static Vec3 findPeekOnSide(PathfinderMob mob, Vec3 center, Direction direction, Vec3 threatEye, double rangeSqr) {
        Level level = mob.level();
        EntityDimensions dimensions = mob.getDimensions(mob.getPose());
        for (double offset : PEEK_OFFSETS) {
            Vec3 peek = center.add(direction.getStepX() * offset, 0, direction.getStepZ() * offset);
            if (!level.noCollision(mob, dimensions.makeBoundingBox(peek))) {
                continue;
            }
            Vec3 eye = peek.add(0, mob.getEyeHeight(), 0);
            if (eye.distanceToSqr(threatEye) > rangeSqr) {
                continue;
            }
            HitResult sight = level.clip(new ClipContext(eye, threatEye,
                    ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, CollisionContext.empty()));
            if (sight.getType() == HitResult.Type.MISS) {
                return peek;
            }
        }
        return null;
    }

    /** 기둥에서 몬스터가 설 수 있는 칸을 찾는다. 발 높이에 가까운 칸부터 살핀다. */
    @Nullable
    private static BlockPos findStandable(PathfinderMob mob, BlockPos column, int verticalRange) {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int i = 0; i <= verticalRange * 2; i++) {
            // 0, +1, -1, +2, -2 ... 순서
            int dy = (i + 1) / 2 * (i % 2 == 0 ? -1 : 1);
            pos.set(column.getX(), column.getY() + dy, column.getZ());
            if (isStandable(mob, pos)) {
                return pos.immutable();
            }
        }
        return null;
    }

    /** 발밑이 단단하고 위험하지 않으며, 머리 위 칸까지 비어 있는지 */
    private static boolean isStandable(PathfinderMob mob, BlockPos pos) {
        if (WalkNodeEvaluator.getPathTypeStatic(mob, pos) != PathType.WALKABLE) {
            return false;
        }
        Level level = mob.level();
        BlockPos head = pos.above();
        return level.getBlockState(head).getCollisionShape(level, head).isEmpty();
    }

    private record Candidate(BlockPos cover, List<Vec3> peeks, double score) {
    }
}
