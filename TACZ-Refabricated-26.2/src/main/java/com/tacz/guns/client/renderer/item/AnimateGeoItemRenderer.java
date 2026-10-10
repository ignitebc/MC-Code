package com.tacz.guns.client.renderer.item;

import cn.sh1rocu.simplebedrockmodel.api.event.ViewportEvent;
import com.github.mcmodderanchor.simplebedrockmodel.v1.client.animation.IFPAnimationInstance;
import com.github.mcmodderanchor.simplebedrockmodel.v1.client.renderer.IFPGeoItemRenderer;
import com.maydaymemory.mae.basic.DummyPose;
import com.maydaymemory.mae.basic.Pose;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.api.client.animation.statemachine.LuaAnimationStateMachine;
import com.tacz.guns.api.client.event.BeforeRenderHandEvent;
import com.tacz.guns.api.item.IAnimationItem;
import com.tacz.guns.client.animation.statemachine.GunAnimationConstant;
import com.tacz.guns.client.animation.statemachine.ItemAnimationStateContext;
import com.tacz.guns.client.model.BedrockAnimatedModel;
import com.tacz.guns.client.model.bedrock.BedrockPart;
import com.tacz.guns.client.sound.SoundPlayManager;
import com.tacz.guns.util.math.MathUtil;
import cn.sh1rocu.tacz.compat.fabric.BuiltinItemRendererRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

/**
 * 베드락 애니메이션 아이템 모델 BEWLR의 추상 구현. 기본 구현을 일부 담는다
 *
 * @param <M>   베드락 모델
 * @param <CTX> 애니메이션 상태 기계 문맥
 */
