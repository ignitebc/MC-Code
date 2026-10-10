package com.tacz.guns.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.client.model.BedrockAmmoModel;
import com.tacz.guns.client.model.bedrock.BedrockModel;
import com.tacz.guns.client.renderer.item.GunItemRendererWrapper;
import com.tacz.guns.client.resource.GunDisplayInstance;
import com.tacz.guns.client.resource.InternalAssetLoader;
import com.tacz.guns.config.client.RenderConfig;
import com.tacz.guns.entity.EntityKineticBullet;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.Objects;
import java.util.Optional;

public class EntityBulletRenderer extends EntityRenderer<EntityKineticBullet, EntityBulletRenderer.BulletRenderState> {
    /** 1인칭 예광탄이 총구에서 출발해 실제 탄도와 합쳐지는 최대 거리(칸). 이보다 멀리 맞는 탄은 이 거리부터 실제 탄도를 따른다. */
    private static final double MAX_CONVERGE_DISTANCE = 50.0;
    /** 바로 앞을 쏘았을 때 0으로 나누지 않도록 둔 최소 합류 거리(칸) */
    private static final double MIN_CONVERGE_DISTANCE = 1.0;
    /** 탄착점을 찾을 때 엔티티 판정 상자를 넓히는 값(칸). 가장자리를 스치는 탄도 맞은 것으로 본다. */
    private static final double ENTITY_HIT_MARGIN = 0.3;

    public static class BulletRenderState extends EntityRenderState {
        public EntityKineticBullet bullet;
        public float partialTicks;
    }

    public EntityBulletRenderer(EntityRendererProvider.Context pContext) {
        super(pContext);
    }

    @Override
    public BulletRenderState createRenderState() {
        return new BulletRenderState();
    }

    public static Optional<BedrockModel> getModel() {
        return InternalAssetLoader.getBedrockModel(InternalAssetLoader.DEFAULT_BULLET_MODEL);
    }

    @Override
    public void extractRenderState(EntityKineticBullet bullet, BulletRenderState state, float partialTicks) {
        super.extractRenderState(bullet, state, partialTicks);
        state.bullet = bullet;
        state.partialTicks = partialTicks;
    }

    @Override
    public void submit(BulletRenderState state, PoseStack poseStack, SubmitNodeCollector collector, net.minecraft.client.renderer.state.level.CameraRenderState cameraState) {
        EntityKineticBullet bullet = state.bullet;
        if (bullet == null) return;
        float partialTicks = state.partialTicks;
        
        Identifier gunId = bullet.getGunId();
        Identifier gunDisplayId = bullet.getGunDisplayId();
        Optional<GunDisplayInstance> display = TimelessAPI.getGunDisplay(gunDisplayId, gunId);
        if (display.isEmpty()) {
            return;
        }
        float @Nullable [] tracerColor = bullet.getTracerColorOverride().orElse(display.get().getTracerColor());
        Identifier ammoId = bullet.getAmmoId();
        TimelessAPI.getClientAmmoIndex(ammoId).ifPresent(ammoIndex -> {
            BedrockAmmoModel ammoEntityModel = ammoIndex.getAmmoEntityModel();
            Identifier textureLocation = ammoIndex.getAmmoEntityTextureLocation();
            if (ammoEntityModel != null && textureLocation != null) {
                poseStack.mulPose(Axis.YP.rotationDegrees(Mth.lerp(partialTicks, bullet.yRotO, bullet.getYRot()) - 180.0F));
                poseStack.mulPose(Axis.XP.rotationDegrees(Mth.lerp(partialTicks, bullet.xRotO, bullet.getXRot())));
                poseStack.pushPose();
                poseStack.translate(0, 1.5, 0);
                poseStack.scale(-1, -1, 1);
                ammoEntityModel.submit(poseStack, ItemDisplayContext.GROUND, collector, getRenderType(textureLocation), state.lightCoords, OverlayTexture.NO_OVERLAY);
                poseStack.popPose();
            }

            // 예광탄 발광
            // 예광탄은 스스로 빛나는 탄이라 주변 밝기와 관계없이 최대 밝기로 그린다. 주변 밝기를 쓰면 밤에 어둡게 묻힌다.
            if (bullet.isTracerAmmo()) {
                float[] actualTracerColor = Objects.requireNonNullElse(tracerColor, ammoIndex.getTracerColor());
                renderTracerAmmo(bullet, actualTracerColor, partialTicks, poseStack, collector, LightCoordsUtil.FULL_BRIGHT);
            }
        });
    }
    
