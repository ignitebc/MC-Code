package com.daqem.jobsplus.achievement;

import net.minecraft.world.level.block.state.BlockState;

/** 범위 채굴에서 중첩되는 각 파괴 호출의 원본 상태와 실제 제거 여부. */
public final class BlockBreakSnapshot
{
    private final BlockState state;
    private final boolean natural;
    private boolean removed;

    public BlockBreakSnapshot(BlockState state, boolean natural)
    {
        this.state = state;
        this.natural = natural;
    }

    public BlockState state()
    {
        return state;
    }

    public boolean natural()
    {
        return natural;
    }

    public boolean removed()
    {
        return removed;
    }

    public void confirmRemoved()
    {
        removed = true;
    }
}
