package com.tacz.guns.client.renderer.feature;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.tacz.guns.client.model.bedrock.BedrockPart;
import net.minecraft.client.renderer.feature.FeatureFrameContext;
import net.minecraft.client.renderer.feature.FeatureRendererType;
import net.minecraft.client.renderer.feature.RenderTypeFeatureRenderer;

import java.util.List;

/**
 * 26.2 Feature Rendering - 총기 모델 특성 렌더러
 * <p>
 * 예전 {@code BufferSource + getBuffer(RenderType)} 방식에서 26.2 새 FeatureRenderer 방식으로 완전히 옮겼다.
 * <p>
 * 실제 API 출처(2026-07-21 가져옴):
 * <ul>
 *   <li>https://github.com/FabricMC/fabric-api/blob/26.2/fabric-rendering-v1/src/testmodClient/java/net/fabricmc/fabric/test/rendering/client/FeatureRendererTest.java</li>
 *   <li>https://github.com/FabricMC/fabric-api/blob/26.2/fabric-rendering-v1/src/client/java/net/fabricmc/fabric/api/client/rendering/v1/FeatureRendererRegistry.java</li>
 * </ul>
 * <p>
 * 등록 방법({@code onInitializeClient}에서):
 * <pre>{@code
 * FeatureRendererRegistry.register(GunModelFeatureRenderer.TYPE, GunModelFeatureRenderer::new);
 * }</pre>
 * <p>
 * 작업 흐름:
 * <ol>
 *   <li>{@code buildGroup}이 prepare 단계에서 호출되어 같은 프레임의 GunModelSubmit을 모두 받는다</li>
 *   <li>submit마다 {@link #getVertexBuilder}로 RenderType에 맞는 VertexConsumer를 얻는다</li>
 *   <li>{@link BedrockPart#render}를 호출해 정점을 쓴다(시그니처는 1.20.1과 호환: {@code (PoseStack, ItemDisplayContext, VertexConsumer, int, int, float, float, float, float)})</li>
 * </ol>
 */
public class GunModelFeatureRenderer extends RenderTypeFeatureRenderer<GunModelSubmit> {

    /**
     * FeatureRenderer 종류 식별자 - 등록할 때 쓴다
     * <p>
     * 네임스페이스 {@code tacz}, 경로 {@code gun_model}
     * 실제 API: {@code FeatureRendererType.create(String name)} (26.2 브랜치 소스에서 확인)
     */
    public static final FeatureRendererType TYPE = FeatureRendererType.create("tacz:gun_model");

    @Override
    protected void buildGroup(FeatureFrameContext context, List<GunModelSubmit> submits) {
        if (submits.isEmpty()) {
            return;
        }

        for (GunModelSubmit submit : submits) {
            // getVertexBuilder는 RenderTypeFeatureRenderer의 인스턴스 메서드로,
            // 안에서 FeatureFrameContext에 맡기며 submit.renderType에 맞는 VertexConsumer를 돌려준다
            VertexConsumer builder = getVertexBuilder(submit.renderType());

            // BedrockModel에서 그려야 할 부품을 돈다
            for (BedrockPart part : submit.model().getShouldRender()) {
                // BedrockPart.render 시그니처: (PoseStack, ItemDisplayContext, VertexConsumer, int light, int overlay, float r, float g, float b, float a)
                part.render(
                        submit.poseStack(),
                        submit.transformType(),
                        builder,
                        submit.light(),
                        submit.overlay(),
                        submit.r() / 255.0f,
                        submit.g() / 255.0f,
                        submit.b() / 255.0f,
                        submit.a() / 255.0f
                );
            }
        }
    }
}
