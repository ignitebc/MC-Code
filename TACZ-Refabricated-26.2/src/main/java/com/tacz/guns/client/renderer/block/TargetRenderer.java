package com.tacz.guns.client.renderer.block;

import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.tacz.guns.block.TargetBlock;
import com.tacz.guns.block.entity.TargetBlockEntity;
import com.tacz.guns.client.model.bedrock.BedrockModel;
import com.tacz.guns.client.model.bedrock.BedrockPart;
import com.tacz.guns.client.resource.InternalAssetLoader;
import com.tacz.guns.config.client.RenderConfig;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;

public class TargetRenderer implements BlockEntityRenderer<TargetBlockEntity, TargetRenderer.TargetRenderState> {
    private static final String UPPER_NAME = "target_upper";
    private static final String HEAD_NAME = "head";

    public TargetRenderer(BlockEntityRendererProvider.Context context) {
    }

    /** 과녁 블록 엔티티용 사용자 정의 렌더 상태 */
    public static class TargetRenderState extends BlockEntityRenderState {
        public Direction facing = Direction.NORTH;
        public float rot;
        public float oRot;
        public Identifier skinTexture;
        public boolean hasOwner;
    }

    @Override
    public TargetRenderState createRenderState() {
        return new TargetRenderState();
    }

    public static Optional<BedrockModel> getModel() {
        return InternalAssetLoader.getBedrockModel(InternalAssetLoader.TARGET_MODEL_LOCATION);
    }

    @Override
    public void extractRenderState(TargetBlockEntity blockEntity, TargetRenderState state, float partialTick, Vec3 cameraPos, net.minecraft.client.renderer.feature.ModelFeatureRenderer.CrumblingOverlay crumblingOverlay) {
        BlockEntityRenderState.extractBase(blockEntity, state, crumblingOverlay);
        BlockState blockState = blockEntity.getBlockState();
        state.facing = blockState.getValue(TargetBlock.FACING);
        state.rot = blockEntity.rot;
        state.oRot = blockEntity.oRot;
        GameProfile owner = blockEntity.getOwner();
        if (owner != null) {
            state.hasOwner = true;
            state.skinTexture = DefaultPlayerSkin.get(owner).body().texturePath();
        } else {
            state.hasOwner = false;
            state.skinTexture = null;
        }
    }

    @Override
    public void submit(TargetRenderState state, PoseStack poseStack, SubmitNodeCollector collector, net.minecraft.client.renderer.state.level.CameraRenderState cameraState) {
        getModel().ifPresent(model -> {
            int combinedLightIn = state.lightCoords;
            int combinedOverlayIn = OverlayTexture.NO_OVERLAY;
            // submit에서는 partialTick을 쓸 수 없어 1.0f(최신 틱 상태)를 쓴다
            float deg = -state.rot;

            BedrockPart headModel = model.getNode(HEAD_NAME);
            BedrockPart upperModel = model.getNode(UPPER_NAME);
            upperModel.xRot = (float) Math.toRadians(deg);
            headModel.visible = false;

            poseStack.pushPose();
            poseStack.translate(0.5, 0.225, 0.5);
            poseStack.mulPose(Axis.YN.rotationDegrees(state.facing.get2DDataValue() * 90));
            poseStack.mulPose(Axis.ZN.rotationDegrees(180));
            poseStack.translate(0, -1.275, 0.0125);
            RenderType renderType = RenderTypes.entityTranslucent(InternalAssetLoader.TARGET_TEXTURE_LOCATION);
            model.submit(poseStack, ItemDisplayContext.NONE, collector, renderType, combinedLightIn, combinedOverlayIn);

            if (state.hasOwner && state.skinTexture != null) {
                poseStack.translate(0, 1.25, 0);
                poseStack.mulPose(Axis.XP.rotationDegrees(deg));
                headModel.visible = true;
                RenderType skullRenderType = RenderTypes.entityCutout(state.skinTexture);
                // VertexConsumer로 머리 부분을 그리기 위해 submitCustomGeometry를 쓴다
                collector.submitCustomGeometry(poseStack, skullRenderType, (entryPose, consumer) -> {
                    PoseStack working = new PoseStack();
                    working.last().pose().set(entryPose.pose());
                    working.last().normal().set(entryPose.normal());
                    headModel.render(working, ItemDisplayContext.NONE, consumer, combinedLightIn, combinedOverlayIn);
                });
            }
            poseStack.popPose();
        });
    }

    @Override
    public int getViewDistance() {
        return RenderConfig.TARGET_RENDER_DISTANCE.get();
    }

    @Override
    public boolean shouldRenderOffScreen() {
        return true;
    }
}