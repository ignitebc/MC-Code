package com.tacz.guns.client.render.scope;

import com.mojang.blaze3d.vertex.PoseStack;
import com.tacz.guns.client.model.bedrock.BedrockPart;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.world.item.ItemDisplayContext;

/**
 * 조준선(눈금) 그리기 전략.
 *
 * <p>"조준선을 어떻게 그릴지"를 {@code BedrockAttachmentModel}에서 떼어 내,
 * 홀로그램 도트 / 옛 새김 / 혼합 조준경 / 사용자 정의가 각자 구현을 갖게 하고,
 * 외부 총기 팩이 {@link #priority()}로 내장 전략을 덮어쓸 수 있게 한다.</p>
 *
 * <h2>이 추상화가 필요한 이유</h2>
 * 세 종류 조준선의 물리적 동작이 완전히 다르다:
 * <ul>
 *   <li><b>홀로그램/도트</b>: 평행광 광학계라 조준선이 <b>총몸에 붙지 않고</b>
 *       시선 방향을 따라 움직이며(시차 없음) 항상 빛난다.</li>
 *   <li><b>새긴 눈금</b>: 렌즈 유리에 물리적으로 새겨져 <b>총몸을 완전히 따라가며</b> 빛나지 않는다.</li>
 *   <li><b>혼합</b>: 형상은 총몸을 따라가지만 그중 한 구간이 빛난다.</li>
 * </ul>
 * if/else로 굳혀 쓰면 금방 감당할 수 없게 되므로 전략 인터페이스로 정의했다.
 *
 * <h2>구현 약속</h2>
 * <ol>
 *   <li><b>submit 스냅숏</b> 경로({@code BedrockRenderSnapshot})만 쓴다.
 *       예전 {@code renderTempPart}는 <b>호출하지 않는다</b> — 26.2에서는 아무것도 하지 않는다.</li>
 *   <li>{@link BedrockPart#visible}을 바꿨으면 반드시 {@code finally}에서 되돌린다:
 *       모델 노드는 <b>여러 프레임이 함께 쓰므로</b> 되돌리지 않으면 3인칭과 인벤토리까지 망가진다.</li>
 *   <li>구현은 상태가 없어야 하며(싱글턴이면 된다), 프레임마다 필요한 데이터는 모두 {@link Context}에서 얻는다.</li>
 * </ol>
 */
public interface IReticleRenderer {

    /**
     * 이 전략이 해당 조준경에 맞는지.
     *
     * @param nodes 해석한 조준선 노드 모음
     */
    boolean matches(ScopeNodeSet nodes);

    /**
     * 조준선 형상을 제출한다. 조준선이 몸체 위에 덮이도록 {@code super.submit(...)} <b>뒤에</b> 호출된다.
     */
    void submitReticle(Context ctx, ScopeNodeSet nodes);

    /**
     * 우선순위. 값이 클수록 먼저다. 내장 구현은 모두 0이고,
     * 외부 총기 팩/애드온 모드가 {@code > 0}인 구현을 등록하면 덮어쓸 수 있다.
     */
    default int priority() {
        return 0;
    }

    /**
     * 한 프레임 동안 조준선을 그리는 데 필요한 모든 문맥.
     *
     * @param poseStack      현재 행렬(조준경 자신의 변환이 들어 있음)
     * @param collector      26.2의 제출 수집기
     * @param displayContext 표시 문맥(1인칭 / 3인칭 / GUI…)
     * @param baseRenderType 조준경 본체가 쓰는 RenderType(텍스처가 묶여 있음)
     * @param light          물려받은 조명
     * @param overlay        overlay 좌표
     * @param aimingProgress 조준 진행도 0~1. 나타나기/사라지기에 쓸 수 있다
     */
    /**
     * @param baseRenderType 조준선이 써야 할 RenderType. 마스크가 적용되면 "반대 잘라내기" 판
     *                       (접안렌즈 투영 안에만 그림)이고, 아니면 일반 entityCutout이다.
     * @param maskActive     이번 프레임에 접안렌즈 마스크가 실제로 적용되었는지.
     *                       <p>{@link EtchedReticleRenderer}는 반드시 이 표시를 봐야 한다:
     *                       {@code division}에는 큰 차광판이 섞여 있어 마스크가 그것을 잘라낼 때만
     *                       안전하게 그릴 수 있고, 아니면 화면을 덮는다(9차의 교훈).</p>
     */
    record Context(PoseStack poseStack,
                   SubmitNodeCollector collector,
                   ItemDisplayContext displayContext,
                   RenderType baseRenderType,
                   int light,
                   int overlay,
                   float aimingProgress,
                   boolean maskActive) {
    }
}