public abstract class AnimateGeoItemRenderer<M extends BedrockAnimatedModel, CTX extends ItemAnimationStateContext>
        implements IFPGeoItemRenderer, BuiltinItemRendererRegistry.DynamicItemRenderer {
    @Nullable
    protected LuaAnimationStateMachine<CTX> stateMachine;
    protected M model;

    @Override
    public void render(ItemStack stack, ItemDisplayContext mode, PoseStack matrices, SubmitNodeCollector collector, int light, int overlay) {
        // 1인칭은 여기서 처리하지 <b>않는다</b>.
        //
        // 이 메서드는 ItemModel(tacz:dynamic_item)의 SpecialModelRenderer가 호출하는데, 이때 바닐라
        // ItemInHandRenderer#submitArmWithItem이 이미 applyItemArmTransform(±0.56/-0.52/-0.72),
        // 휘두르기 애니메이션, 장비 들기 애니메이션을 적용해 PoseStack이 원본 1.21.1이 기대한 깨끗한 행렬이 아니다 —
        // 그러면 총의 카메라 기준 위치/크기가 틀어지고, 움직일 때 TACZ 애니메이션과 겹쳐 떨린다.
        //
        // 올바른 입구는 ItemInHandRendererMixin#tacz$submitArmWithGun이며, submitArmWithItem의 HEAD에서
        // 가로채 취소한다. 의미는 SimpleBedrockModel의 RenderHandEvent 주입 지점과 같다. 자세한 내용은 그 mixin 주석 참고.
        //
        // 여기서도 firstPerson 분기의 안전장치는 필요하다: 정상이면 여기까지 오지 않지만(mixin이 cancel),
        // mixin이 어떤 이유로 적용되지 않았다면 그냥 return하는 편이 엉뚱한 위치에 그리는 것보다 낫다 — 적어도 "총 두 자루"는 생기지 않는다.
        if (mode.firstPerson()) {
            return;
        }
        this.renderByItem(stack, mode, matrices, collector, light, overlay);
    }

    public Identifier textureLocation;

    public AnimateGeoItemRenderer() {
    }

    public void setModel(M model) {
        this.model = model;
    }

    public M getModel(ItemStack stack) {
        return model;
    }

    @Nullable
    public LuaAnimationStateMachine<CTX> getStateMachine(ItemStack stack) {
        return stateMachine;
    }

    public Identifier getTextureLocation(ItemStack stack) {
        return textureLocation;
    }

    public RenderType getRenderType(ItemStack stack) {
        return RenderTypes.entityCutout(getTextureLocation(stack));
    }

    public boolean needReInit(ItemStack stack) {
        var stateMachine = getStateMachine(stack);
        if (stateMachine == null) {
            return false;
        }
        return !stateMachine.isInitialized() && stateMachine.getExitingTime() < System.currentTimeMillis();
    }

    public abstract CTX initContext(ItemStack stack, Player player, float partialTick);

    public abstract void updateContext(CTX context, ItemStack stack, Player player, float partialTick);

    /**
     * 집어넣기 애니메이션 시간(ms)을 계산해 돌려준다
     *
     * @return 유지 시간
     */
    public long getPutAwayTime(ItemStack stack) {
        return 0;
    }

    /**
     * 상태 기계를 초기화하고 꺼내기 신호를 보내 본다
     */
    public void tryInit(ItemStack stack, Player player, float partialTick) {
        var stateMachine = getStateMachine(stack);
        if (stateMachine == null) {
            return;
        }
        if (stateMachine.isInitialized()) {
            stateMachine.exit();
        }

        stateMachine.setContext(initContext(stack, player, partialTick));
        stateMachine.initialize();

        stateMachine.trigger(GunAnimationConstant.INPUT_DRAW);
    }

    /**
     * 상태 기계를 끝내고 집어넣기 신호를 보내 본다
     */
    public void tryExit(ItemStack stack, long putAwayTime) {
        var stateMachine = getStateMachine(stack);
        if (stateMachine == null) {
            return;
        }
        stateMachine.processContextIfExist(context -> {
            context.setPutAwayTime(putAwayTime / 1000F);
        });
        if (stateMachine.isInitialized()) {
            stateMachine.trigger(GunAnimationConstant.INPUT_PUT_AWAY);
            
            stateMachine.exit();
            // 애니메이션보다 조금 길게 잡아야 예기치 않은 재초기화를 피한다(정밀도 손실 때문일 수 있다)
            // 한 틱 늦춰도 거의 느껴지지 않을 것이다)
            stateMachine.setExitingTime(putAwayTime + 50);
        }
    }

    /**
     * 상태 기계 전이를 일으켜 본다
     *
     * @param input 입력 신호
     */
    public void triggerAnimation(ItemStack stack, String input) {
        var stateMachine = getStateMachine(stack);
        if (stateMachine == null) {
            return;
        }
        stateMachine.trigger(input);
    }

    /**
     * 모델에 쓰지 않고 상태 기계만 갱신한다. 효과음 재생에 쓴다
     */
    public void visualUpdate(ItemStack stack) {
        var stateMachine = getStateMachine(stack);
        if (stateMachine == null) {
            return;
        }
        stateMachine.visualUpdate();
    }

    /**
     * 상태 기계의 월드 카메라 애니메이션을 적용한다. 지금은 플레이어에게만 쓴다
     */
    public void applyLevelCameraAnimation(ViewportEvent.ComputeCameraAngles event, ItemStack stack, LocalPlayer player) {
        this.applyLevelCameraAnimation(event, stack, 1);
    }

    public void applyLevelCameraAnimation(ViewportEvent.ComputeCameraAngles event, ItemStack stack, float multiplier) {
        var model = getModel(stack);
        if (model == null) {
            return;
        }
        Quaternionf q = MathUtil.multiplyQuaternion(model.getCameraAnimationObject().rotationQuaternion, multiplier);
        double yaw = Math.asin(2 * (q.w() * q.y() - q.x() * q.z()));
        double pitch = Math.atan2(2 * (q.w() * q.x() + q.y() * q.z()), 1 - 2 * (q.x() * q.x() + q.y() * q.y()));
        double roll = Math.atan2(2 * (q.w() * q.z() + q.x() * q.y()), 1 - 2 * (q.y() * q.y() + q.z() * q.z()));
        yaw = Math.toDegrees(yaw);
        pitch = Math.toDegrees(pitch);
        roll = Math.toDegrees(roll);
        event.setYaw((float) yaw + event.getYaw());
        event.setPitch((float) pitch + event.getPitch());
        event.setRoll((float) roll + event.getRoll());
    }

    /**
     * 상태 기계의 손에 든 아이템 카메라 애니메이션을 적용한다. 지금은 플레이어에게만 쓴다
     */
    public void applyItemInHandCameraAnimation(BeforeRenderHandEvent event, ItemStack stack, LocalPlayer player) {
        applyItemInHandCameraAnimation(event, stack, 1);
    }

    public void applyItemInHandCameraAnimation(BeforeRenderHandEvent event, ItemStack stack, float multiplier) {
        var model = getModel(stack);
        if (model == null) {
            return;
        }
        Quaternionf quaternion = MathUtil.multiplyQuaternion(model.getCameraAnimationObject().rotationQuaternion, multiplier);
        PoseStack poseStack = event.getPoseStack();
        poseStack.mulPose(quaternion);
    }

    /**
     * 추가 변환을 실행한다
     */
    public void doExtraTransforms(PoseStack poseStack, M model, ItemStack stack) {
        applyFirstPersonPositioningTransform(poseStack, model, stack);
    }

    /**
     * 1인칭을 그린다. 26.2 입구: 클라이언트 ItemModel(tacz:dynamic_item) -> TaczDynamicItemModel의
     * SpecialModelRenderer -> AnimateGeoItemRenderer#render의 mode.firstPerson() 분기.
     */
    public void renderFirstPerson(LocalPlayer player, ItemStack stack, ItemDisplayContext ctx, PoseStack poseStack, SubmitNodeCollector collector,
                                  int light, float partialTick) {
        M model = getModel(stack);
        if (model != null) {
            poseStack.pushPose();
            float xRotOffset = Mth.lerp(partialTick, player.xBobO, player.xBob);
            float yRotOffset = Mth.lerp(partialTick, player.yBobO, player.yBob);
            float xRot = player.getViewXRot(partialTick) - xRotOffset;
            float yRot = player.getViewYRot(partialTick) - yRotOffset;
            poseStack.mulPose(Axis.XP.rotationDegrees(xRot * -0.1F));
            poseStack.mulPose(Axis.YP.rotationDegrees(yRot * -0.1F));
            BedrockPart rootNode = model.getRootNode();
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
            doExtraTransforms(poseStack, model, stack);

            var stateMachine = getStateMachine(stack);
            if (stateMachine != null) {
                stateMachine.processContextIfExist(context -> {
                    updateContext(context, stack, player, partialTick);
                });
                stateMachine.update();
            }

            model.submit(poseStack, ctx, collector, getRenderType(stack), light, OverlayTexture.NO_OVERLAY);

            // 렌더링이 끝난 뒤 애니메이션 변환을 지운다
            model.cleanAnimationTransform();
            poseStack.popPose();
        }
    }

    @ParametersAreNonnullByDefault
    public void renderByItem(ItemStack stack, ItemDisplayContext ctx, PoseStack poseStack, SubmitNodeCollector collector,
                             int light, int overlay) {
        if (ctx.firstPerson()) return;
        M model = getModel(stack);
        if (model != null) {
            poseStack.pushPose();
            // 렌더링 원점 (0, 24, 0)에서 모델 원점 (0, 0, 0)으로 옮긴다
            poseStack.translate(0.5, 1.5f, 0.5);
            // 베드락 모델은 위아래가 뒤집혀 있어 다시 뒤집어야 한다.
            poseStack.mulPose(Axis.ZP.rotationDegrees(180f));
            model.submit(poseStack, ctx, collector, RenderTypes.entityCutout(
                    getTextureLocation(stack)
            ), light, overlay);
            poseStack.popPose();
        }
    }

    /**
     * 카메라 위치 그룹의 역행렬을 얻는다
     */
    @Nonnull
    public static Matrix4f getPositioningNodeInverse(List<BedrockPart> nodePath) {
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

    public static void applyFirstPersonPositioningTransform(PoseStack poseStack, BedrockAnimatedModel model, ItemStack stack) {
        Matrix4f transformMatrix = new Matrix4f();
        transformMatrix.identity();
        // 조준 위치 적용
        List<BedrockPart> idleNodePath = model.getIdleSightPath();

        Matrix4f idleViewMatrix = getPositioningNodeInverse(idleNodePath);

        // 조준 변환 적용
        MathUtil.applyMatrixLerp(transformMatrix, idleViewMatrix, transformMatrix, 1);

        // PoseStack에 변환 적용
        poseStack.translate(0, 1.5f, 0);
        poseStack.mulPose(transformMatrix);
        poseStack.translate(0, -1.5f, 0);
    }

    @Override
    public long getPutAwayDuration(ItemStack stack) {
        return this.getPutAwayTime(stack);
    }

    @Nullable
    @Override
    public IFPAnimationInstance createAnimationInstance(ItemStack stack, Entity entity) {
        return new IFPAnimationInstance() {
            private boolean drawn = false;
            private ItemStack lastItem = stack;

            @Override
            public ItemStack currentItem() {
                return lastItem;
            }

            @Override
            public Pose getPose() {
                return DummyPose.INSTANCE;
            }

            @Override
            public void tick(float v) {

            }

            @Override
            public @NotNull Quaternionf getCameraRotation() {
                return new Quaternionf();
            }

            @Override
            public void setCameraRotation(@NotNull Quaternionf quaternionf) {

            }

            @Override
            public Pose getCachedPose() {
                return DummyPose.INSTANCE;
            }

            @Override
            public void updateItem(ItemStack itemStack) {
                lastItem = itemStack;
            }

            @Override
            public void triggerDraw() {
                if (drawn) return;
                drawn = true;
                tryInit(lastItem, Minecraft.getInstance().player, 0);
                if (Minecraft.getInstance().player == null) return;
                TimelessAPI.getGunDisplay(lastItem).ifPresent(display -> {
                    SoundPlayManager.stopPlayGunSound();
                    SoundPlayManager.playDrawSound(Minecraft.getInstance().player, display);
                });
            }

            @Override
            public void triggerPutAway() {
                tryExit(lastItem, getPutAwayTime(lastItem));
                if (Minecraft.getInstance().player == null) return;
                TimelessAPI.getGunDisplay(lastItem).ifPresent(display -> {
                    SoundPlayManager.stopPlayGunSound();
                    SoundPlayManager.playPutAwaySound(Minecraft.getInstance().player, display);
                });
            }
        };
    }

    @Override
    public boolean isSameItem(ItemStack oldStack, ItemStack newStack) {
        if (oldStack.getItem() instanceof IAnimationItem item) {
            return item.isSame(oldStack, newStack);
        }
        return ItemStack.matches(oldStack, newStack);
    }

    @Override
    public boolean blockOffhandRender() {
        return true;
    }
}
