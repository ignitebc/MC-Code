package com.tacz.guns.event;

import com.tacz.guns.util.CycleTaskHelper;
import net.minecraft.server.MinecraftServer;

public class ServerTickEvent {
    public static void onServerTick(MinecraftServer server) {
        // CycleTaskHelper의 작업 갱신
        CycleTaskHelper.tick();
    }
}
