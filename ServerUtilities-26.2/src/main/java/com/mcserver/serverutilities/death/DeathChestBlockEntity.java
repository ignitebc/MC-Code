package com.mcserver.serverutilities.death;

import com.mcserver.serverutilities.ServerUtilities;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/** 바닐라 상자 27칸을 그대로 쓰되 설치 시각과 주인을 기억해 만료·회수 시 스스로 사라진다. */
public final class DeathChestBlockEntity extends ChestBlockEntity {
    private static final String CREATED_KEY = "ServerUtilitiesCreatedAt";
    private static final String OWNER_KEY = "ServerUtilitiesOwner";

    // 실제 시각 기준이라 청크 언로드·서버 재시작 중에도 사망 순간부터 계속 흐른다.
    private long createdAt = System.currentTimeMillis();
    private String ownerName = "";
    // 비워진 뒤 경과만 재므로 저장하지 않는다. 다시 로드되면 그 시점부터 다시 센다.
    private long emptiedAt = -1L;

    public DeathChestBlockEntity(BlockPos pos, BlockState state) {
        super(DeathChests.BLOCK_ENTITY, pos, state);
    }

    void initialize(String owner) {
        this.createdAt = System.currentTimeMillis();
        this.ownerName = owner;
        setChanged();
    }

    @Override
    protected Component getDefaultName() {
        return ownerName.isEmpty() ? Component.literal("유품 상자") : Component.literal(ownerName + "님의 유품 상자");
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putLong(CREATED_KEY, createdAt);
        output.putString(OWNER_KEY, ownerName);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        createdAt = input.getLongOr(CREATED_KEY, createdAt);
        ownerName = input.getStringOr(OWNER_KEY, "");
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, DeathChestBlockEntity chest) {
        if (!(level instanceof ServerLevel serverLevel)) return;
        var config = ServerUtilities.config();
        long now = System.currentTimeMillis();
        if (now - chest.createdAt >= config.deathChestExpireSeconds() * 1000L) {
            DeathChests.remove(serverLevel, pos, chest);
            return;
        }
        if (!chest.isEmpty()) {
            chest.emptiedAt = -1L;
            return;
        }
        if (chest.emptiedAt < 0) {
            chest.emptiedAt = now;
        } else if (now - chest.emptiedAt >= config.deathChestEmptySeconds() * 1000L) {
            DeathChests.remove(serverLevel, pos, chest);
        }
    }
}