    private RenderType getRenderType(Identifier textureLocation) {
        // entityTranslucentCull을 쓸 수 없어 entityTranslucent로 대신한다
        return RenderTypes.entityTranslucent(textureLocation);
    }

    public void renderTracerAmmo(EntityKineticBullet bullet, float[] tracerColor, float partialTicks, PoseStack poseStack, SubmitNodeCollector collector, int packedLight) {
        getModel().ifPresent(model -> {
            Entity shooter = bullet.getOwner();
            if (shooter == null) {
                return;
            }
            boolean isFirstPerson = this.entityRenderDispatcher.options.getCameraType().isFirstPerson() && shooter instanceof LocalPlayer;
            if (isFirstPerson && !RenderConfig.FIRST_PERSON_BULLET_TRACER_ENABLE.get()) {
                return;
            }
            poseStack.pushPose();
            {
                float width = 0.005f;
                Vec3 bulletPosition = bullet.getPosition(partialTicks);
                double trailLength = 0.85 * bullet.getDeltaMovement().length();
                double disToEye = bulletPosition.distanceTo(shooter.getEyePosition(partialTicks));
                trailLength = Math.min(trailLength, disToEye * 0.8);

                float yaw = Mth.lerp(partialTicks, bullet.yRotO, bullet.getYRot());
                float pitch = Mth.lerp(partialTicks, bullet.xRotO, bullet.getXRot());
                if (isFirstPerson) {
                    // 실제 탄은 눈에서 나가므로 1인칭에서는 총구에서 나가는 것처럼 옮겨 그린다.
                    // 옮긴 양을 날아간 거리에 비례해 줄여 탄착점에서 0이 되게 하면, 예광탄이 총구에서 탄착점까지 곧게 날아간다.
                    prepareFirstPersonPath(bullet, shooter);
                    Vector3f offset = bullet.getFirstPersonRenderOffset();
                    Vec3 origin = bullet.getFirstPersonOrigin();
                    double convergeDistance = origin.distanceTo(bullet.getFirstPersonImpact());
                    double traveled = bulletPosition.distanceTo(origin);
                    double offsetRatio = Math.max(0, convergeDistance - traveled) / convergeDistance;
                    poseStack.translate(offset.x * offsetRatio, offset.y * offsetRatio, offset.z * offsetRatio);
                    if (offsetRatio > 0) {
                        // 탄착점에 닿기 전에는 실제 탄도가 아니라 총구에서 탄착점으로 이어지는 선 방향으로 눕힌다.
                        Vec3 muzzle = origin.add(offset.x, offset.y, offset.z);
                        Vec3 visualDirection = bullet.getFirstPersonImpact().subtract(muzzle);
                        yaw = (float) Math.toDegrees(Mth.atan2(visualDirection.x, visualDirection.z));
                        pitch = (float) Math.toDegrees(Mth.atan2(visualDirection.y, visualDirection.horizontalDistance()));
                    }
                }
                // override라고 하지만 기본값은 1이다
                // 그래서 여기서 그냥 곱해도 괜찮다
                width *= bullet.getTracerSizeOverride();
                width *= (float) Math.max(1.0, disToEye / 3.5);
                poseStack.mulPose(Axis.YP.rotationDegrees(yaw - 180.0F));
                poseStack.mulPose(Axis.XP.rotationDegrees(pitch));
                poseStack.translate(0, isFirstPerson ? 0 : -0.2, trailLength / 2.0);
                poseStack.scale(width, width, (float) trailLength);
                // 두 칸 밖에서만 그리며, 처음 5틱 동안만 판정한다
                double bulletDistance = bulletPosition.distanceTo(shooter.getEyePosition());
                if (bullet.tickCount >= 5 || bulletDistance > 2) {
                    // 일반 반투명 방식은 빛 계산을 받아 밤에 어두워진다. 자체 발광 방식으로 그려 밤에도 밝게 보이게 한다.
                    RenderType type = RenderTypes.entityTranslucentEmissive(InternalAssetLoader.DEFAULT_BULLET_TEXTURE);
                    model.submit(poseStack, ItemDisplayContext.NONE, collector, type, packedLight, OverlayTexture.NO_OVERLAY,
                            tracerColor[0], tracerColor[1], tracerColor[2], 1);
                }
            }
            poseStack.popPose();
        });
    }

