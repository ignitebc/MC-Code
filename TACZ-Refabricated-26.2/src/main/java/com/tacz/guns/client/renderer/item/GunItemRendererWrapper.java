package com.tacz.guns.client.renderer.item;

import cn.sh1rocu.simplebedrockmodel.api.event.ViewportEvent;
import com.google.common.base.Suppliers;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.api.client.animation.statemachine.LuaAnimationStateMachine;
import com.tacz.guns.api.client.event.BeforeRenderHandEvent;
import com.tacz.guns.api.client.gameplay.IClientPlayerGunOperator;
import com.tacz.guns.api.item.IGun;
import com.tacz.guns.client.animation.screen.RefitTransform;
import com.tacz.guns.client.animation.statemachine.GunAnimationConstant;
import com.tacz.guns.client.animation.statemachine.GunAnimationStateContext;
import com.tacz.guns.client.event.CameraSetupEvent;
import com.tacz.guns.client.event.FirstPersonRenderGunEvent;
import com.tacz.guns.client.model.BedrockGunModel;
import com.tacz.guns.client.model.SlotModel;
import com.tacz.guns.client.model.bedrock.BedrockPart;
import com.tacz.guns.client.model.functional.MuzzleFlashRender;
import com.tacz.guns.client.model.functional.ShellRender;
import com.tacz.guns.client.resource.GunDisplayInstance;
import com.tacz.guns.client.resource.pojo.TransformScale;
import com.tacz.guns.util.RenderDistance;
import com.tacz.guns.util.math.MathUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.tuple.Pair;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import static net.minecraft.world.item.ItemDisplayContext.*;

/**
 * 주요 총기 애니메이션 모델 렌더링을 맡는다. 추가 효과는 {@link FirstPersonRenderGunEvent} 참고
 */
public class GunItemRendererWrapper extends AnimateGeoItemRenderer<BedrockGunModel, GunAnimationStateContext> {
    private static final SlotModel SLOT_GUN_MODEL = new SlotModel();
    private static BedrockGunModel lastModel = null;
    public static final Vector3f muzzleRenderOffset = new Vector3f();

    public static final Supplier<GunItemRendererWrapper> INSTANCE = Suppliers.memoize(GunItemRendererWrapper::new);

    public GunItemRendererWrapper() {
        super();
    }

    @Override
    public GunAnimationStateContext initContext(ItemStack stack, Player player, float partialTick) {
        GunAnimationStateContext context = new GunAnimationStateContext();
        this.updateContext(context, stack, player, partialTick);
        return context;
    }

    @Override
    public void updateContext(GunAnimationStateContext context, ItemStack stack, Player player, float partialTick) {
        context.setPartialTicks(partialTick);
        context.setCurrentGunItem(stack);
    }

    @Override
    public void tryInit(ItemStack stack, Player player, float partialTick) {
        super.tryInit(stack, player, partialTick);
    }

    @Override
    public void tryExit(ItemStack stack, long putAwayTime) {
        var stateMachine = getStateMachine(stack);
        if (stateMachine == null) {
            return;
        }
        stateMachine.processContextIfExist(context -> {
            context.setPutAwayTime(putAwayTime / 1000F);
            context.setCurrentGunItem(stack);
        });
        if (stateMachine.isInitialized()) {
            stateMachine.trigger(GunAnimationConstant.INPUT_PUT_AWAY);
            stateMachine.exit();
            stateMachine.setExitingTime(putAwayTime + 50);
        }
    }

    @Override
    public long getPutAwayTime(ItemStack stack) {
        if (stack.getItem() instanceof IGun iGun) {
            return TimelessAPI.getCommonGunIndex(iGun.getGunId(stack))
                    .map(index -> (long) (index.getGunData().getPutAwayTime() * 1000L))
                    .orElse(0L);
        }
        return 0;
    }

    @Nullable
    @Override
    public LuaAnimationStateMachine<GunAnimationStateContext> getStateMachine(ItemStack stack) {
        return TimelessAPI.getGunDisplay(stack).map(GunDisplayInstance::getAnimationStateMachine).orElse(null);
    }

    @Override
    public BedrockGunModel getModel(ItemStack stack) {
        return TimelessAPI.getGunDisplay(stack).map(GunDisplayInstance::getGunModel).orElse(null);
    }

