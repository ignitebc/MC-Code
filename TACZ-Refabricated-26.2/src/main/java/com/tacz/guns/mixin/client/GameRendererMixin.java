package com.tacz.guns.mixin.client;

import cn.sh1rocu.simplebedrockmodel.api.event.RenderTickEvent;
import cn.sh1rocu.tacz.compat.iris.IrisHandPass;
import com.mojang.blaze3d.vertex.PoseStack;
import com.tacz.guns.api.client.event.RenderItemInHandBobEvent;
import com.tacz.guns.api.client.event.RenderLevelBobEvent;
import com.tacz.guns.client.render.scope.ScopeMaskRenderer;
import com.tacz.guns.client.renderer.other.GunHurtBobTweak;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import org.joml.Matrix4fc;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** CameraRenderState 시그니처를 쓰는 26.2 흔들림 훅. */
@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {
    @Shadow @Final private Minecraft minecraft;

    @Unique
    private boolean tacz$renderingItemInHand;

    @Inject(method = "renderItemInHand", at = @At("HEAD"))
    private void tacz$beginHandPass(CameraRenderState cameraState,
                                    float partialTick,
                                    Matrix4fc projection,
                                    CallbackInfo ci) {
        this.tacz$renderingItemInHand = true;
        // renderAllFeatures는 프레임마다 여러 번 호출되며(월드 한 번, 손에 든 것 한 번),
        // 조준경은 손에 든 것을 그릴 때만 있다. 마스크는 그때만 그려야 한다. 아니면 월드 차례가 먼저
        // target을 비워 손에 든 것 차례의 결과를 지워 버린다.
        ScopeMaskRenderer.setInHandPass(true);
    }

    @Inject(method = "renderItemInHand", at = @At("RETURN"))
    private void tacz$endHandPass(CameraRenderState cameraState,
                                  float partialTick,
                                  Matrix4fc projection,
                                  CallbackInfo ci) {
        this.tacz$renderingItemInHand = false;
        ScopeMaskRenderer.setInHandPass(false);
    }

    @Inject(method = "bobHurt", at = @At("HEAD"), cancellable = true)
    private void tacz$bobHurt(CameraRenderState cameraState, PoseStack poseStack, CallbackInfo ci) {
        if (minecraft.getCameraEntity() instanceof LocalPlayer player && !player.isDeadOrDying()) {
            float partialTick = minecraft.getDeltaTracker().getGameTimeDeltaPartialTick(false);
            if (GunHurtBobTweak.onHurtBobTweak(player, poseStack, partialTick)) {
                ci.cancel();
                return;
            }
        }

        if (this.tacz$isHandPass()) {
            RenderItemInHandBobEvent.BobHurt event = new RenderItemInHandBobEvent.BobHurt();
            RenderItemInHandBobEvent.HURT.invoker().post(event);
            if (event.isCanceled()) {
                ci.cancel();
            }
        } else {
            RenderLevelBobEvent.BobHurt event = new RenderLevelBobEvent.BobHurt();
            RenderLevelBobEvent.HURT.invoker().post(event);
            if (event.isCanceled()) {
                ci.cancel();
            }
        }
    }

    @Inject(method = "bobView", at = @At("HEAD"), cancellable = true)
    private void tacz$bobView(CameraRenderState cameraState, PoseStack poseStack, CallbackInfo ci) {
        if (this.tacz$isHandPass()) {
            RenderItemInHandBobEvent.BobView event = new RenderItemInHandBobEvent.BobView();
            RenderItemInHandBobEvent.VIEW.invoker().post(event);
            if (event.isCanceled()) {
                ci.cancel();
            }
        } else {
            RenderLevelBobEvent.BobView event = new RenderLevelBobEvent.BobView();
            RenderLevelBobEvent.VIEW.invoker().post(event);
            if (event.isCanceled()) {
                ci.cancel();
            }
        }
    }

    @Unique
    private boolean tacz$isHandPass() {
        // Iris 셰이더팩은 손을 HandRenderer에서 따로 그리면서 bobHurt/bobView를 직접 호출한다.
        // 이를 월드 흔들림으로 보면 총기 손 흔들림 취소가 빠져 조준 중 총이 출렁인다.
        return this.tacz$renderingItemInHand || IrisHandPass.isActive();
    }

    @Inject(method = "render", at = @At("HEAD"))
    private void tacz$renderTickStart(DeltaTracker deltaTracker, boolean renderLevel, CallbackInfo ci) {
        RenderTickEvent.EVENT.invoker().onRenderTick(new RenderTickEvent(
                RenderTickEvent.Phase.START,
                deltaTracker.getGameTimeDeltaPartialTick(false)
        ));
    }

    @Inject(method = "render", at = @At("RETURN"))
    private void tacz$renderTickEnd(DeltaTracker deltaTracker, boolean renderLevel, CallbackInfo ci) {
        RenderTickEvent.EVENT.invoker().onRenderTick(new RenderTickEvent(
                RenderTickEvent.Phase.END,
                deltaTracker.getGameTimeDeltaPartialTick(false)
        ));
    }
}
