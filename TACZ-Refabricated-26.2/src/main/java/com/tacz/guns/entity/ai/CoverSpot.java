package com.tacz.guns.entity.ai;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.pathfinder.Path;

/**
 * 몬스터가 숨을 칸과 사격하려고 잠깐 나올 칸.
 *
 * @param cover 대상의 탄이 몸 어디에도 닿지 않는 칸
 * @param peek  엄폐 칸 옆에서 대상이 보이는 칸
 * @param path  탐색할 때 이미 만든 엄폐 칸까지의 경로. 처음 이동할 때 다시 계산하지 않도록 넘긴다.
 */
public record CoverSpot(BlockPos cover, BlockPos peek, Path path) {
}