    @Override
    public Identifier getTextureLocation(ItemStack stack) {
        return TimelessAPI.getGunDisplay(stack).map(GunDisplayInstance::getModelTexture).orElse(null);
    }

    @Override
    public void applyLevelCameraAnimation(ViewportEvent.ComputeCameraAngles event, ItemStack stack, LocalPlayer player) {
        if (!(stack.getItem() instanceof IGun iGun)) {
            return;
        }
        Optional.ofNullable(getModel(stack)).ifPresent(model -> {
            if (lastModel != model) {
                // 총기 모델을 바꿀 때 카메라 애니메이션 데이터를 정리해, 지난번에 중간까지 재생된 카메라 애니메이션이 보기에 영향을 주지 않게 한다.
                model.cleanCameraAnimationTransform();
                lastModel = model;
            }
            IClientPlayerGunOperator clientPlayerGunOperator = IClientPlayerGunOperator.fromLocalPlayer(player);
            float partialTicks = Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(false);
            float aimingProgress = clientPlayerGunOperator.getClientAimingProgress(partialTicks);
            float zoom = iGun.getAimingZoom(stack);
            float multiplier = 1 - aimingProgress + aimingProgress / (float) Math.sqrt(zoom);
            this.applyLevelCameraAnimation(event, stack, multiplier);
        });
    }

    @Override
    public void applyItemInHandCameraAnimation(BeforeRenderHandEvent event, ItemStack stack, LocalPlayer player) {
        if (!(stack.getItem() instanceof IGun iGun)) {
            return;
        }
        Optional.ofNullable(getModel(stack)).ifPresent(model -> {
            PoseStack poseStack = event.getPoseStack();
            IClientPlayerGunOperator clientPlayerGunOperator = IClientPlayerGunOperator.fromLocalPlayer(player);
            float partialTicks = Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(false);
            float aimingProgress = clientPlayerGunOperator.getClientAimingProgress(partialTicks);
            float zoom = iGun.getAimingZoom(stack);
            float multiplier = 1 - aimingProgress + aimingProgress / (float) Math.sqrt(zoom);
            Quaternionf quaternion = MathUtil.multiplyQuaternion(model.getCameraAnimationObject().rotationQuaternion, multiplier);
            poseStack.mulPose(quaternion);
            // 지금까지 카메라 애니메이션 데이터는 모두 소비되었다. 애니메이션 데이터를 정리하는 더 좋은 방법이 있을까?
            model.cleanCameraAnimationTransform();
        });
    }

