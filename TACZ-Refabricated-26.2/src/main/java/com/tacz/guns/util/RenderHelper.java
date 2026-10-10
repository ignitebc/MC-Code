package com.tacz.guns.util;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.PlayerModelPart;

@Environment(EnvType.CLIENT)
public final class RenderHelper {
// TODO[26.2]: BufferUploader 제거됨     // 26.2 이전: blit/innerBlit이 쓰던 Tesselator/BufferUploader/RenderSystem.setShader가 모두 제거되었다
    // 26.2는 지연 렌더링 파이프라인(SubmitNodeCollector)을 쓰며 즉시 모드 렌더링을 더는 지원하지 않는다
    // 2D blit 렌더링이 필요하면 GuiGraphics나 SubmitNodeCollector.submitCustomGeometry로 구현한다

    public static void enableItemEntityStencilTest() {
        // 26.2 Vulkan 호환: 원래 GL11.GL_STENCIL_TEST는 Vulkan 백엔드에서 쓸 수 없다
        // 우선 아무것도 하지 않게 바꿨고, 나중에 GpuFormat.D24_UNORM_S8_UINT + RenderPipeline depth/stencil 상태로 다시 구현해야 한다
        // OpenGL 백엔드를 지원해야 하면 여기서 백엔드 종류를 감지할 수 있다:
        // boolean isVulkan = Minecraft.getInstance().getGpuDevice().getDeviceInfo().backendName().contains("vulkan");
        RenderSystem.assertOnRenderThread();
        // 지금은 아무것도 하지 않는다
    }

    public static void disableItemEntityStencilTest() {
        RenderSystem.assertOnRenderThread();
        // Vulkan 호환을 위해 아무것도 하지 않는다
    }

    /**
     * collector를 인식하는 26.2 1인칭 팔 제출.
     *
     * <p><b>5차 정정: 여기서는 submit 뒤에 PlayerModel을 <u>절대</u> 되돌리면 안 된다.</b></p>
     *
     * <p>4차에서 "3인칭 일그러진 팔"을 고치려고 "스냅숏 + finally 되돌리기"를 넣었는데, 방향이 틀려 오히려 증상을 키웠다.
     * {@code SubmitNodeCollection#submitModel}을 디컴파일하면:</p>
     * <pre>
     * Pose pose = poseStack.last().copy();                        // <b>행렬</b>만 복사한다
     * Submit&lt;S&gt; submit = new Submit(renderType, pose, model, ...); // model은 <b>참조</b>다
     * </pre>
     * <p>그리고 {@code submitModelPart} 내부는 {@code new Model.Simple(modelPart, ...)}로
     * <b>살아 있는 ModelPart 루트 참조</b>를 쥐며, 실제로 정점을 훑는 것은 나중의
     * {@code FeatureRenderDispatcher#renderAllFeatures}에서 일어난다.</p>
     *
     * <p>즉 행렬은 스냅숏되지만 <b>뼈대 자세는 아니다</b>. submit 직후
     * {@code arm.visible}/{@code zRot}/pose를 되돌리면 실제로 그릴 때 읽는 것은 되돌린 뒤의 상태다
     * — 이것이 바로 "팔이 일그러짐"의 직접 원인이다.</p>
     *
     * <p>여기서는 바닐라 의미(쓰고 바로 끝)를 되살린다. 3인칭 오염은 <b>근원</b>에서 막는다:
     * {@code ItemInHandRendererMixin}의 1인칭 시점 관문 참고.</p>
     */
    public static void renderFirstPersonArm(LocalPlayer player,
                                            HumanoidArm hand,
                                            PoseStack matrixStack,
                                            SubmitNodeCollector collector,
                                            int combinedLight) {
        if (player == null) {
            return;
        }
        AvatarRenderer<?> renderer = Minecraft.getInstance().getEntityRenderDispatcher().getPlayerRenderer(player);
        var skinTexture = player.getSkin().body().texturePath();
        if (hand == HumanoidArm.RIGHT) {
            renderer.renderRightHand(matrixStack, collector, combinedLight, skinTexture,
                    player.isModelPartShown(PlayerModelPart.RIGHT_SLEEVE));
        } else {
            renderer.renderLeftHand(matrixStack, collector, combinedLight, skinTexture,
                    player.isModelPartShown(PlayerModelPart.LEFT_SLEEVE));
        }
    }

    /** @deprecated 예전 즉시 경로로는 collector 없이 팔을 그릴 수 없다. */
    @Deprecated
    public static void renderFirstPersonArm(LocalPlayer player, HumanoidArm hand, PoseStack matrixStack, int combinedLight) {
        // 일부러 비워 둔다. 이전을 마친 호출자는 모두 위의 collector 오버로드를 쓴다.
    }
}
