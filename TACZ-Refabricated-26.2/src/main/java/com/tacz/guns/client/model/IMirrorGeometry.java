package com.tacz.guns.client.model;

import com.tacz.guns.client.model.bedrock.BedrockPart;

import javax.annotation.Nullable;

/**
 * 표시용 인터페이스: 스냅숏 순회기에게 "<b>이 노드의 변환 아래에서</b> 다른 노드의 형상을 한 번 더 그려라"라고 알린다.
 *
 * <p><b>필요한 이유(8차)</b></p>
 *
 * <p>총기 모델에는 의미가 다른 탄창 노드가 두 개 있다:</p>
 * <ul>
 *   <li>{@code magazine} — 재장전 때 <b>손을 따라 움직이는</b> 탄창</li>
 *   <li>{@code additional_magazine} — <b>총몸에 남는</b> 탄창</li>
 * </ul>
 *
 * <p>원본 1.21.1은 {@code additional_magazine}의 변환 아래에서 {@code magazine}의
 * 메시를 <b>한 번 더 렌더링</b>했다(같은 형상을 두 번 그림). 기본 총기 팩의 {@code reload_tactical},
 * {@code reload_empty}, {@code inspect} 등 애니메이션이 두 노드를 함께 움직이며 이 동작에 기댄다.</p>
 *
 * <p>2차에서 이 provider를 {@code return null}로 바꿨는데,
 * "{@code magazine}은 원래 모델 트리에 있어 순회된다"고 잘못 생각했다. <b>틀린 판단이었다</b> —
 * 트리의 것은 "손을 따라가는" 쪽이고, "총에 남는" 쪽은 여기서 덧그려야만 한다.
 * 증상: 재장전/빈 탄창 재장전 때 총의 탄창이 사라지고 손의 것만 남았다.</p>
 *
 * <p>원본처럼 정점을 직접 쓰는 lambda를 돌려주지 않고 "표시 인터페이스 + 스냅숏 순회기 직접 처리"로 만든 이유:
 * {@code BedrockRenderSnapshot}은 지연 제출이라
 * 거울 형상이 총몸과 같은 {@code RenderType}과 같은 DrawCommand 묶음을 써야
 * 렌더링 순서와 재질이 맞는다. 순회기가 한꺼번에 처리하는 것이 가장 단순하고 안전하다.
 * (참고: {@link IFunctionalSubmitter}가 아닌 renderer는 순회기가 하위 트리 전체를 건너뛴다.)</p>
 */
@FunctionalInterface
public interface IMirrorGeometry extends IFunctionalRenderer {
    /**
     * @return 현재 노드 변환 아래에서 추가로 그릴 노드. {@code null}이면 그리지 않는다.
     */
    @Nullable
    BedrockPart getMirroredPart();

    /**
     * 이 인터페이스는 스냅숏 순회기에서만 인식하며, 예전 같은 버퍼 즉시 렌더링 경로에는 참여하지 않는다.
     */
    @Override
    default void render(com.mojang.blaze3d.vertex.PoseStack poseStack,
                        com.mojang.blaze3d.vertex.VertexConsumer vertexBuffer,
                        net.minecraft.world.item.ItemDisplayContext transformType,
                        int light,
                        int overlay) {
        // 스냅숏 전용 구현.
    }
}
