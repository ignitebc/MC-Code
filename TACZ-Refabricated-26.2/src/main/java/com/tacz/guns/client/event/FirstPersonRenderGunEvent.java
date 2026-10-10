package com.tacz.guns.client.event;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.tacz.guns.api.DefaultAssets;
import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.api.client.event.RenderItemInHandBobEvent;
import com.tacz.guns.api.client.gameplay.IClientPlayerGunOperator;
import com.tacz.guns.api.client.other.KeepingItemRenderer;
import com.tacz.guns.api.event.common.GunFireEvent;
import com.tacz.guns.api.item.IGun;
import com.tacz.guns.api.item.attachment.AttachmentType;
import com.tacz.guns.api.item.nbt.AttachmentItemDataAccessor;
import com.tacz.guns.client.animation.screen.RefitTransform;
import com.tacz.guns.client.model.BedrockAttachmentModel;
import com.tacz.guns.client.model.BedrockGunModel;
import com.tacz.guns.client.model.bedrock.BedrockPart;
import com.tacz.guns.client.model.functional.MuzzleFlashRender;
import com.tacz.guns.client.renderer.item.GunItemRendererWrapper;
import com.tacz.guns.client.resource.index.ClientAttachmentIndex;
import com.tacz.guns.entity.EntityKineticBullet;
import com.tacz.guns.util.math.Easing;
import com.tacz.guns.util.math.MathUtil;
import com.tacz.guns.util.math.PerlinNoise;
import com.tacz.guns.util.math.SecondOrderDynamics;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * 1인칭 총기 모델의 추가 효과 렌더링을 맡는다. 나머지는 {@link GunItemRendererWrapper} 참고
 */
@Environment(EnvType.CLIENT)
public class FirstPersonRenderGunEvent {
    // 조준 동작의 움직임 곡선을 만들어 동작이 부드러워 보이게 한다
    private static final SecondOrderDynamics AIMING_DYNAMICS = new SecondOrderDynamics(1.2f, 1.2f, 0.5f, 0);
    private static SecondOrderDynamics SWITCH_VIEW_DYNAMICS;
    // 개조 화면을 열 때 총기 움직임을 부드럽게 한다
    private static final SecondOrderDynamics REFIT_OPENING_DYNAMICS = new SecondOrderDynamics(1f, 1.2f, 0.5f, 0);
    // 점프 지연 애니메이션을 부드럽게 한다
    private static final SecondOrderDynamics JUMPING_DYNAMICS = new SecondOrderDynamics(0.28f, 1f, 0.65f, 0);
    private static final float JUMPING_Y_SWAY = -2f;
    private static final float JUMPING_SWAY_TIME = 0.3f;
    private static final float LANDING_SWAY_TIME = 0.15f;
    // 총기 반동의 절차적 애니메이션에 쓴다
    private static final PerlinNoise SHOOT_X_SWAY_NOISE = new PerlinNoise(-0.2f, 0.2f, 400);
    private static final PerlinNoise SHOOT_Y_ROTATION_NOISE = new PerlinNoise(-0.0136f, 0.0136f, 100);
    private static final float SHOOT_Y_SWAY = -0.1f;
    private static final float SHOOT_ANIMATION_TIME = 0.3f;

    private static float jumpingSwayProgress = 0;
    private static boolean lastOnGround = false;
    private static long jumpingTimeStamp = -1;
    private static long shootTimeStamp = -1;
    private static Matrix4f oldAimingViewMatrix;
    private static float oldViewIndex;
    private static int currentViewIndex = -1;

