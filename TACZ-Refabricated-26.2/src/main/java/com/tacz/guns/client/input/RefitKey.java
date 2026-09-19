package com.tacz.guns.client.input;

import cn.sh1rocu.tacz.api.event.InputEvent;
import com.mojang.blaze3d.platform.InputConstants;
import com.tacz.guns.api.item.IGun;
import com.tacz.guns.client.gui.GunRefitScreen;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import org.lwjgl.glfw.GLFW;

import static com.tacz.guns.util.InputExtraCheck.isInGame;

@Environment(EnvType.CLIENT)
public class RefitKey {
    // 키 ID를 예전 "key.tacz.refit.desc"에서 바꿨다. 기본 키를 Z에서 P로 옮겼지만, 이미 게임을 한 번이라도
    // 실행한 클라이언트는 options.txt에 예전 ID로 Z가 저장돼 있어 기본값 변경이 먹히지 않았다.
    // ID가 다르면 저장된 값과 짝이 맞지 않아 모든 클라이언트가 새 기본값(P)으로 시작한다.
    public static final KeyMapping REFIT_KEY = new KeyMapping("key.tacz.refit_screen.desc",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_P,
            TaCZKeyCategory.TACZ);

    public static void onRefitPress(InputEvent.Key event) {
        if (event.getAction() == GLFW.GLFW_PRESS && REFIT_KEY.matches(InputConstants.Type.KEYSYM.getOrCreate(event.getKey()))) {
            LocalPlayer player = Minecraft.getInstance().player;
            if (player == null || player.isSpectator()) {
                return;
            }
            if (isInGame()) {
                if (IGun.mainHandHoldGun(player) && Minecraft.getInstance().gui.screen() == null) {
                    IGun iGun = IGun.getIGunOrNull(player.getMainHandItem());
                    if (iGun != null && iGun.hasAttachmentLock(player.getMainHandItem())) {
                        return;
                    }
                    Minecraft.getInstance().gui.setScreen(new GunRefitScreen());
                }
            } else if (Minecraft.getInstance().gui.screen() instanceof GunRefitScreen refitScreen) {
                refitScreen.onClose();
            }
        }
    }
}
