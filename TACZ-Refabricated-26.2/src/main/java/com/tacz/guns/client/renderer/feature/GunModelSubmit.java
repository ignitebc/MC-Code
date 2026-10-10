package com.tacz.guns.client.renderer.feature;

import com.mojang.blaze3d.vertex.PoseStack;
import com.tacz.guns.client.model.bedrock.BedrockModel;
import net.minecraft.client.renderer.feature.FeatureRendererType;
import net.minecraft.client.renderer.feature.submit.SubmitNode;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.world.item.ItemDisplayContext;

/**
 * 26.2 Feature Rendering - 총기 모델 제출 노드
 * <p>
 * 1.20.1의 예전 {@code BedrockModel.render(BufferSource)} 방식에서 26.2 새 SubmitNode 방식으로 완전히 옮겼다.
 * <p>
 * 실제 API 출처(2026-07-21 가져옴):
 * <ul>
 *   <li>https://github.com/FabricMC/fabric-api/blob/26.2/fabric-rendering-v1/src/testmodClient/java/net/fabricmc/fabric/test/rendering/client/FeatureRendererTest.java</li>
 *   <li>https://github.com/FabricMC/fabric-api/blob/26.2/fabric-rendering-v1/src/client/java/net/fabricmc/fabric/api/client/rendering/v1/SubmitRenderPhases.java</li>
 * </ul>
 * <p>
 * 핵심 설계:
 * <ul>
 *   <li>{@link SubmitNode}를 구현하고 {@link #featureType()}을 제공한다</li>
 *   <li>TACZ 모델은 보통 반투명(투명 총기 텍스처)이지만 TRANSLUCENT_* 대신 {@code SOLID} 단계를 쓴다(TranslucentSubmit 인터페이스 제약과 정렬 복잡도를 피하기 위해)</li>
 *   <li>이 record는 아직 {@code TranslucentSubmit}을 구현하지 않았다. BedrockModel이 정말 반투명 정렬이 필요한지 확인한 뒤에 정한다</li>
 * </ul>
 *
 * @param poseStack PoseStack 문맥
 * @param transformType 아이템 표시 문맥(FIRST_PERSON, THIRD_PERSON, GUI 등)
 * @param model 그릴 BedrockModel
 * @param renderType 렌더 타입(엔티티 불투명/반투명/잘라내기 등)
 * @param light 조명
 * @param overlay 덮개 층(예: 치명타 번쩍임)
 * @param r 빨강(0-255)
 * @param g 초록(0-255)
 * @param b 파랑(0-255)
 * @param a 투명도(0-255)
 */
public record GunModelSubmit(
        PoseStack poseStack,
        ItemDisplayContext transformType,
        BedrockModel model,
        RenderType renderType,
        int light,
        int overlay,
        int r, int g, int b, int a
) implements SubmitNode {

    @Override
    public FeatureRendererType featureType() {
        return GunModelFeatureRenderer.TYPE;
    }

    /**
     * 보조 생성자 - float 색을 쓴다(예전 호출하는 쪽과 하위 호환)
     */
    public static GunModelSubmit of(PoseStack poseStack, ItemDisplayContext ctx,
                                    BedrockModel model, RenderType renderType,
                                    int light, int overlay,
                                    float r, float g, float b, float a) {
        return new GunModelSubmit(
                poseStack, ctx, model, renderType, light, overlay,
                (int) (r * 255), (int) (g * 255), (int) (b * 255), (int) (a * 255)
        );
    }
}
