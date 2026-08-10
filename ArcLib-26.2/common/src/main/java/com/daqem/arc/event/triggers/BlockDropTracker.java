package com.daqem.arc.event.triggers;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * 블록 파괴로 실제 드롭된 아이템을 기록해 드롭 배수 보상이 그대로 복제할 수 있게 한다.
 * 루트 테이블을 다시 굴리면 실제 획득량과 다른 수량이 지급될 수 있으므로,
 * Block#popResource 호출을 Mixin으로 가로채 같은 틱·같은 좌표의 실제 드롭만 소비한다.
 */
public class BlockDropTracker {

    private static BlockPos lastPos;
    private static long lastGameTime;
    private static final List<ItemStack> lastDrops = new ArrayList<>();

    private BlockDropTracker() {
    }

    public static void record(BlockPos pos, long gameTime, ItemStack itemStack) {
        boolean isSameBreak = pos.equals(lastPos) && gameTime == lastGameTime;
        if (!isSameBreak) {
            lastPos = pos.immutable();
            lastGameTime = gameTime;
            lastDrops.clear();
        }
        lastDrops.add(itemStack.copy());
    }

    public static List<ItemStack> consume(BlockPos pos, long gameTime) {
        boolean isSameBreak = pos.equals(lastPos) && gameTime == lastGameTime;
        if (!isSameBreak) {
            return List.of();
        }
        List<ItemStack> drops = new ArrayList<>(lastDrops);
        lastPos = null;
        lastDrops.clear();
        return drops;
    }
}
