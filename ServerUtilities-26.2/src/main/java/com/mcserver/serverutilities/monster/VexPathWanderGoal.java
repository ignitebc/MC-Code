package com.mcserver.serverutilities.monster;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.Vex;
import net.minecraft.world.level.pathfinder.Path;

import java.util.EnumSet;

/**
 * 대상이 없는 벡스가 소환된 자리 근처를 떠돈다. 바닐라 무작위 이동 대신 쓴다.
 * <p>
 * 바닐라와 같은 범위(가로 ±7, 세로 ±5)·속도에서 빈 공중 칸을 고르되, 일직선으로 가지 않고 비행 길찾기로 닿을 수 있는
 * 칸만 골라 벽에 박지 않는다. 한 번 출발시키면 끝나고, 도착하면 다시 고른다.
 */
public class VexPathWanderGoal extends Goal {
    private static final int HORIZONTAL_RANGE = 7;
    private static final int VERTICAL_RANGE = 5;
    private static final int ATTEMPTS = 3;
    /** 바닐라 무작위 이동과 같은 속도 */
    private static final double WANDER_SPEED = 0.25;
    /** 바닐라와 같은 출발 빈도. 대략 이 틱 수에 한 번 출발한다. */
    private static final int START_CHANCE_TICKS = 7;

    private final Vex vex;

    public VexPathWanderGoal(Vex vex) {
        this.vex = vex;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        if (!VexFlightRules.enabled() || this.vex.getTarget() != null) return false;
        if (!this.vex.getNavigation().isDone()) return false;
        return this.vex.getRandom().nextInt(reducedTickDelay(START_CHANCE_TICKS)) == 0;
    }

    @Override
    public boolean canContinueToUse() {
        return false;
    }

    @Override
    public void start() {
        BlockPos origin = this.vex.getBoundOrigin();
        if (origin == null) origin = this.vex.blockPosition();
        RandomSource random = this.vex.getRandom();
        for (int attempt = 0; attempt < ATTEMPTS; attempt++) {
            BlockPos destination = origin.offset(
                    random.nextIntBetweenInclusive(-HORIZONTAL_RANGE, HORIZONTAL_RANGE),
                    random.nextIntBetweenInclusive(-VERTICAL_RANGE, VERTICAL_RANGE),
                    random.nextIntBetweenInclusive(-HORIZONTAL_RANGE, HORIZONTAL_RANGE));
            if (!this.vex.level().isEmptyBlock(destination)) continue;
            Path path = this.vex.getNavigation().createPath(destination, 0);
            if (path != null && path.canReach()) {
                this.vex.getNavigation().moveTo(path, WANDER_SPEED);
                return;
            }
        }
    }
}