    /**
     * 주 손에 총기 아이템을 들고 있으면 viewBobbing을 적용하지 않아, 사용자 정의 달리기·걷기 애니메이션을 적용할 수 있게 한다.
     */
    public static void cancelItemInHandViewBobbing(RenderItemInHandBobEvent.BobView event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            return;
        }
        ItemStack itemStack = KeepingItemRenderer.getRenderer().getCurrentItem();
        if (IGun.getIGunOrNull(itemStack) != null) {
            event.setCanceled(true);
        }
    }

    public static void onGunFire(GunFireEvent event) {
        if (event.getLogicalSide().isClient()) {
            LivingEntity shooter = event.getShooter();
            LocalPlayer player = Minecraft.getInstance().player;
            if (!shooter.equals(player)) {
                return;
            }
            ItemStack mainHandItem = player.getMainHandItem();
            IGun iGun = IGun.getIGunOrNull(mainHandItem);
            if (iGun == null) {
                return;
            }
            TimelessAPI.getClientGunIndex(iGun.getGunId(mainHandItem)).ifPresent(gunIndex -> {
                // 반동 절차적 애니메이션에 쓰도록 발사 시각을 기록한다
                shootTimeStamp = System.currentTimeMillis();
                // 총구 화염 데이터를 기록한다
                MuzzleFlashRender.onShoot();
            });
        }
    }

    private static boolean bulletFromPlayer(Entity entity) {
        if (entity instanceof EntityKineticBullet entityBullet) {
            return entityBullet.getOwner() instanceof LocalPlayer;
        }
        return false;
    }

    public static void applyFirstPersonGunTransform(LocalPlayer player, ItemStack gunItemStack, PoseStack poseStack, BedrockGunModel model, float partialTicks) {
        // 움직임 곡선에 맞춰 개조 창 열림 진행도를 계산한다
        float refitScreenOpeningProgress = REFIT_OPENING_DYNAMICS.update(RefitTransform.getOpeningProgress());
        // 움직임 곡선에 맞춰 조준 진행도를 계산한다
        float aimingProgress = AIMING_DYNAMICS.update(IClientPlayerGunOperator.fromLocalPlayer(player).getClientAimingProgress(partialTicks));
        // 반동, 총 들고 점프 등 총기 동작을 적용한다
        applyGunMovements(model, aimingProgress, partialTicks);
        // 여러 카메라 위치 그룹(기본 들기, 조준, 개조 화면 등)의 변환을 적용한다
        applyFirstPersonPositioningTransform(poseStack, model, gunItemStack, aimingProgress, refitScreenOpeningProgress);
        // 애니메이션 구속 변환을 적용한다
        applyAnimationConstraintTransform(poseStack, model, aimingProgress * (1 - refitScreenOpeningProgress));
    }

    private static void applyGunMovements(BedrockGunModel model, float aimingProgress, float partialTicks) {
        applyShootSwayAndRotation(model, aimingProgress);
        applyJumpingSway(model, partialTicks);
    }

    /**
     * 조준경 카메라 위치 그룹, 가늠쇠 카메라 위치 그룹, Idle 카메라 위치 그룹의 변환을 적용한다. 여러 카메라 위치 사이를 보간한다.
     */
    private static void applyFirstPersonPositioningTransform(PoseStack poseStack, BedrockGunModel model, ItemStack stack, float aimingProgress, float refitScreenOpeningProgress) {
        IGun iGun = IGun.getIGunOrNull(stack);
        if (iGun == null) {
            return;
        }
        Matrix4f transformMatrix = new Matrix4f();
        transformMatrix.identity();
        // 조준 위치 적용
        List<BedrockPart> idleNodePath = model.getIdleSightPath();
        List<BedrockPart> aimingNodePath = null;
        Identifier scopeId = iGun.getAttachmentId(stack, AttachmentType.SCOPE);
        if (scopeId.equals(DefaultAssets.EMPTY_ATTACHMENT_ID)) {
            scopeId = iGun.getBuiltInAttachmentId(stack, AttachmentType.SCOPE);
        }
        CompoundTag scopeTag = iGun.getAttachmentTag(stack, AttachmentType.SCOPE);
        int zoomNumber = AttachmentItemDataAccessor.getZoomNumberFromTag(scopeTag);
        int viewIndex = 1;
        if (DefaultAssets.isEmptyAttachmentId(scopeId)) {
            // 조준경이 없으면 가늠쇠 위치 그룹을 쓴다
            aimingNodePath = model.getIronSightPath();
        } else {
            // 조준경이 있으면 조준경 위치 그룹과 조준경 시야 위치 그룹을 합친다
            List<BedrockPart> scopeNodePath = model.getScopePosPath();
            if (scopeNodePath != null) {
                aimingNodePath = new ArrayList<>(scopeNodePath);
                Optional<ClientAttachmentIndex> indexOptional = TimelessAPI.getClientAttachmentIndex(scopeId);
                if (indexOptional.isPresent()) {
                    BedrockAttachmentModel attachmentModel = indexOptional.get().getAttachmentModel();
                    int[] views = indexOptional.get().getViews();
                    viewIndex = views[zoomNumber % views.length] - 1;
                    if (attachmentModel != null) {
                        // [35차] "지금 어느 렌즈 그룹을 쓰는지"를 모델에 알려 복합 조준경이 조준선을 거르게 한다.
                        // views[]에서 1 = 도트 사이트 계통, 2 = 망원경 계통
                        // (scope_hamr_display.json: zoom[3.25, 1.25] / views[2, 1]).
                        // 전체 흐름에서 zoomNumber와 views[]를 동시에 아는 곳은 여기뿐이다.
                        attachmentModel.setActiveViewGroup(views[zoomNumber % views.length]);
                        List<BedrockPart> scopeViewPath = attachmentModel.getScopeViewPath(currentViewIndex == -1 ? viewIndex : currentViewIndex);
                        if (scopeViewPath != null) {
                            aimingNodePath.addAll(scopeViewPath);
                        }
                    }
                }
            }
        }
        Matrix4f aimingViewMatrix = getPositioningNodeInverse(aimingNodePath);
        // 두 scope view 사이를 보간한다
        if (currentViewIndex == -1) {
            currentViewIndex = viewIndex;
            oldViewIndex = viewIndex;
            oldAimingViewMatrix = aimingViewMatrix;
            SWITCH_VIEW_DYNAMICS = new SecondOrderDynamics(0.35f, 1.2f, 0.3f, viewIndex);
        }
        float view_interpret = SWITCH_VIEW_DYNAMICS.update(viewIndex);
        float span = currentViewIndex - oldViewIndex;
        float switchingProgress = Math.abs(span) < 0.05 ? 1 : (view_interpret - oldViewIndex) / span;
        MathUtil.applyMatrixLerp(aimingViewMatrix, oldAimingViewMatrix, aimingViewMatrix, 1 - switchingProgress);
        if (currentViewIndex != viewIndex) {
            oldAimingViewMatrix = aimingViewMatrix;
            oldViewIndex = view_interpret;
            currentViewIndex = viewIndex;
        }
        // 조준 변환 적용
        MathUtil.applyMatrixLerp(transformMatrix, getPositioningNodeInverse(idleNodePath), transformMatrix, (1 - refitScreenOpeningProgress));
        MathUtil.applyMatrixLerp(transformMatrix, aimingViewMatrix, transformMatrix, (1 - refitScreenOpeningProgress) * aimingProgress);
        // 개조 화면이 열렸을 때의 위치 적용
        float refitTransformProgress = (float) Easing.easeOutCubic(RefitTransform.getTransformProgress());
        AttachmentType oldType = RefitTransform.getOldTransformType();
        AttachmentType currentType = RefitTransform.getCurrentTransformType();
        List<BedrockPart> fromNode = model.getRefitAttachmentViewPath(oldType);
        List<BedrockPart> toNode = model.getRefitAttachmentViewPath(currentType);
        MathUtil.applyMatrixLerp(transformMatrix, getPositioningNodeInverse(fromNode), transformMatrix, refitScreenOpeningProgress);
        MathUtil.applyMatrixLerp(transformMatrix, getPositioningNodeInverse(toNode), transformMatrix, refitScreenOpeningProgress * refitTransformProgress);
        // PoseStack에 변환 적용
        poseStack.translate(0, 1.5f, 0);
        poseStack.mulPose(transformMatrix);
        poseStack.translate(0, -1.5f, 0);
    }

    /**
     * 카메라 위치 그룹의 역행렬을 얻는다
     */
    @Nonnull
    private static Matrix4f getPositioningNodeInverse(List<BedrockPart> nodePath) {
        Matrix4f matrix4f = new Matrix4f();
        matrix4f.identity();
        if (nodePath != null) {
            for (int i = nodePath.size() - 1; i >= 0; i--) {
                BedrockPart part = nodePath.get(i);
                // 반대 회전 계산
                matrix4f.rotate(Axis.XN.rotation(part.xRot));
                matrix4f.rotate(Axis.YN.rotation(part.yRot));
                matrix4f.rotate(Axis.ZN.rotation(part.zRot));
                // 반대 이동 계산
                if (part.getParent() != null) {
                    matrix4f.translate(-part.x / 16.0F, -part.y / 16.0F, -part.z / 16.0F);
                } else {
                    matrix4f.translate(-part.x / 16.0F, (1.5F - part.y / 16.0F), -part.z / 16.0F);
                }
            }
        }
        return matrix4f;
    }

    private static void applyShootSwayAndRotation(BedrockGunModel model, float aimingProgress) {
        BedrockPart rootNode = model.getRootNode();
        if (rootNode != null) {
            float progress = 1 - (System.currentTimeMillis() - shootTimeStamp) / (SHOOT_ANIMATION_TIME * 1000);
            if (progress < 0) {
                progress = 0;
            }
            progress = (float) Easing.easeOutCubic(progress);
            rootNode.offsetX += SHOOT_X_SWAY_NOISE.getValue() / 16 * progress * (1 - aimingProgress);
            // 베드락 모델은 y축이 뒤집혀 있으므로 sway 값의 부호를 바꾼다
            rootNode.offsetY += -SHOOT_Y_SWAY / 16 * progress * (1 - aimingProgress);
            rootNode.additionalQuaternion.mul(Axis.YP.rotation(SHOOT_Y_ROTATION_NOISE.getValue() * progress));
        }
    }

    private static void applyJumpingSway(BedrockGunModel model, float partialTicks) {
        if (jumpingTimeStamp == -1) {
            jumpingTimeStamp = System.currentTimeMillis();
        }
        LocalPlayer player = Minecraft.getInstance().player;
        if (player != null) {
            double posY = Mth.lerp(partialTicks, Minecraft.getInstance().player.yOld, Minecraft.getInstance().player.getY());
            // 방어 코드: partialTicks가 0일 때 0으로 나눠 NaN이 생기지 않게 한다
            float velocityY = partialTicks > 0.0f
                    ? (float) (posY - Minecraft.getInstance().player.yOld) / partialTicks
                    : 0.0f;
            if (player.onGround()) {
                if (!lastOnGround) {
                    jumpingSwayProgress = velocityY / -0.1f;
                    if (jumpingSwayProgress > 1) {
                        jumpingSwayProgress = 1;
                    }
                    lastOnGround = true;
                } else {
                    jumpingSwayProgress -= (System.currentTimeMillis() - jumpingTimeStamp) / (LANDING_SWAY_TIME * 1000);
                    if (jumpingSwayProgress < 0) {
                        jumpingSwayProgress = 0;
                    }
                }
            } else {
                if (lastOnGround) {
                    // 0.42는 플레이어가 자연스럽게 점프할 때의 속도다
                    jumpingSwayProgress = velocityY / 0.42f;
                    if (jumpingSwayProgress > 1) {
                        jumpingSwayProgress = 1;
                    }
                    lastOnGround = false;
                } else {
                    jumpingSwayProgress -= (System.currentTimeMillis() - jumpingTimeStamp) / (JUMPING_SWAY_TIME * 1000);
                    if (jumpingSwayProgress < 0) {
                        jumpingSwayProgress = 0;
                    }
                }
            }
        }
        jumpingTimeStamp = System.currentTimeMillis();
        float ySway = JUMPING_DYNAMICS.update(JUMPING_Y_SWAY * jumpingSwayProgress);
        BedrockPart rootNode = model.getRootNode();
        if (rootNode != null) {
            // 베드락 모델은 y축이 뒤집혀 있으므로 sway 값의 부호를 바꾼다
            rootNode.offsetY += -ySway / 16;
        }
    }

    /**
     * 애니메이션 구속점의 변환 데이터를 얻는다.
     *
     * @param originTranslation   구속점의 원래 좌표를 출력한다
     * @param animatedTranslation 구속점이 애니메이션 변환을 거친 뒤의 좌표를 출력한다
     * @param rotation            구속점의 회전을 출력한다
     */
    private static void getAnimationConstraintTransform(List<BedrockPart> nodePath, @Nonnull Vector3f originTranslation, @Nonnull Vector3f animatedTranslation, @Nonnull Vector3f rotation) {
        if (nodePath == null) {
            return;
        }
        // 구속점 애니메이션 변환 행렬
        Matrix4f animeMatrix = new Matrix4f();
        // 구속점 초기 변환 행렬
        Matrix4f originMatrix = new Matrix4f();
        animeMatrix.identity();
        originMatrix.identity();
        BedrockPart constrainNode = nodePath.get(nodePath.size() - 1);
        for (BedrockPart part : nodePath) {
            // 애니메이션 이동을 곱한다
            if (part != constrainNode) {
                animeMatrix.translate(part.offsetX, part.offsetY, part.offsetZ);
            }
            // 그룹 이동을 곱한다
            if (part.getParent() != null) {
                animeMatrix.translate(part.x / 16.0F, part.y / 16.0F, part.z / 16.0F);
            } else {
                animeMatrix.translate(part.x / 16.0F, (part.y / 16.0F - 1.5F), part.z / 16.0F);
            }
            // 애니메이션 회전을 곱한다
            if (part != constrainNode) {
                animeMatrix.rotate(part.additionalQuaternion);
            }
            // 그룹 회전을 곱한다
            animeMatrix.rotate(Axis.ZP.rotation(part.zRot));
            animeMatrix.rotate(Axis.YP.rotation(part.yRot));
            animeMatrix.rotate(Axis.XP.rotation(part.xRot));

            // 그룹 이동을 곱한다
            if (part.getParent() != null) {
                originMatrix.translate(part.x / 16.0F, part.y / 16.0F, part.z / 16.0F);
            } else {
                originMatrix.translate(part.x / 16.0F, (part.y / 16.0F - 1.5F), part.z / 16.0F);
            }
            // 그룹 회전을 곱한다
            originMatrix.rotate(Axis.ZP.rotation(part.zRot));
            originMatrix.rotate(Axis.YP.rotation(part.yRot));
            originMatrix.rotate(Axis.XP.rotation(part.xRot));

        }
        // 변환 데이터를 출력에 쓴다
        animeMatrix.getTranslation(animatedTranslation);
        originMatrix.getTranslation(originTranslation);
        Vector3f animatedRotation = MathUtil.getEulerAngles(animeMatrix);
        Vector3f originRotation = MathUtil.getEulerAngles(originMatrix);
        animatedRotation.sub(originRotation);
        rotation.set(animatedRotation.x(), animatedRotation.y(), animatedRotation.z());
    }

    /**
     * 애니메이션 구속 변환을 적용한다.
     *
     * @param weight 구속 변환의 가중치. 보간에 쓴다.
     */
    public static void applyAnimationConstraintTransform(PoseStack poseStack, BedrockGunModel gunModel, float weight) {
        List<BedrockPart> nodePath = gunModel.getConstraintPath();
        if (nodePath == null) {
            return;
        }
        if (gunModel.getConstraintObject() == null) {
            return;
        }
        // 애니메이션 구속점의 변환 정보를 얻는다
        Vector3f originTranslation = new Vector3f();
        Vector3f animatedTranslation = new Vector3f();
        Vector3f rotation = new Vector3f();
        Vector3f translationICA = gunModel.getConstraintObject().translationConstraint;
        Vector3f rotationICA = gunModel.getConstraintObject().rotationConstraint;
        getAnimationConstraintTransform(nodePath, originTranslation, animatedTranslation, rotation);
        // 구속 계수에 맞춰 구속 이동에 필요한 반대 이동을 계산한다
        Vector3f inverseTranslation = new Vector3f(originTranslation);
        inverseTranslation.sub(animatedTranslation);
        inverseTranslation.mulDirection(poseStack.last().pose());
        inverseTranslation.mul(translationICA.x() - 1, translationICA.y() - 1, 1 - translationICA.z()); // 베드락 모델의 회전 때문에 xy축을 뒤집어야 한다
        // 구속 회전에 필요한 반대 회전을 계산한다. 보간해야 하므로 오일러 각으로 얻는다
        Vector3f inverseRotation = new Vector3f(rotation);
        inverseRotation.mul(rotationICA.x() - 1, rotationICA.y() - 1, rotationICA.z() - 1);
        // 구속 회전
        poseStack.translate(animatedTranslation.x(), animatedTranslation.y() + 1.5f, animatedTranslation.z());
        poseStack.mulPose(Axis.XP.rotation(inverseRotation.x() * weight));
        poseStack.mulPose(Axis.YP.rotation(inverseRotation.y() * weight));
        poseStack.mulPose(Axis.ZP.rotation(inverseRotation.z() * weight));
        poseStack.translate(-animatedTranslation.x(), -animatedTranslation.y() - 1.5f, -animatedTranslation.z());
        // 구속 이동
        Matrix4f poseMatrix = poseStack.last().pose();
        poseMatrix.m30(poseMatrix.m30() - inverseTranslation.x() * weight);
        poseMatrix.m31(poseMatrix.m31() - inverseTranslation.y() * weight);
        poseMatrix.m32(poseMatrix.m32() + inverseTranslation.z() * weight);
    }
}