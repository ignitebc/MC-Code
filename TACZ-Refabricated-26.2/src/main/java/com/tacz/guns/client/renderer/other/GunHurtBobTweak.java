package com.tacz.guns.client.renderer.other;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;

public class GunHurtBobTweak {
    private static long hurtByGunTimeStamp = -1L;
    private static float lastTweakMultiplier = 0.05f;

    public static boolean onHurtBobTweak(LocalPlayer player, PoseStack matrixStack, float partialTicks) {
        // 바닐라 피격 지속 시간은 500ms이므로, 그보다 크면 탄환 때문에 생긴 피해가 아니다
        if (System.currentTimeMillis() - hurtByGunTimeStamp > 500) {
            // false를 돌려줘 바닐라 피격 흔들림을 호출하게 한다
            return false;
        }
        float zRot = (float) player.hurtTime - partialTicks;
        if (zRot < 0) {
            return true;
        }
        zRot /= (float) player.hurtDuration;
        zRot = Mth.sin(zRot * zRot * zRot * zRot * (float) Math.PI);
        float yRot = player.getHurtDir();

        yRot = yRot * lastTweakMultiplier;
        zRot = zRot * lastTweakMultiplier;

        matrixStack.mulPose(Axis.YP.rotationDegrees(-yRot));
        matrixStack.mulPose(Axis.XP.rotationDegrees(-zRot * 14.0F));
        matrixStack.mulPose(Axis.YP.rotationDegrees(yRot));
        return true;
    }

    public static void markTimestamp(float tweakMultiplier) {
        hurtByGunTimeStamp = System.currentTimeMillis();
        lastTweakMultiplier = tweakMultiplier;
    }
}
