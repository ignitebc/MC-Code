package com.tacz.guns.client.renderer.item;

import com.google.common.base.Suppliers;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.api.item.IAttachment;
import com.tacz.guns.client.model.BedrockAttachmentModel;
import com.tacz.guns.client.model.SlotModel;
import com.tacz.guns.client.resource.index.ClientAttachmentIndex;
import com.tacz.guns.util.RenderDistance;
import cn.sh1rocu.tacz.compat.fabric.BuiltinItemRendererRegistry;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.function.Supplier;

public class AttachmentItemRenderer implements BuiltinItemRendererRegistry.DynamicItemRenderer {
    public static final SlotModel SLOT_ATTACHMENT_MODEL = new SlotModel();

    public static final Supplier<AttachmentItemRenderer> INSTANCE = Suppliers.memoize(AttachmentItemRenderer::new);

    public AttachmentItemRenderer() {
    }

    @Override
    public void render(ItemStack stack, ItemDisplayContext mode, PoseStack matrices, SubmitNodeCollector collector, int light, int overlay) {
        renderByItem(stack, mode, matrices, collector, light, overlay);
    }

    public void renderByItem(@Nonnull ItemStack stack, @Nonnull ItemDisplayContext transformType, @Nonnull PoseStack poseStack, @Nonnull SubmitNodeCollector collector, int pPackedLight, int pPackedOverlay) {
        if (stack.getItem() instanceof IAttachment iAttachment) {
            Identifier attachmentId = iAttachment.getAttachmentId(stack);
            poseStack.pushPose();
            TimelessAPI.getClientAttachmentIndex(attachmentId).ifPresentOrElse(attachmentIndex -> {
                // GUI 특수 렌더링
                if (transformType == ItemDisplayContext.GUI) {
                    poseStack.translate(0.5, 1.5, 0.5);
                    poseStack.mulPose(Axis.ZN.rotationDegrees(180));
                    collector.submitCustomGeometry(poseStack, RenderTypes.entityTranslucent(attachmentIndex.getSlotTexture()), (pose, buffer) -> {
                        // 26.2: 바깥 poseStack이 아니라 콜백 인자 pose(= 제출하는 순간 poseStack.last().copy()의 스냅숏)를 써야 한다.
                        // 콜백이 실행될 때 바깥 poseStack은 이미 popPose되었거나 다시 쓰이고 있어,
                        // 그러면 아이콘이 엉뚱한 위치에 그려진다(인벤토리가 텅 빔).
                        PoseStack tacz$snapshotPose = new PoseStack();
                        tacz$snapshotPose.last().pose().set(pose.pose());
                        tacz$snapshotPose.last().normal().set(pose.normal());
                        SLOT_ATTACHMENT_MODEL.renderToBuffer(tacz$snapshotPose, buffer, pPackedLight, pPackedOverlay, 1.0F, 1.0F, 1.0F, 1.0F);
                    });
                    return;
                }
                poseStack.translate(0.5, 2, 0.5);
                // 모델 뒤집기
                poseStack.scale(-1, -1, 1);
                if (transformType == ItemDisplayContext.FIXED) {
                    poseStack.mulPose(Axis.YN.rotationDegrees(90f));
                }
                this.renderDefaultAttachment(transformType, poseStack, collector, pPackedLight, pPackedOverlay, attachmentIndex);
            }, () -> {
                // 이 attachmentId가 없으면 검정·보라 텍스처를 그려 알린다
                poseStack.translate(0.5, 1.5, 0.5);
                poseStack.mulPose(Axis.ZN.rotationDegrees(180));
                collector.submitCustomGeometry(poseStack, RenderTypes.entityTranslucent(MissingTextureAtlasSprite.getLocation()), (pose, buffer) -> {
                    // 26.2: 바깥 poseStack이 아니라 콜백 인자 pose(= 제출하는 순간 poseStack.last().copy()의 스냅숏)를 써야 한다.
                    // 콜백이 실행될 때 바깥 poseStack은 이미 popPose되었거나 다시 쓰이고 있어,
                    // 그러면 아이콘이 엉뚱한 위치에 그려진다(인벤토리가 텅 빔).
                    PoseStack tacz$snapshotPose = new PoseStack();
                    tacz$snapshotPose.last().pose().set(pose.pose());
                    tacz$snapshotPose.last().normal().set(pose.normal());
                    SLOT_ATTACHMENT_MODEL.renderToBuffer(tacz$snapshotPose, buffer, pPackedLight, pPackedOverlay, 1.0F, 1.0F, 1.0F, 1.0F);
                });
            });
            poseStack.popPose();
        }
    }

    private void renderDefaultAttachment(@NotNull ItemDisplayContext transformType, @NotNull PoseStack poseStack, @NotNull SubmitNodeCollector collector, int pPackedLight, int pPackedOverlay, ClientAttachmentIndex attachmentIndex) {
        BedrockAttachmentModel model = attachmentIndex.getAttachmentModel();
        Identifier texture = attachmentIndex.getModelTexture();
        // 모델이 있으면 정상 렌더링
        if (model != null && texture != null) {
            // 저해상도 모델 호출
            Pair<BedrockAttachmentModel, Identifier> lodModel = attachmentIndex.getLodModel();
            // 저해상도 모델이 있고, 고해상도 모델 렌더링 범위 밖이며, 1인칭이 아님
            if (lodModel != null && !RenderDistance.inRenderHighPolyModelDistance(poseStack) && !transformType.firstPerson()) {
                model = lodModel.getLeft();
                texture = lodModel.getRight();
            }
            RenderType renderType = RenderTypes.entityCutout(texture);
            model.submit(null, ItemStack.EMPTY, poseStack, transformType, collector, renderType, pPackedLight, pPackedOverlay);
        }
        // 아니면 GUI 형태로 그린다
        else {
            poseStack.translate(0, 0.5, 0);
            // 아이템 액자 안에서는 정상 표시
            if (transformType == ItemDisplayContext.FIXED) {
                poseStack.mulPose(Axis.YP.rotationDegrees(90));
            }
            collector.submitCustomGeometry(poseStack, RenderTypes.entityTranslucent(attachmentIndex.getSlotTexture()), (pose, buffer) -> {
                // 26.2: 바깥 poseStack이 아니라 콜백 인자 pose(= 제출하는 순간 poseStack.last().copy()의 스냅숏)를 써야 한다.
                // 콜백이 실행될 때 바깥 poseStack은 이미 popPose되었거나 다시 쓰이고 있어,
                // 그러면 아이콘이 엉뚱한 위치에 그려진다(인벤토리가 텅 빔).
                PoseStack tacz$snapshotPose = new PoseStack();
                tacz$snapshotPose.last().pose().set(pose.pose());
                tacz$snapshotPose.last().normal().set(pose.normal());
                SLOT_ATTACHMENT_MODEL.renderToBuffer(tacz$snapshotPose, buffer, pPackedLight, pPackedOverlay, 1.0F, 1.0F, 1.0F, 1.0F);
            });
        }
    }
}
