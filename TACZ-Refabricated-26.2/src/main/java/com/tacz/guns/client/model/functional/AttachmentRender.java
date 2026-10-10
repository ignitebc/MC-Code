package com.tacz.guns.client.model.functional;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.api.item.IAttachment;
import com.tacz.guns.api.item.attachment.AttachmentType;
import com.tacz.guns.client.model.BedrockAttachmentModel;
import com.tacz.guns.client.model.BedrockGunModel;
import com.tacz.guns.client.model.IFunctionalSubmitter;
import com.tacz.guns.client.renderer.item.AttachmentItemRenderer;
import com.tacz.guns.util.RenderDistance;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.tuple.Pair;


public class AttachmentRender implements IFunctionalSubmitter {
    private final BedrockGunModel bedrockGunModel;
    private final AttachmentType type;

    public AttachmentRender(BedrockGunModel bedrockGunModel, AttachmentType type) {
        this.bedrockGunModel = bedrockGunModel;
        this.type = type;
    }



    public static void submitAttachment(ItemStack attachmentItem,
                                        ItemStack gunItem,
                                        PoseStack poseStack,
                                        ItemDisplayContext transformType,
                                        SubmitNodeCollector collector,
                                        int light,
                                        int overlay) {
        poseStack.translate(0, -1.5, 0);
        if (!(attachmentItem.getItem() instanceof IAttachment iAttachment)) {
            return;
        }
        Identifier attachmentId = iAttachment.getAttachmentId(attachmentItem);
        TimelessAPI.getClientAttachmentIndex(attachmentId).ifPresentOrElse(attachmentIndex -> {
            BedrockAttachmentModel model = attachmentIndex.getAttachmentModel();
            Identifier texture = attachmentIndex.getModelTexture();
            if (model != null && texture != null) {
                Pair<BedrockAttachmentModel, Identifier> lodModel = attachmentIndex.getLodModel();
                if (lodModel != null && !RenderDistance.inRenderHighPolyModelDistance(poseStack) && !transformType.firstPerson()) {
                    model = lodModel.getLeft();
                    texture = lodModel.getRight();
                }
                // 텍스처를 하나 더 넘긴다: 조준경 몸체는 "접안렌즈 마스크로 잘리는" RenderType으로 바뀔 수 있고,
                // 그것은 텍스처로 만들어야 한다. 쓸지 말지는 model 안에서 판단한다(resolveBodyRenderType 참고).
                model.submit(attachmentItem, gunItem, poseStack, transformType, collector,
                        RenderTypes.entityCutout(texture), texture, light, overlay);
            }
        }, () -> collector.submitCustomGeometry(
                poseStack,
                RenderTypes.entityTranslucent(MissingTextureAtlasSprite.getLocation()),
                (pose, buffer) -> AttachmentItemRenderer.SLOT_ATTACHMENT_MODEL.renderToBuffer(
                        poseStack, buffer, light, overlay, 1.0F, 1.0F, 1.0F, 1.0F
                )
        ));
    }

    @Override
    public void extract(ExtractionContext context) {
        ItemStack attachmentItem = bedrockGunModel.getCurrentAttachmentItem().get(type);
        if (attachmentItem == null || attachmentItem.isEmpty()) {
            return;
        }
        ItemStack frozenAttachment = attachmentItem.copy();
        ItemStack frozenGun = bedrockGunModel.getCurrentGunItem().copy();
        PoseStack frozenPose = context.poseStack();
        ItemDisplayContext displayContext = context.displayContext();
        int light = context.light();
        int overlay = context.overlay();
        context.add(collector -> {
            PoseStack taskPose = new PoseStack();
            taskPose.last().pose().set(frozenPose.last().pose());
            taskPose.last().normal().set(frozenPose.last().normal());
            submitAttachment(frozenAttachment, frozenGun, taskPose, displayContext, collector, light, overlay);
        });
    }

    /**
     * [r44] 예전 VertexConsumer 경로. 26.2에서는 <b>실제로 하는 일이 없어</b> 구현을 비웠다.
     *
     * <p>원래는 {@code bedrockGunModel.delegateRender(...)}로 부착물 렌더링을 총기 모델 뒤로 미뤘다.
     * 하지만 26.2의 {@code BedrockModel#submit}(현재 경로)에서 {@code delegateRenderers}는
     * <b>바로 비워지고 한 번도 실행되지 않는다</b>(그 메서드 주석에 따르면 예전 위임 렌더러는
     * VertexConsumer 콜백에서 중첩 RenderType을 안전하게 제출할 수 없다).
     * delegate를 아직 쓰는 유일한 {@code renderInto(...)}는 예전 render 사슬에 속하며,
     * 그 사슬의 입구 {@code BedrockGunModel#render}는 이번 정리에서 함께 지웠다.</p>
     *
     * <p>부착물의 실제 렌더링은 {@link #submitAttachment}가 맡고, {@code IFunctionalCollectorRenderer}의
     * {@code submit(...)}이 구동한다 — 이 클래스 위쪽의 그 메서드 참고.</p>
     *
     * <p>빈 구현을 남긴 것은 이 메서드가 {@code IFunctionalRenderer}의 인터페이스 약속이라
     * 지우면 구현 관계가 깨지기 때문이다.</p>
     */
    @Override
    public void render(PoseStack poseStack, VertexConsumer vertexBuffer, ItemDisplayContext transformType, int light, int overlay) {
        // 아무것도 하지 않음: 위 javadoc 참고. 부착물 렌더링은 submitAttachment로 통일한다.
    }
}
