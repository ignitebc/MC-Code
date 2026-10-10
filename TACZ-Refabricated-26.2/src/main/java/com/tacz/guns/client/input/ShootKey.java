package com.tacz.guns.client.input;

import cn.sh1rocu.tacz.api.event.InputEvent;
import com.mojang.blaze3d.platform.InputConstants;
import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.api.client.gameplay.IClientPlayerGunOperator;
import com.tacz.guns.api.entity.ShootResult;
import com.tacz.guns.api.item.IGun;
import com.tacz.guns.api.item.gun.FireMode;
import com.tacz.guns.client.gameplay.LocalPlayerSprint;
import com.tacz.guns.client.sound.SoundPlayManager;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.ItemStack;
import org.lwjgl.glfw.GLFW;

import static com.tacz.guns.util.InputExtraCheck.isInGame;

@Environment(EnvType.CLIENT)
public class ShootKey {
    public static final KeyMapping SHOOT_KEY = new KeyMapping("key.tacz.shoot.desc",
            InputConstants.Type.MOUSE,
            GLFW.GLFW_MOUSE_BUTTON_LEFT,
            TaCZKeyCategory.TACZ);
    /**
     * 사격 키를 누른 입력을 기억해 두는 시간(ms).
     * <p>
     * 사격 키는 틱이 끝날 때 눌려 있는지로만 판단해서, 장전·연사 간격·총 꺼내기가 끝나기 직전에 짧게 누르고 뗀 클릭이나
     * 틱 사이에 누르고 뗀 클릭은 그대로 사라진다. 장전이 끝나자마자 누른 첫 발이 씹히는 원인이라, 누른 입력을 잠시 기억했다가
     * 쏠 수 있게 되는 즉시 쏜다.
     */
    private static final long SHOOT_INPUT_BUFFER_MS = 200;
    private static boolean lastTimeShootSuccess = false;
    private static boolean controllerShootDown = false;
    private static long bufferedShootPressTimestamp = -1;

    public static void onShootMousePress(InputEvent.MouseButton.Post event) {
        boolean pressed = event.getAction() == GLFW.GLFW_PRESS;
        if (pressed && SHOOT_KEY.matches(InputConstants.Type.MOUSE.getOrCreate(event.getButton()))) {
            bufferShootPress();
        }
    }

    public static void onShootKeyPress(InputEvent.Key event) {
        boolean pressed = event.getAction() == GLFW.GLFW_PRESS;
        if (pressed && SHOOT_KEY.matches(InputConstants.Type.KEYSYM.getOrCreate(event.getKey()))) {
            bufferShootPress();
        }
    }

    private static void bufferShootPress() {
        if (!isInGame()) {
            return;
        }
        bufferedShootPressTimestamp = System.currentTimeMillis();
        // 새로 당긴 방아쇠다. 틱 사이에 떼었다 다시 눌러 손을 뗀 틱이 없었어도 단발을 다시 쏠 수 있게 한다.
        lastTimeShootSuccess = false;
    }

    private static boolean isShootPressBuffered() {
        if (bufferedShootPressTimestamp < 0) {
            return false;
        }
        return System.currentTimeMillis() - bufferedShootPressTimestamp <= SHOOT_INPUT_BUFFER_MS;
    }

    /** 잠깐 뒤에 다시 쏘면 성공할 수 있는 실패인지. 탄약이 없거나 노리쇠를 당겨야 하는 등은 기억한 입력을 버린다. */
    private static boolean isTemporaryFailure(ShootResult result) {
        return switch (result) {
            case COOL_DOWN, IS_RELOADING, IS_DRAWING, IS_BOLTING, IS_MELEE, IS_SPRINTING -> true;
            default -> false;
        };
    }

    public static void autoShoot(Minecraft mc, boolean isPhaseEnd) {
        if (!isPhaseEnd || !isInGame()) {
            return;
        }
        LocalPlayerSprint.stopSprint = false;

        LocalPlayer player = mc.player;
        if (player == null || player.isSpectator()) {
            return;
        }
        ItemStack mainHandItem = player.getMainHandItem();
        if (mainHandItem.getItem() instanceof IGun iGun) {
            FireMode fireMode = iGun.getFireMode(mainHandItem);
            boolean isBurstAuto = fireMode == FireMode.BURST && TimelessAPI.getCommonGunIndex(iGun.getGunId(mainHandItem))
                    .map(index -> index.getGunData().getBurstData().isContinuousShoot())
                    .orElse(false);
            // 충전식 총은 누르고 있는 시간이 곧 충전량이라 손을 뗀 뒤까지 누른 것으로 치지 않는다.
            boolean hasChargeData = TimelessAPI.getCommonGunIndex(iGun.getGunId(mainHandItem))
                    .map(index -> index.getGunData().getChargeData(fireMode) != null)
                    .orElse(false);
            boolean shootBuffered = !hasChargeData && isShootPressBuffered();
            IClientPlayerGunOperator operator = IClientPlayerGunOperator.fromLocalPlayer(player);
            boolean isShootDown = SHOOT_KEY.isDown() || controllerShootDown || shootBuffered;
            boolean canContinuouslyShoot = fireMode == FireMode.AUTO || isBurstAuto;
            boolean shouldCharge = isShootDown && (canContinuouslyShoot || !lastTimeShootSuccess);
            if (operator.chargeShoot(shouldCharge)) {
                LocalPlayerSprint.stopSprint = true;
                if (!canContinuouslyShoot && lastTimeShootSuccess) {
                    // 자동이 아니면 연속 발사를 막고, 직전 방아쇠를 누르고 있는 동안 충전을 이어가지도 않는다
                    return;
                }
                ShootResult result = operator.shoot();
                if (result == ShootResult.SUCCESS) {
                    lastTimeShootSuccess = true;
                }
                if (!isTemporaryFailure(result)) {
                    bufferedShootPressTimestamp = -1;
                }
            }
            if (isShootDown) {
                LocalPlayerSprint.stopSprint = true;
            } else {
                lastTimeShootSuccess = false;
                SoundPlayManager.resetDryFireSound();
            }
        }
    }

    public static boolean shootControllerTick(boolean isShootDown) {
        controllerShootDown = isShootDown;
        return false;
    }
}
