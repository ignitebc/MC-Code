package com.tacz.guns.client.model.bedrock;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

public interface BedrockCube {
    void compile(PoseStack.Pose pose, VertexConsumer consumer, int light, int overlay, float red, float green, float blue, float alpha);

    /**
     * 이 육면체를 이루는 여섯 면에 읽기 전용으로 접근한다.
     *
     * <p><b>구현 클래스가 아니라 인터페이스에 둔 이유:</b>
     * 두 구현 {@code BedrockCubeBox}와 {@code BedrockCubePerFace}는 모두
     * 구조가 똑같은 {@code BedrockPolygon[6]} 필드를 갖고, UV 출처만 다르다
     * (앞쪽은 전체 uv 오프셋으로 계산, 뒤쪽은 face_uv로 면마다 지정).
     * 형상 정점 좌표 계산법은 둘이 완전히 같다.</p>
     *
     * <p>조준경 마스크({@code ScopeMaskRenderer})는 {@code VertexConsumer}를 거치지 않고
     * 정점 버퍼를 직접 만들어야 해서 {@link #compile}을 쓸 수 없지만, <b>완전히 같은</b> 정점 데이터를 다시 써야 한다.
     * 아니면 마스크와 화면이 어긋난다.</p>
     *
     * <p>첫 판은 접근자를 {@code BedrockCubeBox}에만 두고 {@code instanceof}로 걸렀는데,
     * 실측해 보니 접안렌즈 마스크가 온통 검었다 — 기본 총기 팩의 <b>접안렌즈 육면체 161개가
     * 하나도 빠짐없이 {@code BedrockCubePerFace}</b>였기 때문이다(모두 {@code face_uv}를 가짐).
     * 그 {@code instanceof}가 100% 걸러 냈다. 교훈: 구현 클래스로 능력을 판단하지 않는다.</p>
     */
    BedrockPolygon[] getPolygons();
}
