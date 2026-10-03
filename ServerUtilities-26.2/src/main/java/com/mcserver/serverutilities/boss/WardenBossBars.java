package com.mcserver.serverutilities.boss;

import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.EntityTrackingEvents;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.world.BossEvent;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.monster.warden.Warden;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.UUID;

/** 위더처럼 개체를 추적하는 플레이어에게 바닐라 보스바를 전송한다. */
public final class WardenBossBars {
    private static final Map<Warden, ServerBossEvent> BARS = new IdentityHashMap<>();

    private WardenBossBars() { }

    public static void register() {
        EntityTrackingEvents.START_TRACKING.register((entity, player) -> {
            if (!(entity instanceof Warden warden) || !warden.isAlive()) return;
            ServerBossEvent bar = BARS.computeIfAbsent(warden, boss -> new ServerBossEvent(
                    UUID.randomUUID(), boss.getDisplayName(), BossEvent.BossBarColor.BLUE, BossEvent.BossBarOverlay.PROGRESS));
            update(warden, bar);
            bar.addPlayer(player);
        });
        EntityTrackingEvents.STOP_TRACKING.register((entity, player) -> {
            if (!(entity instanceof Warden warden)) return;
            ServerBossEvent bar = BARS.get(warden);
            if (bar == null) return;
            bar.removePlayer(player);
            if (bar.getPlayers().isEmpty()) BARS.remove(warden);
        });
        ServerEntityEvents.ENTITY_UNLOAD.register((entity, level) -> {
            if (!(entity instanceof Warden warden)) return;
            ServerBossEvent bar = BARS.remove(warden);
            if (bar != null) bar.removeAllPlayers();
        });
        ServerPlayerEvents.LEAVE.register(player -> {
            var iterator = BARS.values().iterator();
            while (iterator.hasNext()) {
                ServerBossEvent bar = iterator.next();
                bar.removePlayer(player);
                if (bar.getPlayers().isEmpty()) iterator.remove();
            }
        });
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            var iterator = BARS.entrySet().iterator();
            while (iterator.hasNext()) {
                var entry = iterator.next();
                Warden warden = entry.getKey();
                if (!warden.isAlive() || warden.isRemoved()) {
                    entry.getValue().removeAllPlayers();
                    iterator.remove();
                    continue;
                }
                update(warden, entry.getValue());
            }
        });
        ServerLifecycleEvents.SERVER_STOPPED.register(server -> {
            BARS.values().forEach(ServerBossEvent::removeAllPlayers);
            BARS.clear();
        });
    }

    private static void update(Warden warden, ServerBossEvent bar) {
        bar.setName(warden.getDisplayName());
        bar.setProgress(Math.clamp(warden.getHealth() / warden.getMaxHealth(), 0.0F, 1.0F));
        // 땅속으로 돌아가는 워든은 전투에서 퇴장하므로 추적이 끝나기 전에도 숨긴다.
        bar.setVisible(warden.getPose() != Pose.DIGGING);
    }
}
