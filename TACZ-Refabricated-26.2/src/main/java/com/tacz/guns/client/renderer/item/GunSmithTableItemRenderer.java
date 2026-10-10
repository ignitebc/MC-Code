package com.tacz.guns.client.renderer.item;

import com.google.common.base.Suppliers;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.tacz.guns.client.model.SlotModel;
import com.tacz.guns.client.model.bedrock.BedrockModel;
import com.tacz.guns.client.renderer.block.GunSmithTableRenderer;
import com.tacz.guns.client.resource.pojo.display.block.BlockTransformParser;
import net.minecraft.client.resources.model.cuboid.ItemTransforms;
import cn.sh1rocu.tacz.compat.fabric.BuiltinItemRendererRegistry;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;
import java.util.function.Supplier;

public class GunSmithTableItemRenderer implements BuiltinItemRendererRegistry.DynamicItemRenderer {
    private static final SlotModel SLOT_BLOCK_MODEL = new SlotModel();

    public static final Supplier<GunSmithTableItemRenderer> INSTANCE = Suppliers.memoize(GunSmithTableItemRenderer::new);

    public GunSmithTableItemRenderer() {
    }

    @Override
    public void render(ItemStack stack, ItemDisplayContext mode, PoseStack matrices, SubmitNodeCollector collector, int light, int overlay) {
        renderByItem(stack, mode, matrices, collector, light, overlay);
    }

    public void renderByItem(@Nonnull ItemStack stack, @Nonnull ItemDisplayContext transformType, @Nonnull PoseStack poseStack, @Nonnull SubmitNodeCollector collector, int pPackedLight, int pPackedOverlay) {
        GunSmithTableRenderer.getIndex(stack).ifPresentOrElse(index -> {
            BedrockModel model = index.getModel();
            Identifier texture = index.getTexture();
            if (model == null) {
                return;
            }
            poseStack.pushPose();

            // 26.2 수정: 원본의 display transforms 적용을 되살렸다. 이식할 때 ClientBlockIndex의
            // transforms 해석이 지워지며 이 부분도 사라져, 손에 든 모델이 블록 원래 크기(1m³)로 그려졌다
            // — 기본 팩은 scale 0.25를 선언하므로 실제로 4배 컸다.
            //
            // 원본 1.21.1 작성법:
            //   poseStack.translate(0.5F, 0.5F, 0.5F);
            //   transforms.getTransform(ctx).apply(false, poseStack);
            //   poseStack.translate(-0.5F, -0.5F, -0.5F);
            //
            // 26.2의 차이(모두 디컴파일로 확인):
            //   1) ItemTransform#apply의 두 번째 인자는 PoseStack이 아니라 PoseStack.Pose다.
            //   2) apply 안에 translate(-0.5,-0.5,-0.5)가 이미 있어 호출하는 쪽은 마지막 이동을 더하지 않는다.
            //   3) 왼손 문맥에서는 applyLeftHandFix=true를 넘겨야 한다. 원본은 false로 고정해 왼손 거울 처리가 틀렸다.
            ItemTransforms transforms = index.getTransforms();
            if (transforms != null && transforms != ItemTransforms.NO_TRANSFORMS) {
                poseStack.translate(0.5F, 0.5F, 0.5F);
                transforms.getTransform(transformType)
                        .apply(BlockTransformParser.isLeftHand(transformType), poseStack.last());
            }

            poseStack.translate(0.5, 1.5, 0.5);
            poseStack.mulPose(Axis.ZN.rotationDegrees(180));
            RenderType renderType = RenderTypes.entityTranslucent(texture);
            model.submit(poseStack, transformType, collector, renderType, pPackedLight, pPackedOverlay);
            poseStack.popPose();
        }, () -> {
            poseStack.translate(0.5, 1.5, 0.5);
            poseStack.mulPose(Axis.ZN.rotationDegrees(180));
            collector.submitCustomGeometry(poseStack, RenderTypes.entityTranslucent(MissingTextureAtlasSprite.getLocation()), (pose, buffer) -> {
                // 26.2: 바깥 poseStack이 아니라 콜백 인자 pose(= 제출하는 순간 poseStack.last().copy()의 스냅숏)를 써야 한다.
                // 콜백이 실행될 때 바깥 poseStack은 이미 popPose되었거나 다시 쓰이고 있어,
                // 그러면 아이콘이 엉뚱한 위치에 그려진다(인벤토리가 텅 빔).
                PoseStack tacz$snapshotPose = new PoseStack();
                tacz$snapshotPose.last().pose().set(pose.pose());
                tacz$snapshotPose.last().normal().set(pose.normal());
                SLOT_BLOCK_MODEL.renderToBuffer(tacz$snapshotPose, buffer, pPackedLight, pPackedOverlay, 1, 1, 1, 1);
            });
        });
    }
}
