package com.mcserver.serverutilities.combat;

import com.mcserver.serverutilities.ServerUtilities;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.animal.golem.IronGolem;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.raid.Raid;

public final class CombatRules {
    private static final Component KILL_MESSAGE = Component.literal("전투 중 비행은 금지되어 있습니다 ~");

    private CombatRules() { }

    public static void tick(ServerPlayer player) {
        var config = ServerUtilities.config();
        if (!player.isAlive() || (!config.combatElytra() && !config.combatGolems())) return;

        var level = player.level();
        Raid raid = level.getRaidAt(player.blockPosition());
        boolean raidActive = raid != null && raid.isActive() && !raid.isStopped();
        var bounds = player.getBoundingBox().inflate(config.combatRange());
        if (!raidActive && level.getEntitiesOfClass(WitherBoss.class, bounds, WitherBoss::isAlive).isEmpty()) return;

        if (config.combatGolems()) {
            for (IronGolem golem : level.getEntitiesOfClass(IronGolem.class, bounds, IronGolem::isAlive)) {
                if (golem.isAlive()) golem.kill(level);
            }
        }
        if (config.combatElytra() && player.isFallFlying()) {
            level.getServer().getPlayerList().broadcastSystemMessage(KILL_MESSAGE, false);
            player.kill(level);
        }
    }
}
