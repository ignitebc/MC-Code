package com.mcserver.serverutilities.spawn;

import net.minecraft.core.BlockPos;

public interface PersonalSpawnAccess {
    /** 배정된 개인 시작 좌표. 아직 배정받지 않았으면 null이다. */
    BlockPos serverutilities$personalSpawn();

    void serverutilities$setPersonalSpawn(BlockPos position);
}
