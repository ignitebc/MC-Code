package com.tacz.guns.client.renderer.item;

import com.google.common.base.Suppliers;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.api.item.IAmmo;
import com.tacz.guns.client.model.BedrockAmmoModel;
import com.tacz.guns.client.model.SlotModel;
import com.tacz.guns.client.model.bedrock.BedrockPart;
import com.tacz.guns.client.resource.pojo.TransformScale;
import cn.sh1rocu.tacz.compat.fabric.BuiltinItemRendererRegistry;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.joml.Vector3f;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.function.Supplier;

import static net.minecraft.world.item.ItemDisplayContext.GUI;


public class AmmoItemRenderer implements BuiltinItemRendererRegistry.DynamicItemRenderer {
    private static final SlotModel SLOT_AMMO_MODEL = new SlotModel();

    public static final Supplier<AmmoItemRenderer> INSTANCE = Suppliers.memoize(AmmoItemRenderer::new);

    public AmmoItemRenderer() {
    }

    private static void applyPositioningNodeTransform(List<BedrockPart> nodePath, PoseStack poseStack, Vector3f scale) {
        if (nodePath == null) {
            return;
        }
        if (scale == null) {
            scale = new Vector3f(1, 1, 1);
        }
        // 위치 그룹의 반대 이동·회전을 적용해 위치 그룹의 위치가 렌더링 중심이 되게 한다
        poseStack.translate(0, 1.5, 0);
        for (int i = nodePath.size() - 1; i >= 0; i--) {
            BedrockPart t = nodePath.get(i);
            poseStack.mulPose(Axis.XN.rotation(t.xRot));
            poseStack.mulPose(Axis.YN.rotation(t.yRot));
            poseStack.mulPose(Axis.ZN.rotation(t.zRot));
            if (t.getParent() != null) {
                poseStack.translate(-t.x * scale.x() / 16.0F, -t.y * scale.y() / 16.0F, -t.z * scale.z() / 16.0F);
            } else {
                poseStack.translate(-t.x * scale.x() / 16.0F, (1.5F - t.y / 16.0F) * scale.y(), -t.z * scale.z() / 16.0F);
            }
        }
        poseStack.translate(0, -1.5, 0);
    }

    @Override
    public void render(ItemStack itemStack, ItemDisplayContext itemDisplayContext, PoseStack poseStack, SubmitNodeCollector collector, int light, int overlay) {
        renderByItem(itemStack, itemDisplayContext, poseStack, collector, light, overlay);
    }

