package com.mcserver.serverutilities.death;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.ChestType;

/**
 * 사망 지점에 놓이는 유품 상자. 바닐라 상자와 같은 모양이지만 별도 블록이라
 * 옆 상자와 합쳐지지 않고, 회수·만료 전에는 어떤 경로로도 파괴되지 않는다.
 */
public final class DeathChestBlock extends ChestBlock {
    public static final MapCodec<DeathChestBlock> CODEC = simpleCodec(DeathChestBlock::new);

    public DeathChestBlock(Properties properties) {
        super(() -> DeathChests.BLOCK_ENTITY, SoundEvents.CHEST_OPEN, SoundEvents.CHEST_CLOSE, properties);
    }

    @Override
    public MapCodec<DeathChestBlock> codec() {
        return CODEC;
    }

    // 바닐라 상자는 자기 블록 종류만 짝으로 인정하므로 유품 상자 옆에 놓아도 합쳐지지 않는다.
    // 유품 상자 쪽에서도 어떤 이웃과도 연결하지 않아 항상 단일 상자로 유지한다.
    @Override
    public boolean chestCanConnectTo(BlockState state) {
        return false;
    }

    @Override
    protected ChestType getChestType(Level level, BlockPos pos, Direction facing) {
        return ChestType.SINGLE;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new DeathChestBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        // 클라이언트는 바닐라 뚜껑 애니메이션을 그대로 쓰고, 서버는 만료·회수 타이머를 돌린다.
        if (level.isClientSide()) return super.getTicker(level, state, type);
        return createTickerHelper(type, DeathChests.BLOCK_ENTITY, DeathChestBlockEntity::serverTick);
    }
}
