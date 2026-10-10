package com.tacz.guns.client.event;

import com.tacz.guns.client.gui.GunRefitScreen;
import com.tacz.guns.client.gui.GunSmithTableScreen;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

import java.util.concurrent.atomic.AtomicBoolean;

@Environment(EnvType.CLIENT)
public class PreventsHotbarEvent {
    public static void onRenderHotbarEvent(AtomicBoolean cancelled) {
        // TODO 동작 확인 필요
        Screen screen = Minecraft.getInstance().gui.screen();
        // 총기 제작대 화면은 배경을 끈다
        if (screen instanceof GunSmithTableScreen) {
            cancelled.set(true);
            return;
        }
        // 총기 개조 화면은 배경을 끈다
        if (screen instanceof GunRefitScreen) {
            cancelled.set(true);
        }
    }
}
