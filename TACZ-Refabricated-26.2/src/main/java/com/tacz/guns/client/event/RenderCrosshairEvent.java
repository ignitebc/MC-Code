package com.tacz.guns.client.event;

import cn.sh1rocu.simplebedrockmodel.api.event.RenderTickEvent;
import com.mojang.blaze3d.platform.Window;
import com.tacz.guns.GunMod;
import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.api.client.animation.statemachine.AnimationStateContext;
import com.tacz.guns.api.client.animation.statemachine.AnimationStateMachine;
import com.tacz.guns.api.client.gameplay.IClientPlayerGunOperator;
import com.tacz.guns.api.entity.IGunOperator;
import com.tacz.guns.api.entity.ReloadState;
import com.tacz.guns.api.item.IGun;
import com.tacz.guns.client.gui.GunRefitScreen;
import com.tacz.guns.client.renderer.crosshair.CrosshairType;
import com.tacz.guns.config.client.RenderConfig;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;


@Environment(EnvType.CLIENT)
public class RenderCrosshairEvent {
    private static final Identifier HIT_ICON = Identifier.fromNamespaceAndPath(GunMod.MOD_ID, "textures/crosshair/hit/hit_marker.png");
    private static final long KEEP_TIME = 300;
    private static boolean isRefitScreen = false;
    private static long hitTimestamp = -1L;
    private static long killTimestamp = -1L;
    private static long headShotTimestamp = -1L;

    /**
     * 플레이어가 총을 들고 있을 때 특정 애니메이션을 재생하거나 조준하면 조준선을 숨겨야 한다
     */
    public static void onRenderOverlay(GuiGraphicsExtractor guiGraphics, Window window) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) {
            return;
        }
        if (!IGun.mainHandHoldGun(player)) {
            return;
        }

        // 명중 표시
        renderHitMarker(guiGraphics, window);
        // 재장전 중에는 조준선을 그리지 않는다
        ReloadState reloadState = IGunOperator.fromLivingEntity(player).getSynReloadState();
        if (reloadState.getStateType().isReloading()) {
            return;
        }
        // 총기 개조 화면을 열면 조준선을 그리지 않는다
        if (isRefitScreen) {
            return;
        }
        // 재생 중인 애니메이션이 조준선을 숨겨야 하면 그리지 않는다
        ItemStack stack = player.getMainHandItem();
        if (!(stack.getItem() instanceof IGun)) {
            return;
        }

        IClientPlayerGunOperator playerGunOperator = IClientPlayerGunOperator.fromLocalPlayer(player);
        TimelessAPI.getGunDisplay(stack).ifPresent(gunIndex -> {
            // 조준이 거의 끝나면 조준선을 그리지 않는다
            if (playerGunOperator.getClientAimingProgress(Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(true)) > 0.9) {
                // 총기 팩이 조준선 표시를 강제할 수 있다
                boolean forceShow = gunIndex.isShowCrosshair();
                // 어깨 너머 시점은 조준선 표시를 강제할 수 있다
                // 두 강제 조건이 모두 없을 때만 숨길 수 있다
                if (!forceShow) {
                    return;
                }
            }

            AnimationStateMachine<?> animationStateMachine = gunIndex.getAnimationStateMachine();
            if (animationStateMachine == null) {
                renderCrosshair(guiGraphics, window);
                return;
            }
            AnimationStateContext context = animationStateMachine.getContext();
            if (context == null || !context.shouldHideCrossHair()) {
                renderCrosshair(guiGraphics, window);
            }
        });
    }

    public static void onRenderTick(RenderTickEvent event) {
        // 놀랍게도 RenderGameOverlayEvent.PreLayer 이벤트에서는 screen이 아직 정해지지 않았다...
        isRefitScreen = Minecraft.getInstance().gui.screen() instanceof GunRefitScreen;
    }

    private static void renderCrosshair(GuiGraphicsExtractor graphics, Window window) {
        Options options = Minecraft.getInstance().options;
        // 어깨 너머 시점은 조준선 표시를 강제할 수 있다
        if (!options.getCameraType().isFirstPerson()) {
            return;
        }
        // 26.2: options.hideGui는 제거되었고, GUI 표시 여부는 이제 Hud 렌더 파이프라인이 관리한다
        MultiPlayerGameMode gameMode = Minecraft.getInstance().gameMode;
        if (gameMode == null) {
            return;
        }
        if (gameMode.getPlayerMode() == GameType.SPECTATOR) {
            return;
        }
        int width = window.getGuiScaledWidth();
        int height = window.getGuiScaledHeight();

        Identifier location = CrosshairType.getTextureLocation(RenderConfig.CROSSHAIR_TYPE.get());

        float x = width / 2f - 8;
        float y = height / 2f - 8;
        // 26.2: 혼합은 이제 RenderPipeline이 처리하며, 색은 마지막 int 인자(ARGB)로 넘긴다
        graphics.blit(RenderPipelines.GUI_TEXTURED, location, (int) x, (int) y, 0, 0, 16, 16, 16, 16, 0xE6FFFFFF);
    }

    private static void renderHitMarker(GuiGraphicsExtractor graphics, Window window) {
        long remainHitTime = System.currentTimeMillis() - hitTimestamp;
        long remainKillTime = System.currentTimeMillis() - killTimestamp;
        long remainHeadShotTime = System.currentTimeMillis() - headShotTimestamp;
        float offset = RenderConfig.HIT_MARKET_START_POSITION.get().floatValue();
        float fadeTime;

        if (remainKillTime > KEEP_TIME) {
            if (remainHitTime > KEEP_TIME) {
                return;
            } else {
                fadeTime = remainHitTime;
            }
        } else {
            // 최대 이동은 4픽셀이다
            offset += (remainKillTime * 4f) / KEEP_TIME;
            fadeTime = remainKillTime;
        }

        int width = window.getGuiScaledWidth();
        int height = window.getGuiScaledHeight();
        float x = width / 2f - 8;
        float y = height / 2f - 8;

        // 26.2: 혼합은 이제 RenderPipeline이 처리하며, 색은 마지막 int 인자(ARGB)로 넘긴다
        int color;
        if (remainHeadShotTime > KEEP_TIME) {
            color = ((int) ((1 - fadeTime / KEEP_TIME) * 255) << 24) | 0x00FFFFFF;
        } else {
            color = ((int) ((1 - fadeTime / KEEP_TIME) * 255) << 24) | 0x00FF0000;
        }

        graphics.blit(RenderPipelines.GUI_TEXTURED, HIT_ICON, (int) (x - offset), (int) (y - offset), 0, 0, 8, 8, 16, 16, color);
        graphics.blit(RenderPipelines.GUI_TEXTURED, HIT_ICON, (int) (x + 8 + offset), (int) (y - offset), 8, 0, 8, 8, 16, 16, color);
        graphics.blit(RenderPipelines.GUI_TEXTURED, HIT_ICON, (int) (x - offset), (int) (y + 8 + offset), 0, 8, 8, 8, 16, 16, color);
        graphics.blit(RenderPipelines.GUI_TEXTURED, HIT_ICON, (int) (x + 8 + offset), (int) (y + 8 + offset), 8, 8, 8, 8, 16, 16, color);
    }

    public static void markHitTimestamp() {
        RenderCrosshairEvent.hitTimestamp = System.currentTimeMillis();
    }

    public static void markKillTimestamp() {
        RenderCrosshairEvent.killTimestamp = System.currentTimeMillis();
    }

    public static void markHeadShotTimestamp() {
        RenderCrosshairEvent.headShotTimestamp = System.currentTimeMillis();
    }
}