    @Override
    public void renderFirstPerson(LocalPlayer player, ItemStack stack, ItemDisplayContext ctx, PoseStack poseStack, SubmitNodeCollector collector,
                                  int light, float partialTick) {
        if (!(stack.getItem() instanceof IGun)) {
            return;
        }

        TimelessAPI.getGunDisplay(stack).ifPresent(display -> {
            BedrockGunModel gunModel = display.getGunModel();
            var animationStateMachine = display.getAnimationStateMachine();
            if (gunModel == null) {
                return;
            }

            // 렌더링 전에 애니메이션을 먼저 갱신해 애니메이션 데이터를 모델에 쓴다
            if (animationStateMachine != null) {
                animationStateMachine.processContextIfExist(context -> {
                    updateContext(context, stack, player, partialTick);
                });
                animationStateMachine.update();
            }

            poseStack.pushPose();
            // 바닐라가 손에 거는 지연 효과를 되돌리고 모델 애니메이션 데이터에 대신 쓴다
            float xRotOffset = Mth.lerp(partialTick, player.xBobO, player.xBob);
            float yRotOffset = Mth.lerp(partialTick, player.yBobO, player.yBob);
            float xRot = player.getViewXRot(partialTick) - xRotOffset;
            float yRot = player.getViewYRot(partialTick) - yRotOffset;
            poseStack.mulPose(Axis.XP.rotationDegrees(xRot * -0.1F));
            poseStack.mulPose(Axis.YP.rotationDegrees(yRot * -0.1F));
            BedrockPart rootNode = gunModel.getRootNode();
            if (rootNode != null) {
                xRot = (float) Math.tanh(xRot / 25) * 25;
                yRot = (float) Math.tanh(yRot / 25) * 25;
                rootNode.offsetX += yRot * 0.1F / 16F / 3F;
                rootNode.offsetY += -xRot * 0.1F / 16F / 3F;
                rootNode.additionalQuaternion.mul(Axis.XP.rotationDegrees(xRot * 0.05F));
                rootNode.additionalQuaternion.mul(Axis.YP.rotationDegrees(yRot * 0.05F));
            }
            // 렌더링 원점 (0, 24, 0)에서 모델 원점 (0, 0, 0)으로 옮긴다
            poseStack.translate(0, 1.5f, 0);
            // 베드락 모델은 위아래가 뒤집혀 있어 다시 뒤집어야 한다.
            poseStack.mulPose(Axis.ZP.rotationDegrees(180f));
            // 1인칭 카메라 위치 같은 총 들기 자세 변환을 적용한다
            FirstPersonRenderGunEvent.applyFirstPersonGunTransform(player, stack, poseStack, gunModel, partialTick);

            // 1인칭 탄피와 화염 렌더링을 켠다
            MuzzleFlashRender.isSelf = true;
            ShellRender.isSelf = true;
            // 개조 화면을 여는 중이면 팔을 그리지 않는다
            boolean renderHand = gunModel.getRenderHand();
            if (RefitTransform.getOpeningProgress() != 0) {
                gunModel.setRenderHand(false);
            }
            // 총기 모델 렌더링 호출
            RenderType renderType = display.enablesTransparency()
                    ? RenderTypes.entityTranslucent(display.getModelTexture())
                    : RenderTypes.entityCutout(display.getModelTexture());
            gunModel.submit(poseStack, stack, ctx, collector, renderType, light, OverlayTexture.NO_OVERLAY);
            // 1인칭 예광탄 렌더링을 준비하려고 총구 위치를 캐시한다
            cacheMuzzlePosition(poseStack, gunModel);
            // 팔 렌더링 되돌리기
            gunModel.setRenderHand(renderHand);
            // 렌더링이 끝나면 모델에서 애니메이션 데이터를 지워 다른 시점의 모델 렌더링에 영향을 주지 않게 한다
            poseStack.popPose();
            gunModel.cleanAnimationTransform();
            // 1인칭 탄피와 화염 렌더링을 끈다
            MuzzleFlashRender.isSelf = false;
            ShellRender.isSelf = false;
        });
    }

    private static void cacheMuzzlePosition(PoseStack poseStack, BedrockGunModel gunModel) {
        if (gunModel.getMuzzleFlashPosPath() != null) {
            // 카메라 중심 기준 총구 좌표를 계산한다
            poseStack.pushPose();
            for (BedrockPart bedrockPart : gunModel.getMuzzleFlashPosPath()) {
                bedrockPart.translateAndRotateAndScale(poseStack);
            }
            // 26.2의 바닐라 손 렌더링은 PoseStack을 시점 회전의 역행렬로 시작하고 시점 회전을 모델뷰 행렬에 둔다.
            // 그래서 포즈만 읽으면 이미 월드 축 좌표라, 예광탄 쪽에서 카메라 회전을 한 번 더 곱하면 바라보는 방향에 따라
            // 출발점이 좌우로 돌아간다. Iris 손 렌더링은 단위 행렬로 시작하고 흔들림만 모델뷰에 둔다.
            // 두 경우 모두 "모델뷰 x 포즈"가 카메라 기준 시점 공간이므로 그 값으로 총구 위치를 구한다.
            Matrix4f pose = new Matrix4f(RenderSystem.getModelViewStack()).mul(poseStack.last().pose());
            double itemRenderFov = CameraSetupEvent.ITEM_MODEL_FOV_DYNAMICS.get();
            double levelRenderFov = CameraSetupEvent.WORLD_FOV_DYNAMICS.get();
            poseStack.popPose();
            // 변환한 오프셋 좌표를 캐시한다
            muzzleRenderOffset.set(
                    pose.m30(),
                    pose.m31(),
                    pose.m32() * Math.tan(itemRenderFov / 2 * Math.PI / 180) / Math.tan(levelRenderFov / 2 * Math.PI / 180)
            );
        }
    }


