package com.tacz.guns.util;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;

@Environment(EnvType.CLIENT)
public final class InputExtraCheck {
    public static boolean isInGame() {
        Minecraft mc = Minecraft.getInstance();
        // 로딩 화면이면 안 된다
        if (mc.gui.overlay() != null) {
            return false;
        }
        // 어떤 GUI도 열려 있으면 안 된다 - 26.2 GUI 재구성: screen -> gui.screen()
        if (mc.gui.screen() != null) {
            return false;
        }
        // 현재 창이 마우스 조작을 잡고 있다
        if (!mc.mouseHandler.isMouseGrabbed()) {
            return false;
        }
        // 현재 창이 선택되어 있다
        return mc.isWindowActive();
    }
}
