package com.tacz.guns.client.input;

import cn.sh1rocu.tacz.api.event.InputEvent;
import com.mojang.blaze3d.platform.InputConstants;
import com.tacz.guns.api.client.gameplay.IClientPlayerGunOperator;
import com.tacz.guns.api.item.IGun;
import com.tacz.guns.config.client.KeyConfig;
import com.tacz.guns.util.InputExtraCheck;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import org.lwjgl.glfw.GLFW;

import static com.tacz.guns.util.InputExtraCheck.isInGame;

@Environment(EnvType.CLIENT)
public class AimKey {
    public static final KeyMapping AIM_KEY = new KeyMapping("key.tacz.aim.desc",
            InputConstants.Type.MOUSE,
            GLFW.GLFW_MOUSE_BUTTON_RIGHT,
            TaCZKeyCategory.TACZ);

    public static void onAimPress(InputEvent.MouseButton.Post event) {
        if (isInGame() && AIM_KEY.matches(InputConstants.Type.MOUSE.getOrCreate(event.getButton()))) {
            LocalPlayer player = Minecraft.getInstance().player;
            if (player == null || player.isSpectator()) {
                return;
            }
            if (!(player instanceof IClientPlayerGunOperator operator)) {
                return;
            }
            if (IGun.mainHandHoldGun(player)) {
                boolean holdToAim = isHoldToAim(player);
                boolean action = true;
                if (!holdToAim) {
                    action = !operator.isAim();
                }
                if (event.getAction() == GLFW.GLFW_PRESS) {
                    IClientPlayerGunOperator.fromLocalPlayer(player).aim(action);
                }
                if (holdToAim && event.getAction() == GLFW.GLFW_RELEASE) {
                    IClientPlayerGunOperator.fromLocalPlayer(player).aim(false);
                }
            }
        }
    }

    /**
     * 该监听器能正确处理 按住瞄准模式 下的
     * 1.预输入（典型：按住瞄准切换武器后，能保持瞄准状态）
     * 2.键盘按键输入
     *
     * 建议将按下切换瞄准也支持 键盘按键输入
     * */
    public static void onAimHoldingPreInput(Minecraft client) {
        if (!KeyConfig.HOLD_TO_AIM.get()) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        boolean press = AimKey.AIM_KEY.isDown();
        if (InputExtraCheck.isInGame()) {
            LocalPlayer player = mc.player;
            if (player == null || player.isSpectator()) {
                return;
            }
            if (!(player instanceof IClientPlayerGunOperator operator)) {
                return;
            }
            // 배율 조준경은 토글 조준이므로 키를 뗐다고 조준을 풀지 않는다.
            if (!isHoldToAim(player)) {
                return;
            }
            if (operator.isAim() && press) {
                return;
            }
            if (!operator.isAim()) {
                if (!press) {
                    return;
                }
            }
            if (IGun.mainHandHoldGun(player)) {
                IClientPlayerGunOperator.fromLocalPlayer(player).aim(press);
            }
        }
    }

    public static boolean onAimControllerPress(boolean isPress) {
        if (!isInGame()) {
            return false;
        }
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null || player.isSpectator()) {
            return false;
        }
        if (!(player instanceof IClientPlayerGunOperator operator)) {
            return false;
        }
        if (!IGun.mainHandHoldGun(player)) {
            return false;
        }
        boolean holdToAim = isHoldToAim(player);
        boolean action = true;
        if (!holdToAim) {
            action = !operator.isAim();
        }
        if (isPress) {
            IClientPlayerGunOperator.fromLocalPlayer(player).aim(action);
            return true;
        }
        if (holdToAim) {
            IClientPlayerGunOperator.fromLocalPlayer(player).aim(false);
            return true;
        }
        return false;
    }

    /**
     * 조준 키를 누르고 있는 동안만 조준하는지.
     *
     * <p>배율 조준경(부착형·일체형)이 달린 총은 설정과 관계없이 한 번 누르면 조준을 유지하고 다시 누르면 푼다.
     * 레드닷·홀로그래픽과 기본 가늠자는 {@link KeyConfig#HOLD_TO_AIM} 설정을 따른다.
     */
    private static boolean isHoldToAim(LocalPlayer player) {
        if (ScopeZoomWheel.hasMagnifiedScope(player.getMainHandItem())) {
            return false;
        }
        return KeyConfig.HOLD_TO_AIM.get();
    }

    public static void cancelAim(Minecraft mc) {
        LocalPlayer player = mc.player;
        if (!(player instanceof IClientPlayerGunOperator operator)) {
            return;
        }
        if (operator.isAim() && (!isInGame() || player.isSpectator())) {
            IClientPlayerGunOperator.fromLocalPlayer(player).aim(false);
        }
    }
}