    public void renderByItem(@Nonnull ItemStack stack, @Nonnull ItemDisplayContext transformType, @Nonnull PoseStack poseStack, @Nonnull SubmitNodeCollector collector, int pPackedLight, int pPackedOverlay) {
        if (!(stack.getItem() instanceof IAmmo iAmmo)) {
            return;
        }
        Identifier ammoId = iAmmo.getAmmoId(stack);
        poseStack.pushPose();
        TimelessAPI.getClientAmmoIndex(ammoId).ifPresentOrElse(ammoIndex -> {
            // 먼저 3D 모델을 얻고, 비어 있으면 모두 GUI 렌더링을 쓴다
            BedrockAmmoModel ammoModel = ammoIndex.getAmmoModel();
            Identifier modelTexture = ammoIndex.getModelTextureLocation();
            // GUI 특수 렌더링
            if (transformType == GUI || ammoModel == null || modelTexture == null) {
                poseStack.translate(0.5, 1.5, 0.5);
                poseStack.mulPose(Axis.ZN.rotationDegrees(180));
                collector.submitCustomGeometry(poseStack, RenderTypes.entityTranslucent(ammoIndex.getSlotTextureLocation()), (pose, buffer) -> {
                    // 26.2: 바깥 poseStack이 아니라 콜백 인자 pose(= 제출하는 순간 poseStack.last().copy()의 스냅숏)를 써야 한다.
                    // 콜백이 실행될 때 바깥 poseStack은 이미 popPose되었거나 다시 쓰이고 있어,
                    // 그러면 아이콘이 엉뚱한 위치에 그려진다(인벤토리가 텅 빔).
                    PoseStack tacz$snapshotPose = new PoseStack();
                    tacz$snapshotPose.last().pose().set(pose.pose());
                    tacz$snapshotPose.last().normal().set(pose.normal());
                    SLOT_AMMO_MODEL.renderToBuffer(tacz$snapshotPose, buffer, pPackedLight, pPackedOverlay, 1.0F, 1.0F, 1.0F, 1.0F);
                });
                return;
            }
            // 나머지 렌더링
            // 모델 원점으로 이동
            poseStack.translate(0.5, 2, 0.5);
            // 모델 뒤집기
            poseStack.scale(-1, -1, 1);
            // 위치 그룹의 변환 적용(이동과 회전, 크기는 제외)
            applyPositioningTransform(transformType, ammoIndex.getTransform().getScale(), ammoModel, poseStack);
            // display 데이터의 크기 적용
            applyScaleTransform(transformType, ammoIndex.getTransform().getScale(), poseStack);
            // 탄약 상자 모델 렌더링
            RenderType renderType = RenderTypes.entityCutout(modelTexture);
            ammoModel.submit(poseStack, transformType, collector, renderType, pPackedLight, pPackedOverlay);
        }, () -> {
            // 이 ammoID가 없으면 오류 텍스처를 그려 알린다
            poseStack.translate(0.5, 1.5, 0.5);
            poseStack.mulPose(Axis.ZN.rotationDegrees(180));
            collector.submitCustomGeometry(poseStack, RenderTypes.entityTranslucent(MissingTextureAtlasSprite.getLocation()), (pose, buffer) -> {
                // 26.2: 바깥 poseStack이 아니라 콜백 인자 pose(= 제출하는 순간 poseStack.last().copy()의 스냅숏)를 써야 한다.
                // 콜백이 실행될 때 바깥 poseStack은 이미 popPose되었거나 다시 쓰이고 있어,
                // 그러면 아이콘이 엉뚱한 위치에 그려진다(인벤토리가 텅 빔).
                PoseStack tacz$snapshotPose = new PoseStack();
                tacz$snapshotPose.last().pose().set(pose.pose());
                tacz$snapshotPose.last().normal().set(pose.normal());
                SLOT_AMMO_MODEL.renderToBuffer(tacz$snapshotPose, buffer, pPackedLight, pPackedOverlay, 1.0F, 1.0F, 1.0F, 1.0F);
            });
        });
        poseStack.popPose();
    }

    private void applyPositioningTransform(ItemDisplayContext transformType, TransformScale scale, BedrockAmmoModel model, PoseStack poseStack) {
        switch (transformType) {
            case FIXED -> applyPositioningNodeTransform(model.getFixedOriginPath(), poseStack, scale.getFixed());
            case GROUND -> applyPositioningNodeTransform(model.getGroundOriginPath(), poseStack, scale.getGround());
            case THIRD_PERSON_RIGHT_HAND, THIRD_PERSON_LEFT_HAND ->
                    applyPositioningNodeTransform(model.getThirdPersonHandOriginPath(), poseStack, scale.getThirdPerson());
        }
    }

    private void applyScaleTransform(ItemDisplayContext transformType, TransformScale scale, PoseStack poseStack) {
        if (scale == null) {
            return;
        }
        Vector3f vector3f = null;
        switch (transformType) {
            case FIXED -> vector3f = scale.getFixed();
            case GROUND -> vector3f = scale.getGround();
            case THIRD_PERSON_RIGHT_HAND, THIRD_PERSON_LEFT_HAND -> vector3f = scale.getThirdPerson();
        }
        if (vector3f != null) {
            poseStack.translate(0, 1.5, 0);
            poseStack.scale(vector3f.x(), vector3f.y(), vector3f.z());
            poseStack.translate(0, -1.5, 0);
        }
    }
}