    /**
     * 1인칭 예광탄의 경로를 처음 그릴 때 한 번 정한다.
     * <p>
     * 총구 위치는 1인칭 손 렌더링에서 얻은 카메라 기준 좌표다. 엔티티는 카메라 회전 없이 월드 축으로 그려지므로,
     * 쏜 순간의 카메라 회전으로 월드 축 좌표로 바꿔 둔다. 바꾸지 않으면 바라보는 방향과 관계없이 같은 월드 방향으로
     * 밀려 예광탄이 조준점에서 벗어나 보인다. 이후 고개를 돌려도 예광탄이 따라 흔들리지 않도록 이 값을 계속 쓴다.
     */
    private static void prepareFirstPersonPath(EntityKineticBullet bullet, Entity shooter) {
        if (bullet.getFirstPersonRenderOffset() != null) {
            return;
        }
        Camera camera = Minecraft.getInstance().gameRenderer.mainCamera();
        Quaternionf cameraRotation = new Quaternionf(camera.rotation());
        Vector3f worldOffset = new Vector3f(GunItemRendererWrapper.muzzleRenderOffset).rotate(cameraRotation);
        Vec3 origin = camera.position();
        bullet.setFirstPersonRenderOffset(worldOffset);
        bullet.setFirstPersonPath(origin, findImpactPoint(bullet, shooter, origin));
    }

    /**
     * 탄이 날아가는 방향으로 처음 맞을 블록이나 엔티티의 지점. 합류 최대 거리 안에 없으면 그 거리 끝 지점이다.
     * 탄착점이 멀면 그 거리부터 예광탄이 실제 탄도를 따라가므로 이후 탄착점까지도 곧게 이어진다.
     */
    private static Vec3 findImpactPoint(EntityKineticBullet bullet, Entity shooter, Vec3 origin) {
        Vec3 direction = bullet.getDeltaMovement().normalize();
        if (direction.lengthSqr() == 0) {
            direction = Vec3.directionFromRotation(shooter.getXRot(), shooter.getYRot());
        }
        Vec3 farthest = origin.add(direction.scale(MAX_CONVERGE_DISTANCE));
        BlockHitResult blockHit = bullet.level().clip(new ClipContext(origin, farthest,
                ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, shooter));
        Vec3 blockLimit = farthest;
        if (blockHit.getType() != HitResult.Type.MISS) {
            blockLimit = blockHit.getLocation();
        }
        Vec3 impact = blockLimit;
        double nearestSqr = origin.distanceToSqr(blockLimit);
        AABB searchArea = new AABB(origin, blockLimit).inflate(1.0);
        for (Entity entity : bullet.level().getEntities(shooter, searchArea, candidate -> canStopTracer(candidate, shooter))) {
            Optional<Vec3> entry = entity.getBoundingBox().inflate(ENTITY_HIT_MARGIN).clip(origin, blockLimit);
            if (entry.isEmpty()) {
                continue;
            }
            double distanceSqr = origin.distanceToSqr(entry.get());
            if (distanceSqr < nearestSqr) {
                nearestSqr = distanceSqr;
                impact = entry.get();
            }
        }
        if (nearestSqr < MIN_CONVERGE_DISTANCE * MIN_CONVERGE_DISTANCE) {
            impact = origin.add(direction.scale(MIN_CONVERGE_DISTANCE));
        }
        return impact;
    }

    /** 탄을 멈추는 엔티티인지. 쏜 사람의 펫은 탄이 통과하므로 서버 판정(EntityUtil)과 같이 뺀다. */
    private static boolean canStopTracer(Entity entity, Entity shooter) {
        if (entity instanceof EntityKineticBullet || entity.isSpectator() || !entity.isPickable()) {
            return false;
        }
        boolean shootersPet = entity instanceof OwnableEntity pet && pet.getOwner() == shooter;
        return !shootersPet;
    }

    @Override
    public boolean shouldRender(EntityKineticBullet bullet, Frustum camera, double pCamX, double pCamY, double pCamZ) {
        AABB aabb = bullet.getBoundingBox().inflate(0.5);
        if (aabb.hasNaN() || aabb.getSize() == 0) {
            aabb = new AABB(bullet.getX() - 2.0, bullet.getY() - 2.0, bullet.getZ() - 2.0, bullet.getX() + 2.0, bullet.getY() + 2.0, bullet.getZ() + 2.0);
        }
        return camera.isVisible(aabb);
    }
}
