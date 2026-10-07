package com.tacz.guns.entity.ai;

import com.tacz.guns.event.ammo.DestroyGlassBlock;
import com.tacz.guns.util.block.BlockRayTrace;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.LivingEntity;
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
    /** 몬스터를 중심으로 엄폐 칸을 찾는 수평 반경(칸). */
    private static final int SEARCH_RADIUS = 10;
    /** 몬스터의 발 높이에서 위아래로 살펴보는 높이(칸). */
    private static final int VERTICAL_RANGE = 3;
    /** 한 번 탐색할 때 살펴보는 무작위 칸 수. */
    private static final int SAMPLE_COUNT = 40;
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
     * 대상의 탄을 막아 주고, 옆으로 살짝 내밀면 대상을 쏠 수 있으며, 실제로 걸어갈 수 있는 엄폐 칸을 찾는다.
     *
     * @param range 내민 위치에서 대상까지 허용하는 최대 거리. 무기의 사거리다.
     * @return 찾지 못하면 null
     */
    @Nullable
    public static CoverSpot find(PathfinderMob mob, LivingEntity threat, double range) {
        Vec3 threatEye = threat.getEyePosition();
        RandomSource random = mob.getRandom();
        BlockPos origin = mob.blockPosition();
        double rangeSqr = range * range;
        LongSet taken = takenCells(mob, origin);
        LongSet visited = new LongOpenHashSet();
        List<Candidate> candidates = new ArrayList<>();
        for (int i = 0; i < SAMPLE_COUNT; i++) {
            int dx = random.nextInt(SEARCH_RADIUS * 2 + 1) - SEARCH_RADIUS;
            int dz = random.nextInt(SEARCH_RADIUS * 2 + 1) - SEARCH_RADIUS;
            BlockPos cover = findStandable(mob, origin.offset(dx, 0, dz), VERTICAL_RANGE);
            if (cover == null || !visited.add(cover.asLong()) || taken.contains(cover.asLong())
                    || !mob.isWithinHome(cover)) {
                continue;
            }
            double threatDistance = Vec3.atBottomCenterOf(cover).distanceTo(threat.position());
            if (threatDistance < MIN_THREAT_DISTANCE || threatDistance > range) {
                continue;
            }
            if (!isProtected(mob, cover, threatEye)) {
                continue;
            }
            Vec3 peek = findPeek(mob, cover, threatEye, rangeSqr);
            if (peek == null) {
                continue;
            }
            // 가까운 칸을 먼저 쓰되, 대상과의 거리가 사거리의 60% 근처인 칸을 조금 더 선호한다.
            double score = mob.position().distanceTo(Vec3.atBottomCenterOf(cover))
                    + 0.5 * Math.abs(threatDistance - range * 0.6);
            candidates.add(new Candidate(cover, peek, score));
        }
        candidates.sort(Comparator.comparingDouble(Candidate::score));
        int pathChecks = Math.min(PATH_CHECKS, candidates.size());
        for (int i = 0; i < pathChecks; i++) {
            Candidate candidate = candidates.get(i);
            Path path = mob.getNavigation().createPath(candidate.cover(), 0);
            if (path != null && path.canReach()) {
                return new CoverSpot(candidate.cover(), candidate.peek(), path);
            }
        }
        return null;
    }

    /**
     * 다른 몬스터가 서 있거나 숨으려고 잡아 둔 칸.
     * <p>
     * 여럿이 한 칸으로 몰리면 겹쳐 선 몬스터가 칸 밖으로 밀려나 드러난다. 다른 몬스터도 자기 주변 탐색 반경
     * 안에서 칸을 잡으므로, 그 칸까지 놓치지 않도록 탐색 반경의 두 배 안의 몬스터를 살핀다.
     */
    private static LongSet takenCells(PathfinderMob mob, BlockPos origin) {
        int horizontal = SEARCH_RADIUS * 2 + 1;
        int vertical = VERTICAL_RANGE * 2 + 1;
        AABB area = new AABB(origin).inflate(horizontal, vertical, horizontal);
        LongSet taken = new LongOpenHashSet();
        for (Mob other : mob.level().getEntitiesOfClass(Mob.class, area, other -> other != mob && other.isAlive())) {
            taken.add(other.blockPosition().asLong());
            if (other instanceof CoverCombatant combatant && combatant.tacz$getCoverPos() != null) {
                taken.add(combatant.tacz$getCoverPos().asLong());
            }
        }
        return taken;
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
     * 엄폐 칸 한가운데에서 좌우로 0.5~1칸만 몸을 내밀어 대상이 보이는 위치를 찾는다. 덜 내미는 위치부터 살핀다.
     * <p>
     * 옆 칸이 설 수 있는 칸일 때만 내민다. 1칸을 다 내밀면 옆 칸에 올라서고, 덜 내밀어도 몸 일부가 옆 칸 위에 걸치기 때문이다.
     * 시야는 바닐라 시야 판정과 같은 블록 충돌 기준으로 본다. 몬스터 총기와 활은 이 시야가 트여야 쏜다.
     *
     * @return 내민 발 위치. 찾지 못하면 null
     */
    @Nullable
    public static Vec3 findPeek(PathfinderMob mob, BlockPos cover, Vec3 threatEye, double rangeSqr) {
        Level level = mob.level();
        Vec3 center = Vec3.atBottomCenterOf(cover);
        Vec3 toThreat = new Vec3(threatEye.x - center.x, 0, threatEye.z - center.z);
        Direction side = Direction.getApproximateNearest(-toThreat.z, 0, toThreat.x);
        List<Direction> openSides = new ArrayList<>(2);
        for (Direction direction : new Direction[]{side, side.getOpposite()}) {
            if (isStandable(mob, cover.relative(direction))) {
                openSides.add(direction);
            }
        }
        EntityDimensions dimensions = mob.getDimensions(mob.getPose());
        for (double offset : PEEK_OFFSETS) {
            for (Direction direction : openSides) {
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

    private record Candidate(BlockPos cover, Vec3 peek, double score) {
    }
}
