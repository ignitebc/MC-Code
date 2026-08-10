package com.daqem.arc.event.triggers;

import net.minecraft.core.BlockPos;

/**
 * 블록 파괴로 실제 드롭된 경험치를 기록해 BREAK_BLOCK 액션에 전달한다.
 * 26.2에서 Architectury BREAK 이벤트가 경험치 값을 제공하지 않으므로,
 * Block#popExperience 호출을 Mixin으로 가로채 파괴 위치별 드롭량을 기록한다.
 * 같은 틱, 같은 위치의 기록만 유효한 값으로 소비해 이전 파괴의 잔여값이 섞이지 않게 한다.
 */
public class BlockBreakExpTracker {

    private static BlockPos lastPos;
    private static long lastGameTime;
    private static int lastExp;

    private BlockBreakExpTracker() {
    }

    public static void record(BlockPos pos, long gameTime, int exp) {
        boolean isSameBreak = pos.equals(lastPos) && gameTime == lastGameTime;
        if (isSameBreak) {
            lastExp += exp;
        } else {
            lastPos = pos.immutable();
            lastGameTime = gameTime;
            lastExp = exp;
        }
    }

    public static int consume(BlockPos pos, long gameTime) {
        boolean isSameBreak = pos.equals(lastPos) && gameTime == lastGameTime;
        if (!isSameBreak) {
            return 0;
        }
        int exp = lastExp;
        lastPos = null;
        lastExp = 0;
        return exp;
    }
}
