package com.daqem.arc.player;

import com.daqem.arc.config.ArcCommonConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 이동 보상이 실제 이동에만 지급되도록 틱마다 인정 비율을 계산한다.
 * <p>
 * 바닐라 moveDist 는 방향과 무관한 누적 경로 길이라서 한 칸 위에서 앞뒤로 흔들기만 해도
 * 계속 쌓인다. 이 추적기는 두 가지로 막는다.
 * <ul>
 *     <li>블록 좌표 하나당 인정 거리에 상한을 둔다. 제자리 왕복은 상한까지만 인정된다.</li>
 *     <li>최근에 지나간 좌표는 재방문 대기시간이 지나기 전까지 인정하지 않는다.
 *         두세 칸을 오가는 셔틀은 첫 통과 뒤 막힌다.</li>
 * </ul>
 * 정상 이동은 매 틱 새 좌표로 들어가므로 상한에 걸리지 않고 전액 인정된다.
 */
public class MovementCreditTracker {

    private static final long UNSET = Long.MIN_VALUE;

    private final Map<Long, Long> visitedAtGameTime = new LinkedHashMap<>() {
        @Override
        protected boolean removeEldestEntry(Map.Entry<Long, Long> eldest) {
            return size() > Math.max(64, ArcCommonConfig.movementVisitCacheSize.get());
        }
    };

    private long currentBlockKey = UNSET;
    private boolean currentBlockCreditable = true;
    private double creditedInCurrentBlock = 0.0D;
    private Vec3 lastPosition;

    /**
     * 이번 틱의 이동 중 보상으로 인정할 비율을 돌려준다. 0.0 이면 전혀 인정하지 않고,
     * 1.0 이면 전액 인정한다. 틱당 정확히 한 번만 호출해야 한다.
     */
    public double creditFactor(ServerPlayer player) {
        if (!ArcCommonConfig.movementRequiresNewGround.get()) {
            return 1.0D;
        }

        Vec3 position = player.position();
        double moved = this.lastPosition == null ? 0.0D : position.distanceTo(this.lastPosition);
        this.lastPosition = position;

        BlockPos blockPos = player.blockPosition();
        long key = blockPos.asLong();
        if (key != this.currentBlockKey) {
            this.currentBlockKey = key;
            this.creditedInCurrentBlock = 0.0D;
            this.currentBlockCreditable = arc$enterBlock(key, player.level().getGameTime());
        }

        if (!this.currentBlockCreditable) {
            return 0.0D;
        }
        if (moved <= 1.0E-6D) {
            // 이동이 없으면 배분할 거리도 없다. 다른 누적기가 0 을 더하도록 그대로 통과시킨다.
            return 1.0D;
        }

        double maxPerBlock = ArcCommonConfig.movementMaxCreditPerBlock.get();
        double remaining = Math.max(0.0D, maxPerBlock - this.creditedInCurrentBlock);
        if (remaining <= 0.0D) {
            return 0.0D;
        }

        double granted = Math.min(moved, remaining);
        this.creditedInCurrentBlock += granted;
        return granted / moved;
    }

    /** 새 좌표에 들어갔을 때 인정 여부를 정하고 방문 시각을 갱신한다. */
    private boolean arc$enterBlock(long key, long gameTime) {
        Long lastVisit = this.visitedAtGameTime.get(key);
        long cooldown = ArcCommonConfig.movementRevisitCooldownTicks.get();
        boolean creditable = lastVisit == null || gameTime - lastVisit >= cooldown;
        // 재방문이어도 시각은 갱신한다. 계속 맴돌면 대기시간이 계속 밀린다.
        this.visitedAtGameTime.remove(key);
        this.visitedAtGameTime.put(key, gameTime);
        return creditable;
    }

    public void reset() {
        this.visitedAtGameTime.clear();
        this.currentBlockKey = UNSET;
        this.currentBlockCreditable = true;
        this.creditedInCurrentBlock = 0.0D;
        this.lastPosition = null;
    }
}