    @Override
    public void renderByItem(@Nonnull ItemStack stack, @Nonnull ItemDisplayContext transformType, @Nonnull PoseStack poseStack, @Nonnull SubmitNodeCollector collector,
                             int pPackedLight, int pPackedOverlay) {
        if (!(stack.getItem() instanceof IGun)) {
            return;
        }
        poseStack.pushPose();
        TimelessAPI.getGunDisplay(stack).ifPresentOrElse(gunIndex -> {
            // 1인칭은 여기서 그리지 않고 다른 곳에 맡긴다
            if (transformType == FIRST_PERSON_LEFT_HAND || transformType == FIRST_PERSON_RIGHT_HAND) {
                return;
            }
            // 3인칭 보조 손도 그리지 않는다
            if (transformType == THIRD_PERSON_LEFT_HAND) {
                return;
            }
            // GUI 특수 렌더링
            if (transformType == GUI) {
                renderSlotTexture(poseStack, collector, pPackedLight, pPackedOverlay, gunIndex.getSlotTexture());
                return;
            }
            // 나머지 렌더링
            BedrockGunModel gunModel;
            Identifier gunTexture;
            Pair<BedrockGunModel, Identifier> lodModel = gunIndex.getLodModel();
            if (lodModel == null || RenderDistance.inRenderHighPolyModelDistance(poseStack)) {
                gunModel = gunIndex.getGunModel();
                gunTexture = gunIndex.getModelTexture();
            } else {
                gunModel = lodModel.getLeft();
                gunTexture = lodModel.getRight();
            }
            if (gunModel == null) {
                renderSlotTexture(poseStack, collector, pPackedLight, pPackedOverlay, gunIndex.getSlotTexture());
                return;
            }
            // 모델 원점으로 이동
            poseStack.translate(0.5, 2, 0.5);
            // 모델 뒤집기
            poseStack.scale(-1, -1, 1);
            // 위치 그룹의 변환 적용(이동과 회전, 크기는 제외)
            applyPositioningTransform(transformType, gunIndex.getTransform().getScale(), gunModel, poseStack);
            // display 데이터의 크기 적용
            applyScaleTransform(transformType, gunIndex.getTransform().getScale(), poseStack);
            // 총기 모델 렌더링
            RenderType renderType = RenderTypes.entityCutout(gunTexture);
            gunModel.submit(poseStack, stack, transformType, collector, renderType, pPackedLight, pPackedOverlay);
        }, () -> {
            // 이 gunID가 없으면 오류 텍스처를 그려 알린다
            renderSlotTexture(poseStack, collector, pPackedLight, pPackedOverlay, MissingTextureAtlasSprite.getLocation());
        });
        poseStack.popPose();
    }

    private static void renderSlotTexture(PoseStack poseStack, SubmitNodeCollector collector, int packedLight, int packedOverlay, Identifier texture) {
        poseStack.translate(0.5, 1.5, 0.5);
        poseStack.mulPose(Axis.ZN.rotationDegrees(180));
        collector.submitCustomGeometry(poseStack, RenderTypes.entityTranslucent(texture), (pose, buffer) -> {
            // 26.2: 바깥 poseStack이 아니라 콜백 인자 pose(= 제출하는 순간 poseStack.last().copy()의 스냅숏)를 써야 한다.
            // 콜백이 실행될 때 바깥 poseStack은 이미 popPose되었거나 다시 쓰이고 있어,
            // 그러면 아이콘이 엉뚱한 위치에 그려진다(인벤토리가 텅 빔).
            PoseStack tacz$snapshotPose = new PoseStack();
            tacz$snapshotPose.last().pose().set(pose.pose());
            tacz$snapshotPose.last().normal().set(pose.normal());
            SLOT_GUN_MODEL.renderToBuffer(tacz$snapshotPose, buffer, packedLight, packedOverlay, 1.0F, 1.0F, 1.0F, 1.0F);
        });
    }

    private static void applyPositioningTransform(ItemDisplayContext transformType, TransformScale scale, BedrockGunModel model,
                                                  PoseStack poseStack) {
        switch (transformType) {
            case FIXED -> applyPositioningNodeTransform(model.getFixedOriginPath(), poseStack, scale.getFixed());
            case GROUND -> applyPositioningNodeTransform(model.getGroundOriginPath(), poseStack, scale.getGround());
            case THIRD_PERSON_RIGHT_HAND, THIRD_PERSON_LEFT_HAND ->
                    applyPositioningNodeTransform(model.getThirdPersonHandOriginPath(), poseStack, scale.getThirdPerson());
        }
    }

    private static void applyScaleTransform(ItemDisplayContext transformType, TransformScale scale, PoseStack poseStack) {
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
}
