package com.tacz.guns.client.renderer.entity;

import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.tacz.guns.client.model.bedrock.BedrockModel;
import com.tacz.guns.client.model.bedrock.BedrockPart;
import com.tacz.guns.client.resource.InternalAssetLoader;
import com.tacz.guns.entity.TargetMinecart;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.client.renderer.entity.AbstractMinecartRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.MinecartRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemDisplayContext;

import java.util.Optional;

/**
 * 과녁 광산 수레 렌더러.
 *
 * <p><b>8차 수정: 모델 위치/방향이 충돌 상자와 맞지 않던 문제.</b></p>
 *
 * <p>원본 1.21.1은 {@code extends MinecartRenderer<TargetMinecart>}로
 * {@code renderMinecartContents(...)}만 재정의했다 — 광산 수레의 <b>위치 보간, 방향(yRot/xRot),
 * 선로를 따르는 자세, 피격 흔들림</b>은 모두 부모 {@code AbstractMinecartRenderer}가 맡았다.</p>
 *
 * <p>이식할 때 {@code extends EntityRenderer<TargetMinecart, 사용자 정의 State>}로 바꾸고,
 * {@code submit}에서 {@code translate + scale + 고정 각도 mulPose 두 번}을 직접 썼다.
 * 그래서 <b>부모의 위치·방향 로직을 모두 잃었다</b>:</p>
 * <ul>
 *   <li>모델이 늘 같은 방향을 봤다(보고된 "방향이 고정되어 있고 하드코딩된 것 같다").</li>
 *   <li>모델이 선로를 따르는 광산 수레의 보간 위치를 따라가지 않아 충돌 상자와 어긋났다
 *       (충돌 상자와 상호작용은 서버 엔티티가 정하므로 그쪽이 맞다).</li>
 * </ul>
 *
 * <p>26.2에도 {@code AbstractMinecartRenderer}는 남아 있다(javap 확인).
 * 렌더링 입구가 {@code render/renderMinecartContents}에서
 * {@code submit/submitMinecartContents}로 바뀌었고 상태 운반자가 {@code MinecartRenderState}일 뿐이다.
 * 여기서 상속 관계를 되살리고 내용물 제출만 재정의해 원본 의미와 맞춘다.</p>
 */
@Environment(EnvType.CLIENT)
public class TargetMinecartRenderer extends AbstractMinecartRenderer<TargetMinecart, MinecartRenderState> {
    private static final String HEAD_NAME = "head";
    private static final String HEAD_2_NAME = "head2";

    /** 이번 프레임의 GameProfile 캐시: extractRenderState 단계에서 얻고 submit 단계에서 쓴다. */
    private static final String PROFILE_KEY = "tacz$profile";

    public TargetMinecartRenderer(EntityRendererProvider.Context ctx) {
        super(ctx, ModelLayers.TNT_MINECART);
        this.shadowRadius = 0.25F;
    }

    @Override
    public MinecartRenderState createRenderState() {
        return new TargetMinecartRenderState();
    }

    /** 바닐라 상태를 넓혀 스킨에 필요한 GameProfile을 함께 담는다. */
    public static class TargetMinecartRenderState extends MinecartRenderState {
        public GameProfile gameProfile;
    }

    @Override
    public void extractRenderState(TargetMinecart entity, MinecartRenderState state, float partialTicks) {
        // 부모가 위치, 방향, 피격 흔들림, 선로 자세 등 바닐라 상태를 모두 채우게 한다.
        super.extractRenderState(entity, state, partialTicks);
        if (state instanceof TargetMinecartRenderState targetState) {
            targetState.gameProfile = entity.getGameProfile();
        }
    }

    public static Optional<BedrockModel> getModel() {
        return InternalAssetLoader.getBedrockModel(InternalAssetLoader.TARGET_MINECART_MODEL_LOCATION);
    }

    public Identifier getTextureLocation(TargetMinecart minecart) {
        return InternalAssetLoader.ENTITY_EMPTY_TEXTURE;
    }

    /**
     * 부모가 <b>광산 수레 위치/방향을 이미 적용한</b> PoseStack에서 호출한다.
     * 그래서 여기서는 "수레 안 내용물" 자신의 지역 변환만 처리하면 되며, 원본 renderMinecartContents와 같다.
     */
    @Override
    protected void submitMinecartContents(MinecartRenderState state,
                                          BlockModelRenderState blockModelRenderState,
                                          PoseStack stack,
                                          SubmitNodeCollector collector,
                                          int packedLight) {
        getModel().ifPresent(model -> {
            BedrockPart headModel = model.getNode(HEAD_NAME);
            BedrockPart head2Model = model.getNode(HEAD_2_NAME);
            if (headModel == null || head2Model == null) {
                return;
            }
            headModel.visible = false;
            head2Model.visible = false;

            stack.pushPose();
            // 지역 변환은 원본과 줄마다 같다(부모가 월드 위치와 방향을 이미 처리했다).
            stack.translate(0.5, 1.875, 0.5);
            stack.scale(1.5f, 1.5f, 1.5f);
            stack.mulPose(Axis.ZN.rotationDegrees(180));
            stack.mulPose(Axis.YN.rotationDegrees(90));

            RenderType renderType = RenderTypes.entityTranslucent(InternalAssetLoader.TARGET_MINECART_TEXTURE_LOCATION);
            model.submit(stack, ItemDisplayContext.NONE, collector, renderType, packedLight, OverlayTexture.NO_OVERLAY);

            GameProfile gameProfile = state instanceof TargetMinecartRenderState t ? t.gameProfile : null;
            if (gameProfile != null) {
                stack.translate(0, 1, -4.5 / 16d);
                Minecraft minecraft = Minecraft.getInstance();
                Identifier skin = minecraft.getSkinManager().createLookup(gameProfile, false).get().body().texturePath();
                RenderType skullRenderType = RenderTypes.entityTranslucent(skin);

                headModel.visible = true;
                collector.submitCustomGeometry(stack, skullRenderType, (entryPose, consumer) -> {
                    PoseStack working = new PoseStack();
                    working.last().pose().set(entryPose.pose());
                    working.last().normal().set(entryPose.normal());
                    headModel.render(working, ItemDisplayContext.NONE, consumer, packedLight, OverlayTexture.NO_OVERLAY);
                });

                head2Model.visible = true;
                stack.translate(0, 0, 0.01);
                collector.submitCustomGeometry(stack, skullRenderType, (entryPose, consumer) -> {
                    PoseStack working = new PoseStack();
                    working.last().pose().set(entryPose.pose());
                    working.last().normal().set(entryPose.normal());
                    head2Model.render(working, ItemDisplayContext.NONE, consumer, packedLight, OverlayTexture.NO_OVERLAY);
                });
            }
            stack.popPose();
        });
    }
}
