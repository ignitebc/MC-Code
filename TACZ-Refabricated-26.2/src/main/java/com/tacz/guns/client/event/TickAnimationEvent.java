package com.tacz.guns.client.event;

import cn.sh1rocu.simplebedrockmodel.api.event.RenderTickEvent;
import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.client.animation.statemachine.GunAnimationConstant;
import com.tacz.guns.client.renderer.item.AnimateGeoItemRenderer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import cn.sh1rocu.tacz.compat.fabric.BuiltinItemRendererRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.ItemStack;

@Environment(EnvType.CLIENT)
public class TickAnimationEvent {
    public static void tickAnimation(Minecraft client) {
        LocalPlayer player = client.player;
        if (player == null) {
            return;
        }
        ItemStack mainHandItem = player.getMainHandItem();
        TimelessAPI.getGunDisplay(mainHandItem).ifPresent(gunIndex -> {
            var animationStateMachine = gunIndex.getAnimationStateMachine();
            if (animationStateMachine == null) {
                return;
            }
            // 연동 서버에서 월드를 옮길 때 생기는 특수 버그 처리. 보통은 이 문제가 생기지 않는다
            if (player.input == null) {
                animationStateMachine.trigger(GunAnimationConstant.INPUT_IDLE);
                return;
            }
            if (!player.isMovingSlowly() && player.isSprinting()) {
                // 플레이어가 움직이면 이동 애니메이션을, 아니면 idle 애니메이션을 재생한다
                animationStateMachine.trigger(GunAnimationConstant.INPUT_RUN);
            } else if (!player.isMovingSlowly() && player.input.getMoveVector().length() > 0.01) {
                animationStateMachine.trigger(GunAnimationConstant.INPUT_WALK);
            } else {
                animationStateMachine.trigger(GunAnimationConstant.INPUT_IDLE);
            }
        });
    }

    public static void tickAnimation(RenderTickEvent event) {
        if (event.phase == RenderTickEvent.Phase.END) {
            return;
        }
        if (Minecraft.getInstance().options.getCameraType().isFirstPerson()) {
            return;
        }
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) {
            return;
        }
        ItemStack mainHandItem = player.getMainHandItem();
        if (BuiltinItemRendererRegistry.INSTANCE.get(mainHandItem.getItem()) instanceof AnimateGeoItemRenderer<?, ?> renderer) {
            // 아이템이 바뀌었으면 먼저 상태 기계를 초기화해 본다
            if (renderer.needReInit(mainHandItem)) {
                renderer.tryInit(mainHandItem, player, event.renderTickTime);
            }
            renderer.visualUpdate(mainHandItem);
        }
    }
}
