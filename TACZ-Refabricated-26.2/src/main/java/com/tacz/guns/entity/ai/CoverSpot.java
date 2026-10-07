package com.tacz.guns.entity.ai;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;

/**
 * 몬스터가 숨을 칸과 사격하려고 몸을 내밀 위치.
 *
 * @param cover 대상의 탄이 몸 어디에도 닿지 않는 칸
 * @param peek  엄폐 칸 한가운데에서 옆으로 0.5~1칸 비켜서 대상이 보이는 발 위치
 * @param path  탐색할 때 이미 만든 엄폐 칸까지의 경로. 처음 이동할 때 다시 계산하지 않도록 넘긴다.
 */
public record CoverSpot(BlockPos cover, Vec3 peek, Path path) {
    /** 엄폐 칸 바닥 한가운데. 몸 전체가 가려지는 기준 위치다. */
    public Vec3 coverCenter() {
        return Vec3.atBottomCenterOf(this.cover);
    }
}
