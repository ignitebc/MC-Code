package com.mcserver.serverutilities.monster;

import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public final class NetherPlayerTargetGoal
        extends NearestAttackableTargetGoal<Player> {
    private final Mob owner;

    public NetherPlayerTargetGoal(Mob owner) {
        super(owner, Player.class, true);
        this.owner = owner;
    }

    @Override
    public boolean canUse() {
        return isActiveInNether() && super.canUse();
    }

    @Override
    public boolean canContinueToUse() {
        return isActiveInNether() && super.canContinueToUse();
    }

    private boolean isActiveInNether() {
        Level level = owner.level();

        // 시작과 유지 양쪽에서 검사하여 차원이 바뀌면 추가 선공을 중단한다.
        return !level.isClientSide()
                && Level.NETHER.equals(level.dimension())
                && level.getDifficulty() != Difficulty.PEACEFUL
                && owner.isAlive()
                && !owner.isNoAi();
    }
}
