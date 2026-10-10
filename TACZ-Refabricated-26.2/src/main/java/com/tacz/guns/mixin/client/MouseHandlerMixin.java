package com.tacz.guns.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.tacz.guns.api.DefaultAssets;
import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.api.entity.IGunOperator;
import com.tacz.guns.api.item.IGun;
import com.tacz.guns.api.item.attachment.AttachmentType;
import com.tacz.guns.api.item.nbt.AttachmentItemDataAccessor;
import com.tacz.guns.client.input.ScopeZoomWheel;
import com.tacz.guns.client.resource.GunDisplayInstance;
import com.tacz.guns.client.resource.index.ClientAttachmentIndex;
import com.tacz.guns.config.client.ZoomConfig;
import com.tacz.guns.util.math.MathUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;

@Mixin(MouseHandler.class)
public class MouseHandlerMixin {
    /** 가변 배율 조준경으로 조준 중이면 휠을 배율 조정에 쓰고 핫바 칸은 바꾸지 않는다. */
    @Inject(method = "onScroll", at = @At("HEAD"), cancellable = true)
    private void tacz$scrollScopeZoom(long window, double scrollX, double scrollY, CallbackInfo ci) {
        if (ScopeZoomWheel.onScroll(scrollY)) {
            ci.cancel();
        }
    }

    @WrapOperation(method = "turnPlayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;turn(DD)V"))
    public void reduceSensitivity(LocalPlayer player, double yaw, double pitch, Operation<Void> original) {
        ItemStack mainHandItem = player.getMainHandItem();
        IGun iGun = IGun.getIGunOrNull(mainHandItem);
        if (iGun == null) {
            original.call(player, yaw, pitch);
            return;
        }
        Identifier scopeId = iGun.getAttachmentId(mainHandItem, AttachmentType.SCOPE);
        if (scopeId.equals(DefaultAssets.EMPTY_ATTACHMENT_ID)) {
            scopeId = iGun.getBuiltInAttachmentId(mainHandItem, AttachmentType.SCOPE);
        }
        float zoomLevel = 1;
        if (DefaultAssets.isEmptyAttachmentId(scopeId)) {
            // 확대 배율
            zoomLevel = TimelessAPI.getGunDisplay(mainHandItem).map(GunDisplayInstance::getIronZoom).orElse(1f);
        } else {
            Optional<ClientAttachmentIndex> optional = TimelessAPI.getClientAttachmentIndex(scopeId);
            if (optional.isPresent()) {
                float[] zoom = optional.get().getZoom();
                if (zoom != null && zoom.length > 0) {
                    CompoundTag attachmentTag = iGun.getAttachmentTag(mainHandItem, AttachmentType.SCOPE);
                    zoomLevel = zoom[AttachmentItemDataAccessor.getZoomNumberFromTag(attachmentTag) % zoom.length];
                }
            }
        }
        Minecraft minecraft = Minecraft.getInstance();
        float progress = IGunOperator.fromLivingEntity(player).getSynAimingProgress();
        // 조준 감도 계수
        double sensitivityMultiplier = ZoomConfig.ZOOM_SENSITIVITY_BASE_MULTIPLIER.get();
        sensitivityMultiplier = 1 + (sensitivityMultiplier - 1) * progress;
        // 두 상태의 fov 계산
        double originalFov = minecraft.options.fov().get();
        double currentFov = MathUtil.magnificationToFov(1 + (zoomLevel - 1) * progress, originalFov);
        // 화면 거리 계수. MC는 COD처럼 MDV 기준을 쓰며 기본은 MDV133(계수 1.33)이다
        double coefficient = ZoomConfig.SCREEN_DISTANCE_COEFFICIENT.get();
        double denominator = MathUtil.zoomSensitivityRatio(currentFov, originalFov, coefficient) * sensitivityMultiplier;
        // 최종 결과
        double finalYaw = yaw * denominator;
        double finalPitch = getCrawlPitch(player, pitch, denominator);
        original.call(player, finalYaw, finalPitch);
    }

    @Unique
    private static double getCrawlPitch(LocalPlayer player, double pitch, double denominator) {
        double finalPitch = pitch * denominator;
        // 엎드린 상태이면 pitch 범위도 제한해야 한다
        if (!player.isSwimming() && player.getPose() == Pose.SWIMMING) {
            // 올려다보는 각의 부호는 반대다
            float playerPitch = -player.getXRot();
            // 플레이어가 25도 넘게 올려다보면 더 올리지 못하게 한다
            if (playerPitch > 45) {
                finalPitch = Math.max(finalPitch, 0);
            }
            // 25도 넘게 내려다보면 더 내리지 못하게 한다
            if (playerPitch < -30) {
                finalPitch = Math.min(finalPitch, 0);
            }
        }
        return finalPitch;
    }
}
